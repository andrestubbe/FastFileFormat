package fastfileformat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Standard 12-byte FastJava Binary File Header.
 * Structure:
 * - 4 Bytes: Magic identifier (e.g. 0x46464D54 "FFMT")
 * - 2 Bytes: Format version (e.g. 1)
 * - 2 Bytes: Payload type identifier
 * - 4 Bytes: Payload length in bytes
 */
public final class BinaryHeader {

    /**
     * Standard byte size of a FastJava binary header.
     */
    public static final int HEADER_SIZE = 12;

    private final int magic;
    private final short version;
    private final short payloadType;
    private final int payloadLength;

    /**
     * Constructs a BinaryHeader instance.
     *
     * @param magic         4-byte magic number.
     * @param version       2-byte format version.
     * @param payloadType   2-byte payload type ID.
     * @param payloadLength 4-byte payload size.
     */
    public BinaryHeader(int magic, short version, short payloadType, int payloadLength) {
        this.magic = magic;
        this.version = version;
        this.payloadType = payloadType;
        this.payloadLength = payloadLength;
    }

    /**
     * Returns the 4-byte magic identifier.
     *
     * @return Magic int.
     */
    public int getMagic() {
        return magic;
    }

    /**
     * Returns the 2-byte format version.
     *
     * @return Format version short.
     */
    public short getVersion() {
        return version;
    }

    /**
     * Returns the 2-byte payload type identifier.
     *
     * @return Payload type short.
     */
    public short getPayloadType() {
        return payloadType;
    }

    /**
     * Returns the 4-byte payload length in bytes.
     *
     * @return Payload length int.
     */
    public int getPayloadLength() {
        return payloadLength;
    }

    /**
     * Serializes this header into a 12-byte Little-Endian byte array.
     *
     * @return 12-byte array containing the serialized header.
     */
    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);
        writeTo(buf);
        return buf.array();
    }

    /**
     * Writes this header directly to a target {@link ByteBuffer}.
     *
     * @param buffer Destination buffer.
     */
    public void writeTo(ByteBuffer buffer) {
        buffer.putInt(magic);
        buffer.putShort(version);
        buffer.putShort(payloadType);
        buffer.putInt(payloadLength);
    }

    /**
     * Reads a BinaryHeader from the current position of a ByteBuffer.
     *
     * @param buffer Source buffer.
     * @return Deserialized {@link BinaryHeader}.
     */
    public static BinaryHeader readFrom(ByteBuffer buffer) {
        if (buffer.remaining() < HEADER_SIZE) {
            throw new IllegalArgumentException("Buffer underflow: at least 12 bytes required for BinaryHeader");
        }
        int magic = buffer.getInt();
        short version = buffer.getShort();
        short payloadType = buffer.getShort();
        int payloadLength = buffer.getInt();
        return new BinaryHeader(magic, version, payloadType, payloadLength);
    }
}
