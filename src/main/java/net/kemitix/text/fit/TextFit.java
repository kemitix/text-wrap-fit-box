package net.kemitix.text.fit;

public interface TextFit {
    static WordWrapper wrapper() {
        return new TextLineWrapImpl();
    }
    static BoxFitter fitter() {
        return new BoxFitterImpl(wrapper());
    }
}
