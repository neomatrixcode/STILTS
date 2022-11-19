package uk.ac.starlink.ttools.plot2.task;

import uk.ac.starlink.task.ChoiceParameter;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.ttools.plot2.DataGeom;
import uk.ac.starlink.ttools.plot2.Ganger;
import uk.ac.starlink.ttools.plot2.GangerFactory;
import uk.ac.starlink.ttools.plot2.Padding;
import uk.ac.starlink.ttools.plot2.PlotType;

/**
 * Aggregates some miscellaneous information required for a plot task
 * that may not be available until execution time.
 *
 * @author   Mark Taylor
 * @since    22 Aug 2014
 */
public abstract class PlotContext<P,A> {

    private final PlotType<P,A> plotType_;
    private final DataGeom[] exampleGeoms_;
    private final GangerFactory<P,A> gangerFact_;

    /**
     * Constructor.
     * Information that is not dependent on other variables
     * (environment, layer suffix) is specified here.
     *
     * @param  plotType  plot type
     * @param  exampleGeoms   example data geoms
     * @param  gangerFact   defines plot grouping
     */
    protected PlotContext( PlotType<P,A> plotType, DataGeom[] exampleGeoms,
                           GangerFactory<P,A> gangerFact ) {
        plotType_ = plotType;
        exampleGeoms_ = exampleGeoms;
        gangerFact_ = gangerFact;
    }

    /**
     * Returns the plot type.
     *
     * @return  plot type
     */
    public PlotType<P,A> getPlotType() {
        return plotType_;
    }

    /**
     * Returns a list of one or more DataGeom objects to be used for
     * example purposes.  These may be used to construct parameter
     * auto-documentation, which is needed in absence of an execution
     * environment.
     * The first item in the list is considered most important.
     *
     * @return  one or more example data geoms
     */
    public DataGeom[] getExampleGeoms() {
        return exampleGeoms_;
    }

    /**
     * Returns the ganger factory used by this context.
     *
     * @return  gangerFact 
     */
    public GangerFactory<P,A> getGangerFactory() {
        return gangerFact_;
    }

    /**
     * Returns an array of parameters associated with a particular layer
     * required for determining DataGeom at runtime.
     *
     * @param  layerSuffix  parameter suffix string identifying a plot layer
     * @return   list of zero or more parameters used for determining DataGeom
     */
    public abstract Parameter<?>[] getGeomParameters( String layerSuffix );

    /**
     * Returns the DataGeom to use for a given layer in the context of a
     * given execution environment.
     *
     * @param  env  execution environment
     * @param  layerSuffix  parameter suffix string identifying a plot layer
     * @return  datageom
     */
    public abstract DataGeom getGeom( Environment env, String layerSuffix )
            throws TaskException;

    /**
     * Constructs a PlotContext which allows per-layer choice between
     * those known by a given plot type.
     * The choice is offered (a per-layer parameter is present) even if
     * only a single DataGeom is known by the PlotType.
     * This might conceivably be useful,
     * in that it allows pluggable DataGeoms specified by classname.
     *
     * @param  plotType  plot type
     * @param  gangerFact    defines plot grouping
     * @return  standard plot context
     */
    public static <P,A> PlotContext<P,A>
            createStandardContext( final PlotType<P,A> plotType,
                                   GangerFactory<P,A> gangerFact ) {
        final DataGeom[] geoms = plotType.getPointDataGeoms();
        return new PlotContext<P,A>( plotType, geoms, gangerFact ) {

            public Parameter<?>[] getGeomParameters( String suffix ) {
                return new Parameter<?>[] { createGeomParameter( suffix ) };
            }

            public DataGeom getGeom( Environment env, String suffix )
                    throws TaskException {
                return new ParameterFinder<Parameter<DataGeom>>() {
                    public Parameter<DataGeom> createParameter( String sfix ) {
                        return createGeomParameter( sfix );
                    }
                }.getParameter( env, suffix ).objectValue( env );
            }

            /**
             * Creates a DataGeom selection parameter named with a given suffix.
             *
             * @param  suffix  layer suffix
             * @return  parameter
             */
            private Parameter<DataGeom> createGeomParameter( String suffix ) {
                return new DataGeomParameter( "geom" + suffix, geoms );
            }
        };
    }

    /**
     * Constructs a PlotContext which always uses a fixed given DataGeom.
     * No DataGeom-specific parameters are required or provided.
     *
     * @param  plotType  plot type
     * @param  geom   data geom used in all cases
     * @param  gangerFact  defines plot grouping
     * @return  fixed-geom plot context
     */
    public static <P,A> PlotContext<P,A>
            createFixedContext( final PlotType<P,A> plotType,
                                final DataGeom geom,
                                GangerFactory<P,A> gangerFact ) {
        return new PlotContext<P,A>( plotType, new DataGeom[] { geom },
                                     gangerFact ) {
            public Parameter<?>[] getGeomParameters( String suffix ) {
                return new Parameter<?>[ 0 ];
            }
            public DataGeom getGeom( Environment env, String suffix ) {
                return geom;
            }
        };
    }

    /**
     * Parameter used for choosing between DataGeoms.
     */
    private static class DataGeomParameter extends ChoiceParameter<DataGeom> {

        /**
         * Constructor.
         *
         * @param  name  parameter name
         * @param  geoms  list of known geom options;
         *                the first item is set as the parameter default
         */
        public DataGeomParameter( String name, DataGeom[] geoms ) {
            super( name, geoms );
            setDefaultOption( geoms[ 0 ] );
        }

        @Override
        public String stringifyOption( DataGeom geom ) {
            return geom.getVariantName();
        }
    }
}
