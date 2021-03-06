package com.chain.api.core.Crypto;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoUtil {
    /**
     * Encrypts the data
     * @param encodedhash
     * @return encrypted SHA-256 hex data
     */
    public static String byteArrayToHexString(byte[] encodedhash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < encodedhash.length; i++) {
            String hex = Integer.toHexString(0xff & encodedhash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String encryptSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return byteArrayToHexString(encodedhash);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elliptic Curve Digital Signature Algorithm is a cryptographic algorithm used by Bitcoin to ensure that funds can only be spent by their rightful owners.
     * @param privateKey A secret number, known only to the person that generated it
     * @param input
     * @return
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        Signature dsa;
        byte[] output = new byte[0];
        try {
            dsa = Signature.getInstance("ECDSA", "BC");
            dsa.initSign(privateKey);
            byte[] strByte = input.getBytes();
            dsa.update(strByte);
            byte[] realSig = dsa.sign();
            output = realSig;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return output;
    }

    /**
     *
     * @param publicKey A number that corresponds to a private key, but does not need to be kept secret. A public key can be calculated from a private key, but not vice versa.
     * @param data
     * @param signature
     * @return
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(data.getBytes());
            return ecdsaVerify.verify(signature);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param definingKey
     * @return the PublicKey associated with the PrivateKey
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey DerivePubKeyFromPrivKey(BCECPrivateKey definingKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", Security.getProvider("BC"));

        BigInteger d = definingKey.getD();
        org.bouncycastle.jce.spec.ECParameterSpec ecSpec =
                definingKey.getParameters();
        ECPoint Q = definingKey.getParameters().getG().multiply(d);

        org.bouncycastle.jce.spec.ECPublicKeySpec pubSpec = new
                org.bouncycastle.jce.spec.ECPublicKeySpec(Q, ecSpec);
        PublicKey publicKeyGenerated = keyFactory.generatePublic(pubSpec);
        return publicKeyGenerated;
    }

    /**
     * Returns the PublicKey as a String
     * @param key
     * @return
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Creates a PublicKey from a public key in a string format
     * @param key the public key
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PublicKey getPublicKeyFromString(String key) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytePublicKey = Base64.getDecoder().decode(key);
        KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
        PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(bytePublicKey));
        return publicKey;
    }

    /**
     * Creates a PrivateKey from a public key in a string format
     * @param key the private key
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKeyFromString(String key) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytePrivateKey = Base64.getDecoder().decode(key);
        KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
        PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(bytePrivateKey));
        return privateKey;
    }
}
