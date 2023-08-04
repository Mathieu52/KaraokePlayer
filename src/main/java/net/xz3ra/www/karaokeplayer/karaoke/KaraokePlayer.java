package net.xz3ra.www.karaokeplayer.karaoke;

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.media.*;
import javafx.util.Duration;
import net.xz3ra.www.karaokeplayer.TimedSection;

import java.util.List;
import java.util.ListIterator;

import static net.xz3ra.www.karaokeplayer.util.SmoothingUtil.bezier1D;

public class KaraokePlayer {
    private final Karaoke karaoke;
    private final MediaPlayer mediaPlayer;

    private SimpleDoubleProperty lyricsIndexProperty = new SimpleDoubleProperty(0.0);
    private SimpleDoubleProperty activeParagraphIndexProperty = new SimpleDoubleProperty(0.0);
    private SimpleStringProperty activeParagraphProperty = new SimpleStringProperty("");


    public KaraokePlayer(Karaoke karaoke) {
        this.karaoke = karaoke;
        this.mediaPlayer = new MediaPlayer(karaoke.getMedia());
        initProperty();
    }

    private void initProperty() {
        currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                double lyricsIndex = getLyricsIndex(karaoke, newValue.toSeconds());
                lyricsIndexProperty.set(lyricsIndex);
                double activeParagraphIndex = getActiveParagraphIndex(karaoke, lyricsIndex);
                activeParagraphIndexProperty.set(activeParagraphIndex);
                String activeParagraph = getActiveParagraph(karaoke, lyricsIndex);
                activeParagraphProperty.set(activeParagraph);
            }
        });
    }

    public double getLyricsIndex() {
        return getLyricsIndex(karaoke, getCurrentTime().toSeconds());
    }

    public ReadOnlyDoubleProperty lyricsIndexProperty() {
        return lyricsIndexProperty;
    }
    public static double getLyricsIndex(Karaoke karaoke, double time) {

        List<TimedSection> sections = karaoke.getSections();
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < karaoke.getSections().size(); i++) {
            TimedSection current = sections.get(i);
            if (time >= current.getStartTime() && time <= current.getEndTime()) {

                double domain = current.getEndTime() - current.getStartTime();

                if (domain == 0 || current.getValue().length() == 0) {
                    return text.length();
                }

                double t = (time - current.getStartTime()) / domain;

                ListIterator<TimedSection> iterator = sections.listIterator(i);
                TimedSection previous;
                do {
                    if (!iterator.hasPrevious()) {
                        previous = null;
                        break;
                    }
                    previous = iterator.previous();
                } while (previous.getStartTime() == previous.getEndTime());

                iterator = sections.listIterator(i);
                TimedSection next;
                do {
                    if (!iterator.hasNext()) {
                        next = null;
                        break;
                    }
                    next = iterator.next();
                } while (next.getStartTime() == next.getEndTime());


                double currentSlope = current.calculateSlope();
                double previousSlope = previous == null ? currentSlope : previous.calculateSlope();
                double nextSlope = next == null ? currentSlope : next.calculateSlope();

                double startSlope = currentSlope - 0.5f * 0.5 * (previousSlope - currentSlope);
                double endSlope = currentSlope - 0.5f * 0.5 * (nextSlope - currentSlope);

                return bezier1D(text.length(), text.length() + current.getValue().length(), startSlope, endSlope, domain, t);
            }
            text.append(current.getValue());
        }

        return 0;
    }

    public double getActiveParagraphIndex() {
       return getActiveParagraphIndex(karaoke, getLyricsIndex());
    }

    public static double getActiveParagraphIndex(Karaoke karaoke, double index) {

        int characterCount = 0;
        for (String paragraph : karaoke.getLyrics().split("\n\n")) {
            float paragraphLength = paragraph.length() + 2;

            if (characterCount <= index && index < characterCount + paragraphLength) {
                return index - characterCount;
            }

            characterCount += paragraphLength;
        }

        return 0;
    }

    public ReadOnlyDoubleProperty activeParagraphIndexProperty() {
       return activeParagraphIndexProperty;
    }

    public String getActiveParagraph() {
        return getActiveParagraph(karaoke, getLyricsIndex());
    }
    public static String getActiveParagraph(Karaoke karaoke, double index) {

        int characterCount = 0;
        for (String paragraph : karaoke.getLyrics().split("\n\n")) {
            float paragraphLength = paragraph.length() + 2;

            if (characterCount <= index && index < characterCount + paragraphLength) {
                return paragraph;
            }

            characterCount += paragraphLength;
        }

        return "";
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public ReadOnlyStringProperty activeParagraphProperty() {
        return activeParagraphProperty;
    }

    public AudioEqualizer getAudioEqualizer() {
        return mediaPlayer.getAudioEqualizer();
    }

    public MediaException getError() {
        return mediaPlayer.getError();
    }

    public ReadOnlyObjectProperty<MediaException> errorProperty() {
        return mediaPlayer.errorProperty();
    }

    public void setOnError(Runnable value) {
        mediaPlayer.setOnError(value);
    }

    public Runnable getOnError() {
        return mediaPlayer.getOnError();
    }

    public ObjectProperty<Runnable> onErrorProperty() {
        return mediaPlayer.onErrorProperty();
    }

    public Karaoke getKaraoke() {
        return karaoke;
    }

    public void setAutoPlay(boolean value) {
        mediaPlayer.setAutoPlay(value);
    }

    public boolean isAutoPlay() {
        return mediaPlayer.isAutoPlay();
    }

    public BooleanProperty autoPlayProperty() {
        return mediaPlayer.autoPlayProperty();
    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void setRate(double value) {
        mediaPlayer.setRate(value);
    }

    public double getRate() {
        return mediaPlayer.getRate();
    }

    public DoubleProperty rateProperty() {
        return mediaPlayer.rateProperty();
    }

    public double getCurrentRate() {
        return mediaPlayer.getCurrentRate();
    }

    public ReadOnlyDoubleProperty currentRateProperty() {
        return mediaPlayer.currentRateProperty();
    }

    public void setVolume(double value) {
        mediaPlayer.setVolume(value);
    }

    public double getVolume() {
        return mediaPlayer.getVolume();
    }

    public DoubleProperty volumeProperty() {
        return mediaPlayer.volumeProperty();
    }

    public void setBalance(double value) {
        mediaPlayer.setBalance(value);
    }

    public double getBalance() {
        return mediaPlayer.getBalance();
    }

    public DoubleProperty balanceProperty() {
        return mediaPlayer.balanceProperty();
    }

    public void setStartTime(Duration value) {
        mediaPlayer.setStartTime(value);
    }

    public Duration getStartTime() {
        return mediaPlayer.getStartTime();
    }

    public ObjectProperty<Duration> startTimeProperty() {
        return mediaPlayer.startTimeProperty();
    }

    public void setStopTime(Duration value) {
        mediaPlayer.setStopTime(value);
    }

    public Duration getStopTime() {
        return mediaPlayer.getStopTime();
    }

    public ObjectProperty<Duration> stopTimeProperty() {
        return mediaPlayer.stopTimeProperty();
    }

    public Duration getCycleDuration() {
        return mediaPlayer.getCycleDuration();
    }

    public ReadOnlyObjectProperty<Duration> cycleDurationProperty() {
        return mediaPlayer.cycleDurationProperty();
    }

    public Duration getTotalDuration() {
        return mediaPlayer.getTotalDuration();
    }

    public ReadOnlyObjectProperty<Duration> totalDurationProperty() {
        return mediaPlayer.totalDurationProperty();
    }

    public Duration getCurrentTime() {
        return mediaPlayer.getCurrentTime();
    }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return mediaPlayer.currentTimeProperty();
    }

    public void seek(Duration seekTime) {
        mediaPlayer.seek(seekTime);
    }

    public MediaPlayer.Status getStatus() {
        return mediaPlayer.getStatus();
    }

    public ReadOnlyObjectProperty<MediaPlayer.Status> statusProperty() {
        return mediaPlayer.statusProperty();
    }

    public Duration getBufferProgressTime() {
        return mediaPlayer.getBufferProgressTime();
    }

    public ReadOnlyObjectProperty<Duration> bufferProgressTimeProperty() {
        return mediaPlayer.bufferProgressTimeProperty();
    }

    public void setCycleCount(int value) {
        mediaPlayer.setCycleCount(value);
    }

    public int getCycleCount() {
        return mediaPlayer.getCycleCount();
    }

    public IntegerProperty cycleCountProperty() {
        return mediaPlayer.cycleCountProperty();
    }

    public int getCurrentCount() {
        return mediaPlayer.getCurrentCount();
    }

    public ReadOnlyIntegerProperty currentCountProperty() {
        return mediaPlayer.currentCountProperty();
    }

    public void setMute(boolean value) {
        mediaPlayer.setMute(value);
    }

    public boolean isMute() {
        return mediaPlayer.isMute();
    }

    public BooleanProperty muteProperty() {
        return mediaPlayer.muteProperty();
    }

    public void setOnMarker(EventHandler<MediaMarkerEvent> onMarker) {
        mediaPlayer.setOnMarker(onMarker);
    }

    public EventHandler<MediaMarkerEvent> getOnMarker() {
        return mediaPlayer.getOnMarker();
    }

    public ObjectProperty<EventHandler<MediaMarkerEvent>> onMarkerProperty() {
        return mediaPlayer.onMarkerProperty();
    }

    public void setOnEndOfMedia(Runnable value) {
        mediaPlayer.setOnEndOfMedia(value);
    }

    public Runnable getOnEndOfMedia() {
        return mediaPlayer.getOnEndOfMedia();
    }

    public ObjectProperty<Runnable> onEndOfMediaProperty() {
        return mediaPlayer.onEndOfMediaProperty();
    }

    public void setOnReady(Runnable value) {
        mediaPlayer.setOnReady(value);
    }

    public Runnable getOnReady() {
        return mediaPlayer.getOnReady();
    }

    public ObjectProperty<Runnable> onReadyProperty() {
        return mediaPlayer.onReadyProperty();
    }

    public void setOnPlaying(Runnable value) {
        mediaPlayer.setOnPlaying(value);
    }

    public Runnable getOnPlaying() {
        return mediaPlayer.getOnPlaying();
    }

    public ObjectProperty<Runnable> onPlayingProperty() {
        return mediaPlayer.onPlayingProperty();
    }

    public void setOnPaused(Runnable value) {
        mediaPlayer.setOnPaused(value);
    }

    public Runnable getOnPaused() {
        return mediaPlayer.getOnPaused();
    }

    public ObjectProperty<Runnable> onPausedProperty() {
        return mediaPlayer.onPausedProperty();
    }

    public void setOnStopped(Runnable value) {
        mediaPlayer.setOnStopped(value);
    }

    public Runnable getOnStopped() {
        return mediaPlayer.getOnStopped();
    }

    public ObjectProperty<Runnable> onStoppedProperty() {
        return mediaPlayer.onStoppedProperty();
    }

    public void setOnHalted(Runnable value) {
        mediaPlayer.setOnHalted(value);
    }

    public Runnable getOnHalted() {
        return mediaPlayer.getOnHalted();
    }

    public ObjectProperty<Runnable> onHaltedProperty() {
        return mediaPlayer.onHaltedProperty();
    }

    public void setOnRepeat(Runnable value) {
        mediaPlayer.setOnRepeat(value);
    }

    public Runnable getOnRepeat() {
        return mediaPlayer.getOnRepeat();
    }

    public ObjectProperty<Runnable> onRepeatProperty() {
        return mediaPlayer.onRepeatProperty();
    }

    public void setOnStalled(Runnable value) {
        mediaPlayer.setOnStalled(value);
    }

    public Runnable getOnStalled() {
        return mediaPlayer.getOnStalled();
    }

    public ObjectProperty<Runnable> onStalledProperty() {
        return mediaPlayer.onStalledProperty();
    }

    public void setAudioSpectrumNumBands(int value) {
        mediaPlayer.setAudioSpectrumNumBands(value);
    }

    public int getAudioSpectrumNumBands() {
        return mediaPlayer.getAudioSpectrumNumBands();
    }

    public IntegerProperty audioSpectrumNumBandsProperty() {
        return mediaPlayer.audioSpectrumNumBandsProperty();
    }

    public void setAudioSpectrumInterval(double value) {
        mediaPlayer.setAudioSpectrumInterval(value);
    }

    public double getAudioSpectrumInterval() {
        return mediaPlayer.getAudioSpectrumInterval();
    }

    public DoubleProperty audioSpectrumIntervalProperty() {
        return mediaPlayer.audioSpectrumIntervalProperty();
    }

    public void setAudioSpectrumThreshold(int value) {
        mediaPlayer.setAudioSpectrumThreshold(value);
    }

    public int getAudioSpectrumThreshold() {
        return mediaPlayer.getAudioSpectrumThreshold();
    }

    public IntegerProperty audioSpectrumThresholdProperty() {
        return mediaPlayer.audioSpectrumThresholdProperty();
    }

    public void setAudioSpectrumListener(AudioSpectrumListener listener) {
        mediaPlayer.setAudioSpectrumListener(listener);
    }

    public AudioSpectrumListener getAudioSpectrumListener() {
        return mediaPlayer.getAudioSpectrumListener();
    }

    public ObjectProperty<AudioSpectrumListener> audioSpectrumListenerProperty() {
        return mediaPlayer.audioSpectrumListenerProperty();
    }

    public void dispose() {
        mediaPlayer.dispose();
    }
}
