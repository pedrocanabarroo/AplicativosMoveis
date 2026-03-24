package com.example.aplicativo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private EditText nome;
    private EditText cpf;
    private EditText telefone;
    private AlunoDao dao;
    private Aluno aluno = null;
    /*------------------CONFIGURACOES PARA CAMERA--------------------*/
    private ImageView imageView;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 200;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nome = findViewById(R.id.editNome);
        cpf = findViewById(R.id.editCPF);
        telefone = findViewById(R.id.editTelefone);
        imageView = findViewById(R.id.imageView);
        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        dao = new AlunoDao(this);

        // Aplicando as máscaras
        cpf.addTextChangedListener(Mascara.insert("###.###.###-##", cpf));
        telefone.addTextChangedListener(Mascara.insert("(##) #####-####", telefone));

        Intent it = getIntent(); //pega intenção
        if(it.hasExtra("aluno")){
            aluno = (Aluno) it.getSerializableExtra("aluno");
            nome.setText(aluno.getNome().toString());
            cpf.setText(aluno.getCpf());
            telefone.setText(aluno.getTelefone());
            // Carregar a foto no ImageView no momento que carregar os dados para atualizar
            byte[] fotoBytes = aluno.getFotoBytes();
            if (fotoBytes != null && fotoBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    //--------------------------método para botão salvar qdo clicado--------------------------//
    public void salvar(View view) {

        String nomeDigitado = nome.getText().toString().trim();
        String cpfDigitado = cpf.getText().toString().trim();
        String telefoneDigitado = telefone.getText().toString().trim();

        // Verifica se os campos estão vazios
        if (nomeDigitado.isEmpty() || cpfDigitado.isEmpty() || telefoneDigitado.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação do CPF (verifica se o formato e os dígitos são válidos)
        String cpfLimpo = Mascara.unmask(cpfDigitado);
        if (!dao.validaCpf(cpfLimpo)) {
            Toast.makeText(this, "CPF inválido. Digite novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Se for cadastrar novo aluno ou Se for atualizar os dados ignora o CPF se for igual do próprio aluno
        if (aluno == null || !cpfDigitado.equals(aluno.getCpf())) {
            // verifica se o CPF já existe no banco
            if (dao.cpfExistente(cpfDigitado)) {
                Toast.makeText(this, "CPF duplicado. Insira um CPF diferente.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validação do Telefone
        if (!dao.validaTelefone(telefoneDigitado)) {
            Toast.makeText(this, "Telefone inválido! Use o formato correto: (XX) 9XXXX-XXXX", Toast.LENGTH_SHORT).show();
            return;
        }

        // aluno == null cadastrar, aluno != null está recebendo do ListarAlunos
        if (aluno == null) {
            // Criar objeto Aluno
            Aluno alunoNovo = new Aluno();
            alunoNovo.setNome(nomeDigitado);
            alunoNovo.setCpf(cpfDigitado);
            alunoNovo.setTelefone(telefoneDigitado);

            // Inserir aluno no banco de dados
            long id = dao.inserir(alunoNovo);

            if (id != -1) {
                Toast.makeText(this, "Aluno inserido com id: " + id, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao inserir aluno. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Atualização de um aluno existente
            aluno.setNome(nomeDigitado);
            aluno.setCpf(cpfDigitado);
            aluno.setTelefone(telefoneDigitado);

            dao.atualizar(aluno);
            Toast.makeText(this, "Aluno atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        }

        // Fecha a tela de cadastro e volta para a listagem
        finish();
    }

    public void listarAlunos(View view) {
        Intent it = new Intent(this, ListarAlunosActivity.class);
        startActivity(it);
    }

    //--------------------MÉTODOS DA CAMERA --------------------------------------------------------------------
    /**
     * Método chamado pelo clique do botão "Tirar Foto" (android:onClick no XML).
     * Sua principal função é verificar se o usuário já permitiu o uso da câmera.
     */
    public void tirarFoto(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Verifica se a resposta que chegou é referente ao nosso pedido de câmera (código 100)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            // Verifica se o array de resultados não está vazio e se o usuário clicou em "Permitir"
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("CAMERA_DEBUG", "Usuário permitiu, abrindo câmera...");
                startCamera();
            } else {
                // Se o usuário negou, avisamos que ele não conseguirá tirar fotos.
                Toast.makeText(this, "A permissão é necessária para usar a câmera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        try {
            // Cria uma Intent (intenção) para capturar uma imagem.
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Inicia a atividade da câmera esperando um resultado (a foto).
            // O código REQUEST_IMAGE_CAPTURE (200) serve para identificarmos esta foto quando ela voltar.
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Log.e("CAMERA_DEBUG", "Erro ao abrir a câmera: " + e.getMessage());
            Toast.makeText(this, "Erro ao abrir a câmera no seu dispositivo.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 1. Verifica se o resultado que está voltando é o da nossa Câmera (código 200)
        // 2. Verifica se a foto foi tirada com sucesso (RESULT_OK)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Extrai a imagem compactada (thumbnail) que vem dentro da Intent 'data'
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // Exibe a foto na tela para o usuário ver
            imageView.setImageBitmap(imageBitmap);
            // --- PREPARAÇÃO PARA O BANCO DE DADOS ---
            // Transforma o Bitmap em um Array de Bytes (byte[]), que é como o SQLite guarda imagens (BLOB)
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            // Se o objeto 'aluno' for nulo (novo cadastro), instanciamos ele aqui para guardar a foto
            if (aluno == null){
                aluno = new Aluno();
            }
            // Guarda os bytes da foto no objeto aluno (será usado no método salvar())
            aluno.setFotoBytes(byteArray);
        }
    }
}