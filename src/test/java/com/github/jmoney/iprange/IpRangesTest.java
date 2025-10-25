package com.github.jmoney.iprange;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IpRangesTest {

    @Test
    void testIPv4Ranges() {
        IpRanges checker = new IpRanges();
        checker.addRange("192.168.0.0/16");
        checker.addRange("10.0.0.0/8");

        assertTrue(checker.contains("192.168.1.1"));
        assertTrue(checker.contains("10.5.5.5"));
        assertFalse(checker.contains("8.8.8.8"));
    }

    @Test
    void testIPv6Ranges() {
        IpRanges checker = new IpRanges();
        checker.addRange("2001:db8::/32");
        checker.addRange("fe80::/10");

        assertTrue(checker.contains("2001:db8::1"));
        assertTrue(checker.contains("fe80::1"));
        assertFalse(checker.contains("2001:db9::1"));
    }

    @Test
    void testMixedIPv4AndIPv6() {
        IpRanges checker = new IpRanges();
        checker.addRange("192.168.0.0/16");
        checker.addRange("2001:db8::/32");

        assertTrue(checker.contains("192.168.1.1"));
        assertTrue(checker.contains("2001:db8::1"));
        assertFalse(checker.contains("10.0.0.1"));
        assertFalse(checker.contains("fe80::1"));
    }

    @Test
    void testConstructorWithRanges() {
        List<String> ranges = Arrays.asList(
            "192.168.0.0/16",
            "10.0.0.0/8",
            "2001:db8::/32"
        );

        IpRanges checker = new IpRanges(ranges);

        assertTrue(checker.contains("192.168.1.1"));
        assertTrue(checker.contains("10.5.5.5"));
        assertTrue(checker.contains("2001:db8::1"));
        assertFalse(checker.contains("8.8.8.8"));
    }

    @Test
    void testBuilderPattern() {
        IpRanges checker = IpRanges.builder()
            .addRange("192.168.0.0/16")
            .addRange("10.0.0.0/8")
            .addRange("2001:db8::/32")
            .build();

        assertTrue(checker.contains("192.168.1.1"));
        assertTrue(checker.contains("10.5.5.5"));
        assertTrue(checker.contains("2001:db8::1"));
        assertFalse(checker.contains("8.8.8.8"));
    }

    @Test
    void testBuilderWithCollections() {
        List<String> ipv4Ranges = Arrays.asList("192.168.0.0/16", "10.0.0.0/8");
        List<String> ipv6Ranges = Arrays.asList("2001:db8::/32", "fe80::/10");

        IpRanges checker = IpRanges.builder()
            .addRanges(ipv4Ranges)
            .addRanges(ipv6Ranges)
            .build();

        assertTrue(checker.contains("192.168.1.1"));
        assertTrue(checker.contains("2001:db8::1"));
        assertTrue(checker.contains("fe80::1"));
    }

    @Test
    void testAddRangesMethod() {
        IpRanges checker = new IpRanges();
        List<String> ranges = Arrays.asList(
            "192.168.0.0/16",
            "10.0.0.0/8",
            "2001:db8::/32"
        );

        checker.addRanges(ranges);

        assertTrue(checker.contains("192.168.1.1"));
        assertTrue(checker.contains("10.5.5.5"));
        assertTrue(checker.contains("2001:db8::1"));
    }

    @Test
    void testEmptyChecker() {
        IpRanges checker = new IpRanges();

        assertFalse(checker.contains("192.168.1.1"));
        assertFalse(checker.contains("2001:db8::1"));
    }

    @Test
    void testCommonPrivateNetworks() {
        IpRanges checker = IpRanges.builder()
            .addRange("10.0.0.0/8")        // Class A private
            .addRange("172.16.0.0/12")     // Class B private
            .addRange("192.168.0.0/16")    // Class C private
            .build();

        // Test each range
        assertTrue(checker.contains("10.1.2.3"));
        assertTrue(checker.contains("172.16.0.1"));
        assertTrue(checker.contains("172.31.255.255"));
        assertTrue(checker.contains("192.168.1.1"));

        // Test boundaries
        assertFalse(checker.contains("172.15.255.255"));
        assertFalse(checker.contains("172.32.0.0"));
        assertFalse(checker.contains("8.8.8.8"));
    }

    @Test
    void testInvalidInputs() {
        IpRanges checker = new IpRanges();

        assertThrows(IllegalArgumentException.class, () -> checker.addRange(null));
        assertThrows(IllegalArgumentException.class, () -> checker.addRange(""));
        assertThrows(IllegalArgumentException.class, () -> checker.addRange("   "));

        assertThrows(IllegalArgumentException.class, () -> checker.contains(null));
        assertThrows(IllegalArgumentException.class, () -> checker.contains(""));
        assertThrows(IllegalArgumentException.class, () -> checker.contains("   "));
    }

    @Test
    void testContainsIPv4Long() {
        IpRanges checker = new IpRanges();
        checker.addRange("192.168.0.0/16");

        long ip = IPv4RadixTree.ipToLong("192.168.1.100");
        assertTrue(checker.containsIPv4(ip));

        long outsideIp = IPv4RadixTree.ipToLong("10.0.0.1");
        assertFalse(checker.containsIPv4(outsideIp));
    }

    @Test
    void testContainsIPv6Bytes() {
        IpRanges checker = new IpRanges();
        checker.addRange("2001:db8::/32");

        byte[] ip = IPv6RadixTree.ipToBytes("2001:db8::1");
        assertTrue(checker.containsIPv6(ip));

        byte[] outsideIp = IPv6RadixTree.ipToBytes("2001:db9::1");
        assertFalse(checker.containsIPv6(outsideIp));
    }

    @Test
    void testRealWorldScenario() {
        // Simulate a firewall allowing specific internal and cloud provider ranges
        IpRanges checker = IpRanges.builder()
            // Internal networks
            .addRange("10.0.0.0/8")
            .addRange("192.168.0.0/16")
            // AWS specific ranges (example)
            .addRange("52.94.0.0/16")
            // IPv6 internal
            .addRange("fd00::/8")
            .build();

        // Should allow
        assertTrue(checker.contains("10.50.100.200"));
        assertTrue(checker.contains("192.168.1.1"));
        assertTrue(checker.contains("52.94.5.10"));
        assertTrue(checker.contains("fd00::1"));

        // Should deny
        assertFalse(checker.contains("8.8.8.8"));
        assertFalse(checker.contains("1.1.1.1"));
        assertFalse(checker.contains("2001:4860:4860::8888"));
    }

    @Test
    void testCopyConstructor() {
        // Create original with some ranges
        IpRanges original = IpRanges.builder()
            .addRange("192.168.0.0/16")
            .addRange("10.0.0.0/8")
            .addRange("2001:db8::/32")
            .build();

        // Create copy
        IpRanges copy = new IpRanges(original);

        // Verify copy has same ranges
        assertTrue(copy.contains("192.168.1.1"));
        assertTrue(copy.contains("10.5.5.5"));
        assertTrue(copy.contains("2001:db8::1"));
        assertFalse(copy.contains("172.16.0.1"));
    }

    @Test
    void testCopyConstructorIndependence() {
        // Create original
        IpRanges original = new IpRanges();
        original.addRange("192.168.0.0/16");
        original.addRange("10.0.0.0/8");

        // Create copy
        IpRanges copy = new IpRanges(original);

        // Modify copy by adding a new range
        copy.addRange("172.16.0.0/12");

        // Verify copy has the new range
        assertTrue(copy.contains("172.16.1.1"));

        // Verify original does NOT have the new range
        assertFalse(original.contains("172.16.1.1"));

        // Verify both still have original ranges
        assertTrue(original.contains("192.168.1.1"));
        assertTrue(copy.contains("192.168.1.1"));
    }

    @Test
    void testCopyConstructorWithEmptyRanges() {
        IpRanges original = new IpRanges();
        IpRanges copy = new IpRanges(original);

        assertFalse(copy.contains("192.168.1.1"));
        assertFalse(copy.contains("2001:db8::1"));

        // Add to copy
        copy.addRange("192.168.0.0/16");
        assertTrue(copy.contains("192.168.1.1"));
        assertFalse(original.contains("192.168.1.1"));
    }

    @Test
    void testGetRanges() {
        IpRanges ranges = new IpRanges();
        ranges.addRange("192.168.0.0/16");
        ranges.addRange("10.0.0.0/8");
        ranges.addRange("2001:db8::/32");

        List<String> allRanges = ranges.getRanges();

        assertEquals(3, allRanges.size());
        assertTrue(allRanges.contains("192.168.0.0/16"));
        assertTrue(allRanges.contains("10.0.0.0/8"));
        assertTrue(allRanges.contains("2001:db8::/32"));
    }

    @Test
    void testGetRangesUnmodifiable() {
        IpRanges ranges = new IpRanges();
        ranges.addRange("192.168.0.0/16");

        List<String> allRanges = ranges.getRanges();

        // Verify list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> allRanges.add("10.0.0.0/8"));
    }

    @Test
    void testCopyConstructorFromBuiltInstance() {
        // Build original using builder
        IpRanges original = IpRanges.builder()
            .addRange("192.168.0.0/16")
            .addRange("10.0.0.0/8")
            .build();

        // Copy and extend
        IpRanges extended = new IpRanges(original);
        extended.addRange("172.16.0.0/12");

        // Verify both work correctly
        assertTrue(original.contains("192.168.1.1"));
        assertFalse(original.contains("172.16.1.1"));

        assertTrue(extended.contains("192.168.1.1"));
        assertTrue(extended.contains("172.16.1.1"));
    }
}
