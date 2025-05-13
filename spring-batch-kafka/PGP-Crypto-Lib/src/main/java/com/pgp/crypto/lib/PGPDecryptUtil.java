package com.pgp.crypto.lib;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import java.io.*;
import java.security.Security;

public class PGPDecryptUtil {

    public static void decryptFile(String encryptedPath, String outputPath,
                                   String privateKeyPath, String passphrase) throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        try (InputStream keyIn = new FileInputStream(privateKeyPath);
             InputStream in = PGPUtil.getDecoderStream(new FileInputStream(encryptedPath))) {

            PGPObjectFactory pgpF = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
            PGPEncryptedDataList encList;

            Object o = pgpF.nextObject();
            if (o instanceof PGPEncryptedDataList) {
                encList = (PGPEncryptedDataList) o;
            } else {
                encList = (PGPEncryptedDataList) pgpF.nextObject();
            }

            PGPPrivateKey privateKey = null;
            PGPPublicKeyEncryptedData encryptedData = null;
            PGPSecretKeyRingCollection keys = new PGPSecretKeyRingCollection(
                    PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

            for (PGPEncryptedData ed : encList) {
                if (ed instanceof PGPPublicKeyEncryptedData pked) {
                    PGPSecretKey sk = keys.getSecretKey(pked.getKeyID());
                    if (sk != null) {
                        privateKey = sk.extractPrivateKey(
                                new JcePBESecretKeyDecryptorBuilder()
                                        .setProvider("BC")
                                        .build(passphrase.toCharArray()));
                        encryptedData = pked;
                        break;
                    }
                }
            }

            if (privateKey == null || encryptedData == null) {
                throw new IllegalArgumentException("Unable to find matching private key.");
            }

            InputStream clear = encryptedData.getDataStream(
                    new JcePublicKeyDataDecryptorFactoryBuilder()
                            .setProvider("BC")
                            .build(privateKey)
            );

            PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());
            Object message = plainFact.nextObject();
            if (message instanceof PGPCompressedData compData) {
                plainFact = new PGPObjectFactory(compData.getDataStream(), new JcaKeyFingerprintCalculator());
                message = plainFact.nextObject();
            }

            if (message instanceof PGPLiteralData literalData) {
                try (InputStream dataIn = literalData.getInputStream();
                     OutputStream out = new FileOutputStream(outputPath)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = dataIn.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                }
            } else {
                throw new PGPException("Unknown PGP message type.");
            }
        }
    }
}
