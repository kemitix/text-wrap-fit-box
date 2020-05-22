package net.kemitix.text.fit;

import lombok.Getter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.*;
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
                        new Rectangle(width, Integer.MAX_VALUE)))
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
        Deque<Word> wordQ = new ArrayDeque<>(words);
        List<List<String>> wrappings = boxes.stream()
                .map(rectangle2D -> {
                    double width = rectangle2D.getWidth();
                    double height = rectangle2D.getHeight();
                    List<String> lines = new ArrayList<>();
                    int bottom = 0;
                    int end = 0;
                    Deque<Word> lineQ = new ArrayDeque<>();
                    while (!wordQ.isEmpty()) {
                        Word word = wordQ.pop();
                        if ((bottom + word.height) > height) {
                            wordQ.add(word);
                            lineQ.forEach(wordQ::push);
                            return removeBlankLines(lines);
                        }
                        if (end == 0 && word.width > width) {
                            throw new WordTooLong(word.word);
                        }
                        if ((end + word.width) > width) {
                            lines.add(wordsAsString((Deque<Word>) lineQ));
                            lineQ.clear();
                            end = 0;
                            bottom += word.height;
                        }
                        lineQ.add(word);
                        end += word.width;
                    }
                    lines.add(wordsAsString(lineQ));
                    return removeBlankLines(lines);
                }).collect(Collectors.toList());
        if (wordQ.isEmpty()) {
            return wrappings;
        }
        throw new NotEnoughSpace(wordQ.size());
    }

    private List<String> removeBlankLines(List<String> lines) {
        return lines.stream()
                .filter(l -> l.length() > 0)
                .collect(Collectors.toList());
    }

    private String wordsAsString(Deque<Word> lineQ) {
        return lineQ.stream()
                .map(Word::getWord)
                .collect(Collectors.joining(" "));
    }

    private List<Word> wordLengths(String[] words, Font font, Graphics2D graphics2D) {
        FontRenderContext fontRenderContext = graphics2D.getFontRenderContext();
        return Arrays.stream(words)
                .map(word -> new Word(word, font, fontRenderContext))
                .collect(Collectors.toList());
    }

    private static class Word {
        @Getter
        private final String word;
        private final int width;
        private final int height;

        public Word(String word, Font font, FontRenderContext fontRenderContext) {
            this.word = word;
            Rectangle2D stringBounds = font.getStringBounds(word + " ", fontRenderContext);
            this.width = Double.valueOf(stringBounds.getWidth()).intValue();
            this.height = Double.valueOf(stringBounds.getHeight()).intValue();
        }
    }

}
