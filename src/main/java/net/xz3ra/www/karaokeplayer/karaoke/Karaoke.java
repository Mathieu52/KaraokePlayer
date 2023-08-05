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
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    private boolean isMusicOnly;

    private final List<TimedSection> sections;
    private final String lyrics;

    public Karaoke(String path) throws IOException, UnsupportedFileTypeException, MissingFilesException {
        Path file = Paths.get(path);

        if (!Files.exists(file)) {
            throw new FileNotFoundException("File: " + path + " does not exist");
        }
        if (!Files.isDirectory(file) && (Files.isRegularFile(file) && !file.getFileName().toString().matches(FILE_PATTERN))) {
            throw new UnsupportedFileTypeException("Karaoke can only be loaded from a directory or a ." + FILE_TYPE + " file");
        }

        title = loadTitle(file.toString());

        if (file.getFileName().toString().matches(FILE_PATTERN)) {
            ArchiveUtil.unzipFolder(file, Path.of(TEMPORARY_DIRECTORY));
            file = Path.of(TEMPORARY_DIRECTORY, title);
        }

        try {
            sections = loadSections(file);
            lyrics = generateLyrics(sections);
            media = loadMedia(file);
        } catch (FileNotFoundException e) {
           throw new MissingFilesException(e.getMessage());
        }
    }

    private File findMediaFile(Path path) {
        for (File file : path.toFile().listFiles()) {
            if (FileUtils.removeExtension(file.getName()).equals(MEDIA_FILE)) {
                return file;
            }
        }

        return null;
    }

    private Media loadMedia(Path path) throws UnsupportedFileTypeException, FileNotFoundException {
        File mediaFile = findMediaFile(path);

        if (mediaFile == null || !mediaFile.exists()) {
            throw new FileNotFoundException("Media file not found");
        }

        String fileName = mediaFile.getName();

        if (fileName.matches(VIDEO_PATTERN)) {
            isMusicOnly = false;
        } else if (fileName.matches(AUDIO_PATTERN)) {
            isMusicOnly = true;
        } else {
            throw new UnsupportedFileTypeException("A Karaoke can only support video and audio files as its media.");
        }

        return new Media(mediaFile.toURI().toString());
    }

    private List<TimedSection> loadSections(Path path) throws FileNotFoundException {
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

    private String generateLyrics(List<TimedSection> sections) {
        StringBuilder lyrics = new StringBuilder();

        for (TimedSection section : sections) {
            lyrics.append(section.getValue());
        }

        return lyrics.toString();
    }

    private String loadTitle(String path) {
        File file = new File(path);

        if (file.isDirectory()) {
            return file.getName();
        } else if (file.isFile() && file.getName().matches(FILE_PATTERN)) {
            return file.getName().replaceFirst("\\." + FILE_TYPE + "$", "");
        }

        return null;
    }

    public String getTitle() {
        return title;
    }

    public Media getMedia() {
        return media;
    }

    public boolean isMusicOnly() {
        return isMusicOnly;
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
