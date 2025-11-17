package com.vticket.identity.infra.jwt;

import com.vticket.commonlibs.utils.RSAEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
@Service
public class RSAService {

    /**
     * @param filePath - Địa chỉ private file
     * @return RSAPrivateKey - interface của RSA private key
     */
    public RSAPrivateKey loadPrivate(String filePath) {
        String prefix = "[loadPrivate]|filePath=" + filePath;
        try {
            if (filePath == null || filePath.isEmpty()) {
                log.error("{}|FilePath invalid input:{}", prefix, filePath);
                return null;
            }
            String path = filePath.replace("classpath:", "");
            Resource resource = new ClassPathResource(path);

            String key = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            return (RSAPrivateKey) RSAEncryptUtil.getPrivateKeyFromString(key);
        } catch (Exception ex) {
            log.error("{}|Exception={}", prefix, ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * @param filePath - Địa chỉ public file
     * @return RSAPublicKey - interface của RSA public key
     */
    public RSAPublicKey loadPublicKey(String filePath) {
        String prefix = "[loadPublicKey]|filePath=" + filePath;
        try {
            if (filePath == null || filePath.isEmpty()) {
                log.error("{}|FilePath invalid input:{}", prefix, filePath);
                return null;
            }
            String path = filePath.replace("classpath:", "");
            Resource resource = new ClassPathResource(path);

            String key = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            return (RSAPublicKey) RSAEncryptUtil.getPublicKeyFromString(key);
        } catch (Exception ex) {
            log.error("{}|Exception={}", prefix, ex.getMessage(), ex);
            return null;
        }
    }

}
