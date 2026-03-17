package com.example.aplicativo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

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

    //-------------------------- ATUALIZAR --------------------------//
    public void atualizar(Aluno aluno){
        ContentValues values = new ContentValues(); //valores que irei inserir
        values.put("nome", aluno.getNome());
        values.put("cpf", aluno.getCpf());
        values.put("telefone", aluno.getTelefone());
        banco.update("aluno", values, "id = ?", new String[]{aluno.getId().toString()});
    }

    public boolean validaCpf(String cpf) {
        if (cpf == null) return false;
        // Lógica simplificada conforme o slide
        return cpf.length() == 11;
    }

    public boolean cpfExistente(String cpf) {
        Cursor cursor = banco.query("aluno", new String[]{"id"}, "cpf = ?", new String[]{cpf}, null, null, null);
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public boolean validaTelefone(String telefone) {
        // Formato: (XX) 9XXXX-XXXX
        return telefone.matches("^\\(\\d{2}\\) 9\\d{4}-\\d{4}$");
    }

    public void excluir(Aluno a){
        banco.delete("aluno", "id = ?",new String[]{a.getId().toString()}); // no lugar do ? vai colocar o id do aluno
    }
}