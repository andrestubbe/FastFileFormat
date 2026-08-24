package fastfileformat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * High-performance, Little-Endian binary writer for the FastJava ecosystem.
 * Supports zero-allocation primitive writes, raw arrays, strings, and byte slices.
 */
public final class BinaryWriter {

    private final ByteArrayOutputStream stream;
    private final byte[] temp = new byte[16];

    /**
     * Constructs a new BinaryWriter with default initial buffer capacity.
     */
    public BinaryWriter() {
        this(256);
    }

    /**
     * Constructs a new BinaryWriter with specified initial capacity.
     *
     * @param initialCapacity Initial buffer capacity.
     */
    public BinaryWriter(int initialCapacity) {
        this.stream = new ByteArrayOutputStream(initialCapacity);
    }

    /**
     * Writes a 12-byte standard FastJava {@link BinaryHeader}.
     *
     * @param magic         4-byte magic.
     * @param version       2-byte version.
     * @param payloadType   2-byte payload type.
     * @param payloadLength 4-byte payload length.
     * @return This writer instance.
     */
    public BinaryWriter writeHeader(int magic, short version, short payloadType, int payloadLength) {
        new BinaryHeader(magic, version, payloadType, payloadLength).writeTo(wrapTemp(12));
        stream.write(temp, 0, 12);
        return this;
    }

    private ByteBuffer wrapTemp(int size) {
        if (temp.length < size) {
            return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        }
        return ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Writes a single 8-bit byte.
     *
     * @param b Byte value.
     * @return This writer instance.
     */
    public BinaryWriter writeByte(int b) {
        stream.write(b);
        return this;
    }

    /**
     * Writes a raw byte array.
     *
     * @param bytes Byte array.
     * @return This writer instance.
     */
    public BinaryWriter writeBytes(byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            stream.write(bytes, 0, bytes.length);
        }
        return this;
    }

    /**
     * Writes a 16-bit short in Little-Endian format.
     *
     * @param v Short value.
     * @return This writer instance.
     */
    public BinaryWriter writeShort(int v) {
        temp[0] = (byte) v;
        temp[1] = (byte) (v >>> 8);
        stream.write(temp, 0, 2);
        return this;
    }

    /**
     * Writes a 32-bit integer in Little-Endian format.
     *
     * @param v Int value.
     * @return This writer instance.
     */
    public BinaryWriter writeInt(int v) {
        temp[0] = (byte) v;
        temp[1] = (byte) (v >>> 8);
        temp[2] = (byte) (v >>> 16);
        temp[3] = (byte) (v >>> 24);
        stream.write(temp, 0, 4);
        return this;
    }

    /**
     * Writes a 64-bit long in Little-Endian format.
     *
     * @param v Long value.
     * @return This writer instance.
     */
    public BinaryWriter writeLong(long v) {
        temp[0] = (byte) v;
        temp[1] = (byte) (v >>> 8);
        temp[2] = (byte) (v >>> 16);
        temp[3] = (byte) (v >>> 24);
        temp[4] = (byte) (v >>> 32);
        temp[5] = (byte) (v >>> 40);
        temp[6] = (byte) (v >>> 48);
        temp[7] = (byte) (v >>> 56);
        stream.write(temp, 0, 8);
        return this;
    }

    /**
     * Writes a 32-bit float in Little-Endian format.
     *
     * @param v Float value.
     * @return This writer instance.
     */
    public BinaryWriter writeFloat(float v) {
        return writeInt(Float.floatToRawIntBits(v));
    }

    /**
     * Writes a 64-bit double in Little-Endian format.
     *
     * @param v Double value.
     * @return This writer instance.
     */
    public BinaryWriter writeDouble(double v) {
        return writeLong(Double.doubleToRawLongBits(v));
    }

    /**
     * Writes a UTF-8 string prefixed by a 2-byte short length header.
     *
     * @param str String content.
     * @return This writer instance.
     */
    public BinaryWriter writeString(String str) {
        if (str == null) {
            writeShort(0);
            return this;
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeShort(bytes.length);
        writeBytes(bytes);
        return this;
    }

    /**
     * Writes an array of 32-bit integers in Little-Endian format.
     *
     * @param array Primitive int array.
     * @return This writer instance.
     */
    public BinaryWriter writeIntArray(int[] array) {
        if (array == null) {
            writeInt(0);
            return this;
        }
        writeInt(array.length);
        for (int val : array) {
            writeInt(val);
        }
        return this;
    }

    /**
     * Writes a 32-bit integer using LEB128 VarInt compression via FastBinary.
     *
     * @param value 32-bit integer.
     * @return This writer instance.
     */
    public BinaryWriter writeVarInt(int value) {
        try {
            fastbinary.VarInt.write(value, stream);
        } catch (IOException ignored) {}
        return this;
    }

    /**
     * Writes a signed 32-bit integer using ZigZag mapping + VarInt compression via FastBinary.
     *
     * @param value Signed 32-bit integer.
     * @return This writer instance.
     */
    public BinaryWriter writeSignedVarInt(int value) {
        return writeVarInt(fastbinary.ZigZag.encode(value));
    }

    /**
     * Writes a 64-bit long using LEB128 VarLong compression via FastBinary.
     *
     * @param value 64-bit long.
     * @return This writer instance.
     */
    public BinaryWriter writeVarLong(long value) {
        while ((value & ~0x7FL) != 0) {
            stream.write((int) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        stream.write((int) (value & 0x7F));
        return this;
    }

    /**
     * Writes an array of 32-bit floats in Little-Endian format.
     *
     * @param array Primitive float array.
     * @return This writer instance.
     */
    public BinaryWriter writeFloatArray(float[] array) {
        if (array == null) {
            writeInt(0);
            return this;
        }
        writeInt(array.length);
        for (float val : array) {
            writeFloat(val);
        }
        return this;
    }

    /**
     * Returns the current total written bytes.
     *
     * @return Number of bytes written.
     */
    public int size() {
        return stream.size();
    }

    /**
     * Returns a copy of the written byte array.
     *
     * @return Serialized byte array.
     */
    public byte[] toByteArray() {
        return stream.toByteArray();
    }

    /**
     * Writes the complete buffer to a file.
     *
     * @param path Destination file path.
     * @throws IOException If write fails.
     */
    public void writeToFile(Path path) throws IOException {
        Files.write(path, toByteArray());
    }

    /**
     * Writes the complete buffer to a file.
     *
     * @param file Destination file.
     * @throws IOException If write fails.
     */
    public void writeToFile(File file) throws IOException {
        writeToFile(file.toPath());
    }
}
