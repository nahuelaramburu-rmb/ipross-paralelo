package com.capacidad.validationapi.module.encryption;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.ApplicationProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Log4j2
@Service
public class EncryptionServiceImpl implements EncryptionService {

    private final ApplicationProperties applicationProperties;

    @Autowired
    public EncryptionServiceImpl(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String decrypt(String content) throws ObjectNotValidException {
        try {
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
            byte[] contentBytes = cipher.doFinal(Base64.getDecoder().decode(content.getBytes(StandardCharsets.UTF_8)));
            return new String(contentBytes, StandardCharsets.UTF_8);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            log.error("Content Decryption error: ({}) - {} - Content: {}", e.getClass(), e.getMessage(), content);
            throw new ObjectNotValidException("decryptionError");
        }
    }

    @Override
    public String encrypt(String content) throws ObjectNotValidException {
        try {
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
            byte[] contentBytes = Base64.getEncoder().encode(cipher.doFinal(content.getBytes(StandardCharsets.UTF_8)));
            return new String(contentBytes);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            log.error("Content Encryption error: ({}) - {}", e.getClass(), e.getMessage());
            throw new ObjectNotValidException("encryptionError");
        }
    }

    private Cipher createCipher(int mode) throws ObjectNotValidException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] secretKeyBytes = applicationProperties.getQrEncryptionKey().getBytes(StandardCharsets.UTF_8);
            SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
            byte[] ivBytes = applicationProperties.getQrEncryptionIv().getBytes(StandardCharsets.UTF_8);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(mode, secretKey, ivSpec);
            return cipher;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            log.error("QR Decryption error: ({}) - {}", e.getClass(), e.getMessage());
            throw new ObjectNotValidException("encryptionInitializationError");
        }
    }

}
