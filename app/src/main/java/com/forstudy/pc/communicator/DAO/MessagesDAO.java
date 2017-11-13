package com.forstudy.pc.communicator.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.forstudy.pc.communicator.Models.MessagesForLocalDB;
import com.forstudy.pc.communicator.Models.MessagesFromWebService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by pc on 10.03.17.
 */

public class MessagesDAO extends SQLiteOpenHelper {
    private static final String name = "MESSAGES_DATABASE";
    private static int version = 1;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    public MessagesDAO(Context context) {
        super(context, name, null, version);
        Context cont = context;
        prefs = cont.getSharedPreferences("preferences", cont.MODE_PRIVATE);
        prefsEditor = prefs.edit();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table = "CREATE TABLE MessagesForLocalDB ( id_message INTEGER PRIMARY KEY, sent INTEGER, received INTEGER, sender_id INTEGER, receiver INTEGER, sender TEXT, first_name TEXT, second_name TEXT,symmetric_encrypt_key TEXT, message TEXT, signature TEXT )";
        db.execSQL(create_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        version++;
        db.execSQL("DROP TABLE IF EXISTS MessagesForLocalDB");
        db.execSQL("CREATE TABLE MessagesForLocalDB ( id_message INTEGER PRIMARY KEY, sent INTEGER, received INTEGER, sender_id INTEGER, receiver INTEGER, sender TEXT, first_name TEXT, second_name TEXT,symmetric_encrypt_key TEXT, message TEXT, signature TEXT )");
    }

    public void removeAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS MessagesForLocalDB");
        db.execSQL("CREATE TABLE MessagesForLocalDB ( id_message INTEGER PRIMARY KEY, sent INTEGER, received INTEGER, sender_id INTEGER, receiver INTEGER, sender TEXT, first_name TEXT, second_name TEXT,symmetric_encrypt_key TEXT, message TEXT, signature TEXT )");
        db.close();
    }

    public long addMessage(MessagesFromWebService messages) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put("id_message", messages.getId());
        values.put("sent", messages.getSent());
        values.put("received", messages.getReceived());
        values.put("sender", messages.getSender().getLogin());
        values.put("sender_id", messages.getSender().getId());
        values.put("receiver", messages.getReceiver());//prefs.getInt("id", 0));
        values.put("first_name", messages.getSender().getFirstName());
        values.put("second_name", messages.getSender().getSecondName());
        values.put("symmetric_encrypt_key", messages.getEncryptionKey());
        values.put("message", messages.getMessage());
        values.put("signature", messages.getSignature());


        long err = db.insert("MessagesForLocalDB", null, values);
        db.close();
        return err;
    }

    public long addMessage(MessagesForLocalDB messages) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put("id_message", messages.getId());
        values.put("sent", messages.getSent());
        values.put("received", messages.getReceived());
        values.put("sender", messages.getSender());
        values.put("sender_id", messages.getSenderId());
        values.put("receiver", messages.getReceiver());
        values.put("first_name", messages.getFirstName());
        values.put("second_name", messages.getSecondName());
        values.put("symmetric_encrypt_key", messages.getEncryptionKey());
        values.put("message", messages.getMessage());
        values.put("signature", messages.getSignature());

        long err = db.insert("MessagesForLocalDB", null, values);
        db.close();
        return err;
    }

    public MessagesForLocalDB getMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("MessagesForLocalDB", new String[]{"id_message", "sent", "received", "sender_id", "receiver",  "sender", "first_name", "second_name", "symmetric_encrypt_key", "message", "signature"}, "id_message=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        MessagesForLocalDB message = new MessagesForLocalDB();
        message.setId(cursor.getInt(0));
        message.setSent(cursor.getLong(1));
        message.setReceived(cursor.getLong(2));
        message.setSenderId(cursor.getInt(3));
        message.setReceiver(cursor.getInt(4));
        message.setSender(cursor.getString(5));
        message.setFirstName(cursor.getString(6));
        message.setSecondName(cursor.getString(7));
        message.setEncryptionKey(cursor.getString(8));
        message.setMessage(cursor.getString(9));
        message.setSignature(cursor.getString(10));
        db.close();
        return message;
    }

    public List<MessagesForLocalDB> getAllMessages(String whereQuery) {
        List<MessagesForLocalDB> messagesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM MessagesForLocalDB" + " WHERE " + whereQuery;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                MessagesForLocalDB message = new MessagesForLocalDB();
                message.setId(cursor.getInt(0));
                message.setSent(cursor.getLong(1));
                message.setReceived(cursor.getLong(2));
                message.setSenderId(cursor.getInt(3));
                message.setReceiver(cursor.getInt(4));
                message.setSender(cursor.getString(5));
                message.setFirstName(cursor.getString(6));
                message.setSecondName(cursor.getString(7));
                message.setEncryptionKey(cursor.getString(8));
                message.setMessage(cursor.getString(9));
                message.setSignature(cursor.getString(10));
                messagesList.add(message);
            } while (cursor.moveToNext());
        }
        db.close();
        return messagesList;
    }

    public List<MessagesForLocalDB> getNewMessages() {
        List<MessagesForLocalDB> messagesList = new LinkedList<>();
        Set<Integer> temp = new HashSet<Integer>();
        String selectQueryNewMessages = "SELECT * FROM MessagesForLocalDB" + " WHERE " + "sender_id != " + prefs.getInt("id", 0) + " AND received == 0 ORDER BY sent DESC";
        String selectQueryOldMessages = "SELECT * FROM MessagesForLocalDB" + " WHERE " + "sender_id != " + prefs.getInt("id", 0) + " AND received != 0 ORDER BY received DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorNewMessages = db.rawQuery(selectQueryNewMessages, null);
        Cursor cursorOldMessages = db.rawQuery(selectQueryOldMessages, null);

        if(cursorNewMessages.moveToFirst()) {
            do {
                if (temp.add(cursorNewMessages.getInt(3))) {
                    MessagesForLocalDB message = new MessagesForLocalDB();
                    message.setId(cursorNewMessages.getInt(0));
                    message.setSent(cursorNewMessages.getLong(1));
                    message.setReceived(cursorNewMessages.getLong(2));
                    message.setSenderId(cursorNewMessages.getInt(3));
                    message.setReceiver(cursorNewMessages.getInt(4));
                    message.setSender(cursorNewMessages.getString(5));
                    message.setFirstName(cursorNewMessages.getString(6));
                    message.setSecondName(cursorNewMessages.getString(7));
                    message.setEncryptionKey(cursorNewMessages.getString(8));
                    message.setMessage(cursorNewMessages.getString(9));
                    message.setSignature(cursorNewMessages.getString(10));

                    messagesList.add(message);
                }
            } while (cursorNewMessages.moveToNext());
        }

        if (cursorOldMessages.moveToFirst()) {
            do {
                if (temp.add(cursorOldMessages.getInt(3))) {
                    MessagesForLocalDB message = new MessagesForLocalDB();
                    message.setId(cursorOldMessages.getInt(0));
                    message.setSent(cursorOldMessages.getLong(1));
                    message.setReceived(cursorOldMessages.getLong(2));
                    message.setSenderId(cursorOldMessages.getInt(3));
                    message.setReceiver(cursorOldMessages.getInt(4));
                    message.setSender(cursorOldMessages.getString(5));
                    message.setFirstName(cursorOldMessages.getString(6));
                    message.setSecondName(cursorOldMessages.getString(7));
                    message.setEncryptionKey(cursorOldMessages.getString(8));
                    message.setMessage(cursorOldMessages.getString(9));
                    message.setSignature(cursorOldMessages.getString(10));

                    messagesList.add(message);
                }
            } while (cursorOldMessages.moveToNext());
        }
        db.close();
        return messagesList;
    }

    public int updateMessage(MessagesForLocalDB message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id_message", message.getId());
        values.put("sent", message.getSent());
        values.put("received", message.getReceived());
        values.put("sender", message.getSender());
        values.put("sender_id", message.getSenderId());
        values.put("receiver", message.getReceiver());
        values.put("first_name", message.getFirstName());
        values.put("second_name", message.getSecondName());
        values.put("symmetric_encrypt_key", message.getEncryptionKey());
        values.put("message", message.getMessage());
        values.put("signature", message.getSignature());
        int success = db.update("MessagesForLocalDB", values, "id_message = ?",
                new String[] { String.valueOf(message.getId()) });
        db.close();
        return success;
    }

    public void deleteMessage(MessagesForLocalDB message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("MessagesForLocalDB", "id_message = ?",
                new String[] { String.valueOf(message.getId()) });
        db.close();
    }
}
