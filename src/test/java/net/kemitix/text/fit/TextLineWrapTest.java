package net.kemitix.text.fit;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TextLineWrapTest
        implements WithAssertions {

    private static final String WORD = "word";
    private static final String SPACE = " ";

    private final WordWrapper textLineWrap = TextFit.wrapper();
    private final int imageSize = 300;
    private final int fontSize = 20;
    private final Graphics2D graphics2D = graphics(imageSize, imageSize);
    private final Font font;
    private final int spaceWidth;
    private final int wordWidth;
    private final int wordsPerLine;
    private final int wordHeight;
    private final int linesPerBox;

    public TextLineWrapTest()
            throws FontFormatException,
            IOException,
            URISyntaxException {
        URL resource = this.getClass().getResource("alice/Alice-Regular.ttf");
        font = Font.createFont(Font.TRUETYPE_FONT, new File(resource.toURI()))
                .deriveFont(Font.PLAIN, fontSize);
        Rectangle2D spaceBounds = stringBounds(SPACE, font, graphics2D);
        Rectangle2D wordBounds = stringBounds(WORD, font, graphics2D);
        spaceWidth = (int) spaceBounds.getWidth();
        wordWidth = (int) wordBounds.getWidth();
        wordHeight = (int) wordBounds.getHeight();
        wordsPerLine = imageSize / (wordWidth + spaceWidth);
        linesPerBox = imageSize / wordHeight;
    }

    private Rectangle2D stringBounds(
            String string,
            Font font,
            Graphics2D graphics2D
    ) {
        return font.getStringBounds(string, graphics2D.getFontRenderContext());
    }

    @Nested
    @DisplayName("Single box")
    public class SingleBox {

        private List<String> invoke(String in) {
            return textLineWrap.wrap(in, font, graphics2D, imageSize);
        }

        @Test
        @DisplayName("Empty String give empty List")
        public void emptyStringEmptyList() {
            assertThat(invoke("")).isEmpty();
        }

        @Test
        @DisplayName("Short string fits on one line")
        public void shortStringOnOneLine() {
            assertThat(invoke("x")).containsExactly("x");
        }

        @Test
        @DisplayName("Fits max 'words' per line on one line")
        public void fitMaxWordsOneLine() {
            String input = words(wordsPerLine, WORD);
            assertThat(invoke(input)).containsExactly(input);
        }

        @Test
        @DisplayName("Wraps just over max 'words' per line onto two lines")
        public void wrapOneWordTooManyOntoTwoLines() {
            assertThat(invoke(words(wordsPerLine + 1, WORD)))
                    .containsExactly(
                            words(wordsPerLine, WORD),
                            WORD);
        }

        @Test
        @DisplayName("Wraps onto three lines")
        public void longerStringOnThreeLines() {
            String oneLinesWorthOfWords = words(wordsPerLine, WORD);
            assertThat(invoke(words(wordsPerLine + wordsPerLine + 1, WORD)))
                    .containsExactly(
                            oneLinesWorthOfWords,
                            oneLinesWorthOfWords,
                            WORD);
        }
    }

    private String words(int number, String word) {
        return IntStream.range(0, number)
                .mapToObj(i -> word + SPACE)
                .collect(Collectors.joining()).trim();
    }

    private Graphics2D graphics(int width, int height) {
        return image(width, height)
                .createGraphics();
    }

    private BufferedImage image(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Nested
    @DisplayName("Overflowing Boxes")
    public class OverflowingBoxes {

        private List<Rectangle2D> boxes = new ArrayList<>();

        private List<List<String>> wrap(String in) {
            return textLineWrap.wrap(in, font, graphics2D, boxes);
        }

        @Nested
        @DisplayName("Single box")
        public class SingleBox {

            @BeforeEach
            public void setUp() {
                boxes.add(new Rectangle(imageSize, imageSize));
            }

            @Test
            @DisplayName("Empty String give empty List")
            public void emptyStringEmptyList() {
                assertThat(wrap("")).containsExactly(Collections.emptyList());
            }

            @Test
            @DisplayName("Short string fits on one line")
            public void shortStringOnOneLine() {
                assertThat(wrap("x"))
                        .containsExactly(Collections.singletonList("x"));
            }

            @Test
            @DisplayName("Fits max 'words' per line on one line")
            public void fitMaxWordsOneLine() {
                String input = words(wordsPerLine, WORD);
                assertThat(wrap(input))
                        .containsExactly(Collections.singletonList(input));
            }

            @Test
            @DisplayName("Wraps just over max 'words' per line onto two lines")
            public void wrapOneWordTooManyOntoTwoLines() {
                String input = words(wordsPerLine + 1, WORD);
                assertThat(wrap(input)).containsExactly(Arrays.asList(
                        words(wordsPerLine, WORD),
                        WORD));
            }

            @Test
            @DisplayName("Wraps onto three lines")
            public void longerStringOnThreeLines() {
                String input = words(wordsPerLine + wordsPerLine + 1, WORD);
                String oneLinesWorthOfWords = words(wordsPerLine, WORD);
                assertThat(wrap(input)).containsExactly(Arrays.asList(
                        oneLinesWorthOfWords,
                        oneLinesWorthOfWords,
                        WORD));
            }

            @Test
            @DisplayName("Text fills the box")
            public void textFillsBox() {
                String lineOfWords = words(wordsPerLine, WORD);
                String words = words(linesPerBox, lineOfWords);
                assertThat(wrap(words).get(0))
                        .hasSize(linesPerBox)
                        .allSatisfy(line ->
                                assertThat(line).isEqualTo(lineOfWords));
            }

            @Test
            @DisplayName("Text overflows the box")
            public void tooManyLinesForBox() {
                String lineOfWords = words(wordsPerLine, WORD);
                assertThatExceptionOfType(IllegalArgumentException.class)
                        .isThrownBy(() ->
                                words(linesPerBox + 1, lineOfWords));
            }
        }

        @Nested
        @DisplayName("Two boxes of equal width")
        public class TwoEqualWidthBoxes {

            @BeforeEach
            public void setUp() {
                Rectangle box = new Rectangle(imageSize, imageSize);
                boxes.add(box);
                boxes.add(box);
            }

            // Check that even with two boxes the shorted strings are unaffected
            @Test
            @DisplayName("Empty String give empty List")
            public void emptyStringEmptyList() {
                assertThat(wrap("")).containsExactly(
                        Collections.emptyList(),
                        Collections.emptyList());
            }

            @Test
            @DisplayName("Short string fits on one line")
            public void shortStringOnOneLine() {
                assertThat(wrap("x"))
                        .containsExactly(
                                Collections.singletonList("x"),
                                Collections.emptyList());
            }

            @Test
            @DisplayName("Fits max 'words' per line on one line")
            public void fitMaxWordsOneLine() {
                String input = words(wordsPerLine, WORD);
                assertThat(wrap(input))
                        .containsExactly(
                                Collections.singletonList(input),
                                Collections.emptyList()                         );
            }

            @Test
            @DisplayName("Wraps just over max 'words' per line onto two lines")
            public void wrapOneWordTooManyOntoTwoLines() {
                String input = words(wordsPerLine + 1, WORD);
                assertThat(wrap(input)).containsExactly(Arrays.asList(
                        words(wordsPerLine, WORD),
                        WORD),
                        Collections.emptyList());
            }

            @Test
            @DisplayName("Wraps onto three lines")
            public void longerStringOnThreeLines() {
                String input = words(wordsPerLine + wordsPerLine + 1, WORD);
                String oneLinesWorthOfWords = words(wordsPerLine, WORD);
                assertThat(wrap(input)).containsExactly(Arrays.asList(
                        oneLinesWorthOfWords,
                        oneLinesWorthOfWords,
                        WORD),
                        Collections.emptyList());
            }

            @Test
            @DisplayName("Fit max lines into the first box")
            public void fitMaxLinesInFirstBox() {
                String oneLinesWorthOfWords = words(wordsPerLine, WORD);
                String input = words(linesPerBox, oneLinesWorthOfWords);
                List<List<String>> result = wrap(input);
                List<String> linesBoxOne = result.get(0);
                assertThat(linesBoxOne)
                        .hasSize(linesPerBox)
                        .allSatisfy(line ->
                                assertThat(line)
                                        .isEqualTo(oneLinesWorthOfWords));
            }

            @Test
            @DisplayName("Overflow just over max lines into the second box")
            public void overflowMaxPlusOneLinesIntoSecondBox() {
                String oneLinesWorthOfWords = words(wordsPerLine, WORD);
                String input = words(linesPerBox + 1, oneLinesWorthOfWords);
                List<List<String>> result = wrap(input);
                List<String> linesBoxOne = result.get(0);
                assertThat(linesBoxOne)
                        .hasSize(linesPerBox)
                        .allSatisfy(line ->
                                assertThat(line)
                                        .isEqualTo(oneLinesWorthOfWords));
                assertThat(result.get(1))
                        .containsExactly(oneLinesWorthOfWords);
            }
        }
    }
}