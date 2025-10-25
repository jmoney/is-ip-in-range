# is-ip-in-range

A high-performance, memory-efficient Java library for checking if IP addresses are within a set of CIDR ranges. Supports both IPv4 and IPv6 addresses.

## Features

- **Fast lookups**: O(k) time complexity where k is the address length (32 bits for IPv4, 128 bits for IPv6)
- **Memory efficient**: Uses radix tree (Patricia trie) data structure with prefix sharing
- **No iteration**: Direct tree traversal, no looping over CIDR lists
- **Dual stack support**: Works with both IPv4 and IPv6 addresses
- **Clean API**: Simple, fluent interface with builder pattern support
- **Zero dependencies**: No external runtime dependencies

## Performance Characteristics

The library uses a binary radix tree (Patricia trie) implementation:

- **Lookup Time**: O(k) where k is constant (32 for IPv4, 128 for IPv6)
- **Insert Time**: O(k) where k is constant
- **Space Complexity**: O(n*k) where n is number of ranges, but with significant savings from prefix sharing
- **No rebalancing**: Unlike interval trees, no costly rebalancing operations

This makes it ideal for scenarios with:
- Large numbers of CIDR ranges
- Frequent IP lookups
- Memory-constrained environments

## Installation

This library is published to GitHub Packages. Follow these steps to use it in your Maven project:

### 1. Generate a GitHub Personal Access Token (PAT)

1. Go to GitHub Settings → Developer settings → Personal access tokens → [Tokens (classic)](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Give it a descriptive name (e.g., "Maven GitHub Packages")
4. Select the `read:packages` scope
5. Click "Generate token" and copy the token (you won't see it again!)

### 2. Configure Maven Authentication

Add your GitHub credentials to `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

**Security Note:** Never commit your `settings.xml` with credentials to version control!

### 3. Add Repository to Your Project

Add the GitHub Packages repository to your project's `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/jmoney/is-ip-in-range</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

### 4. Add Dependency

Add the dependency to your `pom.xml`:

**For stable releases (recommended for production):**
```xml
<dependency>
    <groupId>com.github.jmoney</groupId>
    <artifactId>is-ip-in-range</artifactId>
    <version>1.0.0</version>
</dependency>
```

Check [releases](https://github.com/jmoney/is-ip-in-range/releases) for the latest stable version.

**For latest development snapshot:**
```xml
<dependency>
    <groupId>com.github.jmoney</groupId>
    <artifactId>is-ip-in-range</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Snapshots are automatically published on every commit to main. To get the latest snapshot updates:
```bash
mvn clean install -U  # The -U flag forces Maven to check for updates
```

### Alternative: Local Installation

For development, you can install to your local Maven repository:

```bash
git clone https://github.com/jmoney/is-ip-in-range.git
cd is-ip-in-range
mvn clean install
```

Then use the dependency without needing GitHub authentication.

### Published Artifacts

When publishing to GitHub Packages, the build automatically generates and uploads three JARs:

1. **Main JAR** (`is-ip-in-range-1.0.0-SNAPSHOT.jar`)
   - Compiled bytecode
   - What gets added to your classpath

2. **Sources JAR** (`is-ip-in-range-1.0.0-SNAPSHOT-sources.jar`)
   - Original `.java` source files
   - IDEs (IntelliJ, Eclipse, VS Code) automatically download this
   - Allows you to navigate into library code and see actual source
   - Essential for debugging and understanding the code

3. **Javadoc JAR** (`is-ip-in-range-1.0.0-SNAPSHOT-javadoc.jar`)
   - Generated HTML documentation
   - IDEs show javadoc in code completion tooltips
   - Can be viewed in browser

**No additional configuration needed** - Maven and your IDE handle downloading sources/javadoc automatically when you use the dependency!

## Usage

### Basic Usage

```java
import com.github.jmoney.iprange.IpRanges;

// Create IP ranges and add CIDR ranges
IpRanges ranges = new IpRanges();
ranges.addRange("192.168.0.0/16");
ranges.addRange("10.0.0.0/8");
ranges.addRange("2001:db8::/32");

// Check if IPs are in any of the ranges
boolean result1 = ranges.contains("192.168.1.100");  // true
boolean result2 = ranges.contains("8.8.8.8");        // false
boolean result3 = ranges.contains("2001:db8::1");    // true
```

### Builder Pattern

```java
IpRanges ranges = IpRanges.builder()
    .addRange("192.168.0.0/16")
    .addRange("10.0.0.0/8")
    .addRange("172.16.0.0/12")
    .addRange("2001:db8::/32")
    .build();

if (ranges.contains("192.168.1.1")) {
    System.out.println("IP is in allowed range");
}
```

### Constructor with Collection

```java
List<String> privateRanges = Arrays.asList(
    "10.0.0.0/8",
    "172.16.0.0/12",
    "192.168.0.0/16"
);

IpRanges ranges = new IpRanges(privateRanges);
```

### Copy Constructor

Create a copy of an existing `IpRanges` instance. The copy is independent and can be modified without affecting the original:

```java
// Create original ranges
IpRanges baseRanges = IpRanges.builder()
    .addRange("192.168.0.0/16")
    .addRange("10.0.0.0/8")
    .build();

// Create a copy
IpRanges extendedRanges = new IpRanges(baseRanges);

// Add additional ranges to the copy
extendedRanges.addRange("172.16.0.0/12");

// Original is unmodified
baseRanges.contains("172.16.1.1");     // false
extendedRanges.contains("172.16.1.1"); // true
```

This is useful for creating variations of a base configuration:

```java
// Base configuration for all internal networks
IpRanges internalBase = IpRanges.builder()
    .addRange("10.0.0.0/8")
    .addRange("172.16.0.0/12")
    .addRange("192.168.0.0/16")
    .build();

// Development environment: internal + dev cloud
IpRanges devRanges = new IpRanges(internalBase);
devRanges.addRange("52.94.0.0/16");  // AWS dev

// Production environment: internal + prod cloud
IpRanges prodRanges = new IpRanges(internalBase);
prodRanges.addRange("52.95.0.0/16");  // AWS prod
```

## Thread Safety

**Note:** `IpRanges` instances are **not thread-safe** by default. However, you can achieve thread-safe, lock-free reads using a **copy-on-write pattern** with the copy constructor:

### Copy-on-Write Pattern for Concurrent Access

Ideal for read-heavy workloads with rare updates:

```java
public class IpAllowList {
    // AtomicReference provides thread-safe reference swap
    private final AtomicReference<IpRanges> ranges =
        new AtomicReference<>(new IpRanges());

    // READERS: Multiple threads, wait-free, no locks
    public boolean isAllowed(String clientIp) {
        IpRanges current = ranges.get();  // Atomic read
        return current.contains(clientIp); // Read from immutable snapshot
    }

    // WRITER: Single thread performs copy-on-write
    public void addAllowedRange(String cidr) {
        IpRanges oldRanges = ranges.get();
        IpRanges newRanges = new IpRanges(oldRanges);  // Copy all ranges
        newRanges.addRange(cidr);                       // Modify copy
        ranges.set(newRanges);                          // Atomic swap
    }
}
```

**How it works:**
- Each `IpRanges` instance is effectively immutable once published
- Readers access their snapshot without any locking
- Writer creates new instance, modifies it, then atomically publishes
- `AtomicReference` ensures visibility: readers always see fully constructed state

**Trade-offs:**
- ✅ **Pros**: Wait-free reads, maximum read throughput, no reader contention
- ✅ **Perfect for**: Many readers, infrequent writes (< 1/second)
- ❌ **Cons**: Write cost is O(n) where n = number of existing ranges
- ❌ **Not ideal for**: Frequent updates or very large range sets

**Alternative for frequent updates:**

If you need frequent writes, wrap with `ReadWriteLock` instead:

```java
public class IpAllowList {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final IpRanges ranges = new IpRanges();

    public boolean isAllowed(String clientIp) {
        lock.readLock().lock();
        try {
            return ranges.contains(clientIp);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addAllowedRange(String cidr) {
        lock.writeLock().lock();
        try {
            ranges.addRange(cidr);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

This approach has faster writes but readers may briefly block during updates.

### Performance-Optimized Usage

For maximum performance, you can use the lower-level APIs with pre-converted IP addresses:

```java
IpRanges ranges = new IpRanges();
ranges.addRange("192.168.0.0/16");

// Convert IP once, check multiple times
long ip = IPv4RadixTree.ipToLong("192.168.1.100");
boolean result = ranges.containsIPv4(ip);

// For IPv6
byte[] ipv6 = IPv6RadixTree.ipToBytes("2001:db8::1");
boolean result2 = ranges.containsIPv6(ipv6);
```

## Use Cases

### Firewall Rules

```java
IpRanges allowedIps = IpRanges.builder()
    .addRange("10.0.0.0/8")           // Internal network
    .addRange("192.168.0.0/16")       // Internal network
    .addRange("52.94.0.0/16")         // AWS services
    .addRange("2001:db8::/32")        // IPv6 internal
    .build();

public boolean isAllowed(String clientIp) {
    return allowedIps.contains(clientIp);
}
```

### Rate Limiting by IP Range

```java
IpRanges trustedNetworks = new IpRanges();
trustedNetworks.addRange("10.0.0.0/8");
trustedNetworks.addRange("172.16.0.0/12");

public int getRateLimit(String ip) {
    if (trustedNetworks.contains(ip)) {
        return 10000;  // Higher limit for trusted networks
    }
    return 100;  // Standard limit
}
```

### Geographic IP Filtering

```java
// Example: Block known cloud provider ranges
IpRanges blockedRanges = IpRanges.builder()
    .addRanges(getAWSRanges())
    .addRanges(getGCPRanges())
    .addRanges(getAzureRanges())
    .build();

public boolean shouldBlock(String ip) {
    return blockedRanges.contains(ip);
}
```

## API Reference

### IpRanges

Main API class for checking if IP addresses are within CIDR ranges.

#### Constructors

- `IpRanges()` - Create empty IP ranges
- `IpRanges(Collection<String> cidrRanges)` - Create with initial CIDR ranges
- `IpRanges(IpRanges other)` - Copy constructor, creates independent copy with same ranges

#### Static Methods

- `builder()` - Create a new Builder instance for fluent construction

#### Instance Methods

- `addRange(String cidr)` - Add a CIDR range (auto-detects IPv4 vs IPv6), returns this for chaining
- `addRanges(Collection<String> cidrs)` - Add multiple CIDR ranges, returns this for chaining
- `contains(String ip)` - Check if IP is in any range (auto-detects IPv4 vs IPv6)
- `containsIPv4(long ip)` - Check IPv4 address as long
- `containsIPv6(byte[] ip)` - Check IPv6 address as byte array
- `getRanges()` - Get unmodifiable list of all CIDR ranges

### IPv4RadixTree

Low-level radix tree for IPv4 addresses.

#### Methods

- `addRange(String cidr)` - Add IPv4 CIDR range
- `addRange(long ip, int prefixLength)` - Add range with numeric values
- `contains(String ip)` - Check if IPv4 address is in any range
- `contains(long ip)` - Check with numeric IP
- `ipToLong(String ip)` - Convert IPv4 string to long

### IPv6RadixTree

Low-level radix tree for IPv6 addresses.

#### Methods

- `addRange(String cidr)` - Add IPv6 CIDR range
- `addRange(byte[] ip, int prefixLength)` - Add range with byte array
- `contains(String ip)` - Check if IPv6 address is in any range
- `contains(byte[] ip)` - Check with byte array
- `ipToBytes(String ip)` - Convert IPv6 string to byte array

## Requirements

- Java 21 or higher
- Maven 3.6+ (for building)

## Building

```bash
# Compile and run tests
mvn clean test

# Package as JAR
mvn clean package

# Install to local repository
mvn clean install
```

## Testing

The library includes comprehensive tests covering:
- IPv4 and IPv6 range matching
- Edge cases (0.0.0.0/0, /32, /128)
- Overlapping ranges
- Various CIDR prefix lengths
- Invalid input handling

Run tests:
```bash
mvn test
```

## Design Rationale

### Why Radix Tree over Interval Tree?

While interval trees are a common choice for range queries, radix trees (Patricia tries) are superior for CIDR range matching because:

1. **Natural fit**: CIDR notation represents prefixes, which map directly to trie structure
2. **Constant time**: O(32) for IPv4, O(128) for IPv6 regardless of range count
3. **Memory efficiency**: Shared prefixes use the same nodes
4. **Simpler implementation**: No rebalancing required
5. **Cache friendly**: Sequential memory access patterns

### Data Structure Details

- **Binary trie**: Each node has left (0 bit) and right (1 bit) children
- **Path compression**: Only stores bits up to the CIDR prefix length
- **Prefix matching**: Traversal stops when a CIDR endpoint is reached
- **Space optimization**: Empty branches are not allocated

## Performance Benchmarks

Expected performance characteristics:

- **Small dataset** (< 100 ranges): ~100-200 ns per lookup
- **Medium dataset** (< 10K ranges): ~100-200 ns per lookup
- **Large dataset** (> 100K ranges): ~100-200 ns per lookup

Note: Lookup time remains constant regardless of dataset size!

## Publishing (Maintainers)

The project uses three automated GitHub Actions workflows:

### 1. Pull Request Validation (`pr.yaml`)

**Trigger:** Pull requests to `main`

**Actions:**
- Runs all tests
- No deployment

**Purpose:** Validates changes before merging

### 2. Snapshot Publishing (`main.yaml`)

**Trigger:** Every push to `main` branch

**Actions:**
- Builds the project
- Runs all tests
- Publishes snapshot to GitHub Packages (e.g., `1.0.0-SNAPSHOT`)

**How it works:**
1. Merge a PR to `main`
2. Workflow automatically publishes new snapshot
3. Developers can use latest snapshot with `mvn -U clean install`

**No manual action required** - snapshots are published automatically!

### 3. Release Publishing (`release.yaml`)

**Trigger:** Manual workflow dispatch from GitHub UI

**Actions:**
Uses Maven Release Plugin to:
1. Remove `-SNAPSHOT` from version (e.g., `1.0.0`)
2. Commit the release version
3. Create and push git tag (e.g., `v1.0.0`)
4. Build and deploy release to GitHub Packages
5. Bump to next development version (e.g., `1.1.0-SNAPSHOT`)
6. Commit and push to `main`

**How to create a release:**

1. Go to **Actions** tab on GitHub
2. Select **"Release"** workflow
3. Click **"Run workflow"**
4. (Optional) Specify versions or let Maven auto-increment:
   - Release version: `1.0.0`
   - Development version: `1.1.0-SNAPSHOT`
5. Click **"Run workflow"**

**That's it!** Maven Release Plugin handles:
- ✅ Version updates
- ✅ Git commits and tags
- ✅ Building and publishing release
- ✅ Version bumping for next development cycle

**Note on snapshots:** Due to GitHub Actions security (workflows triggered by `GITHUB_TOKEN` don't trigger other workflows), the new snapshot version won't be automatically published. The snapshot will be published on the next push to `main`, or you can manually trigger `main.yaml` if you need the snapshot immediately.

### Workflow Summary

```
PR → main (via merge)
  ↓
main.yaml runs → Publishes 1.0.0-SNAPSHOT

Release workflow (manual trigger)
  ↓
release.yaml runs → mvn release:prepare release:perform
  ↓
  ├─ Creates tag v1.0.0
  ├─ Publishes 1.0.0 release
  └─ Pushes 1.1.0-SNAPSHOT to main
       ↓
     (Note: main.yaml won't auto-trigger due to GitHub Actions security)

Next push to main
  ↓
main.yaml runs → Publishes 1.1.0-SNAPSHOT
```

### Local Development

For local testing, install to your local Maven repository:
```bash
mvn clean install
```

### Version Types

**Snapshots** (`1.0.0-SNAPSHOT`):
- Latest development version
- Updated automatically on every main branch push
- May be unstable
- Use for testing and development
- Maven checks for updates with `-U` flag

**Releases** (`1.0.0`):
- Stable, tested versions
- Created via Release workflow
- Immutable once published
- Use in production
- Semantic versioning recommended

## License

[Add your license here]

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

Jon Monette
