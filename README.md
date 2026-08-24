# FastFileFormat 0.1.0 [ALPHA-2026-08-24] — High-Performance Dual-Format Serialization Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastFileFormat/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Cross--Platform-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastFileFormat)

---

**⚡ Universal, zero-bloat dual-format serialization and parsing engine for the FastJava ecosystem.**

**FastFileFormat** standardizes data storage across FastJava. It bridges **human-readable text specifications** (`.format`, `.kv`, `.theme`, `.config` with `@KEY` variable aliasing and sections) with **sub-microsecond binary streaming** (`.bin`, `.fbin`, `.themebin` with 12-byte magic headers and zero-allocation primitive reads).

---

## Quick Start

### 1. Structured Text Format & Variable Aliasing
```java
import fastfileformat.FastFileFormat;
import fastfileformat.TextFormatParser;
import fastfileformat.TextFormatWriter;

public class TextDemo {
    public static void main(String[] args) {
        // 1. Fluent Text Generation
        String configText = FastFileFormat.textWriter("Engine Configuration")
                .section("Graphics")
                .set("resolution.width", 1920)
                .set("resolution.height", 1080)
                .set("vsync", true)
                .blankLine()
                .section("Palette")
                .set("primary", "#00F0FF")
                .set("accent", "#FF007F")
                .alias("cursor", "Palette.accent") // Resolves dynamically to #FF007F
                .toText();

        // 2. High-Speed Text Parsing & Alias Resolution
        TextFormatParser doc = FastFileFormat.parseText(configText);
        int width = doc.getInt("Graphics.resolution.width", 1280);
        boolean vsync = doc.getBoolean("Graphics.vsync", false);
        String cursor = doc.getString("Palette.cursor", "#FFFFFF"); // "#FF007F"
    }
}
```

### 2. High-Throughput Binary Serialization
```java
import fastfileformat.BinaryHeader;
import fastfileformat.BinaryReader;
import fastfileformat.BinaryWriter;
import fastfileformat.FastFileFormat;

public class BinaryDemo {
    public static void main(String[] args) {
        // 1. Zero-Allocation Binary Streaming
        BinaryWriter writer = FastFileFormat.binaryWriter()
                .writeHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 100, 0)
                .writeString("FastJava Payload")
                .writeIntArray(new int[]{10, 20, 30, 40})
                .writeDouble(Math.PI);

        byte[] payload = writer.toByteArray();

        // 2. Direct Little-Endian Deserialization
        BinaryReader reader = FastFileFormat.binaryReader(payload);
        BinaryHeader header = reader.readHeader(); // 12-byte FastJava header
        String name = reader.readString();
        int[] numbers = reader.readIntArray();
        double pi = reader.readDouble();
    }
}
```

---

## Table of Contents

- [Why FastFileFormat?](#why-fastfileformat)
- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [Real-World Scenarios](#real-world-scenarios)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Examples & Hero Demos](#technical-examples--hero-demos)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastFileFormat?

Traditional data formats in Java (JSON, YAML, XML, Java Serialization) are ill-suited for performance-critical engines:

1. **Massive Memory Bloat & GC Overhead**  
   Jackson, Gson, and SnakeYAML create millions of intermediate objects, wrapper instances, and HashMaps during startup, causing garbage collection spikes.

2. **Dangerous Java Serialization**  
   Java's built-in `Serializable` is notoriously slow, insecure, and tightly coupled to classpath class definitions.

3. **Complex Schema Setup**  
   Protocol Buffers and FlatBuffers require external code generation (`protoc`) and rigid schema compilation.

**FastFileFormat solves this by offering a zero-dependency, dual-format standard:**
- **Human-Readable Text**: Clean `KEY = VALUE` syntax with `@ALIAS` resolution and `[SECTION]` grouping.
- **Binary Streaming**: Little-Endian raw primitive packing with 12-byte standardized headers and zero-allocation memory reads.

---

## Key Features

- **⚡ Dual-Format Standard** — Human-readable `.format` text and sub-microsecond `.bin` binary streaming.
- **🔗 Variable Alias Resolution** — Native `@KEY` and `@SECTION.KEY` referencing for dynamic configurations.
- **📦 12-Byte Standard Binary Header** — 4-byte Magic, 2-byte Version, 2-byte Payload Type, 4-byte Length.
- **🧮 Zero-Allocation Primitive Streaming** — Little-Endian writers and readers for `int`, `float`, `double`, `long`, `String`, arrays, and byte slices.
- **🌐 Zero Dependencies** — Self-contained pure Java 17+ core backed by `FastCore`.

---

## Real-World Scenarios

- **🎨 Engine & UI Theming** — Powers `FastTheme` with human-readable `.theme` palettes and instant `.themebin` caches.
- **🎮 Game & Animation Timelines** — Serializes complex keyframes and tracks in `FastAnimation` and `FastTween`.
- **⚙️ Hot-Reloadable Application Configurations** — Human-editable configuration files with dynamic variable references.
- **🚀 Network IPC & Shared Memory Streaming** — Fast binary serialization for inter-process memory pipes and sockets.

---

## Performance Benchmarks

FastFileFormat is profiled using **JMH** to guarantee zero-overhead serialization.

| Benchmark Operation | Score (ops/ms) | Ops per Second | Memory Allocation |
|---|---|---|---|
| **Binary Stream Deserialization** | **~20,000 ops/ms** | **> 20 Million** | **0 bytes / op (Zero GC)** |
| **Binary Stream Serialization** | **~11,800 ops/ms** | **> 11.8 Million** | **Minimal buffer churn** |
| **Text Parsing with Alias Resolution** | **~294 ops/ms** | **> 294,000** | **Linear memory footprint** |

*Run the benchmarks locally:* `.\run-benchmark.bat`

---

## API Quick Reference

| Class / Method | Description |
|---|---|
| `FastFileFormat.textWriter()` / `(title)` | Creates a fluent pretty-printer for human-readable text formats. |
| `FastFileFormat.parseText(String text)` | Deserializes formatted text and resolves all `@KEY` alias references. |
| `FastFileFormat.binaryWriter()` / `(capacity)` | Creates a Little-Endian primitive stream writer. |
| `FastFileFormat.binaryReader(byte[] bytes)` | Creates a high-speed Little-Endian binary deserializer. |
| `FastFileFormat.isBinaryFile(Path path)` | Checks if a file starts with a valid FastJava binary magic header. |
| `BinaryHeader.readFrom(ByteBuffer buffer)` | Decodes standard 12-byte FastJava binary header. |
| `TextFormatParser.getInt / getFloat / getBoolean` | Type-safe value accessors with default fallback values. |

---

## Technical Examples & Hero Demos

| Case | Java Example | Launcher | Description |
|---|---|---|---|
| **Interactive Format Showcase** | [Demo.java](examples/Demo/src/main/java/fastfileformat/demo/Demo.java) | `run-demo.bat` | Text format generation, alias resolution, and binary roundtrip demonstration. |
| **JMH Microbenchmark Suite** | [FastFileFormatBenchmark.java](examples/Benchmark/src/main/java/fastfileformat/benchmark/FastFileFormatBenchmark.java) | `run-benchmark.bat` | High-throughput throughput benchmarks for text and binary serialization. |

---

## Installation

FastJava modules require **two** dependencies: the module itself, and `FastCore` (which handles native utilities and loading).

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastFileFormat</artifactId>
        <version>0.1.0</version>
    </dependency>
    <!-- Required FastJava loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastcore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastFileFormat:0.1.0'
    // Required FastJava loader
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JAR directly to add it to your classpath:

1. 📦 **[FastFileFormat-0.1.0.jar](https://github.com/andrestubbe/FastFileFormat/releases/download/0.1.0/FastFileFormat-0.1.0.jar)** (The Core Library)
2. 📦 **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/FastCore-0.1.0.jar)** (Required FastJava loader)

---

## Documentation

* **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (Maven Build Setup).
* **[REFERENCE.md](docs/REFERENCE.md)**: Exhaustive catalog of API contracts, binary specs, and data structures.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-allocation and dual-format design principles.
* **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestone features and performance extensions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.

---

## Platform Support

| Platform | Status |
|---|---|
| Windows 10/11 | ✅ Fully Supported |
| Linux | ✅ Fully Supported (Pure Java) |
| macOS | ✅ Fully Supported (Pure Java) |

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI Loader and Utilities
- [FastTheme](https://github.com/andrestubbe/FastTheme) — High-performance native window styling and dynamic themes
- [FastAnimation](https://github.com/andrestubbe/FastAnimation) — Zero overhead timeline orchestration
- [FastTween](https://github.com/andrestubbe/FastTween) — Zero overhead pool-based tweening
- [FastDWM](https://github.com/andrestubbe/FastDWM) — Native Desktop Window Manager API
- [FastDisplay](https://github.com/andrestubbe/FastDisplay) — Native display telemetry and multi-monitor DPI scaling API
- [FastANSI](https://github.com/andrestubbe/FastANSI) — High-performance terminal ANSI compositor
- [FastUI](https://github.com/andrestubbe/FastUI) — High-Performance GUI Framework
- [FastTUI](https://github.com/andrestubbe/FastTUI) — Terminal User Interface Toolkit

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
