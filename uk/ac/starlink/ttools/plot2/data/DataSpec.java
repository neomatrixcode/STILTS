package uk.ac.starlink.ttools.plot2.data;

import java.io.IOException;
import java.util.Iterator;
import uk.ac.starlink.table.DomainMapper;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.ttools.plot2.Equality;

/**
 * Defines the table-like data that is required for a certain plot.
 * This object does not store the data itself, but can be passed to a
 * {@link DataStore} object to obtain it.
 * A DataSpec has a small memory footprint, is cheap to produce,
 * and can be examined to determine what data is required.
 *
 * <p>The data specification is an aggregation of the following items:
 * </p>
 * <ul>
 * <li>The table the data comes from
 * <li>A list of the columns, or column-like objects, used from the table
 * <li>An identifier for a mask indicating which rows from the table will be
 *     included
 * </ul>
 *
 * <p>Two DataSpecs should evaluate equal if their specification of the
 * above items have the same content, that is if they would generate
 * the same {@link TupleSequence} when presented to a {@link DataStore}.
 *
 * @author   Mark Taylor
 * @since    7 Feb 2013
 */
@Equality
public interface DataSpec {
 
    /**
     * Returns the table object from which this data spec's data is obtained.
     *
     * @return   data source table
     */
    StarTable getSourceTable();

    /**
     * Returns the number of columns that this object produces.
     *
     * @return  TupleSequence column count
     */
    int getCoordCount();

    /**
     * Returns an identifier for the row mask for this object.
     * Masks with the same identifier must mean the same thing,
     * and preferably vice versa.
     *
     * @return   mask identifier
     */
    String getMaskId();

    /**
     * Returns an identifier for one of the the columns produced by this object.
     * Coords with the same identifier must mean the same thing,
     * and preferably vice versa.
     *
     * @param  icoord  column index
     * @return   column identifier
     */
    String getCoordId( int icoord );

    /**
     * Returns the coord reader that can read the data for one of this
     * object's output columns.
     * 
     * @param   icoord  column index
     * @return  column data reader
     */
    Coord getCoord( int icoord );

    /**
     * Returns the metadata, if known, for the values supplied by the user
     * to provide data for one of this object's output columns.
     *
     * @param  icoord  column index
     * @return  array of value infos for column data inputs;
     *          elements may be null if not known
     */
    ValueInfo[] getUserCoordInfos( int icoord );

    /**
     * Returns the domain mappers to use for the values supplied by the user
     * to provide data for one of this object's output columns.
     * These objects define how the input values as supplied by the user
     * are to be interpreted as the expected input values for this dataspec.
     * In many cases this mapping is trivial (for instance Y axis coordinate),
     * but in some (for instance absolute epoch) it may not be.
     *
     * @param  icoord  column index
     * @return   array of DomainMappers for column data inputs
     */
    DomainMapper[] getUserCoordMappers( int icoord );

    /**
     * Returns an object that can be used to read the mask and coordinate
     * values from a row sequence derived from this object's source table.
     * A given UserDataReader can only be used from a single thread,
     * but multiple returns from this method may be used concurrently
     * in different threads.
     *
     * @return   new data reader
     */
    UserDataReader createUserDataReader();

    /**
     * Indicates whether the mask flag specified by this object
     * is known always to be true.
     * If true is returned, then
     * {@link UserDataReader#getMaskFlag getUserDataReader().getMaskFlag}
     * will always return true.
     * False negatives are permitted; even if the result is false, the
     * mask may in fact always return true.
     *
     * @return  true if all rows are known to be included in the mask
     */
    boolean isMaskTrue();

    /**
     * Indicates whether the value for a given coord specified by this object
     * is known to have a constant, blank value in all cases.
     * Clients don't have to test this, since data stores will always dispense
     * the relevant blank value based on this data spec, and should do so
     * in an efficient manner, but it may be useful for clients to know
     * in advance that a column is blank all the way down.
     * False negatives are permitted: even if the result is false, the
     * column may in fact have all blank values.
     *
     * @param  icoord  column index
     * @return  true if all values in the column are always blank
     */
    boolean isCoordBlank( int icoord );
}
