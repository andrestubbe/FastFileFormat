# FastFileFormat Roadmap

## Milestones

### Version 0.1.0 (Current)
- [x] Standard 12-byte Little-Endian binary header specification.
- [x] Zero-allocation binary stream reader and writer.
- [x] Human-readable text format with `@KEY` alias resolution and section support.
- [x] JMH microbenchmark and interactive showcase.

### Version 0.2.0 (Planned)
- [ ] Direct Memory-Mapped (`FileChannel.map`) zero-copy binary deserializer.
- [ ] Native SIMD acceleration for text scanner using `FastSIMD`.
- [ ] Compression flags (LZ4 / Snappy) integrated directly into `BinaryHeader`.
