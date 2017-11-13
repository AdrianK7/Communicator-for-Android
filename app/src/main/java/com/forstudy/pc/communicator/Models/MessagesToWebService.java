package com.forstudy.pc.communicator.Models;

/**
 * Created by pc on 13.03.17.
 */

public class MessagesToWebService {
    private int id_message;

    private long sent;

    private long received;

    private int sender;

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

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
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
