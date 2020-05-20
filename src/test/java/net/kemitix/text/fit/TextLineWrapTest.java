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

public class TextLineWrapTest
        implements WithAssertions {

    private final WordWrapper textLineWrap = TextFit.wrapper();
    private final int imageSize = 300;
    private final int fontSize = 20;
    private final Graphics2D graphics2D = graphics(imageSize, imageSize);
    private final Font font;

    public TextLineWrapTest()
            throws FontFormatException,
            IOException,
            URISyntaxException {
        URL resource = this.getClass().getResource("alice/Alice-Regular.ttf");
        font = Font.createFont(Font.TRUETYPE_FONT, new File(resource.toURI()))
                .deriveFont(Font.PLAIN, fontSize);
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
        @DisplayName("Longer string fits on two lines")
        public void longerStringOnTwoLines() {
            assertThat(invoke(
                    "xxxxxxxxxxx xxxxxxxxxxxx " +
                            "xxxxxxxxxxx xxxxxxxxxxxx"))
                    .containsExactly(
                            "xxxxxxxxxxx xxxxxxxxxxxx",
                            "xxxxxxxxxxx xxxxxxxxxxxx");
        }

        @Test
        @DisplayName("Longer string fits on three lines")
        public void longerStringOnThreeLines() {
            assertThat(invoke(
                    "xxxxxxxxxxx xxxxxxxxxxxx " +
                            "xxxxxxxxxxx xxxxxxxxxxxx " +
                            "xxxxxxxxxxx xxxxxxxxxxxx"))
                    .containsExactly(
                            "xxxxxxxxxxx xxxxxxxxxxxx",
                            "xxxxxxxxxxx xxxxxxxxxxxx",
                            "xxxxxxxxxxx xxxxxxxxxxxx");
        }
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

        private List<List<String>> invoke(String in) {
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
                assertThat(invoke("")).containsExactly(Collections.emptyList());
            }

            @Test
            @DisplayName("Short string fits on one line")
            public void shortStringOnOneLine() {
                assertThat(invoke("x"))
                        .containsExactly(Collections.singletonList("x"));
            }

            @Test
            @DisplayName("Longer string fits on two lines")
            public void longerStringOnTwoLines() {
                assertThat(invoke(
                        "xxxxxxxxxxx xxxxxxxxxxxx " +
                                "xxxxxxxxxxx xxxxxxxxxxxx"))
                        .containsExactly(Arrays.asList(
                                "xxxxxxxxxxx xxxxxxxxxxxx",
                                "xxxxxxxxxxx xxxxxxxxxxxx"));
            }

            @Test
            @DisplayName("Longer string fits on three lines")
            public void longerStringOnThreeLines() {
                assertThat(invoke(
                        "xxxxxxxxxxx xxxxxxxxxxxx " +
                                "xxxxxxxxxxx xxxxxxxxxxxx " +
                                "xxxxxxxxxxx xxxxxxxxxxxx"))
                        .containsExactly(Arrays.asList(
                                "xxxxxxxxxxx xxxxxxxxxxxx",
                                "xxxxxxxxxxx xxxxxxxxxxxx",
                                "xxxxxxxxxxx xxxxxxxxxxxx"));
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

        }
    }
}