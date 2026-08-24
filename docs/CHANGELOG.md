# Changelog: FastFileFormat

All notable changes to this project will be documented in this file.

## [0.1.0] - 2026-08-24
### Added
- **Standardized 12-Byte Binary Header (`BinaryHeader`)**: FastJava binary identification (`Magic [4B]`, `Version [2B]`, `PayloadType [2B]`, `PayloadLength [4B]`).
- **Zero-Allocation Binary Streaming (`BinaryWriter`, `BinaryReader`)**: Little-Endian primitive stream serialization and deserialization for primitives, strings, and arrays.
- **Universal Text Format Parser (`TextFormatParser`)**: High-speed parser for `KEY = VALUE` configuration with `@KEY` and `@SECTION.KEY` alias resolution, sections, and comments.
- **Structured Text Formatter (`TextFormatWriter`)**: Fluent pretty-printer for formatted text documents.
- **Central API Facade (`FastFileFormat`)**: Unified static methods for encoding, decoding, file loading, and type auto-detection.
- **Interactive Showcase & Benchmarks**: Full JMH microbenchmark suite and interactive demo application.
