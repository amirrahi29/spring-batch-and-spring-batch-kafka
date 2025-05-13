package com.pgp.crypto.lib;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.Security;

public class PGPEncryptUtil {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static void encryptFile(String inputFilePath, String outputFilePath, String publicKeyFile) throws Exception {
        PGPPublicKey publicKey = loadPublicKey(new FileInputStream(publicKeyFile));

        try (OutputStream out = new ArmoredOutputStream(new FileOutputStream(outputFilePath))) {
            ByteArrayOutputStream literalOut = new ByteArrayOutputStream();
            PGPCompressedDataGenerator compressor = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

            try (OutputStream compressedOut = compressor.open(literalOut)) {
                PGPLiteralDataGenerator literalGen = new PGPLiteralDataGenerator();

                try (OutputStream pOut = literalGen.open(
                        compressedOut,
                        PGPLiteralData.BINARY,
                        new File(inputFilePath).getName(),
                        new File(inputFilePath).length(),
                        new java.util.Date()
                )) {
                    byte[] buf = new byte[1024];
                    try (InputStream in = new FileInputStream(inputFilePath)) {
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            pOut.write(buf, 0, len);
                        }
                    }
                }
            }

            compressor.close();

            byte[] compressedData = literalOut.toByteArray();

            PGPEncryptedDataGenerator encGen = new PGPEncryptedDataGenerator(
                    new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                            .setWithIntegrityPacket(true)
                            .setSecureRandom(new SecureRandom())
                            .setProvider("BC")
            );

            encGen.addMethod(
                    new JcePublicKeyKeyEncryptionMethodGenerator(publicKey)
                            .setProvider("BC")
            );

            try (OutputStream encOut = encGen.open(out, compressedData.length)) {
                encOut.write(compressedData);
            }
        }
    }

    private static PGPPublicKey loadPublicKey(InputStream in) throws IOException, PGPException {
        in = PGPUtil.getDecoderStream(in);
        PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(in, new JcaKeyFingerprintCalculator());

        for (PGPPublicKeyRing keyRing : keyRingCollection) {
            for (PGPPublicKey key : keyRing) {
                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException("No encryption key found in public key file.");
    }
}
