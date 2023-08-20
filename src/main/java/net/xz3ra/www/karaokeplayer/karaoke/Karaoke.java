package net.xz3ra.www.karaokeplayer.karaoke;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.Media;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.TimedSection;
import net.xz3ra.www.karaokeplayer.exceptions.InvalidFormatException;
import net.xz3ra.www.karaokeplayer.exceptions.MissingFilesException;
import net.xz3ra.www.karaokeplayer.exceptions.SaveFailedException;
import net.xz3ra.www.karaokeplayer.exceptions.UnsupportedFileTypeException;
import net.xz3ra.www.karaokeplayer.ressource.RessourceManager;
import net.xz3ra.www.karaokeplayer.util.ArchiveUtil;
import net.xz3ra.www.karaokeplayer.util.FileUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Karaoke {
    private static final Path TEMPORARY_DIRECTORY = RessourceManager.TEMPORARY_DIRECTORY;
    public static final Karaoke EMPTY = new Karaoke();
    public static final String FILE_TYPE = "skf";

    public static final List<FileChooser.ExtensionFilter> ALLOWED_LOADING_TYPES = new ArrayList<>();
    public static final List<FileChooser.ExtensionFilter> ALLOWED_SAVING_TYPES = new ArrayList<>();
    static {
        FileChooser.ExtensionFilter Skf = new FileChooser.ExtensionFilter("Sing along karaoke file", "*.skf");
        FileChooser.ExtensionFilter Folder = new FileChooser.ExtensionFilter("Folder", "*");

        ALLOWED_LOADING_TYPES.add(Skf);

        ALLOWED_SAVING_TYPES.add(Skf);
        ALLOWED_SAVING_TYPES.add(Folder);
    }

    private static final String LABEL_PATTERN = "(?<start>\\d+.\\d+)\\t(?<end>\\d+.\\d+)\\t(?<value>.*)";
    private static final String VIDEO_PATTERN = ".*\\.(mp4|avi|mov|wmv|mkv|flv|mpg)$";
    private static final String AUDIO_PATTERN = ".*\\.(mp3|wav|ogg|flac|m4a)$";
    private static final String FILE_PATTERN = ".*\\." + FILE_TYPE + "$";
    private static final String DIRECTORY_PATTERN = ".*$";

    private static final String LABEL_FILE = "label.txt";
    private static final String MEDIA_FILE = "media";

    private static final String DEFAULT_MEDIA_EXTENSION = ".mp3";

    private static final String SECTION_LINE_FORMAT = "%.6f\t%.6f\t%s";

    private final String title;

    private final Media media;

    private final List<TimedSection> sections;
    private final String lyrics;

    private Karaoke() {
        this("", null, new ArrayList<>());
    }
    private Karaoke(String title, Media media, List<TimedSection> sections) {
        this.title = title;
        this.media = media;
        this.sections = sections;
        this.lyrics = sectionsToString(sections);
    }

    public boolean isEmpty() {
        return isEmpty(this);
    }
    public static boolean isEmpty(Karaoke karaoke) {
        return karaoke.media == null || karaoke.sections == null || (karaoke.title.isEmpty() && karaoke.sections.isEmpty() && karaoke.lyrics.isEmpty());
    }

    public static Karaoke load(String path) throws UnsupportedFileTypeException, IOException, MissingFilesException, InvalidFormatException {
        Path file = Paths.get(path);

        if (Files.isDirectory(file)) {
            return loadFromFolder(file.toString());
        }

        if (isKaraokeFile(file)) {
            return loadFromKaraokeFile(file.toString());
        }

        throw new UnsupportedFileTypeException("File " + file + " is not supported");
    }

    public static Karaoke loadFromFolder(String path) throws IOException, UnsupportedFileTypeException, MissingFilesException, InvalidFormatException {
        Path file = Paths.get(path);

        if (!Files.exists(file)) {
            throw new FileNotFoundException("File: " + path + " does not exist");
        }
        if (!Files.isDirectory(file)) {
            throw new UnsupportedFileTypeException(path + " isn't a directory");
        }

        String title = file.getFileName().toString();

        return getKaraoke(file, title);
    }

    public static Karaoke loadFromKaraokeFile(String path) throws IOException, UnsupportedFileTypeException, MissingFilesException, InvalidFormatException {
        Path file = Paths.get(path);

        if (!Files.exists(file)) {
            throw new FileNotFoundException("File: " + path + " does not exist");
        }

        if (Files.isDirectory(file) || !isKaraokeFile(file)) {
            throw new UnsupportedFileTypeException(path + " isn't a ." + FILE_TYPE + " file");
        }

        String title = file.getFileName().toString().replaceFirst("\\." + FILE_TYPE + "$", "");

        ArchiveUtil.unzipFolder(file, TEMPORARY_DIRECTORY);
        file = Path.of(TEMPORARY_DIRECTORY.toString(), title);

        return getKaraoke(file, title);
    }

    /**
     * Saves this Karaoke to filesystem
     * @param path Path indicating wheer to save the Karaoke
     * @throws UnsupportedFileTypeException
     */
    public void save(String path) throws UnsupportedFileTypeException, IOException {
        save(path, this, false);
    }

    /**
     * Saves this Karaoke to filesystem
     * @param path Path indicating wheer to save the Karaoke
     * @param assumeNoExtensionIsDirectory Whether to assume no extension means directory
     * @throws UnsupportedFileTypeException
     */
    public void save(String path, boolean assumeNoExtensionIsDirectory) throws UnsupportedFileTypeException {
        save(path, assumeNoExtensionIsDirectory);
    }

    /**
     * Saves a Karaoke to filesystem
     * @param path Path indicating wheer to save the Karaoke
     * @param karaoke The Karaoke to save
     * @throws UnsupportedFileTypeException
     */
    public static void save(String path, Karaoke karaoke) throws UnsupportedFileTypeException, IOException {
        save(path, karaoke, false);
    }

    /**
     * Saves a Karaoke to filesystem
     * @param path Path indicating wheer to save the Karaoke
     * @param karaoke The Karaoke to save
     * @param assumeNoExtensionIsDirectory Whether to assume no extension means directory
     * @throws UnsupportedFileTypeException
     */
    public static void save(String path, Karaoke karaoke, boolean assumeNoExtensionIsDirectory) throws UnsupportedFileTypeException, IOException {
        Path file = Paths.get(path);

        if (assumeNoExtensionIsDirectory && isDirectory(file)) {
            saveToFolder(file.toString(), karaoke);
            return;
        }

        if (isKaraokeFile(file)) {
            saveToKaraokeFile(file.toString(), karaoke);
            return;
        }

        throw new UnsupportedFileTypeException("File " + file + " is not supported for saving");
    }

    public void saveToKaraokeFile(String path) throws IOException {
        saveToKaraokeFile(path, this);
    }

    public static void saveToKaraokeFile(String path, Karaoke karaoke) throws IOException {
        Path filePath = Paths.get(path);
        String title = filePath.getFileName().toString().replaceFirst("\\." + FILE_TYPE + "$", "");
        Path tempFolderPath = Path.of(TEMPORARY_DIRECTORY.toString(), title);
        saveToFolder(tempFolderPath.toAbsolutePath().toString(), karaoke);

        try {
            ArchiveUtil.zipFolder(tempFolderPath, filePath);
        } catch (IOException e) {
            throw new IOException("Failed to create karaoke file", e);
        } finally {
            //FileUtils.deleteDirectory(tempFolderPath.toFile());
        }
    }

    public void saveToFolder(String path) throws IOException {
        saveToFolder(path, this);
    }

    public static void saveToFolder(String path, Karaoke karaoke) throws IOException {
        Path folderPath = Paths.get(path);

        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create karaoke folder", e);
        }

        Path labelPath = Path.of(folderPath.toString(), LABEL_FILE);
        Path mediaPath = Path.of(folderPath.toString(), MEDIA_FILE + DEFAULT_MEDIA_EXTENSION);

        saveSectionsToFile(labelPath, karaoke.getSections());
        saveMediaToFile(mediaPath, karaoke.getMedia());
    }

    public static boolean isDirectory(Path path) {
        return path.getFileName().toString().matches(DIRECTORY_PATTERN);
    }

    public static boolean isDirectory(File file) {
        return isKaraokeFile(file.toPath());
    }

    public static boolean isDirectory(String path) {
        return isKaraokeFile(Paths.get(path));
    }

    public static boolean isKaraokeFile(Path path) {
        return path.getFileName().toString().matches(FILE_PATTERN);
    }

    public static boolean isKaraokeFile(File file) {
        return isKaraokeFile(file.toPath());
    }

    public static boolean isKaraokeFile(String path) {
        return isKaraokeFile(Paths.get(path));
    }

    private static Karaoke getKaraoke(Path file, String title) throws UnsupportedFileTypeException, MissingFilesException, InvalidFormatException {
        List<TimedSection> sections = null;
        Media media = null;
        try {
            sections = loadSectionsFromFolder(file);
            media = loadMediaFromFolder(file);
        } catch (FileNotFoundException e) {
            throw new MissingFilesException(e.getMessage());
        }

        return new Karaoke(title, media, sections);
    }

    private static File findMediaFile(Path path) {
        for (File file : path.toFile().listFiles()) {
            if (FileUtils.removeExtension(file.getName()).equals(MEDIA_FILE)) {
                return file;
            }
        }

        return null;
    }

    protected static Media loadMediaFromFolder(Path path) throws UnsupportedFileTypeException, FileNotFoundException {
        File mediaFile = findMediaFile(path);

        if (mediaFile == null || !mediaFile.exists()) {
            throw new FileNotFoundException("Media file not found");
        }

        String fileName = mediaFile.getName();

        if (!fileName.matches(VIDEO_PATTERN) && !fileName.matches(AUDIO_PATTERN)) {
            throw new UnsupportedFileTypeException("A Karaoke can only support video and audio files as its media.");
        }

        return new Media(mediaFile.toURI().toString());
    }

    public static List<TimedSection> loadSectionsFromFolder(Path path) throws FileNotFoundException, InvalidFormatException {
        File labelFile = Path.of(path.toString(), LABEL_FILE).toFile();

        if (!labelFile.exists()) {
            throw new FileNotFoundException("Label file not found");
        }

        List<String> lines = FileUtils.loadStringList(labelFile);

        if (lines.size() == 0) {
            return new ArrayList<>();
        }

        List<TimedSection> sections = new ArrayList<>();

        Pattern pattern = Pattern.compile(LABEL_PATTERN);
        Matcher matcher;

        // Initial parsing and validity check
        int lineIndex = 0;
        for (String line : lines) {
            matcher = pattern.matcher(line);

            if (!matcher.matches()) {
                throw new InvalidFormatException(String.format("Line of invalid format (%s) on line %d of file %s", line, lineIndex, labelFile.toString()));
            }

            float start = Float.parseFloat(matcher.group("start"));
            float end = Float.parseFloat(matcher.group("end"));
            String value = matcher.group("value").replace("\\n", "\n");

            sections.add(new TimedSection(start, end, value));
            lineIndex++;
        }

        //  Fix any continuity issue
        ListIterator<TimedSection> iterator = sections.listIterator();

        TimedSection current = iterator.next();
        while (iterator.hasNext()) {
            TimedSection next = iterator.next();

            if (current.getEndTime() != next.getStartTime()) {
                iterator.previous();
                iterator.add(new TimedSection(current.getEndTime(), next.getStartTime(), ""));
                iterator.next();
            }

            current = next;
        }

        return sections;
    }

    private void saveSectionsToFile(Path path) {
       saveSectionsToFile(path, this.getSections());
    }
    private static void saveSectionsToFile(Path path, List<TimedSection> sections) {
        List<String> lines = new ArrayList<String>(sections.size());
        for (TimedSection section : sections) {
            String formattedValue = StringEscapeUtils.escapeJava(section.getValue());
            String line = String.format(Locale.ROOT, SECTION_LINE_FORMAT, section.getStartTime(), section.getEndTime(), formattedValue);
            lines.add(line);
        }

        FileUtils.saveStringList(path.toFile(), lines);
    }

    private void saveMediaToFile(Path path) throws IOException {
        saveMediaToFile(path, this.getMedia());
    }
    private static void saveMediaToFile(Path path, Media media) throws IOException {
        if (media != null) {
            Path source = Path.of(URI.create(media.getSource()).getPath());

            try {
                Files.deleteIfExists(path);
                Files.copy(source, path);
            } catch (IOException e) {
                throw new IOException("Failed to copy media file to new location", e);
            }
        } else {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new IOException("Failed to create media file", e);
            }
        }
    }


    public static String sectionsToString(List<TimedSection> sections) {
        StringBuilder lyrics = new StringBuilder();

        for (TimedSection section : sections) {
            lyrics.append(section.getValue());
        }

        return lyrics.toString();
    }

        /*
    private String loadTitleFromFile(String path);{
        File file = new File(path);

        if (file.isDirectory()) {
            return file.getName();
        } else if (file.isFile() && file.getName().matches(FILE_PATTERN)) {
            return file.getName().replaceFirst("\\." + FILE_TYPE + "$", "");
        }

        return null;
    }
     */

    public String getTitle() {
        return title;
    }

    public Media getMedia() {
        return media;
    }
    public List<TimedSection> getSections() {
        return sections;
    }

    public String getLyrics() {
        return lyrics;
    }

    public Duration getDuration() {
        return media.getDuration();
    }

    ReadOnlyObjectProperty<Duration> durationProperty() {
        return media.durationProperty();
    }


}
