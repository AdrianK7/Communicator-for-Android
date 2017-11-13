package com.forstudy.pc.communicator.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by pc on 13.03.17.
 */

public class MessagesFromWebService implements Serializable {

    private int id_message;

    private long sent;

    private long received;

    private Employees sender;

    private int receiver;

    private String symmetric_encrypt_key;

    private String signature;

    private String message;

    public int getId() {
        return id_message;
    }

    public void setId(int id_message) {
        this.id_message = id_message;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public long getReceived() {
        return received;
    }

    public void setReceived(long received) {
        this.received = received;
    }

    public Employees getSender() {
        return sender;
    }

    public void setSender(Employees sender) {
        this.sender = sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public String getEncryptionKey() {
        return symmetric_encrypt_key;
    }

    public void setEncryptionKey(String symmetric_encrypt_key) {
        this.symmetric_encrypt_key = symmetric_encrypt_key;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
