package com.github.jmoney.iprange;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Binary radix tree implementation for efficient IPv6 CIDR range matching.
 * Uses a binary trie where each bit in the IP address determines the path.
 * Memory-efficient through prefix sharing and constant O(128) lookup time.
 */
public class IPv6RadixTree {

    private static class Node {
        Node left;   // 0 bit
        Node right;  // 1 bit
        boolean isEndOfRange;  // true if this node represents a complete CIDR range

        Node() {
            this.left = null;
            this.right = null;
            this.isEndOfRange = false;
        }
    }

    private final Node root;

    public IPv6RadixTree() {
        this.root = new Node();
    }

    /**
     * Add a CIDR range to the tree.
     * @param cidr CIDR notation string (e.g., "2001:db8::/32")
     * @throws IllegalArgumentException if CIDR format is invalid
     */
    public void addRange(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format: " + cidr);
        }

        byte[] ip = ipToBytes(parts[0]);
        int prefixLength = Integer.parseInt(parts[1]);

        if (prefixLength < 0 || prefixLength > 128) {
            throw new IllegalArgumentException("Invalid prefix length: " + prefixLength);
        }

        addRange(ip, prefixLength);
    }

    /**
     * Add a CIDR range using byte array IP and prefix length.
     * @param ip IP address as 16-byte array
     * @param prefixLength prefix length (0-128)
     */
    public void addRange(byte[] ip, int prefixLength) {
        if (ip.length != 16) {
            throw new IllegalArgumentException("IPv6 address must be 16 bytes");
        }

        Node current = root;

        // Traverse/create tree based on prefix bits
        for (int i = 0; i < prefixLength; i++) {
            boolean bit = getBit(ip, i);

            if (bit) {
                if (current.right == null) {
                    current.right = new Node();
                }
                current = current.right;
            } else {
                if (current.left == null) {
                    current.left = new Node();
                }
                current = current.left;
            }
        }

        current.isEndOfRange = true;
    }

    /**
     * Check if an IP address matches any CIDR range in the tree.
     * @param ip IP address as string (e.g., "2001:db8::1")
     * @return true if IP is in any range, false otherwise
     */
    public boolean contains(String ip) {
        return contains(ipToBytes(ip));
    }

    /**
     * Check if an IP address matches any CIDR range in the tree.
     * @param ip IP address as 16-byte array
     * @return true if IP is in any range, false otherwise
     */
    public boolean contains(byte[] ip) {
        if (ip.length != 16) {
            throw new IllegalArgumentException("IPv6 address must be 16 bytes");
        }

        Node current = root;

        // Traverse tree following the IP's bits
        for (int i = 0; i < 128; i++) {
            if (current.isEndOfRange) {
                return true;  // Found a matching CIDR range
            }

            boolean bit = getBit(ip, i);

            if (bit) {
                if (current.right == null) {
                    return false;
                }
                current = current.right;
            } else {
                if (current.left == null) {
                    return false;
                }
                current = current.left;
            }
        }

        return current.isEndOfRange;
    }

    /**
     * Get the bit at a specific position in the byte array.
     * @param bytes byte array
     * @param bitIndex bit index (0 = MSB of first byte)
     * @return true if bit is 1, false if 0
     */
    private static boolean getBit(byte[] bytes, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitOffset = 7 - (bitIndex % 8);
        return ((bytes[byteIndex] >> bitOffset) & 1) == 1;
    }

    /**
     * Convert IPv6 string to byte array representation.
     * @param ip IPv6 address string (e.g., "2001:db8::1")
     * @return IP address as 16-byte array
     */
    static byte[] ipToBytes(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            byte[] bytes = addr.getAddress();
            if (bytes.length != 16) {
                throw new IllegalArgumentException("Not an IPv6 address: " + ip);
            }
            return bytes;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPv6 address: " + ip, e);
        }
    }
}
