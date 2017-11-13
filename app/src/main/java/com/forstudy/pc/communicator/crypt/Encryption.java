package com.forstudy.pc.communicator.crypt;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.Toast;

import com.fasterxml.jackson.databind.deser.Deserializers;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionKeyPair;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionParameters;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionPublicKey;
import com.forstudy.pc.communicator.NTRU.encrypt.NtruEncrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by pc on 19.03.17.
 */

public class Encryption {
    public static String encryptNTRUPrivateKey(byte[] privateKey, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("preferences", context.MODE_PRIVATE);
        Editor prefsEditor = prefs.edit();
        byte[] encodedBytes = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey("ForPrivateKeyEncryption", null);
            //Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            Key key = secretKey;
            SecureRandom randomSecure = new SecureRandom();
            byte[] ivGen = new byte[16];
            randomSecure.nextBytes(ivGen);
            IvParameterSpec iv = new IvParameterSpec(ivGen);
            c.init(Cipher.ENCRYPT_MODE, key, iv);
            encodedBytes = c.doFinal(privateKey);
            prefsEditor.putString("ivForPrivateKeyNTRUEncryption", Base64.encodeToString(iv.getIV(), Base64.DEFAULT));
            prefsEditor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    public static byte[] encryptMessage(String messageBody, SecretKey secretKey, IvParameterSpec iv) {
        byte[] encodedBytes = null;
        try {

            //Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            Key key = secretKey;

            c.init(Cipher.ENCRYPT_MODE, key, iv);
            encodedBytes = c.doFinal(messageBody.getBytes(Charset.forName("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodedBytes;
    }

    public static String encryptAes(byte[] keyAndIv, String publicKey) {
        EncryptionParameters ntruParams = EncryptionParameters.APR2011_743_FAST;
        //EncryptionParameters ntruParams = EncryptionParameters.APR2011_439_FAST;
        NtruEncrypt ntru = new NtruEncrypt(ntruParams);
        byte[] ntruEncrypted = ntru.encrypt(keyAndIv, new EncryptionPublicKey(Base64.decode(publicKey, Base64.DEFAULT)));
        return Base64.encodeToString(ntruEncrypted, Base64.DEFAULT);
    }
}
