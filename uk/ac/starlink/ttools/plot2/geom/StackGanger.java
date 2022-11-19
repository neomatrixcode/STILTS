package uk.ac.starlink.ttools.plot2.geom;

import java.awt.Point;
import java.awt.Rectangle;
import uk.ac.starlink.ttools.plot2.Gang;
import uk.ac.starlink.ttools.plot2.Ganger;
import uk.ac.starlink.ttools.plot2.Padding;
import uk.ac.starlink.ttools.plot2.PlotPlacement;
import uk.ac.starlink.ttools.plot2.ShadeAxis;
import uk.ac.starlink.ttools.plot2.SurfaceFactory;
import uk.ac.starlink.ttools.plot2.ZoneContent;

/**
 * Ganger implementation for a vertically stacked gang of plots,
 * all sharing the same horizontal axis.
 *
 * @author   Mark Taylor
 * @since    25 Jan 2016
 */
public abstract class StackGanger<P,A> implements Ganger<P,A> {

    private final boolean isUp_;
    private final Padding padding_;

    /**
     * Constructor.
     * The supplied padding is currently applied outside each plot zone.
     * That's not the only way to do it; you could imagine wanting to
     * apply this padding outside the union of plot zones, or to
     * be able to supply different paddings for each zone.
     *
     * @param  isUp  true if zones are ordered upwards on the graphics plane,
     *               false if they go down
     * @param  padding  defines user preferences, if any, for space
     *                  reserved outside each plot zone
     */
    protected StackGanger( boolean isUp, Padding padding ) {
        isUp_ = isUp;
        padding_ = padding;
    }

    /**
     * Returns the data limits of the horizontal axis defined by a given aspect.
     *
     * @param  aspect  surface aspect
     * @return   2-element array giving (min,max) values of data coordinates
     *           on the horizontal axis
     */
    public abstract double[] getXLimits( A aspect );

    /**
     * Modifies an aspect object to give it fixed data limits on the
     * horizontal axis.
     *
     * @param  aspect   input surface aspect
     * @param  xmin    required lower limit on horizontal axis
     * @param  xmax    required upper limit on horizontal axis
     * @return   new aspect resembling input aspect but with supplied
     *           horizontal axis limits
     */
    public abstract A fixXLimits( A aspect, double xmin, double xmax );

    public Gang createGang( Rectangle[] zonePlotBounds ) {
        if ( zonePlotBounds.length > 0 ) {
            return new StackGang( zonePlotBounds );
        }
        else {
            throw new IllegalArgumentException( "no regions" );
        }
    }

    public Gang createGang( Rectangle gangExtBox,
                            SurfaceFactory<P,A> surfFact,
                            int nz, ZoneContent[] contents,
                            P[] profiles, A[] aspects,
                            ShadeAxis[] shadeAxes, boolean withScroll ) {
        if ( nz == 0 ) {
            throw new IllegalArgumentException( "no zones" );
        }
        int[] heights = new int[ nz ];
        for ( int iz = 0; iz < nz; iz++ ) {
            heights[ iz ] = gangExtBox.height / nz
                          + ( iz < gangExtBox.height % nz ? 1 : 0 );
        }
        Rectangle[] zboxes = new Rectangle[ nz ];
        int y = 0;
        for ( int iz = 0; iz < nz; iz++ ) {
            int h = heights[ iz ];
            Rectangle zoneExtBox =
                new Rectangle( gangExtBox.x,
                               isUp_ ? gangExtBox.height - y - h : y,
                               gangExtBox.width, h );
            y += h;
            ZoneContent content = contents[ iz ];
            zboxes[ iz ] =
                PlotPlacement
               .calculateDataBounds( zoneExtBox, padding_, surfFact,
                                     profiles[ iz ], aspects[ iz ],
                                     withScroll,
                                     content.getLegend(),
                                     content.getLegendPosition(),
                                     content.getTitle(), shadeAxes[ iz ] );
        }
        assert y == gangExtBox.height : y + " !=" + gangExtBox.height;
        int maxxlo = zboxes[ 0 ].x;
        int minxhi = zboxes[ 0 ].x + zboxes[ 0 ].width;
        for ( int iz = 1; iz < nz; iz++ ) {
            maxxlo = Math.max( maxxlo, zboxes[ iz ].x );
            minxhi = Math.min( minxhi, zboxes[ iz ].x + zboxes[ iz ].width );
        }
        for ( int iz = 0; iz < nz; iz++ ) {
            zboxes[ iz ].x = maxxlo;
            zboxes[ iz ].width = Math.max( minxhi - maxxlo, 10 );
        }
        return new StackGang( zboxes );
    }

    public Gang createApproxGang( Rectangle extBounds, int nz ) {
        int h = extBounds.height / nz;
        Rectangle[] boxes = new Rectangle[ nz ];
        for ( int iz = 0; iz < nz; iz++ ) {
            boxes[ iz ] =
               new Rectangle( extBounds.x,
                              extBounds.y + h * ( isUp_ ? nz - 1 - iz : iz ),
                              extBounds.width,
                              h );
        }
        return new StackGang( boxes );
    }

    public A[] adjustAspects( A[] aspects, int index ) {
        final double[] xlimits;
        if ( index >= 0 ) {
            xlimits = getXLimits( aspects[ index ] );
        }
        else {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for ( A aspect : aspects ) {
                double[] lims = getXLimits( aspect );
                min = Math.min( min, lims[ 0 ] );
                max = Math.max( max, lims[ 1 ] );
            }
            xlimits = min < max ? new double[] { min, max } : null;
        }
        if ( xlimits != null ) {
            A[] newAspects = aspects.clone();
            for ( int iz = 0; iz < aspects.length; iz++ ) {
                newAspects[ iz ] =
                    fixXLimits( aspects[ iz ], xlimits[ 0 ], xlimits[ 1 ] );
            }
            return newAspects;
        }
        else {
            return aspects;
        }
    }

    public P[] adjustProfiles( P[] profiles ) {
        return profiles;
    }

    /**
     * Gang implementation for vertical plot stacks.
     */
    private static class StackGang implements Gang {

        private final Rectangle[] zoneBoxes_;
 
        /**
         * Constructor.
         *
         * @param  zoneBoxes   per-zone plot bounds array  
         */
        StackGang( Rectangle[] zoneBoxes ) {
            zoneBoxes_ = zoneBoxes;
        }

        public int getZoneCount() {
            return zoneBoxes_.length;
        }

        public Rectangle getZonePlotBounds( int iz ) {
            return new Rectangle( zoneBoxes_[ iz ] );
        }

        /**
         * The returned zone is the one whose Y range the given point
         * falls within.  If none, the closest by Y is used.
         * So if it's off the top/bottom of the plot area,
         * the top/bottom zone is used.
         */
        public int getNavigationZoneIndex( Point pos ) {
            int y = pos.y;
            int minDist = Integer.MAX_VALUE;
            int izClosest = -1;
            for ( int iz = 0; iz < zoneBoxes_.length; iz++ ) {
                Rectangle box = zoneBoxes_[ iz ];
                int ylo = box.y;
                int yhi = box.y + box.height;
                if ( y >= ylo && y < yhi ) {
                    return iz;
                }
                int ydist = Math.min( Math.abs( y - ylo ),
                                      Math.abs( y - yhi ) );
                if ( ydist < minDist ) {
                    minDist = ydist;
                    izClosest = iz;
                }
            }
            return izClosest;
        }
    }
}
