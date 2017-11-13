package com.forstudy.pc.communicator.Models;

/**
 * Created by pc on 10.03.17.
 */

        import java.sql.Timestamp;



public class MessagesForLocalDB {

    private int id_message;

    private int sender_id;

    private long sent;

    private long received;

    private String sender;

    private int receiver;

    private String first_name;

    private String second_name;

    private String symmetric_encrypt_key;

    private String signature;

    private String message;


    public int getId() {
        return id_message;
    }

    public void setId(int id_message) {
        this.id_message = id_message;
    }

    public int getSenderId() {
        return sender_id;
    }

    public void setSenderId(int sender_id) {
        this.sender_id = sender_id;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
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

