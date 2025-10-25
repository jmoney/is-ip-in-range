package com.github.jmoney.iprange;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class IPv6RadixTreeTest {

    @Test
    void testSingleIpRange() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("2001:db8::1/128");

        assertTrue(tree.contains("2001:db8::1"));
        assertFalse(tree.contains("2001:db8::2"));
        assertFalse(tree.contains("2001:db8::"));
    }

    @Test
    void testCommonPrefix() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("2001:db8::/32");

        assertTrue(tree.contains("2001:db8::"));
        assertTrue(tree.contains("2001:db8::1"));
        assertTrue(tree.contains("2001:db8:1::1"));
        assertTrue(tree.contains("2001:db8:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(tree.contains("2001:db7:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(tree.contains("2001:db9::"));
    }

    @Test
    void testMultipleRanges() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("2001:db8::/32");
        tree.addRange("fe80::/10");
        tree.addRange("::1/128");

        assertTrue(tree.contains("2001:db8::1"));
        assertTrue(tree.contains("fe80::1"));
        assertTrue(tree.contains("::1"));
        assertFalse(tree.contains("2001:db7::1"));
        assertFalse(tree.contains("::2"));
    }

    @Test
    void testOverlappingRanges() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("2001:db8::/32");
        tree.addRange("2001:db8:1::/48");  // More specific range within the first

        assertTrue(tree.contains("2001:db8:1::1"));
        assertTrue(tree.contains("2001:db8:2::1"));
        assertFalse(tree.contains("2001:db9::1"));
    }

    @Test
    void testLinkLocalRange() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("fe80::/10");

        assertTrue(tree.contains("fe80::"));
        assertTrue(tree.contains("fe80::1"));
        assertTrue(tree.contains("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(tree.contains("fec0::"));
    }

    @Test
    void testZeroPrefixLength() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("::/0");  // Match all IPv6 addresses

        assertTrue(tree.contains("::"));
        assertTrue(tree.contains("::1"));
        assertTrue(tree.contains("2001:db8::1"));
        assertTrue(tree.contains("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
    }

    @ParameterizedTest
    @CsvSource({
        "2001:db8::/32, 2001:db8::1, true",
        "2001:db8::/32, 2001:db9::1, false",
        "fe80::/10, fe80::1, true",
        "fe80::/10, fec0::1, false",
        "::1/128, ::1, true",
        "::1/128, ::2, false",
        "2001:db8:1::/48, 2001:db8:1:2::1, true",
        "2001:db8:1::/48, 2001:db8:2:1::1, false"
    })
    void testVariousCidrRanges(String cidr, String testIp, boolean expected) {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange(cidr);
        assertEquals(expected, tree.contains(testIp));
    }

    @Test
    void testCompressedNotation() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("2001:db8::/32");

        // Different representations of the same address
        assertTrue(tree.contains("2001:db8:0:0:0:0:0:1"));
        assertTrue(tree.contains("2001:db8::1"));
        assertTrue(tree.contains("2001:0db8:0000:0000:0000:0000:0000:0001"));
    }

    @Test
    void testInvalidCidrFormat() {
        IPv6RadixTree tree = new IPv6RadixTree();
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("2001:db8::"));
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("2001:db8::/"));
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("2001:db8::/129"));
        assertThrows(IllegalArgumentException.class, () -> tree.addRange("2001:db8::/-1"));
    }

    @Test
    void testInvalidIpFormat() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("2001:db8::/32");

        assertThrows(IllegalArgumentException.class, () -> tree.contains("not-an-ip"));
        assertThrows(IllegalArgumentException.class, () -> tree.contains("gggg::1"));
    }

    @Test
    void testEmptyTree() {
        IPv6RadixTree tree = new IPv6RadixTree();
        assertFalse(tree.contains("2001:db8::1"));
        assertFalse(tree.contains("::1"));
    }

    @Test
    void testUniqueLocalRange() {
        IPv6RadixTree tree = new IPv6RadixTree();
        tree.addRange("fc00::/7");  // Unique local address range

        assertTrue(tree.contains("fc00::1"));
        assertTrue(tree.contains("fd00::1"));
        assertTrue(tree.contains("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(tree.contains("fe00::1"));  // Outside range
        assertFalse(tree.contains("fb00::1"));  // Outside range
    }
}
