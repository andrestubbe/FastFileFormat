package fastfileformat.benchmark;

import fastfileformat.BinaryHeader;
import fastfileformat.BinaryReader;
import fastfileformat.BinaryWriter;
import fastfileformat.FastFileFormat;
import fastfileformat.TextFormatParser;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class FastFileFormatBenchmark {

    private String textConfig;
    private byte[] binaryPayload;

    @Setup
    public void setup() {
        textConfig = """
                TITLE = High Performance Benchmark
                
                [Engine]
                resolution.x = 3840
                resolution.y = 2160
                fps = 240
                vsync = true
                
                [Theme]
                primary = #00F0FF
                secondary = #FF007F
                accent = @secondary
                """;

        BinaryWriter bw = FastFileFormat.binaryWriter();
        bw.writeHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 10, 0);
        bw.writeString("Benchmark String Payload");
        bw.writeIntArray(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        bw.writeFloatArray(new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f});
        bw.writeLong(123456789L);
        binaryPayload = bw.toByteArray();
    }

    @Benchmark
    public TextFormatParser benchmarkTextParsing() {
        return FastFileFormat.parseText(textConfig);
    }

    @Benchmark
    public byte[] benchmarkBinarySerialization() {
        BinaryWriter bw = FastFileFormat.binaryWriter();
        bw.writeHeader(FastFileFormat.DEFAULT_MAGIC, (short) 1, (short) 10, 0);
        bw.writeString("Benchmark String Payload");
        bw.writeIntArray(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        bw.writeFloatArray(new float[]{1.0f, 2.0f, 3.0f, 4.0f, 5.0f});
        bw.writeLong(123456789L);
        return bw.toByteArray();
    }

    @Benchmark
    public BinaryHeader benchmarkBinaryDeserialization() {
        BinaryReader br = FastFileFormat.binaryReader(binaryPayload);
        BinaryHeader h = br.readHeader();
        String s = br.readString();
        int[] ints = br.readIntArray();
        float[] floats = br.readFloatArray();
        long l = br.readLong();
        return h;
    }
}
