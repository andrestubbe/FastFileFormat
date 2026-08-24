package fastfileformat;

import fastcore.FastCore;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Universal, Zero-Bloat Dual-Format Serialization &amp; Parsing Engine for the FastJava Ecosystem.
 * Bridges human-readable text specifications (.format/.kv/.theme) with sub-microsecond binary streaming (.bin/.fbin).
 */
public final class FastFileFormat {

    /**
     * Standard FastJava generic binary format magic identifier ("FFMT" = 0x46464D54).
     */
    public static final int DEFAULT_MAGIC = 0x46464D54;

    /**
     * Standard FastJava generic format version.
     */
    public static final short DEFAULT_VERSION = 1;

    static {
        try {
            FastCore.loadLibrary("fastcore");
        } catch (Throwable ignored) {}
    }

    private FastFileFormat() {}

    /**
     * Creates a new fluent {@link BinaryWriter}.
     *
     * @return Fresh BinaryWriter instance.
     */
    public static BinaryWriter binaryWriter() {
        return new BinaryWriter();
    }

    /**
     * Creates a new fluent {@link BinaryWriter} with initial capacity.
     *
     * @param initialCapacity Initial buffer capacity.
     * @return Fresh BinaryWriter instance.
     */
    public static BinaryWriter binaryWriter(int initialCapacity) {
        return new BinaryWriter(initialCapacity);
    }

    /**
     * Creates a {@link BinaryReader} wrapping the provided byte array.
     *
     * @param bytes Binary payload.
     * @return Fresh BinaryReader instance.
     */
    public static BinaryReader binaryReader(byte[] bytes) {
        return new BinaryReader(bytes);
    }

    /**
     * Creates a {@link BinaryReader} wrapping the provided {@link ByteBuffer}.
     *
     * @param buffer Source buffer.
     * @return Fresh BinaryReader instance.
     */
    public static BinaryReader binaryReader(ByteBuffer buffer) {
        return new BinaryReader(buffer);
    }

    /**
     * Parses a human-readable text format string into a {@link TextFormatParser} document.
     *
     * @param text Raw text format content.
     * @return Parsed document.
     */
    public static TextFormatParser parseText(String text) {
        return TextFormatParser.parse(text);
    }

    /**
     * Creates a new fluent {@link TextFormatWriter}.
     *
     * @return Fresh TextFormatWriter instance.
     */
    public static TextFormatWriter textWriter() {
        return new TextFormatWriter();
    }

    /**
     * Creates a new fluent {@link TextFormatWriter} with a document title.
     *
     * @param title Document title.
     * @return Fresh TextFormatWriter instance.
     */
    public static TextFormatWriter textWriter(String title) {
        return new TextFormatWriter(title);
    }

    /**
     * Loads and auto-detects a file (text vs. binary) from a Path.
     *
     * @param path File path.
     * @return True if the file contains a valid FastJava binary magic header.
     * @throws IOException If file read fails.
     */
    public static boolean isBinaryFile(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        if (bytes.length < BinaryHeader.HEADER_SIZE) return false;
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int magic = buf.getInt();
        return magic == DEFAULT_MAGIC || magic == 0x4654484D; // FastTheme MAGIC
    }

    /**
     * Loads a text file directly into a parsed {@link TextFormatParser} document.
     *
     * @param path File path.
     * @return Parsed document.
     * @throws IOException If file read fails.
     */
    public static TextFormatParser loadTextFile(Path path) throws IOException {
        return TextFormatParser.loadFromFile(path);
    }

    /**
     * Loads a text file directly into a parsed {@link TextFormatParser} document.
     *
     * @param file File object.
     * @return Parsed document.
     * @throws IOException If file read fails.
     */
    public static TextFormatParser loadTextFile(File file) throws IOException {
        return TextFormatParser.loadFromFile(file);
    }

    /**
     * Loads a binary file directly into a {@link BinaryReader}.
     *
     * @param path File path.
     * @return Binary reader ready for stream deserialization.
     * @throws IOException If file read fails.
     */
    public static BinaryReader loadBinaryFile(Path path) throws IOException {
        return BinaryReader.fromFile(path);
    }

    /**
     * Transcodes a human-readable text document (.format) into a compact binary format payload.
     *
     * @param text Text document content.
     * @return Compact binary byte array.
     */
    public static byte[] textToBinary(String text) {
        TextFormatParser doc = parseText(text);
        BinaryWriter bw = binaryWriter();
        var all = doc.getAll();
        bw.writeHeader(DEFAULT_MAGIC, DEFAULT_VERSION, (short) 1, 0);
        bw.writeString(doc.getTitle());
        bw.writeInt(all.size());
        for (var entry : all.entrySet()) {
            bw.writeString(entry.getKey());
            bw.writeString(entry.getValue());
        }
        return bw.toByteArray();
    }

    /**
     * Transcodes a compact binary format payload back into human-readable text.
     *
     * @param binaryData Binary payload.
     * @return Formatted text string.
     */
    public static String binaryToText(byte[] binaryData) {
        BinaryReader br = binaryReader(binaryData);
        BinaryHeader header = br.readHeader();
        String title = br.readString();
        int count = br.readInt();
        TextFormatWriter tw = textWriter(title);
        String lastSection = null;
        for (int i = 0; i < count; i++) {
            String fullKey = br.readString();
            String value = br.readString();
            int dot = fullKey.indexOf('.');
            if (dot != -1) {
                String section = fullKey.substring(0, dot);
                String subKey = fullKey.substring(dot + 1);
                if (!section.equals(lastSection)) {
                    tw.section(section);
                    lastSection = section;
                }
                tw.set(subKey, value);
            } else {
                tw.set(fullKey, value);
            }
        }
        return tw.toText();
    }

    /**
     * Converts a text file to a binary file on disk.
     *
     * @param sourceText Input text file path.
     * @param targetBin  Output binary file path.
     * @throws IOException If I/O fails.
     */
    public static void convertTextToBinary(Path sourceText, Path targetBin) throws IOException {
        String text = Files.readString(sourceText);
        byte[] bytes = textToBinary(text);
        Files.write(targetBin, bytes);
    }

    /**
     * Converts a binary file to a human-readable text file on disk.
     *
     * @param sourceBin  Input binary file path.
     * @param targetText Output text file path.
     * @throws IOException If I/O fails.
     */
    public static void convertBinaryToText(Path sourceBin, Path targetText) throws IOException {
        byte[] bytes = Files.readAllBytes(sourceBin);
        String text = binaryToText(bytes);
        Files.writeString(targetText, text);
    }

    /**
     * Loads a binary file directly into a {@link BinaryReader}.
     *
     * @param file File object.
     * @return Binary reader ready for stream deserialization.
     * @throws IOException If file read fails.
     */
    public static BinaryReader loadBinaryFile(File file) throws IOException {
        return BinaryReader.fromFile(file);
    }
}
