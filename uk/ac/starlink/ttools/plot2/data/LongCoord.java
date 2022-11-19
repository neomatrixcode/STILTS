package uk.ac.starlink.ttools.plot2.data;

import java.util.function.Function;
import uk.ac.starlink.table.DomainMapper;
import uk.ac.starlink.table.ValueInfo;

/**
 * Coord implementation for long values.
 *
 * @author   Mark Taylor
 * @since    30 Nov 2017
 */
public class LongCoord extends SingleCoord {

    private final Number badval_;

    /**
     * Constructor.
     *
     * @param   meta  input value metadata
     * @param   isRequired  true if this coordinate is required for plotting
     */
    public LongCoord( InputMeta meta, boolean isRequired ) {
        super( meta, isRequired, SimpleDomain.NUMERIC_DOMAIN,
               StorageType.LONG );
        badval_ = Long.MIN_VALUE;
    }

    public Function<Object[],Number> inputStorage( ValueInfo[] infos,
                                                   DomainMapper[] dms ) {
        return userValues -> {
            Object c = userValues[ 0 ];
            return c instanceof Number ? ((Number) c) : badval_;
        };
    }

    /**
     * Reads a long value from an appropriate field in a given Tuple.
     *
     * @param  tuple  tuple
     * @param  icol  index of field in tuple corresponding to this Coord
     * @return  value of long field
     */
    public long readLongCoord( Tuple tuple, int icol ) {
        return tuple.getLongValue( icol );
    }
}
