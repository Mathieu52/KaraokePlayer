package net.xz3ra.www.karaokeplayer.util;

import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextLayoutCalculator {
    private static Text TEMP_TEXT = new Text();

    //  ************** LABEL **************

    public static List<Rectangle2D> calculateLabeledBounds(Labeled labeled) {
        return calculateTextBounds(labeled.getFont(), labeled.getText(), labeled.getTextAlignment(), labeled.getLineSpacing(), labeled.getLayoutBounds());
    }

    public static List<Rectangle2D> calculateLabeledBounds(Labeled labeled, double beginIndex) {
        return calculateTextBounds(labeled.getFont(), labeled.getText(), labeled.getTextAlignment(), labeled.getLineSpacing(), labeled.getLayoutBounds(), beginIndex);
    }

    public static List<Rectangle2D> calculateLabeledBounds(Labeled labeled, double beginIndex, double endIndex) {
        return calculateTextBounds(labeled.getFont(), labeled.getText(), labeled.getTextAlignment(), labeled.getLineSpacing(), labeled.getLayoutBounds(), beginIndex, endIndex);
    }

    //  ************** TEXT **************
    public static List<Rectangle2D> calculateTextBounds(Text text) {
        return calculateTextBounds(text.getFont(), text.getText(), text.getTextAlignment(), text.getLineSpacing(), text.getLayoutBounds());
    }

    public static List<Rectangle2D> calculateTextBounds(Text text, double beginIndex) {
        return calculateTextBounds(text.getFont(), text.getText(), text.getTextAlignment(), text.getLineSpacing(), text.getLayoutBounds(), beginIndex);
    }

    public static List<Rectangle2D> calculateTextBounds(Text text, double beginIndex, double endIndex) {
        return calculateTextBounds(text.getFont(), text.getText(), text.getTextAlignment(), text.getLineSpacing(), text.getLayoutBounds(), beginIndex, endIndex);
    }

    //  ************** RAW **************
    protected static List<Rectangle2D> calculateTextBounds(Font font, String text, TextAlignment alignment, double lineSpacing, Bounds bounds) {
        if (text != null && !text.isEmpty()) {
            return calculateTextBounds(font, text, alignment, lineSpacing, bounds, 0, text.length());
        }
        return new ArrayList<>();
    }

    protected static List<Rectangle2D> calculateTextBounds(Font font, String text, TextAlignment alignment, double lineSpacing, Bounds bounds, double beginIndex) {
        if (text != null && !text.isEmpty()) {
            return calculateTextBounds(font, text, alignment, lineSpacing, bounds, beginIndex, text.length());
        }
        return new ArrayList<>();
    }

    protected static List<Rectangle2D> calculateTextBounds(Font font, String text, TextAlignment alignment, double lineSpacing, Bounds bounds, double beginIndex, double endIndex) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> lines = calculateTextLayout(font, text, bounds);

        List<Rectangle2D> linesBounds = new ArrayList<>();

        double height = 0;

        int characterIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (endIndex <= characterIndex) {
                break;
            }

            String line = lines.get(i);

            final double lineWidth = getStringWidth(font, line);
            final double lineHeight = getStringHeight(font, line);
            final double lineLength = line.length();

            final double alignmentOffset = calculateAlignmentOffset(lineWidth, bounds.getWidth(), alignment);

            final  double relativeBeginIndex = beginIndex - characterIndex;
            final double relativeEndIndex = endIndex - characterIndex;

            if (relativeBeginIndex < lineLength || relativeEndIndex < lineLength) {
                double clampedRelativeBeginIndex = Math.min(Math.max(relativeBeginIndex, 0), lineLength);
                double clampedRelativeEndIndex = Math.min(Math.max(relativeEndIndex, 0), lineLength);

                if (clampedRelativeBeginIndex != clampedRelativeEndIndex) {
                    double boundStartFloor = getStringWidth(font, line.substring(0, (int) Math.floor(clampedRelativeBeginIndex)));
                    double boundStartCeil = getStringWidth(font, line.substring(0, (int) Math.ceil(clampedRelativeBeginIndex)));

                    double boundWidthFloor = getStringWidth(font, line.substring(0, (int) Math.floor(clampedRelativeEndIndex)));
                    double boundWidthCeil = getStringWidth(font, line.substring(0, (int) Math.ceil(clampedRelativeEndIndex)));

                    // Lerp : a - (a - b) * t
                    double boundStart = boundStartFloor - (boundStartFloor - boundStartCeil) * (beginIndex - (int) beginIndex);
                    double boundWidth = boundWidthFloor - (boundWidthFloor - boundWidthCeil) * (endIndex - (int) endIndex);
                    boundWidth -= boundStart;

                    Rectangle2D bound = new Rectangle2D(alignmentOffset + boundStart, height, boundWidth, lineHeight + lineSpacing);

                    linesBounds.add(bound);
                }
            }

            height += lineHeight + lineSpacing;
            characterIndex += lineLength + 1;
        }

        return linesBounds;
    }

    private static double getStringWidth(Font font, String str) {
        TEMP_TEXT.setFont(font);
        TEMP_TEXT.setText(str);
        TEMP_TEXT.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);

        return TEMP_TEXT.getLayoutBounds().getWidth();
    }

    private static double getStringHeight(Font font, String str) {
        TEMP_TEXT.setFont(font);
        TEMP_TEXT.setText(str);
        TEMP_TEXT.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);

        return TEMP_TEXT.getLayoutBounds().getHeight();
    }

    private static double calculateAlignmentOffset(double width, double areaWidth, TextAlignment alignX) {
        return switch (alignX) {
            case CENTER -> (areaWidth - width) / 2.0;
            case RIGHT -> areaWidth - width;
            default -> 0;
        };
    }

    public static List<String> calculateTextLayout(Font font, String text, Bounds bounds) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        TEMP_TEXT.getLayoutBounds();
        List<String> lines = new ArrayList<>();

        String[] words = text.split(" ");

        StringBuilder str = new StringBuilder();
        boolean newLine = true;

        for (String s : words) {
            String word = s;

            if (getStringWidth(font,str + " " + word) > bounds.getWidth() || word.contains("\n")) {
                if (word.contains("\n")) {
                    String[] splitWord = word.split("\n");

                    lines.add(str + " " + splitWord[0]);

                    lines.addAll(Arrays.asList(splitWord).subList(1, splitWord.length - 1));

                    word = splitWord[splitWord.length - 1];
                } else if (!str.isEmpty()) {
                    lines.add(str.toString());
                }

                newLine = true;
                str = new StringBuilder();
            }

            str.append(newLine ? "" : " ").append(word);
            newLine = false;
        }

        lines.add(str.toString());

        return lines;
    }
}
