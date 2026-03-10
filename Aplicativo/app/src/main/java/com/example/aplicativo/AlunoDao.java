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
    //context é usado para a conexão
    public AlunoDao(Context context){
        conexao = new Conexao(context); //criei uma conexao
        banco = conexao.getWritableDatabase(); //iniciar um banco de dados para escrita
    }
    //método para inserir - PARTE I
    public long inserir(Aluno aluno){ // long porque retorna o id do aluno
        ContentValues values = new ContentValues();
        values.put("nome", aluno.getNome());
        values.put("cpf", aluno.getCpf());
        values.put("telefone", aluno.getTelefone());
        return banco.insert("aluno",null, values);
    }
    public List<Aluno> obterTodos(){
        List<Aluno> alunos = new ArrayList<>();
        //cursor aponta para as linhas retornadas
        Cursor cursor = banco.query("aluno", new String[]{"id", "nome", "cpf", "telefone"},
                null, null,null,null,null); //nome da tabela, nome das colunas, completa com null o método
        //que por padrão pede esse número de colunas obrigatórias
        while(cursor.moveToNext()){ //verifica se consegue mover para o próximo ponteiro ou linha
            Aluno a = new Aluno();
            a.setId(cursor.getInt(0)); // new String[]{"id", "nome", "cpf", "telefone"}, id é coluna '0'
            a.setNome(cursor.getString(1)); // new String[]{"id", "nome", "cpf", "telefone"}, nome é coluna '1'
            a.setCpf(cursor.getString(2)); // new String[]{"id", "nome", "cpf", "telefone"}, cpf é coluna '2'
            a.setTelefone(cursor.getString(3)); // new String[]{"id", "nome", "cpf", "telefone"}, telefone é coluna '3'
            alunos.add(a);
        }
        return alunos;
    }
}