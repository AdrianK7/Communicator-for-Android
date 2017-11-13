package com.forstudy.pc.communicator.Models;

import java.io.Serializable;

/**
 * Created by pc on 10.03.17.
 */

public class Employees implements Serializable{

    private int id_employee;

    private String first_name;

    private String second_name;

    private String login;

    private String password;

    private String fcm_token;

    private String private_key;

    private String public_key;

    private String sign_key;

    public Employees getEmployeeWithIdSet(int id) {
        this.setId(id);
        return this;
    }

    public int getId() {
        return id_employee;
    }

    public void setId(int id_employee) {
        this.id_employee = id_employee;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getSecondName() {
        return second_name;
    }

    public void setSecondName(String second_name) {
        this.second_name = second_name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFcmToken() {
        return fcm_token;
    }

    public void setFcmToken(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public String getPrivateKey() {
        return private_key;
    }

    public void setPrivateKey(String private_key) {
        this.private_key = private_key;
    }


    public String getPublicKey() {
        return public_key;
    }

    public void setPublicKey(String public_key) {
        this.public_key = public_key;
    }

    public String getSigningKey() {
        return sign_key;
    }

    public void setSigningKey(String sign_key) {
        this.sign_key = sign_key;
    }
}
