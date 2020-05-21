package net.kemitix.text.fit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WordTooLong extends RuntimeException {
    private final String longWord;
}
