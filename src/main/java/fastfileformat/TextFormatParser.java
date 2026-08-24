package fastfileformat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Universal human-readable text format parser for FastJava.
 * Supports:
 * - KEY = VALUE assignments
 * - Variable alias resolution via '@KEY' or '@SECTION.KEY'
 * - Section blocks '[SectionName]'
 * - Comments via '#', '//', or ';'
 * - Data conversions for int, float, double, boolean, hex, rgb, arrays
 */
public final class TextFormatParser {

    private final Map<String, String> values = new LinkedHashMap<>(64);
    private final Map<String, Map<String, String>> sections = new LinkedHashMap<>(16);
    private String title = "";

    private TextFormatParser() {}

    /**
     * Parses a formatted string into a {@link TextFormatParser} instance.
     *
     * @param text Formatted text content.
     * @return Populated TextFormatParser document.
     */
    public static TextFormatParser parse(String text) {
        TextFormatParser doc = new TextFormatParser();
        if (text == null || text.trim().isEmpty()) {
            return doc;
        }

        String currentSection = null;
        Map<String, String> rawPairs = new LinkedHashMap<>();

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//") || trimmed.startsWith(";")) {
                continue;
            }

            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentSection = trimmed.substring(1, trimmed.length() - 1).trim();
                doc.sections.putIfAbsent(currentSection, new LinkedHashMap<>());
                continue;
            }

            int eqIdx = trimmed.indexOf('=');
            if (eqIdx == -1) continue;

            String key = trimmed.substring(0, eqIdx).trim();
            String val = trimmed.substring(eqIdx + 1).trim();

            if (key.equalsIgnoreCase("TITLE") || key.equalsIgnoreCase("NAME")) {
                doc.title = val;
            }

            if (currentSection == null) {
                rawPairs.put(key, val);
                doc.values.put(key, val);
            } else {
                String fullKey = currentSection + "." + key;
                rawPairs.put(fullKey, val);
                doc.sections.get(currentSection).put(key, val);
                doc.values.put(fullKey, val);
            }
        }

        // Resolve @KEY and @SECTION.KEY aliases
        boolean changed;
        int passes = 0;
        do {
            changed = false;
            passes++;
            for (Map.Entry<String, String> entry : new ArrayList<>(rawPairs.entrySet())) {
                String fullKey = entry.getKey();
                String val = entry.getValue();
                if (val.startsWith("@")) {
                    String targetKey = val.substring(1).trim();
                    String sectionPrefix = fullKey.contains(".") ? fullKey.substring(0, fullKey.indexOf('.') + 1) : "";

                    String resolved = resolveAlias(rawPairs, sectionPrefix, targetKey, 0);
                    if (resolved != null && !resolved.startsWith("@")) {
                        rawPairs.put(fullKey, resolved);
                        doc.values.put(fullKey, resolved);
                        int dot = fullKey.indexOf('.');
                        if (dot != -1) {
                            String sec = fullKey.substring(0, dot);
                            String sub = fullKey.substring(dot + 1);
                            if (doc.sections.containsKey(sec)) {
                                doc.sections.get(sec).put(sub, resolved);
                            }
                        }
                        changed = true;
                    }
                }
            }
        } while (changed && passes < 10);

        return doc;
    }

    private static String resolveAlias(Map<String, String> map, String sectionPrefix, String key, int depth) {
        if (depth > 10) return null;
        // 1. Try with section prefix (e.g. Colors.secondary)
        String val = map.get(sectionPrefix + key);
        if (val == null) {
            // 2. Try direct key
            val = map.get(key);
        }
        if (val == null) {
            // 3. Case-insensitive fallback
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (e.getKey().equalsIgnoreCase(sectionPrefix + key) || e.getKey().equalsIgnoreCase(key)) {
                    val = e.getValue();
                    break;
                }
            }
        }
        if (val == null) return null;
        if (val.startsWith("@")) {
            return resolveAlias(map, sectionPrefix, val.substring(1).trim(), depth + 1);
        }
        return val;
    }

    /**
     * Returns the title or name defined in this document.
     *
     * @return Title string.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the raw string value for a key, or null if absent.
     *
     * @param key Key name.
     * @return String value.
     */
    public String get(String key) {
        return values.get(key);
    }

    /**
     * Returns the string value for a key, or defaultValue if absent.
     *
     * @param key          Key name.
     * @param defaultValue Default fallback.
     * @return String value.
     */
    public String getString(String key, String defaultValue) {
        String val = values.get(key);
        return val != null ? val : defaultValue;
    }

    /**
     * Parses an integer value.
     *
     * @param key          Key name.
     * @param defaultValue Default fallback.
     * @return Parsed int value.
     */
    public int getInt(String key, int defaultValue) {
        String val = values.get(key);
        if (val == null) return defaultValue;
        try {
            if (val.startsWith("0x") || val.startsWith("0X") || val.startsWith("#")) {
                String hex = val.startsWith("#") ? val.substring(1) : val.substring(2);
                return (int) Long.parseLong(hex, 16);
            }
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses a float value.
     *
     * @param key          Key name.
     * @param defaultValue Default fallback.
     * @return Parsed float value.
     */
    public float getFloat(String key, float defaultValue) {
        String val = values.get(key);
        if (val == null) return defaultValue;
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses a double value.
     *
     * @param key          Key name.
     * @param defaultValue Default fallback.
     * @return Parsed double value.
     */
    public double getDouble(String key, double defaultValue) {
        String val = values.get(key);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses a boolean value (true, false, 1, 0, yes, no, on, off).
     *
     * @param key          Key name.
     * @param defaultValue Default fallback.
     * @return Parsed boolean.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String val = values.get(key);
        if (val == null) return defaultValue;
        String s = val.trim().toLowerCase();
        if (s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("on")) return true;
        if (s.equals("false") || s.equals("0") || s.equals("no") || s.equals("off")) return false;
        return defaultValue;
    }

    /**
     * Parses a comma-separated list of integers.
     *
     * @param key Key name.
     * @return Parsed int array.
     */
    public int[] getIntArray(String key) {
        String val = values.get(key);
        if (val == null || val.trim().isEmpty()) return new int[0];
        String[] parts = val.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    /**
     * Returns an unmodifiable view of all key-value entries.
     *
     * @return Map of all keys and values.
     */
    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Returns an unmodifiable map of section key-values for a given section name.
     *
     * @param sectionName Section name.
     * @return Section map, or empty map if section does not exist.
     */
    public Map<String, String> getSection(String sectionName) {
        Map<String, String> sec = sections.get(sectionName);
        return sec != null ? Collections.unmodifiableMap(sec) : Collections.emptyMap();
    }

    /**
     * Loads and parses a text format file from a Path.
     *
     * @param path File path.
     * @return Parsed document.
     * @throws IOException If file read fails.
     */
    public static TextFormatParser loadFromFile(Path path) throws IOException {
        String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        return parse(content);
    }

    /**
     * Loads and parses a text format file from a File.
     *
     * @param file File object.
     * @return Parsed document.
     * @throws IOException If file read fails.
     */
    public static TextFormatParser loadFromFile(File file) throws IOException {
        return loadFromFile(file.toPath());
    }
}
