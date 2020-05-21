package net.kemitix.text.fit;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public interface WordWrapper {
    /**
     * Wraps the text using the font in the graphics context to fit within the
     * width.
     *
     * @param text the text to be line wrapped
     * @param font the font to calculate character widths from
     * @param graphics2D the context into which the font would be rendered
     * @param width the maximum width of each line in pixels
     * @return a list of the each line of text
     * @throws NotEnoughSpace if there are more than {@link Integer#MAX_VALUE}
     * lines - so not likely.
     */
    List<String> wrap(
            String text,
            Font font,
            Graphics2D graphics2D,
            int width
    );

    /**
     * Wraps the text using the font in the graphics context to fit within the
     * boxes, filling them in order.
     *
     * @param text the text to be line wrapped
     * @param font the font to calculate character widths from
     * @param graphics2D the context into which the font would be rendered
     * @param boxes the list of rectangles to fit each line within.
     * @return a list of the each line of text
     * @throws NotEnoughSpace if there are more lines than can be fitted in the
     * boxes provided.
     */
    List<List<String>> wrap(
            String text,
            Font font,
            Graphics2D graphics2D,
            List<Rectangle2D> boxes
    );
}
