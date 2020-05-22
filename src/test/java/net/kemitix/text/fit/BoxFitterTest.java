package net.kemitix.text.fit;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoxFitterTest
        implements WithAssertions {

    private final BoxFitter boxFitter = TextFit.fitter();
    private final Font font;
    private Function<Integer, Font> fontFactory;

    public BoxFitterTest() throws URISyntaxException, IOException, FontFormatException {
        URL resource = this.getClass().getResource("alice/Alice-Regular.ttf");
        int initialFontSize = 20;
        font = Font.createFont(Font.TRUETYPE_FONT, new File(resource.toURI()))
                .deriveFont(Font.PLAIN, initialFontSize);
        fontFactory = size -> font.deriveFont(Font.PLAIN, size);
    }

    @Nested
    @DisplayName("Single Box")
    public class SingleBox {

        private final int imageSize = 300;
        private final Graphics2D graphics2D = graphics(imageSize, imageSize);
        private Rectangle2D box = new Rectangle(imageSize, imageSize);

        @Test
        @DisplayName("Fit single words")
        public void fitSingleWord() {
            Map<String, Integer> wordMap = Map.of(
                    ".", 263,
                    "a", 263,
                    "Word", 110,
                    "longer", 96,
                    "extralongword", 43
            );
            wordMap.forEach((word, expectedSize) ->
                    assertThat(fit(word))
                            .as(word)
                            .isEqualTo(expectedSize));
        }

        @Test
        @DisplayName("Fit various lengths")
        public void fitVariousLengths() {
            Map<String, Integer> wordMap = Map.of(
                    ". .", 263,
                    "a a", 208,
                    "Another Word", 76,
                    longStringGenerator(1), 93,
                    longStringGenerator(2), 36,
                    longStringGenerator(3), 27,
                    longStringGenerator(4), 22,
                    longStringGenerator(5), 20,
                    longStringGenerator(100), 4,
                    longStringGenerator(196), 3
            );
            wordMap.forEach((word, expectedSize) ->
                    assertThat(fit(word))
                            .as(word)
                            .isEqualTo(expectedSize));
        }

        @Test
        @DisplayName("Text too long to fit throws and exception")
        // too long to fit means it would need to be rendered at a font size of <2
        public void tooLongThrows() {
            String longText = longStringGenerator(197);
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> fit(longText));
        }

        @Test
        @DisplayName("Long text can be fitted down to a font size of 3")
        public void veryLongFits() {
            String longText = longStringGenerator(196);
            assertThatCode(() -> fit(longText))
                    .doesNotThrowAnyException();
        }

        private int fit(String longText) {
            return boxFitter.fit(longText, fontFactory, graphics2D, box);
        }
    }

    @Nested
    @DisplayName("Overflow boxes")
    public class OverflowBoxes {

        private final int imageSize = 300;
        private final Graphics2D graphics2D = graphics(imageSize, imageSize);
        private Rectangle2D box = new Rectangle(imageSize, imageSize);
        private List<Rectangle2D> boxes = Arrays.asList(box, box);

        private int fit(String longText) {
            return boxFitter.fit(longText, fontFactory, graphics2D, boxes);
        }

        @Test
        @DisplayName("Text too long to fit single box - fits into two")
        public void tooLongThrows() {
            String longText = longStringGenerator(197);
            //TODO: should overflow into second box
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> fit(longText));
        }

    }

    private String longStringGenerator(int cycles) {
        String text = "This is a long piece of text that should result in an " +
                "attempt to render it at a font size on less than 2.";
        return "cycles: " + cycles + IntStream.range(0, cycles)
                .mapToObj(x -> "\n").collect(Collectors.joining(text));
    }

    private Graphics2D graphics(int width, int height) {
        return image(width, height)
                .createGraphics();
    }

    private BufferedImage image(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
}
