package com.forstudy.pc.communicator.Models;

/**
 * Created by pc on 28.02.17.
 */

public class AuthorizationModel {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username ) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password ) {
        this.password = password;
    }


    @Override
    public String toString(){
        return "username=" + username + ", password=" + password;
    }
}
