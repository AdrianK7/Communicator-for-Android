package com.forstudy.pc.communicator.Models;

/**
 * Created by pc on 14.03.17.
 */

public class StoredContacts {
    public int id;
    public String name;
    public String login;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
