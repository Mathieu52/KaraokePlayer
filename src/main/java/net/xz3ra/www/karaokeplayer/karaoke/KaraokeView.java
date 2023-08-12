package net.xz3ra.www.karaokeplayer.karaoke;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import net.xz3ra.www.karaokeplayer.util.TextLayoutCalculator;

import java.util.List;

import static net.xz3ra.www.karaokeplayer.util.RelativeUITools.calculate;

public class KaraokeView extends StackPane {
    public static final double DEFAULT_RELATIVE_FONT_SIZE = 35;
    public static final Paint DEFAULT_HIGHLIGHT_FILL = Color.BLACK;
    public static final Paint DEFAULT_UPCOMING_FILL = new Color(0, 0, 0, 0.2);

    private KaraokePlayer player;

    private final MediaView mediaView;
    private final Label highlightedLyricsLabel;
    private final Label upcomingLyricsLabel;

    private final Pane highlightedLyricsMask;
    private final Pane upcomingLyricsMask;

    private Font font;
    private final SimpleObjectProperty<Font> relativeFont = new SimpleObjectProperty<>();

    public KaraokeView() {
        super();
        setRelativeFont(new Font(DEFAULT_RELATIVE_FONT_SIZE));
        font = generateFont();

        mediaView = new MediaView();

        highlightedLyricsLabel = new Label("");
        upcomingLyricsLabel = new Label("");

        highlightedLyricsMask = new Pane();
        upcomingLyricsMask = new Pane();

        initGraphics();
    }

    public KaraokeView(KaraokePlayer player) {
        this();
        setKaraokePlayer(player);
    }

    public void setKaraokePlayer(KaraokePlayer player) {
        if (player != null) {
            this.player = player;

            if (player.getStatus() != null && player.getStatus() != MediaPlayer.Status.UNKNOWN) {
                initBindings();
            }

            player.statusProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue == MediaPlayer.Status.READY) {
                    initBindings();
                }
            }));

            mediaView.setMediaPlayer(player.getMediaPlayer());
        } else {
            throw new NullPointerException("A null player was passed to KaraokeView");
        }
    }

    protected void initBindings() {
        highlightedLyricsLabel.textProperty().bind(player.activeParagraphProperty());
        upcomingLyricsLabel.textProperty().bind(player.activeParagraphProperty());

        relativeFontProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                font = generateFont();
                updateLabels(player.getActiveParagraphIndex());
            }
        }));
    }

    private void initGraphics() {
        highlightedLyricsLabel.setFont(font);
        upcomingLyricsLabel.setFont(font);

        highlightedLyricsLabel.setClip(highlightedLyricsMask);
        upcomingLyricsLabel.setClip(upcomingLyricsMask);

        highlightedLyricsLabel.setTextFill(DEFAULT_HIGHLIGHT_FILL);
        upcomingLyricsLabel.setTextFill(DEFAULT_UPCOMING_FILL);

        highlightedLyricsLabel.setWrapText(true);
        upcomingLyricsLabel.setWrapText(true);

        highlightedLyricsLabel.setTextAlignment(TextAlignment.CENTER);
        upcomingLyricsLabel.setTextAlignment(TextAlignment.CENTER);

        mediaView.setPreserveRatio(true);
        mediaView.setManaged(false);
        mediaView.fitWidthProperty().bind(this.widthProperty());
        mediaView.fitHeightProperty().bind(this.heightProperty());

        getChildren().addAll(mediaView, highlightedLyricsLabel, upcomingLyricsLabel);

        highlightedLyricsLabel.toBack();
        upcomingLyricsLabel.toBack();
        mediaView.toBack();

        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (player != null) {
                    updateLabels(player.getActiveParagraphIndex());
                }
            }
        };

        animationTimer.start();
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);

        if (font != null && highlightedLyricsLabel != null && upcomingLyricsLabel != null) {
            font = generateFont(width, height);
            highlightedLyricsLabel.setFont(font);
            upcomingLyricsLabel.setFont(font);
        }

        if (player != null) {
            Platform.runLater(() -> updateLabels(player.getActiveParagraphIndex()));
        }
    }

    private Font generateFont() {
        return generateFont(this.getWidth(), this.getHeight());
    }

    private Font generateFont(double width, double height) {
        double fontSize = calculate(getRelativeFont().getSize(), 800.0, width, 500.0, height);
        return new Font(getRelativeFont().getName(), fontSize);
    }

    private void updateLabels(double index) {
        if (highlightedLyricsLabel != null  && upcomingLyricsLabel != null && highlightedLyricsMask != null && upcomingLyricsMask != null) {
            List<Rectangle2D> highlightedLabelBounds = TextLayoutCalculator.calculateLabeledBounds(highlightedLyricsLabel, 0, index);
            List<Rectangle2D> upcomingLabelBounds = TextLayoutCalculator.calculateLabeledBounds(upcomingLyricsLabel, index);

            highlightedLyricsMask.getChildren().clear();
            highlightedLabelBounds.forEach(r -> highlightedLyricsMask.getChildren().add(new Rectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight())));
            upcomingLyricsMask.getChildren().clear();
            upcomingLabelBounds.forEach(r -> upcomingLyricsMask.getChildren().add(new Rectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight())));
       }
    }

    public Font getRelativeFont() {
        return relativeFont.get();
    }

    public SimpleObjectProperty<Font> relativeFontProperty() {
        return relativeFont;
    }

    public void setRelativeFont(Font relativeFontProperty) {
        this.relativeFont.set(relativeFontProperty);
    }

    public Paint getHighlightTextFill() {
        return highlightedLyricsLabel.textFillProperty().get();
    }

    public ObjectProperty<Paint> highlightTextFillProperty() {
        return highlightedLyricsLabel.textFillProperty();
    }

    public void setHighlightTextFill(Paint highlightFill) {
        highlightedLyricsLabel.textFillProperty().set(highlightFill);
    }

    public Paint getUpcomingTextFill() {
        return upcomingLyricsLabel.textFillProperty().get();
    }

    public ObjectProperty<Paint> upcomingTextFillProperty() {
        return upcomingLyricsLabel.textFillProperty();
    }

    public void setUpcomingTextFill(Paint upcomingFill) {
        this.upcomingLyricsLabel.textFillProperty().set(upcomingFill);
    }
}
