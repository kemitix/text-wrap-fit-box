package net.kemitix.text.fit;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.Function;

public interface BoxFitter {
    int fit(
            String text,
            Function<Integer, Font> fontFactory,
            Graphics2D graphics2D,
            Rectangle2D box
    );
}
