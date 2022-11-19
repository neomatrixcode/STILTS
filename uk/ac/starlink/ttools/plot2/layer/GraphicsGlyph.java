package uk.ac.starlink.ttools.plot2.layer;

import java.awt.Graphics;
import java.awt.Rectangle;
import uk.ac.starlink.ttools.plot2.Glyph;
import uk.ac.starlink.ttools.plot2.Pixer;

/**
 * Glyph implementation whose Pixer is generated by drawing onto a bitmap.
 * This provides an easy way to generate a Glyph, but the Pixer implementation
 * is typically inefficient, since it needs first to paint to, and then
 * to interrogate, a BufferedImage (GreyImage).
 *
 * @author  Mark Taylor
 * @since   27 Jan 2021
 */
public abstract class GraphicsGlyph implements Glyph {

    private final Rectangle bounds_;

    /**
     * Constructs a GraphicsGlyph that may cover pixels within a given
     * bounding rectangle.  For reasons of efficiency, the extent of this
     * rectangle should be kept as small as possible based on knowledge
     * of the size of the output painting and the eventual visible region.
     *
     * @param  bounds  maximum extent of the image that this glyph covers
     */
    public GraphicsGlyph( Rectangle bounds ) {
        bounds_ = bounds;
    }

    public Pixer createPixer( Rectangle clip ) {
        final Rectangle box = bounds_ == null
                            ? clip
                            : clip.intersection( bounds_ );
        if ( box.isEmpty() ) {
            return Pixers.EMPTY;
        }
        else {
            GreyImage gim = GreyImage.createGreyImage( box.width, box.height );
            Graphics g = gim.getImage().createGraphics();
            g.translate( -box.x, -box.y );
            paintGlyph( g );
            g.dispose();
            return Pixers.translate( gim.createPixer(), box.x, box.y );
        }
    }
}
