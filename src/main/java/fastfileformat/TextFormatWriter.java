package fastfileformat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clean, structured pretty-printer and serializer for human-readable FastJava text formats.
 */
public final class TextFormatWriter {

    private final StringBuilder sb = new StringBuilder(512);
    private String currentSection = null;

    /**
     * Constructs a new TextFormatWriter.
     */
    public TextFormatWriter() {}

    /**
     * Constructs a new TextFormatWriter with a document title.
     *
     * @param title Document title.
     */
    public TextFormatWriter(String title) {
        if (title != null && !title.trim().isEmpty()) {
            sb.append("# FastJava Formatted Document\r\n");
            sb.append("TITLE = ").append(title).append("\r\n\r\n");
        }
    }

    /**
     * Appends a comment line.
     *
     * @param comment Comment text.
     * @return This writer instance.
     */
    public TextFormatWriter comment(String comment) {
        sb.append("# ").append(comment).append("\r\n");
        return this;
    }

    /**
     * Starts a new section block.
     *
     * @param sectionName Section title.
     * @return This writer instance.
     */
    public TextFormatWriter section(String sectionName) {
        this.currentSection = sectionName;
        sb.append("\r\n[").append(sectionName).append("]\r\n");
        return this;
    }

    /**
     * Appends a key-value assignment.
     *
     * @param key   Key name.
     * @param value String value.
     * @return This writer instance.
     */
    public TextFormatWriter set(String key, String value) {
        sb.append(String.format("%-28s = %s\r\n", key, value != null ? value : ""));
        return this;
    }

    /**
     * Appends an integer key-value assignment.
     *
     * @param key   Key name.
     * @param value Int value.
     * @return This writer instance.
     */
    public TextFormatWriter set(String key, int value) {
        return set(key, String.valueOf(value));
    }

    /**
     * Appends a float key-value assignment.
     *
     * @param key   Key name.
     * @param value Float value.
     * @return This writer instance.
     */
    public TextFormatWriter set(String key, float value) {
        return set(key, String.valueOf(value));
    }

    /**
     * Appends a double key-value assignment.
     *
     * @param key   Key name.
     * @param value Double value.
     * @return This writer instance.
     */
    public TextFormatWriter set(String key, double value) {
        return set(key, String.valueOf(value));
    }

    /**
     * Appends a boolean key-value assignment.
     *
     * @param key   Key name.
     * @param value Boolean value.
     * @return This writer instance.
     */
    public TextFormatWriter set(String key, boolean value) {
        return set(key, String.valueOf(value));
    }

    /**
     * Appends a variable alias reference.
     *
     * @param key       Key name.
     * @param targetKey Target key being referenced.
     * @return This writer instance.
     */
    public TextFormatWriter alias(String key, String targetKey) {
        return set(key, "@" + targetKey);
    }

    /**
     * Appends a blank line.
     *
     * @return This writer instance.
     */
    public TextFormatWriter blankLine() {
        sb.append("\r\n");
        return this;
    }

    /**
     * Serializes this document into a formatted text string.
     *
     * @return Formatted string.
     */
    public String toText() {
        return sb.toString();
    }

    /**
     * Writes this document directly to a file.
     *
     * @param path Destination path.
     * @throws IOException If write fails.
     */
    public void writeToFile(Path path) throws IOException {
        Files.writeString(path, toText(), StandardCharsets.UTF_8);
    }

    /**
     * Writes this document directly to a File.
     *
     * @param file Destination file.
     * @throws IOException If write fails.
     */
    public void writeToFile(File file) throws IOException {
        writeToFile(file.toPath());
    }

    @Override
    public String toString() {
        return toText();
    }
}
