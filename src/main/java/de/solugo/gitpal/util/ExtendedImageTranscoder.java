package de.solugo.gitpal.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.keys.StringKey;

/**
 *
 * @author Frederic Kneier
 */
public class ExtendedImageTranscoder extends PNGTranscoder {

    public static final TranscodingHints.Key KEY_COLOR = new StringKey();

    @Override
    public void writeImage(final BufferedImage image, final TranscoderOutput to) throws TranscoderException {

        if (this.getTranscodingHints().containsKey(KEY_COLOR)) {
            final Graphics2D graphics = image.createGraphics();
            graphics.setComposite(AlphaComposite.SrcIn);
            graphics.setPaint(parseColor((String) this.getTranscodingHints().get(KEY_COLOR)));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        }

        super.writeImage(image, to);
    }

    private Color parseColor(final String value) {
        if (value.length() == 3 || value.length() == 4) {
            final int r = Integer.parseInt(value.substring(0, 1), 16) * 17;
            final int g = Integer.parseInt(value.substring(1, 2), 16) * 17;
            final int b = Integer.parseInt(value.substring(2, 3), 16) * 17;
            final int a;
            if (value.length() == 4) {
                a = Integer.parseInt(value.substring(3, 4), 16) * 17;
            } else {
                a = 0xFF;
            }
            return new Color(r, g, b, a);
        } else if (value.length() == 6 || value.length() == 8) {
            final int r = Integer.parseInt(value.substring(0, 2), 16);
            final int g = Integer.parseInt(value.substring(2, 4), 16);
            final int b = Integer.parseInt(value.substring(4, 6), 16);
            final int a;
            if (value.length() == 8) {
                a = Integer.parseInt(value.substring(6, 8), 16);
            } else {
                a = 0xFF;
            }
            return new Color(r, g, b, a);
        } else {
            return Color.decode(value);
        }
    }

}
