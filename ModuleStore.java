import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/** Small file-backed store for the non-patient MediCare modules. */
public class ModuleStore {
    private final File folder = new File("medicare_data");

    public synchronized List<String[]> read(String module, int columns) throws IOException {
        File file = fileFor(module);
        List<String[]> rows = new ArrayList<>();
        if (!file.exists()) return rows;
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) continue;
            String[] cells = line.split("\\t", -1);
            String[] row = new String[columns];
            for (int i = 0; i < columns; i++) {
                row[i] = i < cells.length ? decode(cells[i]) : "";
            }
            rows.add(row);
        }
        return rows;
    }

    public synchronized void write(String module, List<String[]> rows) throws IOException {
        if (!folder.exists() && !folder.mkdirs()) throw new IOException("Cannot create " + folder.getPath());
        File file = fileFor(module);
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) writer.write('\t');
                    writer.write(encode(row[i]));
                }
                writer.newLine();
            }
        }
    }

    public synchronized void log(String message) throws IOException {
        List<String[]> entries = read("Activity", 2);
        entries.add(new String[]{new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()), message});
        while (entries.size() > 100) entries.remove(0);
        write("Activity", entries);
    }

    private File fileFor(String module) {
        return new File(folder, module.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "") + ".txt");
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        try { return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8); }
        catch (IllegalArgumentException ex) { return value; }
    }
}
