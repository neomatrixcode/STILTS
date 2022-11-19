package uk.ac.starlink.ttools.plot2;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A navigator controls how user gestures affect a plot surface.
 * The methods in this interface have the option to return a modified
 * surface aspect in response to a user interface event.
 * If a non-null value is returned from one of the navigation methods,
 * the intention is that for the presented surface to be replaced immediately
 * by one resembling it with the returned aspect.  The intent of a
 * null return is that nothing happens (that user gesture has no effect).
 * 
 * @author   Mark Taylor
 * @since    4 Oct 2013
 */
public interface Navigator<A> {

    /**
     * Drag gesture.
     *
     * <p>Drag gestures typically indicate panning, and in this case should
     * preferably have the outcome that the same data position remains
     * under the cursor before and after the pan (from <code>origin</code>
     * to <code>evt.getPoint()</code>).
     *
     * @param   surface  initial plot surface
     * @param   pos    current mouse position
     * @param   ibutton  logical mouse button index of drag
     * @param   origin   starting point of the drag gesture
     * @return  navigation action indicated by the gesture,
     *          or null for no change
     */
    NavAction<A> drag( Surface surface, Point pos, int ibutton, Point origin );

    /**
     * Terminating drag gesture.  This method is invoked following a sequence
     * of drags when the mouse button has been released.
     *
     * @param   surface   initial plot surface
     * @param   pos     current mouse position
     * @param   ibutton   logical mouse button index of terminated drag
     * @param   origin   starting point of drag gesture
     * @return  navigation action indicated by the gesture,
     *          or null for no change
     */
    NavAction<A> endDrag( Surface surface, Point pos, int ibutton,
                          Point origin );

    /**
     * Mouse wheel gesture.
     *
     * <p>Wheel gestures usually indicate zooming, and in this case should
     * preferably have the outcome that the same data position remains
     * at the mouse position before and after the zoom.
     *
     * @param   surface  initial plot surface
     * @param   pos     current mouse position
     * @param   wheelrot   number of wheel rotation clicks
     * @return  navigation action indicated by the gesture,
     *          or null for no change
     */
    NavAction<A> wheel( Surface surface, Point pos, int wheelrot );

    /**
     * Mouse click gesture.
     *
     * <p>Note that other elements of the plotting system may intercept
     * some mouse clicks for other purposes, so the navigator may not
     * receive all clicks.  For instance the topcat plot window currently
     * intercepts button-1 clicks and interprets them as row selection
     * requests.  Typically this navigator method may only get invoked
     * for modified or non-button-1 clicks.
     *
     * <p>Implementation of this gesture may require identifying
     * a data position from a screen position,
     * which is not always trivial, for instance in a 3D plot one
     * graphics position maps to a line of data positions.
     * The <code>dposIt</code> argument can optionally
     * be supplied to cope with such instances.  If a data pos cannot be
     * determined, null is returned.  If <code>dposIt</code> is absent,
     * the method will run quickly.  If it's present, the method may or may
     * not run slowly by iterating over the data points.
     *
     * @param   surface   initial plot surface
     * @param   pos     current mouse position
     * @param   ibutton  logical mouse button index
     * @param   dposSupplier iterable over dataDimCount-element arrays
     *                  representing all the data space positions plotted,
     *                  or null
     * @return  navigation action indicated by the gesture,
     *          or null for no change
     */
    NavAction<A> click( Surface surface, Point pos, int ibutton,
                        Supplier<CoordSequence> dposSupplier );

    /**
     * Returns a description of the available navigation gestures and the
     * behaviour they cause when the mouse is positioned at a particular point.
     * The order of the returned list may be reflected in their presentation
     * to users, so it is generally a good idea to use a LinkedHashMap.
     *
     * @param   surface  plot surface
     * @param   pos  mouse position
     * @return   mapping of available gestures to short textual descriptions
     *           of their behaviour
     */
    Map<Gesture,String> getNavOptions( Surface surface, Point pos );
}
