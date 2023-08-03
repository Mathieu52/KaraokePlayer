package net.xz3ra.www.karaokeplayer.karaoke;

import javafx.animation.AnimationTimer;
import javafx.beans.NamedArg;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import net.xz3ra.www.karaokeplayer.util.TextLayoutCalculator;

import java.util.List;

public class KaraokeView extends StackPane {
    private Label highlightedLyricsLabel;
    private Label upcomingLyricsLabel;

    private Pane highlightedLyricsMask;
    private Pane upcomingLyricsMask;

    private Font font = new Font(35);

    private KaraokePlayer player;

    private long lastTime = 0;

    public KaraokeView() {
        super();
        initGraphics();
    }

    public KaraokeView(KaraokePlayer player) {
        this();
        setKaraokePlayer(player);
    }

    public void setKaraokePlayer(KaraokePlayer player) {
        this.player = player;

        highlightedLyricsLabel.textProperty().bind(player.activeParagraphProperty());
        upcomingLyricsLabel.textProperty().bind(player.activeParagraphProperty());
    }

    private void initGraphics() {
        highlightedLyricsLabel = new Label("");
        upcomingLyricsLabel = new Label("");

        highlightedLyricsLabel.setFont(font);
        upcomingLyricsLabel.setFont(font);

        highlightedLyricsMask = new Pane();
        upcomingLyricsMask = new Pane();

        highlightedLyricsLabel.setClip(highlightedLyricsMask);
        upcomingLyricsLabel.setClip(upcomingLyricsMask);

        highlightedLyricsLabel.setTextFill(Color.BLACK);
        upcomingLyricsLabel.setTextFill(Color.LIGHTGRAY);

        highlightedLyricsLabel.setWrapText(true);
        upcomingLyricsLabel.setWrapText(true);


        getChildren().addAll(highlightedLyricsLabel, upcomingLyricsLabel);

        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateLabels(player.getActiveParagraphIndex());
            }
        };

        animationTimer.start();
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);

        double fontSize = calculate(35.0, 800.0, width, 500.0, height);
        font = new Font(fontSize);
        highlightedLyricsLabel.setFont(font);
        upcomingLyricsLabel.setFont(font);
    }

    //private void resize()

    private void updateLabels(double index) {
        long time = System.currentTimeMillis();
        System.out.println(time - lastTime);
        lastTime = time;

        List<Rectangle2D> lyricsBounds = TextLayoutCalculator.calculateTextSectionBounds(font, TextAlignment.LEFT, VPos.BOTTOM, highlightedLyricsLabel.getText(), 0, 0, this.getWidth(), this.getHeight(), 0, index);
        List<Rectangle2D> maskBounds = TextLayoutCalculator.calculateTextSectionBounds(font, TextAlignment.LEFT, VPos.BOTTOM, upcomingLyricsLabel.getText(), 0, 0, this.getWidth(), this.getHeight(), index);

        highlightedLyricsMask.getChildren().clear();
        lyricsBounds.forEach(r -> highlightedLyricsMask.getChildren().add(new Rectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight())));
        upcomingLyricsMask.getChildren().clear();
        maskBounds.forEach(r -> upcomingLyricsMask.getChildren().add(new Rectangle(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight())));
    }

    public static double calculate(double size, double initialWidth, double realWidth, double initialHeight, double realHeight) {
        double ratio = Math.min(realWidth / initialWidth, realHeight / initialHeight);
        return size * ratio;
    }
}
