package com.example.eventlyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelaEvento extends AppCompatActivity {

    private static final int REQ_BT_CONNECT = 1001;

    // Executor para impressão (evita travar a UI)
    private final ExecutorService printerExecutor = Executors.newSingleThreadExecutor();

    // Guarda o bitmap pendente caso o usuário precise conceder permissão e tentar novamente
    private Bitmap pendingPrintBitmap = null;

    private String id, img, nomeOriginal, dataOriginal, descricaoOriginal;
    private TextInputEditText descricaoInput, nomeInput, dataInput;

    // Bitmap global para guardar o QR Code gerado para impressão
    private Bitmap currentQrCodeBitmap;

    // Array com as duas permissões necessárias para a biblioteca Dantsu funcionar no Android 12+
    private final String[] permissoesBluetooth = new String[]{
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_evento);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            BottomNavigationView bottomNavInset = findViewById(R.id.bottom_navigation);
            bottomNavInset.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // Inicialização das Views padrão (XML principal)
        TextView txtvoltar = findViewById(R.id.txtBack1);
        TextInputLayout descricaoInfo = findViewById(R.id.descricaoInfo);
        TextInputLayout nomeInfo = findViewById(R.id.nomeInfo);
        TextInputLayout dataInfo = findViewById(R.id.dataInfo);

        descricaoInput = findViewById(R.id.descricaoInput);
        nomeInput = findViewById(R.id.nomeInput);
        dataInput = findViewById(R.id.dataInput);

        // Recebimento de dados da Intent
        Intent intentRecebida = getIntent();
        id = intentRecebida.getStringExtra("id");
        img = intentRecebida.getStringExtra("imagem");
        nomeOriginal = intentRecebida.getStringExtra("nome");
        dataOriginal = intentRecebida.getStringExtra("data");
        descricaoOriginal = intentRecebida.getStringExtra("descricao");

        descricaoInput.setText(descricaoOriginal);
        nomeInput.setText(nomeOriginal);
        dataInput.setText(dataOriginal);

        // Botões Alterar e Excluir
        Button btnExcluir = findViewById(R.id.btnExcluir);
        Button btnAlterar = findViewById(R.id.btnAlterar);

        btnExcluir.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TelaEvento.this);
            builder.setTitle("Excluir Evento");
            builder.setMessage("Deseja realmente excluir o evento?");
            builder.setPositiveButton("Sim", (dialog, which) -> {
                EventoDAO dao = new EventoDAO();
                Evento evento = new Evento();
                evento.setId(id);
                dao.deletar(evento);
                finish();
            });
            builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        btnAlterar.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TelaEvento.this);
            builder.setTitle("Alterar Evento");
            builder.setMessage("Deseja realmente alterar o evento?");
            builder.setPositiveButton("Sim", (dialog, which) -> {
                EventoDAO dao = new EventoDAO();
                dao.atualizarParcial(
                        id,
                        nomeInput.getText() != null ? nomeInput.getText().toString() : "",
                        dataInput.getText() != null ? dataInput.getText().toString() : "",
                        descricaoInput.getText() != null ? descricaoInput.getText().toString() : "",
                        img
                );
                finish();
            });
            builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        // --- BOTTOM NAVIGATION ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int menuItemId = item.getItemId();

            if (menuItemId == R.id.nav_listar) {
                Intent intentListar = new Intent(TelaEvento.this, TelaListar.class);
                intentListar.putExtra("id", id);
                intentListar.putExtra("nome", nomeInput.getText() != null ? nomeInput.getText().toString() : "");
                intentListar.putExtra("descricao", descricaoInput.getText() != null ? descricaoInput.getText().toString() : "");
                intentListar.putExtra("EMAIL_ORGANIZADOR", "organizador@fatec.com");
                startActivity(intentListar);
                return true;

            } else if (menuItemId == R.id.nav_qrcode) {
                if (id != null && !id.isEmpty()) {
                    String uidOrganizador = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String linkFormularioWeb = "https://eventifyform.netlify.app/?uid=" + uidOrganizador + "&id=" + id;
                    exibirDialogQrCode(linkFormularioWeb);
                } else {
                    Toast.makeText(TelaEvento.this, "ID do evento inválido ou ausente.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    // --- QR CODE ---

    private Bitmap gerarQrCode(String conteudo) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(conteudo, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void exibirDialogQrCode(String linkCompleto) {
        currentQrCodeBitmap = gerarQrCode(linkCompleto);

        if (currentQrCodeBitmap == null) {
            Toast.makeText(this, "Erro ao gerar a imagem do QR Code.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(TelaEvento.this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_qrcode, null);
        builder.setView(dialogView);

        ImageView imgQrCodeDialog = dialogView.findViewById(R.id.imgQrCodeDialog);
        Button btnImprimirDialog = dialogView.findViewById(R.id.btnImprimirDialog);
        Button btnFecharDialog = dialogView.findViewById(R.id.btnFecharDialog);

        imgQrCodeDialog.setImageBitmap(currentQrCodeBitmap);

        AlertDialog dialog = builder.create();

        btnImprimirDialog.setOnClickListener(v -> {
            Toast.makeText(TelaEvento.this, "Enviando para a impressora térmica...", Toast.LENGTH_SHORT).show();
            imprimirQrCodeBluetooth(currentQrCodeBitmap);
        });

        btnFecharDialog.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // --- PERMISSÃO BLUETOOTH (Android 12+) ---

    private boolean garantirePermissaoBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            // Verifica se alguma das duas permissões cruciais está faltando
            boolean connectConcedido = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
            boolean scanConcedido = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

            if (!connectConcedido || !scanConcedido) {
                // Solicita ambas em uma única caixa de diálogo para o usuário
                ActivityCompat.requestPermissions(
                        this,
                        permissoesBluetooth,
                        REQ_BT_CONNECT
                );
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_BT_CONNECT) {
            boolean todasConcedidas = true;

            // Percorre o array para garantir que as duas permissões foram aceitas
            if (grantResults.length > 0) {
                for (int resultado : grantResults) {
                    if (resultado != PackageManager.PERMISSION_GRANTED) {
                        todasConcedidas = false;
                        break;
                    }
                }
            } else {
                todasConcedidas = false;
            }

            if (todasConcedidas) {
                Toast.makeText(this, "Permissões Bluetooth concedidas.", Toast.LENGTH_SHORT).show();

                // Se tinha uma impressão aguardando a permissão terminar, dispara ela agora
                if (pendingPrintBitmap != null) {
                    Bitmap toPrint = pendingPrintBitmap;
                    pendingPrintBitmap = null;
                    imprimirQrCodeBluetooth(toPrint);
                }
            } else {
                Toast.makeText(this, "Permissões negadas. É necessário aceitar Connect e Scan para imprimir.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- IMPRESSÃO BLUETOOTH ---

    private void imprimirQrCodeBluetooth(Bitmap qrBitmap) {
        if (qrBitmap == null) {
            Toast.makeText(this, "QR Code inválido para impressão.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!garantirePermissaoBluetooth()) {
            pendingPrintBitmap = qrBitmap;
            return;
        }

        printerExecutor.execute(() -> {
            try {
                // Seleciona a primeira impressora pareada (Agora com permissão de SCAN garantida)
                BluetoothConnection connection = BluetoothPrintersConnections.selectFirstPaired();

                if (connection == null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Nenhuma impressora Bluetooth pareada encontrada.", Toast.LENGTH_LONG).show()
                    );
                    return;
                }

                // Configuração padrão para 58mm/203dpi
                EscPosPrinter printer = new EscPosPrinter(connection, 203, 48f, 32);

                // Ajusta a largura do bitmap para 58mm (geralmente 384px)
                Bitmap scaled = Bitmap.createScaledBitmap(qrBitmap, 384, 384, false);

                String hexImage = PrinterTextParserImg.bitmapToHexadecimalString(printer, scaled);

                String payload =
                        "[C]QR Code do Evento\n" +
                                "[C]<img>" + hexImage + "</img>\n" +
                                "[C]\n";

                printer.printFormattedTextAndCut(payload, 50);

                runOnUiThread(() ->
                        Toast.makeText(this, "QR Code impresso com sucesso!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao imprimir: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        printerExecutor.shutdown();
    }

    public void sair(View view) {
        finish();
    }
}