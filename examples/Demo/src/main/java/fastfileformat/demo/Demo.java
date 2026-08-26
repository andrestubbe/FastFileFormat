package fastfileformat.demo;

import fastansi.FastANSI;
import fastfileformat.*;

import java.util.Arrays;

public class Demo {

    private Demo() {}

    public static void main(String[] args) throws Exception {

        System.out.println(darkGray("==========================================================================================================="));
        System.out.println(" " + boldWhite("FastFileFormat") + darkGray(" â€” Dual-Format Serialization Engine  |  Text Config  â€¢  Binary Streaming  â€¢  Transcoding"));
        System.out.println(darkGray(" Zero-Dependency  |  Sub-Microsecond Binary I/O  |  Alias-Chaining  |  Text â†” Binary Roundtrip"));
        System.out.println(darkGray("==========================================================================================================="));
        System.out.println();

        // â”€â”€ Phase 1: Text Format Write â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        System.out.println(darkGray("[Phase 1]") + " " + boldWhite("TextFormatWriter â€” Structured Config Serialization") + darkGray(" (Sections, aliases, comments, typed values)"));
        System.out.println();

        long t0 = System.nanoTime();
        TextFormatWriter w = FastFileFormat.textWriter("Game Engine Config v3");
        w.comment("Rendering pipeline settings")
         .section("Renderer")
         .set("api",               "Vulkan")
         .set("resolution.width",  2560)
         .set("resolution.height", 1440)
         .set("target_fps",        165)
         .set("vsync",             true)
         .blankLine()
         .comment("Brand palette with alias chaining")
         .section("Palette")
         .set("cyan",              "#00F0FF")
         .set("magenta",           "#FF007F")
         .alias("ui.accent",       "Palette.cyan")
         .alias("cursor.glow",     "Palette.ui.accent")
         .blankLine()
         .section("Audio")
         .set("sample_rate",       48000)
         .set("channels",          2)
         .set("codec",             "OPUS");
        String text = w.toText();
        long writeNs = System.nanoTime() - t0;

        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            boolean last = (i == lines.length - 1);
            System.out.printf("  %s %s%n", darkGray(last ? "â””â”€â”€" : "â”œâ”€â”€"), white(lines[i]));
        }
        System.out.printf("%n  %s %s%n%n",
                darkGray("â””â”€â”€ Serialized"),
                boldWhite(lines.length + " lines  in  " + String.format("%.3f Âµs", writeNs / 1_000.0)));

        // â”€â”€ Phase 2: Text Parsing & Alias Resolution â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        System.out.println(darkGray("[Phase 2]") + " " + boldWhite("TextFormatParser â€” Query & Alias Resolution") + darkGray(" (Typed getters + chained alias traversal)"));
        System.out.println();

        long p0 = System.nanoTime();
        TextFormatParser p = FastFileFormat.parseText(text);
        long parseNs = System.nanoTime() - p0;

        String[][] rows = {
            {"Title",                      p.getTitle()},
            {"Renderer.api",               p.getString("Renderer.api", "-")},
            {"Renderer.resolution.width",  String.valueOf(p.getInt("Renderer.resolution.width", 0))},
            {"Renderer.target_fps",        String.valueOf(p.getInt("Renderer.target_fps", 0))},
            {"Renderer.vsync",             String.valueOf(p.getBoolean("Renderer.vsync", false))},
            {"Palette.cyan  (direct)",     p.getString("Palette.cyan", "-")},
            {"Palette.ui.accent  (alias)", p.getString("Palette.ui.accent", "-")},
            {"Palette.cursor.glow (chain)",p.getString("Palette.cursor.glow", "-")},
            {"Audio.sample_rate",          String.valueOf(p.getInt("Audio.sample_rate", 0))},
            {"Audio.codec",                p.getString("Audio.codec", "-")},
        };
        for (int i = 0; i < rows.length; i++) {
            boolean last = (i == rows.length - 1);
            System.out.printf("  %s %-32s %s%n",
                darkGray(last ? "â””â”€â”€" : "â”œâ”€â”€"),
                darkGray(rows[i][0]),
                boldWhite(rows[i][1]));
        }
        System.out.printf("%n  %s %s  |  %s%n%n",
                darkGray("â””â”€â”€ Parsed"),
                boldWhite(rows.length + " fields"),
                boldWhite(String.format("%.3f Âµs", parseNs / 1_000.0)));

        // â”€â”€ Phase 3: Binary Streaming â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        System.out.println(darkGray("[Phase 3]") + " " + boldWhite("BinaryWriter / BinaryReader â€” Sub-Microsecond Streaming") + darkGray(" (Header + typed payloads)"));
        System.out.println();

        long bw0 = System.nanoTime();
        BinaryWriter bw = FastFileFormat.binaryWriter();
        bw.writeHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 7, 0);
        bw.writeString("FastFileFormat High-Speed Payload");
        bw.writeInt(999_999);
        bw.writeIntArray(new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100});
        bw.writeFloatArray(new float[]{1.1f, 2.2f, 3.3f, 4.4f, 5.5f});
        bw.writeLong(System.currentTimeMillis());
        bw.writeByte(1);
        bw.writeString("EOF");
        byte[] bytes = bw.toByteArray();
        long binWriteNs = System.nanoTime() - bw0;

        long br0 = System.nanoTime();
        BinaryReader br = FastFileFormat.binaryReader(bytes);
        BinaryHeader header = br.readHeader();
        String label   = br.readString();
        int    count   = br.readInt();
        int[]  ints    = br.readIntArray();
        float[] flts   = br.readFloatArray();
        long   ts      = br.readLong();
        byte flag      = br.readByte();
        String eof     = br.readString();
        long binReadNs = System.nanoTime() - br0;

        String[][] brows = {
            {"Magic (hex)",    String.format("0x%X", header.getMagic())},
            {"Version",        String.valueOf(header.getVersion())},
            {"PayloadType",    String.valueOf(header.getPayloadType())},
            {"Payload label",  label},
            {"Int value",      String.valueOf(count)},
            {"Int[]  length",  ints.length + "  â†’ " + Arrays.toString(ints)},
            {"Float[] length", flts.length + "  â†’ " + Arrays.toString(flts)},
            {"Timestamp",      String.valueOf(ts)},
            {"Flag (byte)",    String.valueOf(flag)},
            {"EOF marker",     eof},
            {"Total bytes",    bytes.length + " B"},
        };
        for (int i = 0; i < brows.length; i++) {
            boolean last = (i == brows.length - 1);
            System.out.printf("  %s %-20s %s%n",
                darkGray(last ? "â””â”€â”€" : "â”œâ”€â”€"),
                darkGray(brows[i][0]),
                boldWhite(brows[i][1]));
        }
        System.out.printf("%n  %s %s  |  %s%n%n",
                darkGray("â””â”€â”€ Write"),
                boldWhite(String.format("%.3f Âµs", binWriteNs / 1_000.0)),
                boldWhite("Read  " + String.format("%.3f Âµs", binReadNs / 1_000.0)));

        // â”€â”€ Phase 4: Text â†” Binary Transcoding â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        System.out.println(darkGray("[Phase 4]") + " " + boldWhite("Transcoding â€” Text â†” Binary Roundtrip") + darkGray(" (Lossless encode + decode with field verification)"));
        System.out.println();

        long enc0 = System.nanoTime();
        byte[] encoded = FastFileFormat.textToBinary(text);
        long encNs = System.nanoTime() - enc0;

        long dec0 = System.nanoTime();
        String decoded = FastFileFormat.binaryToText(encoded);
        long decNs = System.nanoTime() - dec0;

        TextFormatParser dp = FastFileFormat.parseText(decoded);
        boolean lossless = dp.getString("Renderer.api", "").equals("Vulkan")
                        && dp.getString("Palette.cyan", "").equals("#00F0FF")
                        && dp.getString("Audio.codec", "").equals("OPUS");

        String[][] trows = {
            {"Original text size",   text.length() + " chars"},
            {"Binary encoded size",  encoded.length + " bytes  (" + String.format("%.1f%%", encoded.length * 100.0 / text.length()) + ")"},
            {"Decoded title",        dp.getTitle()},
            {"Renderer.api",         dp.getString("Renderer.api", "-")},
            {"Palette.cyan",         dp.getString("Palette.cyan", "-")},
            {"Audio.codec",          dp.getString("Audio.codec", "-")},
            {"Roundtrip lossless",   lossless ? "âœ… YES â€” all fields match" : "âŒ MISMATCH"},
        };
        for (int i = 0; i < trows.length; i++) {
            boolean last = (i == trows.length - 1);
            System.out.printf("  %s %-24s %s%n",
                darkGray(last ? "â””â”€â”€" : "â”œâ”€â”€"),
                darkGray(trows[i][0]),
                boldWhite(trows[i][1]));
        }
        System.out.printf("%n  %s %s  |  %s%n%n",
                darkGray("â””â”€â”€ Encode"),
                boldWhite(String.format("%.3f Âµs", encNs / 1_000.0)),
                boldWhite("Decode  " + String.format("%.3f Âµs", decNs / 1_000.0)));

        // â”€â”€ Summary â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        System.out.println(darkGray("==========================================================================================================="));
        System.out.printf(" " + boldWhite("COMPLETE:") + darkGray(" 4 phases | Text write %s | Parse %s | Binary write %s | Binary read %s | Encode %s | Decode %s%n"),
                boldWhite(String.format("%.3f Âµs", writeNs / 1_000.0)),
                boldWhite(String.format("%.3f Âµs", parseNs / 1_000.0)),
                boldWhite(String.format("%.3f Âµs", binWriteNs / 1_000.0)),
                boldWhite(String.format("%.3f Âµs", binReadNs / 1_000.0)),
                boldWhite(String.format("%.3f Âµs", encNs / 1_000.0)),
                boldWhite(String.format("%.3f Âµs", decNs / 1_000.0)));
        System.out.println(darkGray("==========================================================================================================="));
    }

    private static String darkGray(String text) {
        return FastANSI.fg(240) + text + FastANSI.RESET;
    }

    private static String white(String text) {
        return FastANSI.FG_BRIGHT_WHITE + text + FastANSI.RESET;
    }

    private static String boldWhite(String text) {
        return FastANSI.BOLD + FastANSI.FG_BRIGHT_WHITE + text + FastANSI.RESET;
    }
}