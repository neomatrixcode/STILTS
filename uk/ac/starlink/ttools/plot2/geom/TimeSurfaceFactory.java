package uk.ac.starlink.ttools.plot2.geom;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot2.Axis;
import uk.ac.starlink.ttools.plot2.Captioner;
import uk.ac.starlink.ttools.plot2.LabelledLine;
import uk.ac.starlink.ttools.plot2.Navigator;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.PlotMetric;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.Subrange;
import uk.ac.starlink.ttools.plot2.Surface;
import uk.ac.starlink.ttools.plot2.SurfaceFactory;
import uk.ac.starlink.ttools.plot2.config.BooleanConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.config.ConfigMeta;
import uk.ac.starlink.ttools.plot2.config.OptionConfigKey;
import uk.ac.starlink.ttools.plot2.config.StringConfigKey;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;
import uk.ac.starlink.ttools.plot2.config.SubrangeConfigKey;
import uk.ac.starlink.ttools.plot2.config.TimeConfigKey;
import uk.ac.starlink.ttools.plot2.data.DataStore;

/**
 * Surface factory for time plots.
 *
 * @author   Mark Taylor
 * @since    15 Aug 2013
 */
public class TimeSurfaceFactory
        implements SurfaceFactory<TimeSurfaceFactory.Profile,TimeAspect> {

    /** Config key for time axis lower bound, before subranging. */
    public static final ConfigKey<Double> TMIN_KEY =
        new TimeConfigKey(
            new ConfigMeta( "tmin", "Time Minimum" )
           .setShortDescription( "Lower limit on time axis" )
           .setXmlDescription( new String[] {
                "<p>Minimum value of the time coordinate plotted.",
                "This sets the value before any subranging is applied.",
                "If not supplied, the value is determined from the plotted",
                "data.",
                "</p>",
                TimeConfigKey.FORMAT_XML,
            } )
        );

    /** Config key for time axis upper bound, before subranging. */
    public static final ConfigKey<Double> TMAX_KEY =
        new TimeConfigKey(
            new ConfigMeta( "tmax", "Time Maximum" )
           .setShortDescription( "Upper limit on time axis" )
           .setXmlDescription( new String[] {
                "<p>Maximum value of the time coordinate plotted.",
                "This sets the value before any subranging is applied.",
                "If not supplied, the value is determined from the plotted",
                "data.",
                "</p>",
                TimeConfigKey.FORMAT_XML,
            } )
        );

    /** Config key for time axis subrange. */
    public static final ConfigKey<Subrange> TSUBRANGE_KEY =
        new SubrangeConfigKey( SubrangeConfigKey
                              .createAxisSubMeta( "t", "Time" ),
                               new Subrange(), -0.1, 1.1 );

    /** Config key for Y axis lower bound, before subranging. */
    public static final ConfigKey<Double> YMIN_KEY =
        PlaneSurfaceFactory.YMIN_KEY;

    /** Config key for Y axis upper bound, before subranging. */
    public static final ConfigKey<Double> YMAX_KEY =
        PlaneSurfaceFactory.YMAX_KEY;

    /** Config key for Y axis subrange. */
    public static final ConfigKey<Subrange> YSUBRANGE_KEY =
        PlaneSurfaceFactory.YSUBRANGE_KEY;

    /** Config key for Y axis log scale flag. */
    public static final ConfigKey<Boolean> YLOG_KEY =
        PlaneSurfaceFactory.YLOG_KEY;

    /** Config key for Y axis flip flag. */
    public static final ConfigKey<Boolean> YFLIP_KEY =
        PlaneSurfaceFactory.YFLIP_KEY;

    /** Config key for time axis text label. */
    public static final ConfigKey<String> TLABEL_KEY =
        new StringConfigKey(
            new ConfigMeta( "tlabel", "Time Label" )
           .setStringUsage( "<text>" )
           .setShortDescription( "Label for Time axis" )
           .setXmlDescription( new String[] {
                "<p>Gives a label to be used for annotating the Time axis.",
                "If not supplied no label will be drawn.",
                "</p>",
            } )
        , null );

    /** Config key for Y axis text label.*/
    public static final ConfigKey<String> YLABEL_KEY =
        PlaneSurfaceFactory.YLABEL_KEY;

    /** Config key to determine if grid lines are drawn. */
    public static final ConfigKey<Boolean> GRID_KEY =
        new BooleanConfigKey(
            new ConfigMeta( "grid", "Draw Grid" )
           .setShortDescription( "Draw grid lines?" )
           .setXmlDescription( new String[] {
                "<p>If true, grid lines are drawn on the plot",
                "at positions determined by the major tick marks.",
                "If false, they are absent.",
                "</p>",
            } )
        , false );

    /** Config key to control tick mark crowding on time axis. */
    public static final ConfigKey<Double> TCROWD_KEY =
        PlaneSurfaceFactory.createAxisCrowdKey( "Time" );

    /** Config key to control tick mark crowding on Y axis. */
    public static final ConfigKey<Double> YCROWD_KEY =
        PlaneSurfaceFactory.YCROWD_KEY;

    /** Config key to control time value formatting. */
    public static final ConfigKey<TimeFormat> TFORMAT_KEY =
        createTimeFormatKey();

    /** Default time axis extent in seconds when no range is known. */
    private static final double DFLT_TIME_RANGE_SEC = 2 * 365.25 * 24 * 60 * 60;

    private static final PlotMetric plotMetric_ = new TimePlotMetric();

    public Surface createSurface( Rectangle plotBounds, Profile profile,
                                  TimeAspect aspect ) {
        Profile p = profile;
        return TimeSurface
              .createSurface( plotBounds, aspect,
                              p.ylog_, p.yflip_, p.tlabel_, p.ylabel_,
                              p.captioner_, p.grid_, p.tformat_,
                              p.tcrowd_, p.ycrowd_, p.minor_, p.tannotate_ );
    }

    public ConfigKey<?>[] getProfileKeys() {
        List<ConfigKey<?>> list = new ArrayList<ConfigKey<?>>();
        list.addAll( Arrays.asList( new ConfigKey<?>[] {
            YLOG_KEY,
            YFLIP_KEY,
            TLABEL_KEY,
            YLABEL_KEY,
            GRID_KEY,
            TCROWD_KEY,
            YCROWD_KEY,
            TFORMAT_KEY,
            StyleKeys.MINOR_TICKS,
        } ) );
        list.addAll( Arrays.asList( StyleKeys.CAPTIONER.getKeys() ) );
        return list.toArray( new ConfigKey<?>[ 0 ] );
    }

    public Profile createProfile( ConfigMap config ) {
        boolean ylog = config.get( YLOG_KEY );
        boolean yflip = config.get( YFLIP_KEY );
        String tlabel = config.get( TLABEL_KEY );
        String ylabel = config.get( YLABEL_KEY );
        boolean grid = config.get( GRID_KEY );
        double tcrowd = config.get( TCROWD_KEY );
        double ycrowd = config.get( YCROWD_KEY );
        TimeFormat tformat = config.get( TFORMAT_KEY );
        boolean minor = config.get( StyleKeys.MINOR_TICKS );
        Captioner captioner = StyleKeys.CAPTIONER.createValue( config );
        return new Profile( ylog, yflip, tlabel, ylabel, captioner,
                            grid, tcrowd, ycrowd, tformat, minor, true );
    }

    public ConfigKey<?>[] getAspectKeys() {
        return new ConfigKey<?>[] {
            TMIN_KEY, TMAX_KEY, TSUBRANGE_KEY,
            YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
        };
    }

    public boolean useRanges( Profile profile, ConfigMap config ) {
        return createUnrangedAspect( profile, config ) == null;
    }

    public TimeAspect createAspect( Profile profile, ConfigMap config,
                                    Range[] ranges ) {
        TimeAspect unrangedAspect = createUnrangedAspect( profile, config );
        if ( unrangedAspect != null ) {
            return unrangedAspect;
        }
        else {

            /* Determine range on the Time axis.  We need to do this
             * in a customised way so that we end up with sensible ranges;
             * the off-the-shelf method from PlaneSurfaceFactory defaults
             * to a range of 1 (second) in case of no data. */
            Range trange = ranges == null ? new Range() : ranges[ 0 ];
            trange = new Range( trange );
            trange.limit( config.get( TMIN_KEY ), config.get( TMAX_KEY ) );
            double[] tr0 = trange.getBounds();
            double tlo0 = tr0[ 0 ];
            double thi0 = tr0[ 1 ];
            double nowUnixSec = System.currentTimeMillis() * 0.001;
            final double tlo1;
            final double thi1;
            if ( tlo0 < thi0 ) {
                tlo1 = tlo0;
                thi1 = thi0;
            }
            else if ( PlotUtil.isFinite(tlo0) && ! PlotUtil.isFinite(thi0) ) {
                tlo1 = tlo0;
                thi1 = nowUnixSec > tlo0 ? nowUnixSec
                                         : tlo0 + DFLT_TIME_RANGE_SEC;
            }
            else if ( ! PlotUtil.isFinite(tlo0) && PlotUtil.isFinite(thi0) ) {
                tlo1 = nowUnixSec < thi0 ? nowUnixSec
                                         : thi0 - DFLT_TIME_RANGE_SEC;
                thi1 = thi0;
            }
            else {
                tlo1 = nowUnixSec - DFLT_TIME_RANGE_SEC;
                thi1 = nowUnixSec;
            }
            double[] tlimits =
                PlotUtil.scaleRange( tlo1, thi1, config.get( TSUBRANGE_KEY ),
                                    false );

            /* Determine range on the Y axis using standard technique. */
            Range yrange = ranges == null ? new Range() : ranges[ 1 ];
            double[] ylimits =
                PlaneSurfaceFactory
               .getLimits( config, YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
                           profile.ylog_, yrange );
            return new TimeAspect( tlimits, ylimits );
        }
    }

    public ConfigMap getAspectConfig( Surface surf ) {
        return surf instanceof TimeSurface
             ? ((TimeSurface) surf).getAspectConfig()
             : new ConfigMap();
    }

    public Range[] readRanges( Profile profile, PlotLayer[] layers,
                               DataStore dataStore ) {
        boolean[] logFlags = new boolean[] { false, profile.getYLog() };
        Range[] ranges = new Range[] { new Range(), new Range() };
        PlotUtil.extendCoordinateRanges( layers, ranges, logFlags, true,
                                         dataStore );
        return ranges;
    }

    public ConfigKey<?>[] getNavigatorKeys() {
        return TimeNavigator.getConfigKeys();
    }

    public Navigator<TimeAspect> createNavigator( ConfigMap navConfig ) {
        return TimeNavigator.createNavigator( navConfig );
    }

    public PlotMetric getPlotMetric() {
        return plotMetric_;
    }

    /**
     * Attempts to determine an aspect value from profile and configuration,
     * but not ranging, information.  If not enough information is supplied,
     * null will be returned.
     *
     * @param  profile   config profile
     * @param  config  map which may contain additional range config info
     * @return  aspect, or null
     */
    private static TimeAspect createUnrangedAspect( Profile profile,
                                                    ConfigMap config ) {
        double[] tlimits =
            PlaneSurfaceFactory
           .getLimits( config, TMIN_KEY, TMAX_KEY, TSUBRANGE_KEY,
                       false, null );
        double[] ylimits =
            PlaneSurfaceFactory
           .getLimits( config, YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
                       profile.ylog_, null );
        return tlimits == null || ylimits == null
             ? null
             : new TimeAspect( tlimits, ylimits );
    }

    /**
     * Returns a config key used to select time display format.
     *
     * @return  config key
     */
    private static ConfigKey<TimeFormat> createTimeFormatKey() {
        TimeFormat[] formats = TimeFormat.getKnownFormats();
        ConfigMeta meta = new ConfigMeta( "tformat", "Time Format" );
        meta.setShortDescription( "Time display format" );
        meta.setXmlDescription( new String[] {
            "<p>Selects the way in which time values are represented",
            "when using them to label the time axis.",
            "</p>",
        } );
        OptionConfigKey<TimeFormat> key =
                new OptionConfigKey<TimeFormat>( meta, TimeFormat.class,
                                                 formats ) {
            private final double unixSec = 1331613420;
            private final double secPrecision = 60 * 60 * 4;
            public String valueToString( TimeFormat format ) {
                return format == null ? null
                                      : format.getFormatName().toLowerCase();
            } 
            public String getXmlDescription( TimeFormat format ) {
                return format.getFormatDescription()
                     + " (e.g. \""
                     + format.formatTime( unixSec, secPrecision )
                     + "\")";
            }
        };
        key.setOptionUsage();
        key.addOptionsXml();
        return key;
    }

    /**
     * PlotMetric implementation for time plot surface.
     */
    private static class TimePlotMetric implements PlotMetric {
        private static final LabelUnit[] SEC_UNITS = new LabelUnit[] {
            new LabelUnit( "\u03bcsec", 1e-6 ),
            new LabelUnit( "millisec", 1e-3 ),
            new LabelUnit( "sec", 1.0 ),
            new LabelUnit( "min", 60 ),
            new LabelUnit( "hour", 60 * 60 ),
            new LabelUnit( "day", 60 * 60 * 24 ),
            new LabelUnit( "year", 60 * 60 * 24 * 365.25 ),
        };
        public LabelledLine[] getMeasures( Surface surf,
                                           Point2D gp0, Point2D gp1 ) {
            final Axis[] axes;
            if ( surf instanceof TimeSurface ) {
                axes = ((TimeSurface) surf).getAxes();
            }
            else {
                return new LabelledLine[ 0 ];
            }
            Axis tAxis = axes[ 0 ];
            Axis yAxis = axes[ 1 ];
            double gx0 = gp0.getX();
            double gy0 = gp0.getY();
            double gx1 = gp1.getX();
            double gy1 = gp1.getY();
            double dt0 = tAxis.graphicsToData( gx0 );
            double dy0 = yAxis.graphicsToData( gy0 );
            double dt1 = tAxis.graphicsToData( gx1 );
            double dy1 = yAxis.graphicsToData( gy1 );
            double et =
                Math.max( Math.abs( tAxis.graphicsToData( gx0 + 1 ) - dt0 ),
                          Math.abs( tAxis.graphicsToData( gx1 + 1 ) - dt1 ) );
            double ey =
                Math.max( Math.abs( yAxis.graphicsToData( gy0 + 1 ) - dy0 ),
                          Math.abs( yAxis.graphicsToData( gy1 + 1 ) - dy1 ) );
            double dt01 = Math.abs( dt1 - dt0 );
            String tLabel = LabelUnit.formatValue( dt01, et, SEC_UNITS );
            String yLabel = PlotUtil.formatNumber( Math.abs( dy1 - dy0 ), ey );
            Point2D gp01 = new Point2D.Double( gx1, gy0 );
            return new LabelledLine[] {
                new LabelledLine( gp0, gp01, tLabel ),
                new LabelledLine( gp01, gp1, yLabel ),
            };
        }
    }

    /**
     * Profile class which defines fixed configuration items for a TimeSurface.
     * Instances of this class are usually obtained from the
     * {@link #createProfile createProfile} method.
     */
    public static class Profile {
        private final boolean ylog_;
        private final boolean yflip_;
        private final String tlabel_;
        private final String ylabel_;
        private final Captioner captioner_;
        private final boolean grid_;
        private final double tcrowd_;
        private final double ycrowd_;
        private final TimeFormat tformat_;
        private final boolean minor_;
        private final boolean tannotate_;

        /**
         * Constructor.
         *
         * @param  ylog   whether to use logarithmic scaling on Y axis
         * @param  yflip  whether to invert direction of Y axis
         * @param  tlabel text for labelling time axis
         * @param  ylabel  text for labelling Y axis
         * @param  captioner  text renderer for axis labels etc
         * @param  grid   whether to draw grid lines
         * @param  tcrowd  crowding factor for tick marks on time axis;
         *                 1 is normal
         * @param  ycrowd  crowding factor for tick marks on Y axis;
         *                 1 is normal
         * @param  tformat time labelling format
         * @param  minor   whether to draw minor ticks
         * @param  tannotate  whether to annotate time axis
         */
        public Profile( boolean ylog, boolean yflip,
                        String tlabel, String ylabel, Captioner captioner,
                        boolean grid, double tcrowd, double ycrowd,
                        TimeFormat tformat, boolean minor, boolean tannotate ) {
            ylog_ = ylog;
            yflip_ = yflip;
            tlabel_ = tlabel;
            ylabel_ = ylabel;
            captioner_ = captioner;
            grid_ = grid;
            tcrowd_ = tcrowd;
            ycrowd_ = ycrowd;
            tformat_ = tformat;
            minor_ = minor;
            tannotate_ = tannotate;
        }

        /**
         * Indicates whether Y axis is logarithmic.
         *
         * @return  true for Y logarithmic scaling, false for linear
         */
        public boolean getYLog() {
            return ylog_;
        }

        /**
         * Returns a new profile instance the same as this one,
         * except that the flag for whether to annotate the time axis
         * may be set.
         *
         * @param   tannotate  whether to annotate time axis
         */
        public Profile fixTimeAnnotation( boolean tannotate ) {
            return new Profile( ylog_, yflip_, tlabel_, ylabel_, captioner_,
                                grid_, tcrowd_, ycrowd_, tformat_, minor_,
                                tannotate );
        }
    }
}
