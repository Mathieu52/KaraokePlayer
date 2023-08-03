package net.xz3ra.www.karaokeplayer.util;

import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class TextLayoutCalculator {
    private static Text TEMP_TEXT = new Text();

    public static List<Rectangle2D> calculateTextBounds(Font font, TextAlignment textAlignX, VPos textAlignY, String text, double x, double y, double w, double h) {
        return calculateTextSectionBounds(font, textAlignX, textAlignY, text, x, y, w, h, 0, text.length());
    }

    public static List<Rectangle2D> calculateTextSectionBounds(Font font, TextAlignment textAlignX, VPos textAlignY, String text, double x, double y, double w, double h, double beginIndex) {
        return calculateTextSectionBounds(font, textAlignX, textAlignY, text, x, y, w, h, beginIndex, text.length());
    }

    public static List<Rectangle2D> calculateTextSectionBounds(Font font, TextAlignment textAlignX, VPos textAlignY, String text, double x, double y, double w, double h, double beginIndex, double endIndex) {
        List<String> lines = calculateTextLayout(font, text, w, h);

        List<Rectangle2D> bounds = new ArrayList<>();

        final double lineHeight = getStringHeight(font, " ");

        //final TextAlignment textAlignX = font.;
        //final VPos textAlignY = canvas.getTextOrigin(); // JavaFX does not support vertical text alignment in the same way as Processing

        int characterIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (endIndex <= characterIndex) {
                break;
            }

            String line = lines.get(i);

            // Use Text objects to calculate font metrics

            double lineWidth = getStringWidth(font, line);
            double lineLength = line.length();

            double offsetX = calculateAlignOffsetX(lineWidth, w, textAlignX);
            double offsetY = calculateAlignOffsetY(lineHeight, h, textAlignY);

            double relativeBeginIndex = beginIndex - characterIndex;
            double relativeEndIndex = endIndex - characterIndex;

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

                    Rectangle2D bound = new Rectangle2D(x + offsetX + boundStart, y + offsetY + lineHeight * i, boundWidth, lineHeight);
                    bounds.add(bound);
                }
            }

            characterIndex += lineLength + 1;
        }

        return bounds;
    }

    private static double getStringWidth(Font font, String str) {
        TEMP_TEXT = new Text(str);
        TEMP_TEXT.setFont(font);
        //TEMP_TEXT.setText(str);
        return TEMP_TEXT.getLayoutBounds().getWidth();
    }

    private static double getStringHeight(Font font, String str) {
        TEMP_TEXT.setFont(font);
        TEMP_TEXT.setText(str);
        return TEMP_TEXT.getLayoutBounds().getHeight();
    }

    private static double calculateAlignOffsetX(double width, double areaWidth, TextAlignment alignX) {
        return switch (alignX) {
            case CENTER -> (areaWidth - width) / 2.0;
            case RIGHT -> areaWidth - width;
            default -> 0;
        };
    }

    private static double calculateAlignOffsetY(double height, double areaHeight, VPos alignY) {
        return switch (alignY) {
            case CENTER -> (areaHeight - height) / 2.0;
            case TOP -> areaHeight - height;
            default -> 0;
        };
    }

    public static List<String> calculateTextLayout(Font font, String text, double w, double h) {
        List<String> lines = new ArrayList<>();

        final double lineHeight = font.getSize();

        if (lineHeight <= h) {

            final double textLeading = lineHeight;

            String[] words = text.split(" ");

            String str = "";
            double width = 0;
            double height = 0;

            final double spaceWidth = getStringWidth(font, " "); // You can adjust this value for the space width

            boolean exitSignal = false;

            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                double wordLength = getStringWidth(font, word);

                if (width + spaceWidth + wordLength >= w || word.contains("\n")) {
                    if (word.contains("\n")) {
                        String[] splitWord = word.split("\n");

                        lines.add(str + " " + splitWord[0]);

                        for (int j = 1; j < splitWord.length - 1; j++) {
                            lines.add(splitWord[j]);
                        }

                        word = splitWord[splitWord.length - 1];
                    } else {
                        lines.add(str);
                    }

                    width = 0;
                    str = "";

                    height += textLeading;

                    if (height + lineHeight > h) {
                        exitSignal = true;
                    }
                }

                if (exitSignal) {
                    break;
                }

                boolean addSpace = width == 0;

                str += (addSpace ? "" : " ") + word;
                width += (addSpace ? 0 : spaceWidth) + wordLength;
            }

            if (!exitSignal) {
                lines.add(str);
            }
        }

        return lines;
    }
}
