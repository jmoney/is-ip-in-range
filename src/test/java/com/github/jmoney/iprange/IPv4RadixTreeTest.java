package com.github.jmoney.iprange;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class IPv4RadixTreeTest {

    @Test
    void testSingleIpRange() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("192.168.1.100/32");

        assertTrue(tree.contains("192.168.1.100"));
        assertFalse(tree.contains("192.168.1.101"));
        assertFalse(tree.contains("192.168.1.99"));
    }

    @Test
    void testClassCNetwork() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("192.168.1.0/24");

        assertTrue(tree.contains("192.168.1.0"));
        assertTrue(tree.contains("192.168.1.1"));
        assertTrue(tree.contains("192.168.1.100"));
        assertTrue(tree.contains("192.168.1.255"));
        assertFalse(tree.contains("192.168.0.255"));
        assertFalse(tree.contains("192.168.2.0"));
    }

    @Test
    void testClassBNetwork() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("172.16.0.0/16");

        assertTrue(tree.contains("172.16.0.0"));
        assertTrue(tree.contains("172.16.1.1"));
        assertTrue(tree.contains("172.16.255.255"));
        assertFalse(tree.contains("172.15.255.255"));
        assertFalse(tree.contains("172.17.0.0"));
    }

    @Test
    void testClassANetwork() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("10.0.0.0/8");

        assertTrue(tree.contains("10.0.0.0"));
        assertTrue(tree.contains("10.1.2.3"));
        assertTrue(tree.contains("10.255.255.255"));
        assertFalse(tree.contains("9.255.255.255"));
        assertFalse(tree.contains("11.0.0.0"));
    }

    @Test
    void testMultipleRanges() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("192.168.0.0/16");
        tree.addRange("10.0.0.0/8");
        tree.addRange("172.16.0.0/12");

        assertTrue(tree.contains("192.168.1.1"));
        assertTrue(tree.contains("10.5.5.5"));
        assertTrue(tree.contains("172.20.1.1"));
        assertFalse(tree.contains("8.8.8.8"));
        assertFalse(tree.contains("1.1.1.1"));
    }

    @Test
    void testOverlappingRanges() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("192.168.0.0/16");
        tree.addRange("192.168.1.0/24");  // More specific range within the first

        assertTrue(tree.contains("192.168.1.1"));
        assertTrue(tree.contains("192.168.2.1"));
        assertFalse(tree.contains("192.169.1.1"));
    }

    @Test
    void testZeroPrefixLength() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("0.0.0.0/0");  // Match all IPs

        assertTrue(tree.contains("0.0.0.0"));
        assertTrue(tree.contains("255.255.255.255"));
        assertTrue(tree.contains("192.168.1.1"));
        assertTrue(tree.contains("8.8.8.8"));
    }

    @ParameterizedTest
    @CsvSource({
        "192.168.1.0/24, 192.168.1.128, true",
        "10.0.0.0/8, 10.255.255.255, true",
        "172.16.0.0/12, 172.31.255.255, true",
        "172.16.0.0/12, 172.32.0.0, false",
        "192.168.1.0/25, 192.168.1.127, true",
        "192.168.1.0/25, 192.168.1.128, false"
    })
    void testVariousCidrRanges(String cidr, String testIp, boolean expected) {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange(cidr);
        assertEquals(expected, tree.contains(testIp));
    }

    @Test
    void testIpToLong() {
        assertEquals(0L, IPv4RadixTree.ipToLong("0.0.0.0"));
        assertEquals(4294967295L, IPv4RadixTree.ipToLong("255.255.255.255"));
        assertEquals(3232235776L, IPv4RadixTree.ipToLong("192.168.1.0"));
        assertEquals(167772161L, IPv4RadixTree.ipToLong("10.0.0.1"));
    }

    @Test
    void testInvalidCidrFormat() {
        IPv4RadixTree tree = new IPv4RadixTree();
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("192.168.1.0"));
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("192.168.1.0/"));
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("192.168.1.0/33"));
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("192.168.1.0/-1"));
    }

    @Test
    void testInvalidIpFormat() {
        IPv4RadixTree tree = new IPv4RadixTree();
        tree.addRange("192.168.1.0/24");

        assertThrows(IllegalArgumentException.class, () -> tree.contains("192.168.1"));
        assertThrows(IllegalArgumentException.class, () -> tree.contains("192.168.1.1.1"));
        assertThrows(IllegalArgumentException.class, () -> tree.contains("256.1.1.1"));
    }

    @Test
    void testEmptyTree() {
        IPv4RadixTree tree = new IPv4RadixTree();
        assertFalse(tree.contains("192.168.1.1"));
        assertFalse(tree.contains("0.0.0.0"));
    }
}
