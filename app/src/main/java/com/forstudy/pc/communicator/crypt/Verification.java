package com.forstudy.pc.communicator.crypt;

import android.util.Base64;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by pc on 19.03.17.
 */

public class Verification {
    public void verifyNTRUPrivateKey() {

    }

    public static boolean verifyMessage(byte[] data, byte[] sig, String signingKeyString) {
        boolean signed = false;
        try {
            PublicKey signingKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(signingKeyString, Base64.DEFAULT)));
            Signature signature = Signature.getInstance("SHA512withRSA/PSS");
            signature.initVerify(signingKey);
            signature.update(data);
            signed = signature.verify(sig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signed;
    }
}
