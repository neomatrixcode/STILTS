package uk.ac.starlink.ttools.filter;

import java.util.Iterator;
import uk.ac.starlink.table.StarTable;

/**
 * Filter for picking only the last few rows of a table.
 *
 * @author   Mark Taylor (Starlink)
 * @since    8 Mar 2005
 */
public class TailFilter extends BasicFilter {

    public TailFilter() {
        super( "tail", "<nrows>" );
    }

    protected String[] getDescriptionLines() {
        return new String[] {
            "<p>Include only the last <code>&lt;nrows&gt;</code> rows",
            "of the table.",
            "If the table has fewer than <code>&lt;nrows&gt;</code> rows",
            "then it will be unchanged.",
            "</p>",
        };
    }

    public ProcessingStep createStep( Iterator<String> argIt )
            throws ArgException {
        if ( argIt.hasNext() ) {
            String countStr = argIt.next();
            argIt.remove();
            long count;
            try {
                count = Long.parseLong( countStr );
            }
            catch ( NumberFormatException e ) {
                throw new ArgException( "Row count " + countStr + 
                                        " not numeric" );
            }
            if ( count < 0 ) {
                throw new ArgException( "Nrows must be >= 0" );
            }
            return new TailStep( count );
        }
        else {
            throw new ArgException( "No row count given" );
        }
    }

    private static class TailStep implements ProcessingStep {
        final long count_;

        TailStep( long count ) {
            count_ = count;
        }

        public StarTable wrap( StarTable base ) {
            return new TailTable( base, count_ );
        }
    }
}
