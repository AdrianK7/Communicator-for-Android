package com.forstudy.pc.communicator.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.forstudy.pc.communicator.Models.MessagesForLocalDB;
import com.forstudy.pc.communicator.Models.StoredContacts;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 14.03.17.
 */

public class ContactsDAO extends SQLiteOpenHelper {
    private static final String name = "CONTACTS_DATABASE";
    private static int version = 1;
    public ContactsDAO(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_table = "CREATE TABLE Contacts ( id INTEGER PRIMARY KEY, name TEXT, login TEXT )";
        db.execSQL(create_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        version++;
    }

    public void removeAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS Contacts");
        db.execSQL("CREATE TABLE Contacts ( id INTEGER PRIMARY KEY, name TEXT, login TEXT )");

    }

    public long addContact(StoredContacts contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", contact.getId());
        values.put("name", contact.getName());
        values.put("login", contact.getLogin());

        long err = db.insert("Contacts", null, values);
        db.close();
        return err;
    }

    public StoredContacts getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("Contacts", new String[]{"id", "name", "login"}, "id=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        StoredContacts contact = new StoredContacts();
        contact.setId(cursor.getInt(0));
        contact.setName(cursor.getString(1));
        contact.setLogin(cursor.getString(2));
        db.close();
        return contact;
    }

    public List<StoredContacts> getAllContacts() {
        List<StoredContacts> contactsList = new ArrayList<>();
        String selectQuery = "SELECT * FROM Contacts";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                StoredContacts contact = new StoredContacts();
                contact.setId(cursor.getInt(0));
                contact.setName(cursor.getString(1));
                contact.setLogin(cursor.getString(2));
                // Adding contact to list
                contactsList.add(contact);
            } while (cursor.moveToNext());
        }
        db.close();
        return contactsList;
    }

    public int updateContact(StoredContacts contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", contact.getId());
        values.put("name", contact.getName());
        values.put("login", contact.getLogin());
        int success = db.update("Contacts", values, "id = ?",
                new String[] { String.valueOf(contact.getId()) });
        db.close();
        return success;
    }

    public void deleteContact(MessagesForLocalDB message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Contacts", "id = ?",
                new String[] { String.valueOf(message.getId()) });
        db.close();
    }
}
