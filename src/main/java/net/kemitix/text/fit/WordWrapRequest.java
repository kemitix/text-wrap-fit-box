package net.kemitix.text.fit;

import lombok.Builder;
import lombok.With;

import java.awt.*;

@With
@Builder
public class WordWrapRequest {
    String text;
    Font font;
    Graphics2D graphics2D;
    int width;

}
