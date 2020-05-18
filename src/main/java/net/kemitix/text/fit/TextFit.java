package net.kemitix.text.fit;

public interface TextFit {
    static WordWrapper wrapper() {
        return new TextLineWrapImpl();
    }
}
