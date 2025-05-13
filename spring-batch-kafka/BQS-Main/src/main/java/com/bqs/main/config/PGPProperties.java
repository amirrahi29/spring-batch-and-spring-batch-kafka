package com.bqs.main.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pgp")
public class PGPProperties {
    private String inputFile;
    private String encryptedFile;
    private String decryptedFile;
    private String publicKey;
    private String privateKey;
    private String revocationCert;
    private String identity;
    private String passphrase;

    public String getInputFile() {
        return inputFile;
    }
    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }
    public String getEncryptedFile() {
        return encryptedFile;
    }
    public void setEncryptedFile(String encryptedFile) {
        this.encryptedFile = encryptedFile;
    }
    public String getDecryptedFile() {
        return decryptedFile;
    }
    public void setDecryptedFile(String decryptedFile) {
        this.decryptedFile = decryptedFile;
    }
    public String getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    public String getPrivateKey() {
        return privateKey;
    }
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    public String getRevocationCert() {
        return revocationCert;
    }
    public void setRevocationCert(String revocationCert) {
        this.revocationCert = revocationCert;
    }
    public String getIdentity() {
        return identity;
    }
    public void setIdentity(String identity) {
        this.identity = identity;
    }
    public String getPassphrase() {
        return passphrase;
    }
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
}
