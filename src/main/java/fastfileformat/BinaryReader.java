package fastfileformat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Zero-allocation, Little-Endian binary reader for FastJava.
 * Wraps a {@link ByteBuffer} and provides high-speed primitive, string, and array deserialization.
 */
public final class BinaryReader {

    private final ByteBuffer buffer;

    /**
     * Constructs a BinaryReader wrapping the provided byte array.
     *
     * @param bytes Serialized byte array.
     */
    public BinaryReader(byte[] bytes) {
        this(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN));
    }

    /**
     * Constructs a BinaryReader wrapping the provided {@link ByteBuffer}.
     *
     * @param buffer Source buffer.
     */
    public BinaryReader(ByteBuffer buffer) {
        if (buffer == null) throw new IllegalArgumentException("Buffer cannot be null");
        this.buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Reads a standard 12-byte {@link BinaryHeader}.
     *
     * @return Deserialized header.
     */
    public BinaryHeader readHeader() {
        return BinaryHeader.readFrom(buffer);
    }

    /**
     * Reads a single 8-bit byte.
     *
     * @return Byte value.
     */
    public byte readByte() {
        return buffer.get();
    }

    /**
     * Reads an 8-bit unsigned byte.
     *
     * @return Unsigned int (0..255).
     */
    public int readUnsignedByte() {
        return buffer.get() & 0xFF;
    }

    /**
     * Reads a 16-bit short in Little-Endian format.
     *
     * @return Short value.
     */
    public short readShort() {
        return buffer.getShort();
    }

    /**
     * Reads a 32-bit integer in Little-Endian format.
     *
     * @return Int value.
     */
    public int readInt() {
        return buffer.getInt();
    }

    /**
     * Reads a 32-bit LEB128 VarInt via FastBinary.
     *
     * @return Decoded integer value.
     */
    public int readVarInt() {
        return fastbinary.VarInt.readInt(buffer);
    }

    /**
     * Reads a signed 32-bit integer decoded with VarInt + ZigZag via FastBinary.
     *
     * @return Decoded signed integer value.
     */
    public int readSignedVarInt() {
        return fastbinary.ZigZag.decode(readVarInt());
    }

    /**
     * Reads a 64-bit LEB128 VarLong via FastBinary.
     *
     * @return Decoded long value.
     */
    public long readVarLong() {
        return fastbinary.VarInt.readLong(buffer);
    }

    /**
     * Reads a 64-bit long in Little-Endian format.
     *
     * @return Long value.
     */
    public long readLong() {
        return buffer.getLong();
    }

    /**
     * Reads a 32-bit float in Little-Endian format.
     *
     * @return Float value.
     */
    public float readFloat() {
        return buffer.getFloat();
    }

    /**
     * Reads a 64-bit double in Little-Endian format.
     *
     * @return Double value.
     */
    public double readDouble() {
        return buffer.getDouble();
    }

    /**
     * Reads a UTF-8 string prefixed by a 2-byte short length.
     *
     * @return String content.
     */
    public String readString() {
        short len = buffer.getShort();
        if (len <= 0) return "";
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads a raw byte slice into a newly allocated array.
     *
     * @param length Number of bytes to read.
     * @return Byte array.
     */
    public byte[] readBytes(int length) {
        if (length <= 0) return new byte[0];
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * Reads an array of 32-bit integers.
     *
     * @return Primitive int array.
     */
    public int[] readIntArray() {
        int length = buffer.getInt();
        if (length <= 0) return new int[0];
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = buffer.getInt();
        }
        return array;
    }

    /**
     * Reads an array of 32-bit floats.
     *
     * @return Primitive float array.
     */
    public float[] readFloatArray() {
        int length = buffer.getInt();
        if (length <= 0) return new float[0];
        float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = buffer.getFloat();
        }
        return array;
    }

    /**
     * Returns remaining unread bytes in buffer.
     *
     * @return Remaining byte count.
     */
    public int remaining() {
        return buffer.remaining();
    }

    /**
     * Returns the underlying ByteBuffer.
     *
     * @return Source buffer.
     */
    public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Reads a BinaryReader from a file path.
     *
     * @param path File path.
     * @return Instantiated BinaryReader.
     * @throws IOException If file reading fails.
     */
    public static BinaryReader fromFile(Path path) throws IOException {
        return new BinaryReader(Files.readAllBytes(path));
    }

    /**
     * Reads a BinaryReader from a File.
     *
     * @param file Source file.
     * @return Instantiated BinaryReader.
     * @throws IOException If file reading fails.
     */
    public static BinaryReader fromFile(File file) throws IOException {
        return fromFile(file.toPath());
    }
}
