package com.github.jmoney.iprange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Main API for checking if IP addresses are within a set of CIDR ranges.
 * Supports both IPv4 and IPv6 addresses with memory-efficient radix tree storage.
 *
 * Example usage:
 * <pre>
 * // Using builder pattern
 * IpRanges ranges = IpRanges.builder()
 *     .addRange("192.168.0.0/16")
 *     .addRange("10.0.0.0/8")
 *     .addRange("2001:db8::/32")
 *     .build();
 *
 * boolean result = ranges.contains("192.168.1.100");  // true
 *
 * // Or using constructor
 * IpRanges ranges = new IpRanges();
 * ranges.addRange("192.168.0.0/16");
 * ranges.addRange("10.0.0.0/8");
 * boolean result = ranges.contains("192.168.1.100");  // true
 *
 * // Copy constructor
 * IpRanges copy = new IpRanges(existingRanges);
 * copy.addRange("172.16.0.0/12");  // Add to copy without affecting original
 * </pre>
 */
public class IpRanges {

    private final IPv4RadixTree ipv4Tree;
    private final IPv6RadixTree ipv6Tree;
    private final List<String> cidrRanges;

    /**
     * Create a new IP ranges checker with no ranges.
     */
    public IpRanges() {
        this.ipv4Tree = new IPv4RadixTree();
        this.ipv6Tree = new IPv6RadixTree();
        this.cidrRanges = new ArrayList<>();
    }

    /**
     * Create a new IP ranges checker with the given CIDR ranges.
     * @param cidrRanges collection of CIDR notation strings
     */
    public IpRanges(Collection<String> cidrRanges) {
        this();
        for (String cidr : cidrRanges) {
            addRange(cidr);
        }
    }

    /**
     * Copy constructor - creates a new IP ranges checker with the same ranges as the given instance.
     * The copy is independent and can be modified without affecting the original.
     *
     * @param other the IpRanges instance to copy
     */
    public IpRanges(IpRanges other) {
        this();
        for (String cidr : other.cidrRanges) {
            addRange(cidr);
        }
    }

    /**
     * Add a CIDR range to check against.
     * Automatically detects IPv4 vs IPv6 based on the format.
     *
     * @param cidr CIDR notation string (e.g., "192.168.1.0/24" or "2001:db8::/32")
     * @return this IpRanges instance for method chaining
     * @throws IllegalArgumentException if CIDR format is invalid
     */
    public IpRanges addRange(String cidr) {
        if (cidr == null || cidr.trim().isEmpty()) {
            throw new IllegalArgumentException("CIDR range cannot be null or empty");
        }

        // Detect IPv4 vs IPv6 by checking for colons
        if (cidr.contains(":")) {
            ipv6Tree.addRange(cidr);
        } else {
            ipv4Tree.addRange(cidr);
        }

        cidrRanges.add(cidr);
        return this;
    }

    /**
     * Add multiple CIDR ranges at once.
     * @param cidrRanges collection of CIDR notation strings
     * @return this IpRanges instance for method chaining
     */
    public IpRanges addRanges(Collection<String> cidrRanges) {
        for (String cidr : cidrRanges) {
            addRange(cidr);
        }
        return this;
    }

    /**
     * Check if an IP address is within any of the configured CIDR ranges.
     * Automatically detects IPv4 vs IPv6 based on the format.
     *
     * @param ip IP address as string (e.g., "192.168.1.100" or "2001:db8::1")
     * @return true if the IP is in any range, false otherwise
     * @throws IllegalArgumentException if IP format is invalid
     */
    public boolean contains(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }

        // Detect IPv4 vs IPv6 by checking for colons
        if (ip.contains(":")) {
            return ipv6Tree.contains(ip);
        } else {
            return ipv4Tree.contains(ip);
        }
    }

    /**
     * Check if an IPv4 address is within any of the configured IPv4 CIDR ranges.
     * @param ip IPv4 address as long (unsigned 32-bit)
     * @return true if the IP is in any range, false otherwise
     */
    public boolean containsIPv4(long ip) {
        return ipv4Tree.contains(ip);
    }

    /**
     * Check if an IPv6 address is within any of the configured IPv6 CIDR ranges.
     * @param ip IPv6 address as 16-byte array
     * @return true if the IP is in any range, false otherwise
     */
    public boolean containsIPv6(byte[] ip) {
        return ipv6Tree.contains(ip);
    }

    /**
     * Get an unmodifiable view of all CIDR ranges in this instance.
     * @return unmodifiable list of CIDR notation strings
     */
    public List<String> getRanges() {
        return Collections.unmodifiableList(cidrRanges);
    }

    /**
     * Create a new builder for fluent construction.
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for convenient construction with fluent API.
     */
    public static class Builder {
        private final IpRanges ranges;

        private Builder() {
            this.ranges = new IpRanges();
        }

        /**
         * Add a CIDR range.
         * @param cidr CIDR notation string
         * @return this builder
         */
        public Builder addRange(String cidr) {
            ranges.addRange(cidr);
            return this;
        }

        /**
         * Add multiple CIDR ranges.
         * @param cidrRanges collection of CIDR notation strings
         * @return this builder
         */
        public Builder addRanges(Collection<String> cidrRanges) {
            ranges.addRanges(cidrRanges);
            return this;
        }

        /**
         * Build and return the configured IpRanges instance.
         * @return configured IpRanges instance
         */
        public IpRanges build() {
            return ranges;
        }
    }
}
