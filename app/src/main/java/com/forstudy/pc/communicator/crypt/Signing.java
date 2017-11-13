package com.forstudy.pc.communicator.crypt;

import android.util.Base64;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by pc on 19.03.17.
 */

public class Signing {

    public static String sign(byte[] data) {
        byte[] sig = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("SigningKey", null);

            Signature signature = Signature.getInstance("SHA512withRSA/PSS");
            signature.initSign(privateKey);
            signature.update(data);
            sig = signature.sign();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(sig , Base64.DEFAULT);
    }
}
