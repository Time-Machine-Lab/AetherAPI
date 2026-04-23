package io.github.timemachinelab;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
public class EncodingUtil {

    @Autowired
    StringEncryptor encryptor;

    @Test
    public void getPass() {
        String encryptedUsername = encryptor.encrypt("root");
        String encryptedPassword = encryptor.encrypt("root");

        System.out.println("Username Encrypted: " + encryptedUsername);
        System.out.println("Password Encrypted: " + encryptedPassword);
    }
}
