package net.kemitix.text.fit;

import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fit the text to a box by finding the best font size and word wrapping to fill
 * the space using a binary search.
 */
@RequiredArgsConstructor
class BoxFitterImpl implements BoxFitter {

    private final WordWrapper wordWrapper;

    @Override
    public int fit(
            String text,
            Function<Integer, Font> fontFactory,
            Graphics2D graphics2D,
            Rectangle2D box
    ) {
        int fit = fitMinMax(0, (int) box.getHeight(),
                new FitEnvironment(text, fontFactory, graphics2D, box));
        if (fit <= 2) {
            throw new IllegalArgumentException("The text is too long to fit");
        }
        return fit;
    }

    @Override
    public int fit(
            String text,
            Function<Integer, Font> fontFactory,
            Graphics2D graphics2D,
            List<Rectangle2D> box
    ) {
        return fit(text, fontFactory, graphics2D, box.get(0));
    }

    private Integer fitMinMax(
            int min,
            int max,
            FitEnvironment e
    ) {
        int mid = (max + min) / 2;
        if (mid == min){
            return mid;
        }
        Font font = e.getFont(mid);
        List<String> lines = wrapLines(font, e);
        List<Rectangle2D> lineSizes =
                lineSizes(font, lines, e.fontRenderContext());
        if (sumLineHeights(lineSizes) > e.boxHeight() ||
                maxLineWidth(lineSizes) > e.boxWidth()) {
            return fitMinMax(min, mid, e);
        }
        return fitMinMax(mid, max, e);
    }

    private List<String> wrapLines(
            Font font,
            FitEnvironment e
    ) {
        return wordWrapper.wrap(e.text, font, e.graphics2D, e.boxWidth());
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
        return lineSizes.stream().map(Rectangle2D::getHeight)
                .mapToInt(Double::intValue).sum();
    }

    private int maxLineWidth(List<Rectangle2D> lineSizes) {
        return lineSizes.stream().map(Rectangle2D::getWidth)
                .mapToInt(Double::intValue).max().orElse(0);
    }

    @RequiredArgsConstructor
    private static class FitEnvironment {
        private final String text;
        private final Function<Integer, Font> fontFactory;
        private final Graphics2D graphics2D;
        private final Rectangle2D box;

        public Font getFont(int size) {
            return fontFactory.apply(size);
        }
        public int boxWidth() {
            return (int) box.getWidth();
        }

        public int boxHeight() {
            return (int) box.getHeight();
        }

        public FontRenderContext fontRenderContext() {
            return graphics2D.getFontRenderContext();
        }
    }
}
