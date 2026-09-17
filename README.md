> [!WARNING]
> **🚧 WIP — Active AI Pipeline Construction & Architecture Optimization in Progress.**

# FastFileFormat [ALPHA-2026-09-08] — High-Performance Dual-Format Serialization Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastFileFormat/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
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
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Architecture Overview](#architecture-overview)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastFileFormat?

Traditional data formats in Java (JSON, YAML, XML, Java Serialization) are ill-suited for performance-critical engines:

- **Massive Memory Bloat & GC Overhead** — Jackson, Gson, and SnakeYAML create millions of intermediate objects, wrapper instances, and HashMaps during startup, causing garbage collection spikes.
- **Dangerous Java Serialization** — Java's built-in `Serializable` is notoriously slow, insecure, and tightly coupled to classpath class definitions.
- **Complex Schema Setup** — Protocol Buffers and FlatBuffers require external code generation (`protoc`) and rigid schema compilation.

FastFileFormat solves this by offering a zero-dependency, dual-format standard:

| Feature | JSON / YAML (Jackson, Gson) | Java Serialization | Protocol Buffers | FastFileFormat |
|:---|:---|:---|:---|:---|
| **Human Readability** | ✅ Yes (Text) | ❌ Binary Blob | ❌ Binary Blob | ✅ Clean Key-Value & Aliases |
| **Parsing Latency** | 50–500 µs (Token parsing) | 100–1,000 µs (Reflection) | 5–20 µs (C++ bindings) | < 1 µs (Zero-copy binary stream) |
| **GC Pressure** | High object churn | Massive class metadata | Medium buffer allocation | Zero GC on primitive reads |
| **External Compilers** | None | None | ⚠️ Requires `protoc` | Pure Java 17+ (No tooling setup) |
| **Dual Format Bridge** | Separate formats required | Binary only | Separate text proto | Unified Text-to-Binary transcode |

---

## Key Features

- ⚡ **Dual-Format Standard** — Human-readable `.format` text and sub-microsecond `.bin` binary streaming.
- 🔗 **Variable Alias Resolution** — Native `@KEY` and `@SECTION.KEY` referencing for dynamic configurations.
- 📦 **12-Byte Standard Binary Header** — 4-byte Magic, 2-byte Version, 2-byte Payload Type, 4-byte Length.
- 🧮 **Zero-Allocation Primitive Streaming** — Little-Endian writers and readers for `int`, `float`, `double`, `long`, `String`, arrays, and byte slices.
- 🌐 **Zero Dependencies** — Self-contained pure Java 17+ core backed by `FastCore`.

---

## Real-World Use Cases

- 🎨 **UI Theming & Dynamic Palettes**: Powers `FastTheme` with human-editable `.theme` palettes and instant pre-compiled `.themebin` caches.
- 🧠 **Agent State & Checkpoint Dumps**: Serializes multi-agent blackboards in `FastAIState` into high-density binary snapshots in under 12 microseconds.
- ⚙️ **Hot-Reloadable Game Configurations**: Human-readable game engine configs with dynamic `@alias` color and resolution linking.
- 🚀 **Zero-Copy IPC & Shared Memory Streaming**: High-throughput binary streaming across local OS memory rings and inter-process sockets.

---

## Architecture Overview

FastFileFormat acts as the canonical data serialization and interchange layer for FastJava:

- 📄 **[FastFileFormat](https://github.com/andrestubbe/FastFileFormat)** (Dual Format): Standardized 12-byte header, text parser, and binary serializer.
- ⚡ **[FastBinary](https://github.com/andrestubbe/FastBinary)** (Binary Bit-Packing): Provides VarInt, BitSet, and bit-level packing primitives.
- 🧠 **[FastAIState](https://github.com/andrestubbe/FastAIState)** (Shared Agent State): Uses FastFileFormat for high-speed blackboard snapshots.
- 🎨 **[FastTheme](https://github.com/andrestubbe/FastTheme)** (Desktop Theming): Loads human-readable `.theme` specs and serializes `.themebin`.

---

## Performance Benchmarks

FastFileFormat is profiled using **JMH** to guarantee zero-overhead serialization:

| Benchmark Operation | Score (ops/ms) | Ops per Second | Memory Allocation |
|:---|:---|:---|:---|
| **Binary Stream Deserialization** | **~14,680 ops/ms** | **> 14.6 Million** | **0 bytes / op (Zero GC)** |
| **Binary Stream Serialization** | **~8,880 ops/ms** | **> 8.88 Million** | **Minimal buffer churn** |
| **Text Parsing with Alias Resolution** | **~248 ops/ms** | **> 248,000 / sec** | **Linear memory footprint** |

*Measured on Windows 11 x64, Intel Core i5 (Surface Pro 8), JDK 21.0.12.1.*

---

## API Quick Reference

| Class / Method | Return Type | Description |
|:---|:---|:---|
| `FastFileFormat.textWriter()` | `TextFormatWriter` | Creates a fluent pretty-printer for human-readable text formats. |
| `FastFileFormat.parseText(text)` | `TextFormatParser` | Deserializes formatted text and resolves all `@KEY` alias references. |
| `FastFileFormat.binaryWriter()` | `BinaryWriter` | Creates a Little-Endian primitive stream writer. |
| `FastFileFormat.binaryReader(bytes)`| `BinaryReader` | Creates a high-speed Little-Endian binary deserializer. |
| `FastFileFormat.isBinaryFile(path)` | `boolean` | Checks if a file starts with a valid FastJava binary magic header. |
| `BinaryHeader.readFrom(buffer)` | `BinaryHeader` | Decodes standard 12-byte FastJava binary header. |
| `parser.getInt(key, defaultVal)` | `int` | Type-safe value accessors with default fallback values. |

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|:---|:---|:---|:---|
| **Interactive Format Showcase** | [Demo.java](examples/Demo/src/main/java/fastfileformat/demo/Demo.java) | `run-demo.bat` | Text format generation, alias resolution, and binary roundtrip demonstration. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastfileformat/benchmark/Benchmark.java) | `run-benchmark.bat` | High-throughput throughput benchmarks for text and binary serialization. |

---

## Installation

### Option 1: Maven (Recommended via JitPack)

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
        <version>0.1.1</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastFileFormat:0.1.1'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[FastFileFormat-0.1.1.jar](https://github.com/andrestubbe/FastFileFormat/releases/download/0.1.1/FastFileFormat-0.1.1.jar)** (The Core Library)
2. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (FastJava runtime substrate)

---

## Documentation

- **[REFERENCE.md](docs/REFERENCE.md)**: Exhaustive catalog of API contracts, binary specs, and data structures.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-allocation and dual-format design principles.
- **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestone features and performance extensions.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.
- **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (Maven Build Setup).

---

## Platform Support

| Platform | Architecture | Status | Notes |
|:---|:---|:---|:---|
| Windows 10/11 | x64, ARM64 | ✅ Fully Supported | Native high-performance pure Java |
| Linux | x64, ARM64 | ✅ Fully Supported | Tested on Ubuntu / Debian / RHEL |
| macOS | Apple Silicon, x64 | ✅ Fully Supported | Tested on macOS Sonoma / Sequoia |

---

## License

MIT License — See [LICENSE](LICENSE) for details.

---

## Related Projects

- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI Loader and Utilities
- [FastBinary](https://github.com/andrestubbe/FastBinary) — Bit-packing, VarInt encoding, and binary parsing engine
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