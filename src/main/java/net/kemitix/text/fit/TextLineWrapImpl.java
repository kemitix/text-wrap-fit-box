package net.kemitix.text.fit;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class TextLineWrapImpl implements WordWrapper {

    @Override
    public List<String> wrap(
            String text,
            Font font,
            Graphics2D graphics2D,
            int width
    ) {
        return wrap(text, font, graphics2D,
                Collections.singletonList(
                        new Rectangle(width, width)))
                .get(0);
    }

    @Override
    public List<List<String>> wrap(
            String text,
            Font font,
            Graphics2D graphics2D,
            List<Rectangle2D> boxes
    ) {
        String source = String.join(" ", text.split("\n"));
        List<Word> words = wordLengths(source.split(" "), font, graphics2D);
        return wrapWords(words, boxes);
    }

    private List<List<String>> wrapWords(
            List<Word> words,
            List<Rectangle2D> boxes
    ) {
        Rectangle2D rectangle2D = boxes.get(0);
        double width = rectangle2D.getWidth();
        List<String> lines = new ArrayList<>();
        int end = 0;
        List<String> line = new ArrayList<>();
        for (Word word : words) {
            if ((end + word.width) > width) {
                lines.add(String.join(" ", line));
                line.clear();
                end = 0;
            }
            line.add(word.word);
            end += word.width;
        }
        lines.add(String.join(" ", line));
        return Collections.singletonList(
                lines.stream()
                        .filter(l -> l.length() > 0)
                        .collect(Collectors.toList()));
    }

    private List<Word> wordLengths(String[] words, Font font, Graphics2D graphics2D) {
        FontRenderContext fontRenderContext = graphics2D.getFontRenderContext();
        return Arrays.stream(words)
                .map(word -> new Word(word, font, fontRenderContext))
                .collect(Collectors.toList());
    }

    private static class Word {
        private final String word;
        private final int width;

        public Word(String word, Font font, FontRenderContext fontRenderContext) {
            this.word = word;
            Rectangle2D stringBounds = font.getStringBounds(word + " ", fontRenderContext);
            this.width = Double.valueOf(stringBounds.getWidth()).intValue();
        }
    }

}
