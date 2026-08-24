package fastfileformat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FastFileFormatTest {

    @Test
    public void testBinaryHeaderSerialization() {
        BinaryHeader header = new BinaryHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 42, 1024);
        byte[] bytes = header.toBytes();
        assertEquals(12, bytes.length);

        BinaryReader reader = FastFileFormat.binaryReader(bytes);
        BinaryHeader readHeader = reader.readHeader();
        assertEquals(FastFileFormat.DEFAULT_MAGIC, readHeader.getMagic());
        assertEquals(1, readHeader.getVersion());
        assertEquals(42, readHeader.getPayloadType());
        assertEquals(1024, readHeader.getPayloadLength());
    }

    @Test
    public void testBinaryStreamingRoundtrip() {
        BinaryWriter writer = FastFileFormat.binaryWriter();
        writer.writeHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 100, 0);
        writer.writeByte(255);
        writer.writeShort(1234);
        writer.writeInt(987654321);
        writer.writeLong(123456789012345L);
        writer.writeFloat(3.1415f);
        writer.writeDouble(2.718281828459045);
        writer.writeString("FastJava Binary Test String");
        writer.writeIntArray(new int[]{10, 20, 30, 40, 50});
        writer.writeFloatArray(new float[]{1.1f, 2.2f, 3.3f});

        byte[] payload = writer.toByteArray();
        assertNotNull(payload);
        assertTrue(payload.length > 0);

        BinaryReader reader = FastFileFormat.binaryReader(payload);
        BinaryHeader h = reader.readHeader();
        assertEquals(FastFileFormat.DEFAULT_MAGIC, h.getMagic());
        assertEquals(255, reader.readUnsignedByte());
        assertEquals(1234, reader.readShort());
        assertEquals(987654321, reader.readInt());
        assertEquals(123456789012345L, reader.readLong());
        assertEquals(3.1415f, reader.readFloat(), 0.0001f);
        assertEquals(2.718281828459045, reader.readDouble(), 0.0000001);
        assertEquals("FastJava Binary Test String", reader.readString());

        int[] ints = reader.readIntArray();
        assertArrayEquals(new int[]{10, 20, 30, 40, 50}, ints);

        float[] floats = reader.readFloatArray();
        assertEquals(3, floats.length);
        assertEquals(1.1f, floats[0], 0.01f);
        assertEquals(2.2f, floats[1], 0.01f);
        assertEquals(3.3f, floats[2], 0.01f);
        assertEquals(0, reader.remaining());
    }

    @Test
    public void testTextFormatParsingAndAliases() {
        String content = """
                # Sample FastJava Format Document
                TITLE = Cyberpunk Config
                
                [Engine]
                resolution.x = 1920
                resolution.y = 1080
                fps.target = 144
                vsync = true
                fov = 90.5
                
                [Colors]
                primary = #00F0FF
                secondary = #FF007F
                accent = @secondary
                cursor = @accent
                glow = 0xFF8800
                palette = 10, 20, 30, 40
                """;

        TextFormatParser doc = FastFileFormat.parseText(content);
        assertEquals("Cyberpunk Config", doc.getTitle());

        assertEquals(1920, doc.getInt("Engine.resolution.x", 0));
        assertEquals(1080, doc.getInt("Engine.resolution.y", 0));
        assertEquals(144, doc.getInt("Engine.fps.target", 0));
        assertTrue(doc.getBoolean("Engine.vsync", false));
        assertEquals(90.5f, doc.getFloat("Engine.fov", 0.0f), 0.01f);

        assertEquals("#00F0FF", doc.getString("Colors.primary", ""));
        assertEquals("#FF007F", doc.getString("Colors.secondary", ""));
        // Check alias resolution
        assertEquals("#FF007F", doc.getString("Colors.accent", ""));
        assertEquals("#FF007F", doc.getString("Colors.cursor", ""));

        // Check hex conversion
        assertEquals(0x00F0FF, doc.getInt("Colors.primary", 0));
        assertEquals(0xFF8800, doc.getInt("Colors.glow", 0));

        // Check int array
        assertArrayEquals(new int[]{10, 20, 30, 40}, doc.getIntArray("Colors.palette"));

        // Check section access
        assertEquals(6, doc.getSection("Colors").size());
        assertEquals("#FF007F", doc.getSection("Colors").get("cursor"));
    }

    @Test
    public void testTextFormatWriter() {
        TextFormatWriter writer = FastFileFormat.textWriter("Generated Config");
        writer.comment("Settings for Windowing")
                .section("Window")
                .set("width", 800)
                .set("height", 600)
                .set("fullscreen", false)
                .blankLine()
                .section("Theme")
                .set("bg", "#121212")
                .alias("titlebar", "Theme.bg");

        String text = writer.toText();
        assertNotNull(text);
        assertTrue(text.contains("[Window]"));
        assertTrue(text.contains("width                        = 800"));
        assertTrue(text.contains("titlebar                     = @Theme.bg"));

        TextFormatParser parsed = FastFileFormat.parseText(text);
        assertEquals(800, parsed.getInt("Window.width", 0));
        assertEquals("#121212", parsed.getString("Theme.titlebar", ""));
    }

    @Test
    public void testFileRoundtrip() throws IOException {
        Path tempFile = Files.createTempFile("fastfileformat-test", ".bin");
        try {
            BinaryWriter bw = FastFileFormat.binaryWriter();
            bw.writeHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 1, 0);
            bw.writeString("Hello Disk");
            bw.writeInt(777);
            bw.writeToFile(tempFile);

            assertTrue(FastFileFormat.isBinaryFile(tempFile));

            BinaryReader br = FastFileFormat.loadBinaryFile(tempFile);
            BinaryHeader h = br.readHeader();
            assertEquals(FastFileFormat.DEFAULT_MAGIC, h.getMagic());
            assertEquals("Hello Disk", br.readString());
            assertEquals(777, br.readInt());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testTranscoding() {
        String text = """
                TITLE = Transcode Test
                
                [Window]
                width = 1024
                height = 768
                """;

        byte[] binary = FastFileFormat.textToBinary(text);
        assertNotNull(binary);
        assertTrue(binary.length > 0);

        String backToText = FastFileFormat.binaryToText(binary);
        assertNotNull(backToText);
        assertTrue(backToText.contains("[Window]"));
        assertTrue(backToText.contains("width                        = 1024"));
        assertTrue(backToText.contains("height                       = 768"));
    }

    @Test
    public void testVarIntIntegration() {
        BinaryWriter bw = FastFileFormat.binaryWriter();
        bw.writeVarInt(42);
        bw.writeVarInt(16384);
        bw.writeSignedVarInt(-1);
        bw.writeSignedVarInt(-5000);
        bw.writeVarLong(9876543210123L);

        BinaryReader br = FastFileFormat.binaryReader(bw.toByteArray());
        assertEquals(42, br.readVarInt());
        assertEquals(16384, br.readVarInt());
        assertEquals(-1, br.readSignedVarInt());
        assertEquals(-5000, br.readSignedVarInt());
        assertEquals(9876543210123L, br.readVarLong());
    }
}
