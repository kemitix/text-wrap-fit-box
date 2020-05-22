package net.kemitix.text.fit;

import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fit the text to a box by finding the best font size and word wrapping to fill
 * the space using a binary search.
 */
@RequiredArgsConstructor
class BoxFitterImpl implements BoxFitter {

    public static final int MAX_FONT_SIZE = 10_000;
    private final WordWrapper wordWrapper;

    @Override
    public int fit(
            String text,
            Function<Integer, Font> fontFactory,
            Graphics2D graphics2D,
            Rectangle2D box
    ) {
        return fit(text, fontFactory, graphics2D,
                Collections.singletonList(box));
    }

    @Override
    public int fit(
            String text,
            Function<Integer, Font> fontFactory,
            Graphics2D graphics2D,
            List<Rectangle2D> boxes
    ) {
        int fit = fitMinMax(0, MAX_FONT_SIZE,
                new FitEnvironment(text, fontFactory, graphics2D,
                        boxes));
        if (fit <= 2) {
            throw new IllegalArgumentException("The text is too long to fit");
        }
        return fit;
    }

    private Integer fitMinMax(
            int min,
            int max,
            FitEnvironment e
    ) {
        int mid = (max + min) / 2;
        if (mid == min && mid + 1 == max) {
            return mid;
        }
        Font font = e.getFont(mid);
        try {
            List<List<Rectangle2D>> linesPerBox = wrapLines(font, e);
            if (tooManyLines(linesPerBox, e.boxes)) {
                return fitMinMax(min, mid, e);
            }
        } catch (WordTooLong | NotEnoughSpace err) {
            return fitMinMax(min, mid, e);
        }
        return fitMinMax(mid, max, e);
    }

    private boolean tooManyLines(
            List<List<Rectangle2D>> linesPerBox,
            List<Rectangle2D> boxes
    ) {
        return StreamZipper.zip(linesPerBox, boxes,
                (stringBounds, box) ->
                        sumLineHeights(stringBounds) > box.getHeight()
        ).anyMatch(b -> b);
    }

    private List<List<Rectangle2D>> wrapLines(
            Font font,
            FitEnvironment e
    ) {
        return wordWrapper.wrap(e.text, font, e.graphics2D, e.boxes)
                .stream()
                .map(l -> lineSizes(font, l, e.fontRenderContext()))
                .collect(Collectors.toList());
    }

    private List<Rectangle2D> lineSizes(
            Font font,
            List<String> lines,
            FontRenderContext renderContext
    ) {
        return lines.stream()
                .map(line -> font.getStringBounds(line.strip(), renderContext))
                .collect(Collectors.toList());
    }

    private int sumLineHeights(List<Rectangle2D> lineSizes) {
        return lineSizes.stream()
                .map(Rectangle2D::getHeight)
                .mapToInt(Double::intValue)
                .sum();
    }

    @RequiredArgsConstructor
    private static class FitEnvironment {
        private final String text;
        private final Function<Integer, Font> fontFactory;
        private final Graphics2D graphics2D;
        private final List<Rectangle2D> boxes;

        public Font getFont(int size) {
            return fontFactory.apply(size);
        }

        public FontRenderContext fontRenderContext() {
            return graphics2D.getFontRenderContext();
        }
    }
}
