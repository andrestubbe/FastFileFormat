package fastfileformat.demo;

import fastfileformat.BinaryHeader;
import fastfileformat.BinaryReader;
import fastfileformat.BinaryWriter;
import fastfileformat.FastFileFormat;
import fastfileformat.TextFormatParser;
import fastfileformat.TextFormatWriter;

public class Demo {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("⚡ FastFileFormat 0.1.0 — Interactive Showcase");
        System.out.println("=================================================\n");

        // 1. Text Format Serialization & Variable Aliasing
        System.out.println("📄 [1/3] Generating Structured Text Document...");
        TextFormatWriter writer = FastFileFormat.textWriter("Application Master Config");
        writer.comment("Core Graphic & Windowing Settings")
                .section("Display")
                .set("resolution.width", 2560)
                .set("resolution.height", 1440)
                .set("target_fps", 165)
                .set("vsync_enabled", true)
                .blankLine()
                .section("Palette")
                .set("brand.cyan", "#00F0FF")
                .set("brand.magenta", "#FF007F")
                .alias("ui.accent", "Palette.brand.cyan")
                .alias("cursor.glow", "Palette.ui.accent");

        String formattedText = writer.toText();
        System.out.println(formattedText);

        // 2. Text Parsing & Dynamic Querying
        System.out.println("🔍 [2/3] Parsing & Resolving Dynamic Aliases...");
        TextFormatParser parser = FastFileFormat.parseText(formattedText);
        System.out.println("Title: " + parser.getTitle());
        System.out.println("Width: " + parser.getInt("Display.resolution.width", 0));
        System.out.println("Target FPS: " + parser.getInt("Display.target_fps", 0));
        System.out.println("VSync: " + parser.getBoolean("Display.vsync_enabled", false));
        System.out.println("Accent (Resolved Alias): " + parser.getString("Palette.ui.accent", ""));
        System.out.println("Glow (Chained Alias): " + parser.getString("Palette.cursor.glow", ""));
        System.out.println();

        // 3. Ultra-Fast Binary Serialization & Deserialization
        System.out.println("⚡ [3/3] Testing Sub-Microsecond Binary Streaming...");
        BinaryWriter bw = FastFileFormat.binaryWriter();
        bw.writeHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 42, 0);
        bw.writeString("FastJava High-Throughput Payload");
        bw.writeIntArray(new int[]{100, 200, 300, 400, 500});
        bw.writeFloatArray(new float[]{1.5f, 2.5f, 3.5f});
        bw.writeLong(System.currentTimeMillis());

        byte[] binaryBytes = bw.toByteArray();
        System.out.printf("Binary payload size: %d bytes (including 12-byte header)\n", binaryBytes.length);

        BinaryReader br = FastFileFormat.binaryReader(binaryBytes);
        BinaryHeader header = br.readHeader();
        System.out.printf("Header Magic: 0x%X | Version: %d | PayloadType: %d\n",
                header.getMagic(), header.getVersion(), header.getPayloadType());
        System.out.println("Payload String: " + br.readString());
        System.out.println("Int Array Length: " + br.readIntArray().length);
        System.out.println("Float Array Length: " + br.readFloatArray().length);
        System.out.println("Timestamp: " + br.readLong());

        System.out.println("\n✅ FastFileFormat Demo Completed Successfully!");
    }
}
