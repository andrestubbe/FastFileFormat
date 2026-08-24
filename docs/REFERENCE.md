# FastFileFormat API Reference

This document outlines the API contracts, binary layouts, and data structures of **FastFileFormat** (version 0.1.0).

---

## 1. Binary Format Specification

Every standard FastJava binary payload begins with a contiguous 12-byte Little-Endian header.

### 12-Byte Header Layout

| Offset (Bytes) | Field Name | Data Type | Description |
|---|---|---|---|
| `0..3` | `magic` | `int` (4B) | Unique 4-byte format identifier (e.g. `0x46464D54` = `"FFMT"`). |
| `4..5` | `version` | `short` (2B) | Format schema version (starts at `1`). |
| `6..7` | `payloadType` | `short` (2B) | Application-specific payload type identifier. |
| `8..11` | `payloadLength` | `int` (4B) | Length of the subsequent payload in bytes (`0` if variable/unbounded). |

---

## 2. Class: `fastfileformat.FastFileFormat`

*   `public static BinaryWriter binaryWriter()` / `binaryWriter(int initialCapacity)`
*   `public static BinaryReader binaryReader(byte[] bytes)` / `binaryReader(ByteBuffer buffer)`
*   `public static TextFormatParser parseText(String text)`
*   `public static TextFormatWriter textWriter()` / `textWriter(String title)`
*   `public static boolean isBinaryFile(Path path)`
*   `public static TextFormatParser loadTextFile(Path path)` / `(File file)`
*   `public static BinaryReader loadBinaryFile(Path path)` / `(File file)`

---

## 3. Class: `fastfileformat.BinaryWriter`

*   `public BinaryWriter writeHeader(int magic, short version, short payloadType, int payloadLength)`
*   `public BinaryWriter writeByte(int b)` / `writeBytes(byte[] bytes)`
*   `public BinaryWriter writeShort(int v)`
*   `public BinaryWriter writeInt(int v)`
*   `public BinaryWriter writeLong(long v)`
*   `public BinaryWriter writeFloat(float v)`
*   `public BinaryWriter writeDouble(double v)`
*   `public BinaryWriter writeString(String str)` (2-byte short length prefixed)
*   `public BinaryWriter writeIntArray(int[] array)` (4-byte length prefixed)
*   `public BinaryWriter writeFloatArray(float[] array)` (4-byte length prefixed)
*   `public byte[] toByteArray()`
*   `public void writeToFile(Path path)` / `(File file)`

---

## 4. Class: `fastfileformat.BinaryReader`

*   `public BinaryHeader readHeader()`
*   `public byte readByte()` / `int readUnsignedByte()`
*   `public short readShort()`
*   `public int readInt()`
*   `public long readLong()`
*   `public float readFloat()`
*   `public double readDouble()`
*   `public String readString()`
*   `public byte[] readBytes(int length)`
*   `public int[] readIntArray()`
*   `public float[] readFloatArray()`
*   `public int remaining()`
*   `public ByteBuffer getBuffer()`

---

## 5. Class: `fastfileformat.TextFormatParser`

*   `public static TextFormatParser parse(String text)`
*   `public String getTitle()`
*   `public String get(String key)` / `getString(String key, String defaultValue)`
*   `public int getInt(String key, int defaultValue)`
*   `public float getFloat(String key, float defaultValue)`
*   `public double getDouble(String key, double defaultValue)`
*   `public boolean getBoolean(String key, boolean defaultValue)`
*   `public int[] getIntArray(String key)`
*   `public Map<String, String> getAll()`
*   `public Map<String, String> getSection(String sectionName)`
*   `public static TextFormatParser loadFromFile(Path path)` / `(File file)`

---

## 6. Class: `fastfileformat.TextFormatWriter`

*   `public TextFormatWriter comment(String comment)`
*   `public TextFormatWriter section(String sectionName)`
*   `public TextFormatWriter set(String key, String value)`
*   `public TextFormatWriter set(String key, int / float / double / boolean)`
*   `public TextFormatWriter alias(String key, String targetKey)`
*   `public TextFormatWriter blankLine()`
*   `public String toText()`
*   `public void writeToFile(Path path)` / `(File file)`
