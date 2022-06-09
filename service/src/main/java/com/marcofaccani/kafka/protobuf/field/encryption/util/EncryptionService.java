package com.marcofaccani.kafka.protobuf.field.encryption.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

  private static final String ALGORITHM = "AES";
  private static final byte[] KEY = "MySecretKey123456789012!".getBytes(); // Chiave AES 24 bytes

  public String encrypt(String data) {
    try {
      SecretKeySpec secretKey = new SecretKeySpec(KEY, ALGORITHM);
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey);
      byte[] encryptedData = cipher.doFinal(data.getBytes());
      return Base64.getEncoder().encodeToString(encryptedData);
    } catch (Exception e) {
      throw new RuntimeException("Error while encrypting", e);
    }
  }

}
