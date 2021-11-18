package framework.user;

import org.apache.commons.lang3.RandomStringUtils;

public class SaltGenerator {
    
    public static String generateSalt() {
        return RandomStringUtils.randomAlphanumeric(64);
    }
}
