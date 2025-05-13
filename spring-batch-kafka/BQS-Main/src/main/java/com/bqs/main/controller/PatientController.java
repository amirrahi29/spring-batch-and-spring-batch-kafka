package com.bqs.main.controller;

import com.bqs.main.config.PGPProperties;
import com.pgp.crypto.lib.PGPDecryptUtil;
import com.pgp.crypto.lib.PGPEncryptUtil;
import com.pgp.crypto.lib.PGPKeyUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pgp")
public class PatientController {

    private final PGPProperties props;

    public PatientController(PGPProperties props) {
        this.props = props;
    }

    @GetMapping("/generate-keys")
    public String generateKeys() {
        try {
            PGPKeyUtil.generatePGPKeyPair(
                    props.getIdentity(),
                    props.getPassphrase(),
                    props.getPublicKey(),
                    props.getPrivateKey(),
                    props.getRevocationCert()
            );
            return "Keys generated successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to generate keys: " + e.getMessage();
        }
    }

    @GetMapping("/encrypt")
    public String encryptCSV() {
        try {
            PGPEncryptUtil.encryptFile(
                    props.getInputFile(),
                    props.getEncryptedFile(),
                    props.getPublicKey()
            );
            return "File encrypted successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Encryption failed: " + e.getMessage();
        }
    }

    @GetMapping("/decrypt")
    public String decryptCSV() {
        try {
            PGPDecryptUtil.decryptFile(
                    props.getEncryptedFile(),
                    props.getDecryptedFile(),
                    props.getPrivateKey(),
                    props.getPassphrase()
            );
            return "File decrypted successfully.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Decryption failed: " + e.getMessage();
        }
    }
}
