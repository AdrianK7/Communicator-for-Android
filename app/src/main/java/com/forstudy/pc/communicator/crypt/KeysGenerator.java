package com.forstudy.pc.communicator.crypt;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.Toast;

import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionKeyPair;
import com.forstudy.pc.communicator.NTRU.encrypt.EncryptionParameters;
import com.forstudy.pc.communicator.NTRU.encrypt.NtruEncrypt;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static android.R.attr.key;

/**
 * Created by pc on 19.03.17.
 */

public class KeysGenerator {

    public static SecretKey generateAES() {
        SecretKey key = null;
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);

            key = generator.generateKey();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static void generateAESForPrivateKeyEncryption() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder("ForPrivateKeyEncryption",
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CTR)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            //.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            //.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .setRandomizedEncryptionRequired(false)
                            .setKeySize(256).build());
            SecretKey key = keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PublicKey generateRSAForSigning() {
        PublicKey publicKey = null;

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(
                            "SigningKey",
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA512)
                            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                            .build());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    public static EncryptionKeyPair generateNTRUKeys() {
        //EncryptionParameters ntruParams = EncryptionParameters.APR2011_439_FAST;
        EncryptionParameters ntruParams = EncryptionParameters.APR2011_743_FAST;
        NtruEncrypt ntru = new NtruEncrypt(ntruParams);
        return ntru.generateKeyPair();
    }

}
