package ua.lpnu.deposits.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hash_returnsNonNull() {
        assertNotNull(PasswordUtil.hash("password"));
    }

    @Test
    void hash_returns64CharHex() {
        String hash = PasswordUtil.hash("test");
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void hash_sameInputSameOutput() {
        assertEquals(PasswordUtil.hash("abc"), PasswordUtil.hash("abc"));
    }

    @Test
    void hash_differentInputsDifferentOutputs() {
        assertNotEquals(PasswordUtil.hash("abc"), PasswordUtil.hash("xyz"));
    }

    @Test
    void hash_emptyString_doesNotThrow() {
        assertNotNull(PasswordUtil.hash(""));
    }

    @Test
    void verify_correctPassword_returnsTrue() {
        String stored = PasswordUtil.hash("secret");
        assertTrue(PasswordUtil.verify("secret", stored));
    }

    @Test
    void verify_wrongPassword_returnsFalse() {
        String stored = PasswordUtil.hash("secret");
        assertFalse(PasswordUtil.verify("wrong", stored));
    }

    @Test
    void verify_emptyVsEmpty_returnsTrue() {
        assertTrue(PasswordUtil.verify("", PasswordUtil.hash("")));
    }
}
