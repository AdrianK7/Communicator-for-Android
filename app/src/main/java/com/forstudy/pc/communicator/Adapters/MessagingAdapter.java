package com.forstudy.pc.communicator.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.forstudy.pc.communicator.Models.MessagesForLocalDB;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionKeyPair;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionPrivateKey;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionPublicKey;
import com.forstudy.pc.communicator.R;
import com.forstudy.pc.communicator.crypt.Decrypt;
import com.forstudy.pc.communicator.crypt.Verification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class MessagingAdapter  extends ArrayAdapter<MessagesForLocalDB> {
    private byte[] privateKey;
    private String signingKey;
    private Context context;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;
    private int direction = 0;
    private LayoutInflater layoutInflater;

    public MessagingAdapter(Context context, LayoutInflater layoutInflater, String privateKey, String signingKey) {
        super(context, 0);
        this.layoutInflater = layoutInflater;
        this.context = context;
        prefs = context.getSharedPreferences("preferences", context.MODE_PRIVATE);
        prefsEditor = prefs.edit();
        this.privateKey = Base64.decode(privateKey, Base64.DEFAULT);
        this.signingKey = signingKey;
    }

    public void setPrivateKeyToNull() {
        privateKey = null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MessagesForLocalDB messages = getItem(position);
        if (messages.getSenderId() != prefs.getInt("id", 0))
            direction = 1;
        else
            direction = 0;
        int res = 0;
        if (direction == DIRECTION_INCOMING) {
            res = R.layout.message_left;
        } else if (direction == DIRECTION_OUTGOING) {
            res = R.layout.message_right;
        }
        convertView = layoutInflater.inflate(res, parent, false);
        TextView message = (TextView) convertView.findViewById(R.id.txtMessage);
        String signingKeyLocal;
        if(messages.getSenderId() == prefs.getInt("id", 0)) {
            signingKeyLocal = prefs.getString("localPublicSigningKey", null);
        }
        else {
            signingKeyLocal = signingKey;
        }
        if(Verification.verifyMessage(Base64.decode(messages.getMessage(), Base64.DEFAULT), Base64.decode(messages.getSignature(), Base64.DEFAULT), signingKeyLocal)) {
            byte[] privateKeyDecrypted = Decrypt.decryptNTRUPrivateKey(context, privateKey);
            byte[] publicKey = Base64.decode(prefs.getString("publicNTRUKey", null), Base64.DEFAULT);
            EncryptionKeyPair keyPair = new EncryptionKeyPair(new EncryptionPrivateKey(privateKeyDecrypted), new EncryptionPublicKey(publicKey));
            String messageDecrypted = Decrypt.decryptMessage(context, messages.getEncryptionKey(), keyPair, messages.getMessage());
            message.setText(messageDecrypted);
        }
        else {
            message.setText("Corrupted message!");
        }


        TextView dateTextView = (TextView) convertView.findViewById(R.id.txtDate);
        Calendar date = new GregorianCalendar();
        date.setTimeInMillis(messages.getReceived());
        dateTextView.setText(date.get(Calendar.DAY_OF_MONTH) + "." + date.get(Calendar.MONTH) + "."  + date.get(Calendar.YEAR) + " " + date.get(Calendar.HOUR_OF_DAY) + ":"  + date.get(Calendar.MINUTE));
        TextView sender = (TextView) convertView.findViewById(R.id.txtSender);
        sender.setText(messages.getFirstName() + " " + messages.getSecondName());

        return convertView;
    }
}