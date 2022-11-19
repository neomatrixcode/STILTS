package uk.ac.starlink.ttools.filter;

import java.io.IOException;
import uk.ac.starlink.table.EmptyRowSequence;
import uk.ac.starlink.table.RowAccess;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.RowSplittable;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.table.WrapperRowAccess;
import uk.ac.starlink.table.WrapperRowSequence;
import uk.ac.starlink.table.WrapperStarTable;

/**
 * Wrapper table which repeats the rows of the base table multiple times.
 *
 * @author   Mark Taylor
 * @since    5 Aug 2010
 */
public class RepeatTable extends WrapperStarTable {

    private final long count_;
    private final boolean byrow_;

    /**
     * Constructor.
     *
     * @param  base  base table
     * @param  count  number of repeats
     * @param  byrow  if false, input table with rows 123 gives output table
     *                123123; if true, it's 112233 
     */
    public RepeatTable( StarTable base, long count, boolean byrow ) {
        super( base );
        count_ = count;
        byrow_ = byrow;
    }

    public long getRowCount() {
        long baseNrow = super.getRowCount();
        return baseNrow >= 0 ? count_ * baseNrow
                             : baseNrow;
    }

    public Object getCell( long irow, int icol ) throws IOException {
        return super.getCell( getBaseRow( irow ), icol );
    }

    public Object[] getRow( long irow ) throws IOException {
        return super.getRow( getBaseRow( irow ) );
    }

    public RowAccess getRowAccess() throws IOException {
        return new WrapperRowAccess( super.getRowAccess() ) {
            @Override
            public void setRowIndex( long irow ) throws IOException {
                super.setRowIndex( getBaseRow( irow ) );
            }
        };
    }

    public RowSequence getRowSequence() throws IOException {
        if ( byrow_ ) {
            return new WrapperRowSequence( super.getRowSequence() ) {
                private long iseq_ = 0;
                @Override
                public boolean next() throws IOException {
                    boolean hasNext = iseq_ > 0 || super.next();
                    if ( hasNext ) {
                        iseq_ = ( iseq_ + 1 ) % count_;
                    }
                    return hasNext;
                }
            };
        }
        else {
            return new RowSequence() {
                private long remaining_ = count_;
                private RowSequence rseq_ = EmptyRowSequence.getInstance();

                public boolean next() throws IOException {
                    if ( rseq_.next() ) {
                        return true;
                    }
                    else if ( remaining_ > 0 ) {
                        remaining_--;
                        rseq_.close();
                        rseq_ = getBaseTable().getRowSequence();
                        return rseq_.next();
                    }
                    else {
                        return false;
                    }
                }

                public Object getCell( int icol ) throws IOException {
                    return rseq_.getCell( icol );
                }

                public Object[] getRow() throws IOException {
                    return rseq_.getRow();
                }

                public void close() throws IOException {
                    rseq_.close();
                }
            };
        }
    }

    public RowSplittable getRowSplittable() throws IOException {
        return Tables.getDefaultRowSplittable( this );
    }

    /**
     * Returns the row in the base table corresponding to a row in the
     * current table.  Only works for random-access tables.
     *
     * @param  irow  current table row index
     * @return   base table row index
     */
    private long getBaseRow( long irow ) {
        long baseNrow = super.getRowCount();
        if ( baseNrow < 0 ) {
            throw new UnsupportedOperationException( "Not random access" );
        }
        else if ( baseNrow == 0 ) {
            throw new IllegalArgumentException( "Table has no rows" );
        }
        else {
            if ( irow / baseNrow < count_ ) {
                return byrow_ ? irow / count_
                              : irow % baseNrow;
            }
            else {
                throw new IllegalArgumentException( "No such row " + irow );
            }
        }
    }
}
