package com.github.jmoney.iprange;

/**
 * Binary radix tree implementation for efficient IPv4 CIDR range matching.
 * Uses a binary trie where each bit in the IP address determines the path.
 * Memory-efficient through prefix sharing and constant O(32) lookup time.
 */
public class IPv4RadixTree {

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

    public IPv4RadixTree() {
        this.root = new Node();
    }

    /**
     * Add a CIDR range to the tree.
     * @param cidr CIDR notation string (e.g., "192.168.1.0/24")
     * @throws IllegalArgumentException if CIDR format is invalid
     */
    public void addRange(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format: " + cidr);
        }

        long ip = ipToLong(parts[0]);
        int prefixLength = Integer.parseInt(parts[1]);

        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Invalid prefix length: " + prefixLength);
        }

        addRange(ip, prefixLength);
    }

    /**
     * Add a CIDR range using numeric IP and prefix length.
     * @param ip IP address as long
     * @param prefixLength prefix length (0-32)
     */
    public void addRange(long ip, int prefixLength) {
        Node current = root;

        // Traverse/create tree based on prefix bits
        for (int i = 31; i >= 32 - prefixLength; i--) {
            boolean bit = ((ip >> i) & 1) == 1;

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
     * @param ip IP address as string (e.g., "192.168.1.100")
     * @return true if IP is in any range, false otherwise
     */
    public boolean contains(String ip) {
        return contains(ipToLong(ip));
    }

    /**
     * Check if an IP address matches any CIDR range in the tree.
     * @param ip IP address as long
     * @return true if IP is in any range, false otherwise
     */
    public boolean contains(long ip) {
        Node current = root;

        // Traverse tree following the IP's bits
        // Check at each level if we've hit a CIDR range endpoint
        for (int i = 31; i >= 0; i--) {
            if (current.isEndOfRange) {
                return true;  // Found a matching CIDR range
            }

            boolean bit = ((ip >> i) & 1) == 1;

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
     * Convert IPv4 string to long representation.
     * @param ip IPv4 address string (e.g., "192.168.1.1")
     * @return IP address as long (unsigned 32-bit)
     */
    static long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid octet value: " + octet);
            }
            result = (result << 8) | octet;
        }

        return result;
    }
}
