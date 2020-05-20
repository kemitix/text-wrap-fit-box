package net.kemitix.text.fit;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public interface WordWrapper {
    List<String> wrap(
            String text,
            Font font,
            Graphics2D graphics2D,
            int width
    );
    List<List<String>> wrap(
            String text,
            Font font,
            Graphics2D graphics2D,
            List<Rectangle2D> boxes
    );
}
