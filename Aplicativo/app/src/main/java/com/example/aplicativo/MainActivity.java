package com.example.aplicativo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText nome;
    private EditText cpf;
    private EditText telefone;
    private AlunoDao dao;
    private Aluno aluno = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nome = findViewById(R.id.editNome);
        cpf = findViewById(R.id.editCPF);
        telefone = findViewById(R.id.editTelefone);
        dao = new AlunoDao(this);

        // Aplicando as máscaras
        cpf.addTextChangedListener(Mascara.insert("###.###.###-##", cpf));
        telefone.addTextChangedListener(Mascara.insert("(##) #####-####", telefone));

        Intent it = getIntent();
        if (it.hasExtra("aluno")) {
            aluno = (Aluno) it.getSerializableExtra("aluno");
            nome.setText(aluno.getNome());
            cpf.setText(aluno.getCpf());
            telefone.setText(aluno.getTelefone());
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
}