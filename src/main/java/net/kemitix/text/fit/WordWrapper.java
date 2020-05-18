package net.kemitix.text.fit;

import java.awt.*;
import java.util.List;

public interface WordWrapper {
    List<String> wrap(
            String text,
            Font font,
            Graphics2D graphics2D,
            int width
    );
}
