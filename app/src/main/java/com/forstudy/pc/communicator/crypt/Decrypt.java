package com.forstudy.pc.communicator.crypt;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

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
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by pc on 19.03.17.
 */

public class Decrypt {
    public static byte[] decryptNTRUPrivateKey(Context context, byte[] encrypted) {
        SharedPreferences prefs = context.getSharedPreferences("preferences", context.MODE_PRIVATE);
        byte[] decodedBytes = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey("ForPrivateKeyEncryption", null);
            //Cipher c = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
            Key key = secretKey;

            IvParameterSpec iv = new IvParameterSpec(Base64.decode(prefs.getString("ivForPrivateKeyNTRUEncryption", null), Base64.DEFAULT));

            c.init(Cipher.DECRYPT_MODE, key, iv);
            decodedBytes = c.doFinal(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodedBytes;
    }

    public static String decryptMessage(Context context, String encrypted, EncryptionKeyPair keyPair, String messageEncrypted) {
        String decryptedMessage = null;
        SharedPreferences prefs = context.getSharedPreferences("preferences", context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        EncryptionParameters ntruParams = EncryptionParameters.APR2011_743_FAST;
        //EncryptionParameters ntruParams = EncryptionParameters.APR2011_439_FAST;
        NtruEncrypt ntru = new NtruEncrypt(ntruParams);
        byte[] encryptedByte = Base64.decode(encrypted, Base64.DEFAULT);
        byte[] keyAndIv = ntru.decrypt(encryptedByte, keyPair);
        byte[] aesKeyArr = Arrays.copyOf(keyAndIv, 32);
        byte[] ivArr = Arrays.copyOfRange(keyAndIv, 32, 48);
        byte[] aesEncrypted = Base64.decode(messageEncrypted, Base64.DEFAULT);
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            //Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SecretKeySpec aesKeySpec = new SecretKeySpec(aesKeyArr, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivArr);
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, ivSpec);
            byte[] plainText = cipher.doFinal(aesEncrypted);
            decryptedMessage = new String(plainText, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedMessage;
    }
}
