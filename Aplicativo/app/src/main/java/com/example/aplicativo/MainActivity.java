package com.example.aplicativo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText nome;
    private EditText cpf;
    private EditText telefone;
    private EditText endereco;
    private EditText curso;

    private AlunoDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nome = findViewById(R.id.editNome);
        cpf = findViewById(R.id.editCPF);
        telefone = findViewById(R.id.editTelefone);
        endereco = findViewById(R.id.editEndereco);
        curso = findViewById(R.id.editCurso);
        dao = new AlunoDao(this);
    }

    public void salvar(View view) {
        String sNome = nome.getText().toString();
        String sCpf = cpf.getText().toString();
        String sTelefone = telefone.getText().toString();
        String sEndereco = endereco.getText().toString();
        String sCurso = curso.getText().toString();

        Aluno a = new Aluno();
        a.setNome(sNome);
        a.setCpf(sCpf);
        a.setTelefone(sTelefone);
        a.setEndereco(sEndereco);
        a.setCurso(sCurso);

        if (validarInterface(a)) {
            long id = dao.inserir(a);
            Toast.makeText(this, "Aluno inserido com id: " + id, Toast.LENGTH_SHORT).show();
            limparCampos();
        }
    }

    /**
     * Valida os campos na interface, utilizando as regras de negócio do AlunoDao.
     */
    private boolean validarInterface(Aluno aluno) {
        boolean valido = true;

        if (aluno.getNome().trim().isEmpty()) {
            nome.setError("Preencha o nome");
            valido = false;
        }

        if (aluno.getCpf().trim().isEmpty()) {
            cpf.setError("Preencha o CPF");
            valido = false;
        } else if (!dao.validarCpf(aluno.getCpf())) {
            cpf.setError("CPF inválido (Regras da Receita Federal)");
            valido = false;
        }

        if (aluno.getTelefone().trim().isEmpty()) {
            telefone.setError("Preencha o telefone");
            valido = false;
        }
        if (aluno.getEndereco().trim().isEmpty()) {
            endereco.setError("Preencha o endereço");
            valido = false;
        }
        if (aluno.getCurso().trim().isEmpty()) {
            curso.setError("Preencha o curso");
            valido = false;
        }

        if (!valido) {
            Toast.makeText(this, "Por favor, verifique os erros nos campos.", Toast.LENGTH_SHORT).show();
        }

        return valido;
    }

    private void limparCampos() {
        nome.setText("");
        cpf.setText("");
        telefone.setText("");
        endereco.setText("");
        curso.setText("");
        nome.requestFocus();
    }
}