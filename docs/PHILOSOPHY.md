# The Philosophy of FastFileFormat

> [!IMPORTANT]
> **"Dual Format Parity. Sub-Microsecond Streams. Zero Garbage Collection Overhead."**

Data storage in modern performance-critical software must satisfy two conflicting needs:
1. **Developers need human-readable, easily editable files** for themes, configurations, and user scripts with comments and variable aliases.
2. **Engines need raw binary speed**, instant zero-copy memory reads, and zero Garbage Collector pressure during application boot and runtime streaming.

## Core Tenets

### 1. Dual Format Parity
FastFileFormat provides a direct bridge between human-readable text (`KEY = VALUE`, `@ALIASES`, `[SECTIONS]`) and binary streams (`.bin` / `.fbin`). Any structured document can be authored in text and instantly converted to compact binary caches.

### 2. Zero-Allocation Binary Reads
Reading primitive data from a binary payload should not instantiate wrapper objects or strings when raw buffers suffice. FastFileFormat uses direct Little-Endian ByteBuffers and contiguous array slices for maximum CPU cache utilization.

### 3. Standardized 12-Byte Binary Header
By enforcing a predictable 12-byte layout (`Magic [4B]`, `Version [2B]`, `PayloadType [2B]`, `Length [4B]`), any FastJava module can validate and route binary packets without parsing the whole file.

### 4. Zero Dependencies
FastFileFormat requires no third-party libraries (no Jackson, no SnakeYAML, no Protobuf). It is pure, fast Java 17+ code backed by the FastJava ecosystem.

---

**⚡ FastFileFormat — Universal serialization and streaming for the FastJava ecosystem.**
