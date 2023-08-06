package net.xz3ra.www.karaokeplayer.karaoke;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.media.Media;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.TimedSection;
import net.xz3ra.www.karaokeplayer.exceptions.MissingFilesException;
import net.xz3ra.www.karaokeplayer.exceptions.UnsupportedFileTypeException;
import net.xz3ra.www.karaokeplayer.util.ArchiveUtil;
import net.xz3ra.www.karaokeplayer.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Karaoke {

    private static final String TEMPORARY_DIRECTORY = System.getProperty("java.io.tmpdir");
    public static final String FILE_TYPE = "skf";
    private static final String LABEL_PATTERN = "(?<start>\\d+.\\d+)\\t(?<end>\\d+.\\d+)\\t(?<value>.*)";
    private static final String VIDEO_PATTERN = ".*\\.(mp4|avi|mov|wmv|mkv|flv|mpg)$";
    private static final String AUDIO_PATTERN = ".*\\.(mp3|wav|ogg|flac|m4a)$";
    private static final String FILE_PATTERN = ".*\\." + FILE_TYPE + "$";

    private static final String LABEL_FILE = "label.txt";
    private static final String MEDIA_FILE = "media";

    private final String title;

    private final Media media;

    private final List<TimedSection> sections;
    private final String lyrics;
    private Karaoke(String title, Media media, List<TimedSection> sections) {
        this.title = title;
        this.media = media;
        this.sections = sections;
        this.lyrics = sectionsToString(sections);
    }

    public static Karaoke load(String path) throws UnsupportedFileTypeException, IOException, MissingFilesException {
        Path file = Paths.get(path);

        if (Files.isDirectory(file)) {
            return loadFromFolder(file.toString());
        }

        if (Files.isRegularFile(file) && isKaraokeFile(file)) {
            return loadFromKaraokeFile(file.toString());
        }

        throw new UnsupportedFileTypeException("File " + file + " is not supported");
    }

    public static Karaoke loadFromFolder(String path) throws IOException, UnsupportedFileTypeException, MissingFilesException {
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

    public static Karaoke loadFromKaraokeFile(String path) throws IOException, UnsupportedFileTypeException, MissingFilesException {
        Path file = Paths.get(path);

        if (!Files.exists(file)) {
            throw new FileNotFoundException("File: " + path + " does not exist");
        }
        if (Files.isDirectory(file) || !isKaraokeFile(file)) {
            throw new UnsupportedFileTypeException(path + " isn't a ." + FILE_TYPE + " file");
        }

        String title = file.getFileName().toString().replaceFirst("\\." + FILE_TYPE + "$", "");

        ArchiveUtil.unzipFolder(file, Path.of(TEMPORARY_DIRECTORY));
        file = Path.of(TEMPORARY_DIRECTORY, title);

        return getKaraoke(file, title);
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


    private static Karaoke getKaraoke(Path file, String title) throws UnsupportedFileTypeException, MissingFilesException {
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

    public static List<TimedSection> loadSectionsFromFolder(Path path) throws FileNotFoundException {
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
        for (String line : lines) {
            matcher = pattern.matcher(line);

            if (!matcher.matches()) {
                throw new RuntimeException("Invalid line : " + line);
            }

            float start = Float.parseFloat(matcher.group("start"));
            float end = Float.parseFloat(matcher.group("end"));
            String value = matcher.group("value").replace("\\n", "\n");

            sections.add(new TimedSection(start, end, value));
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
