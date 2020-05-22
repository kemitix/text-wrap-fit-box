package net.kemitix.text.fit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotEnoughSpace extends RuntimeException {
    private final int excessWordCount;
}
