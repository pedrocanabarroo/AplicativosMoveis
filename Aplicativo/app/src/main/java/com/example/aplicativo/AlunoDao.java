package com.example.aplicativo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe AlunoDao (Database Access Object)
 * Padrão de projeto utilizado para isolar o acesso aos dados da lógica de negócio.
 * Centraliza todas as operações estruturadas (CRUD) no banco de dados SQLite.
 */
public class AlunoDao {
    private Conexao conexao;
    private SQLiteDatabase banco;

    public AlunoDao(Context context){
        conexao = new Conexao(context);
        banco = conexao.getWritableDatabase();
    }

    public long inserir(Aluno aluno){
        ContentValues values = new ContentValues();
        values.put("nome", aluno.getNome());
        values.put("cpf", aluno.getCpf());
        values.put("telefone", aluno.getTelefone());
        values.put("endereco", aluno.getEndereco());
        values.put("curso", aluno.getCurso());
        return banco.insert("aluno", null, values);
    }

    public List<Aluno> obterTodos(){
        List<Aluno> alunos = new ArrayList<>();
        Cursor cursor = banco.query("aluno", new String[]{"id", "nome", "cpf", "telefone", "endereco", "curso"},
                null, null, null, null, null);
        while(cursor.moveToNext()){
            Aluno a = new Aluno();
            a.setId(cursor.getInt(0)); 
            a.setNome(cursor.getString(1)); 
            a.setCpf(cursor.getString(2)); 
            a.setTelefone(cursor.getString(3)); 
            a.setEndereco(cursor.getString(4));
            a.setCurso(cursor.getString(5));
            alunos.add(a);
        }
        cursor.close();
        return alunos;
    }

    /**
     * Valida se os campos obrigatórios do objeto Aluno estão preenchidos.
     */
    public boolean validarCampos(Aluno aluno) {
        return aluno.getNome() != null && !aluno.getNome().trim().isEmpty() &&
               aluno.getCpf() != null && !aluno.getCpf().trim().isEmpty() &&
               aluno.getTelefone() != null && !aluno.getTelefone().trim().isEmpty() &&
               aluno.getEndereco() != null && !aluno.getEndereco().trim().isEmpty() &&
               aluno.getCurso() != null && !aluno.getCurso().trim().isEmpty();
    }

    /**
     * Valida o CPF seguindo as regras da Receita Federal.
     */
    public boolean validarCpf(String cpf) {
        if (cpf == null) return false;
        
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");

        if (cpf.length() != 11 || 
            cpf.equals("00000000000") || cpf.equals("11111111111") ||
            cpf.equals("22222222222") || cpf.equals("33333333333") ||
            cpf.equals("44444444444") || cpf.equals("55555555555") ||
            cpf.equals("66666666666") || cpf.equals("77777777777") ||
            cpf.equals("88888888888") || cpf.equals("99999999999")) {
            return false;
        }

        try {
            // Cálculo do 1º Dígito Verificador (D1)
            int soma = 0;
            int peso = 10;
            for (int i = 0; i < 9; i++) {
                int num = (int) (cpf.charAt(i) - 48);
                soma += (num * peso);
                peso--;
            }
            int resto = 11 - (soma % 11);
            char digito10 = (resto == 10 || resto == 11) ? '0' : (char) (resto + 48);

            // Cálculo do 2º Dígito Verificador (D2)
            soma = 0;
            peso = 11;
            for (int i = 0; i < 10; i++) {
                int num = (int) (cpf.charAt(i) - 48);
                soma += (num * peso);
                peso--;
            }
            resto = 11 - (soma % 11);
            char digito11 = (resto == 10 || resto == 11) ? '0' : (char) (resto + 48);

            return (digito10 == cpf.charAt(9)) && (digito11 == cpf.charAt(10));
        } catch (Exception e) {
            return false;
        }
    }
}