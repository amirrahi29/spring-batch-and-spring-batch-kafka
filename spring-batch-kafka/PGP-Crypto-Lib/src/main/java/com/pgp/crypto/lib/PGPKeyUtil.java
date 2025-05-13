package com.pgp.crypto.lib;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.encoders.Hex;

import java.io.FileOutputStream;
import java.security.*;
import java.util.Date;

public class PGPKeyUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void generatePGPKeyPair(String identity, String passphrase, String pubKeyPath, String privKeyPath, String revokeCertPath) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", "BC");
        kpg.initialize(4096);
        KeyPair kp = kpg.generateKeyPair();

        PGPDigestCalculator sha1Calc = new JcaPGPDigestCalculatorProviderBuilder().build()
                .get(HashAlgorithmTags.SHA1); // ✅ Use SHA1 here

        PGPKeyPair keyPair = new JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, kp, new Date());

        PGPKeyRingGenerator keyRingGen = new PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION,
                keyPair,
                identity,
                sha1Calc,
                null,
                null,
                new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256),
                new JcePBESecretKeyEncryptorBuilder(SymmetricKeyAlgorithmTags.AES_256, sha1Calc)
                        .setProvider("BC")
                        .build(passphrase.toCharArray())
        );

        PGPPublicKeyRing pubRing = keyRingGen.generatePublicKeyRing();
        PGPSecretKeyRing secRing = keyRingGen.generateSecretKeyRing();

        try (FileOutputStream pubOut = new FileOutputStream(pubKeyPath)) {
            pubRing.encode(pubOut);
        }

        try (FileOutputStream secOut = new FileOutputStream(privKeyPath)) {
            secRing.encode(secOut);
        }

        // Log fingerprint
        String fingerprint = Hex.toHexString(pubRing.getPublicKey().getFingerprint());
        System.out.println("Public Key Fingerprint: " + fingerprint);

        // Generate revocation certificate
        PGPSecretKey secretKey = secRing.getSecretKey();
        PGPPrivateKey privateKey = secretKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(passphrase.toCharArray()));

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(keyPair.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256).setProvider("BC")
        );

        signatureGenerator.init(PGPSignature.KEY_REVOCATION, privateKey);
        PGPSignature revocation = signatureGenerator.generateCertification(pubRing.getPublicKey());

        try (FileOutputStream revokeOut = new FileOutputStream(revokeCertPath)) {
            revocation.encode(revokeOut);
        }

        System.out.println("Key pair, public/private key files, and revocation certificate generated successfully.");
    }
}
