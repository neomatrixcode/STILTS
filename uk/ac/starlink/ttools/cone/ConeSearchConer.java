package uk.ac.starlink.ttools.cone;

import java.net.URL;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.task.BooleanParameter;
import uk.ac.starlink.task.ChoiceParameter;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.ParameterValueException;
import uk.ac.starlink.task.StringParameter;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.task.URLParameter;
import uk.ac.starlink.ttools.task.ContentCodingParameter;
import uk.ac.starlink.ttools.task.LineTableEnvironment;
import uk.ac.starlink.util.ContentCoding;
import uk.ac.starlink.vo.ConeSearch;
import uk.ac.starlink.vo.SiaFormatOption;
import uk.ac.starlink.vo.SiaVersion;

/**
 * Coner implementation which uses remote 
 * <a href="http://www.ivoa.net/Documents/latest/ConeSearch.html"
 *    >Cone Search</a> services or similar.
 *
 * @author   Mark Taylor
 * @since    10 Aug 2007
 */
public class ConeSearchConer implements Coner {

    private final URLParameter urlParam_;
    private final ChoiceParameter<String> verbParam_;
    private final ChoiceParameter<ServiceType> serviceParam_;
    private final BooleanParameter believeemptyParam_;
    private final ContentCodingParameter codingParam_;
    private final StringParameter formatParam_;
    private int nside_;
    private static final String BELIEVE_EMPTY_NAME = "emptyok";
    private static final String INCONSISTENT_EMPTY_ADVICE =
            BELIEVE_EMPTY_NAME + "=false";

    /**
     * Constructor.
     */
    public ConeSearchConer() {
        nside_ = -1;
        ServiceType[] serviceTypes = new ServiceType[] {
            new ConeServiceType(),
            new SsaServiceType(),
            new SiaServiceType( SiaVersion.V10 ),
            new SiaServiceType( SiaVersion.V20 ),
            new SiaServiceType( SiaVersion.V10 ) {
                @Override
                public String toString() {
                    return "sia";
                }
                @Override
                public String getDescription() {
                    return "alias for <code>" + super.toString() + "</code>";
                }
            },
        };

        urlParam_ = new URLParameter( "serviceurl" );
        urlParam_.setPrompt( "Base URL for query returning VOTable" );
        urlParam_.setDescription( new String[] {
            "<p>The base part of a URL which defines the queries to be made.",
            "Additional parameters will be appended to this using CGI syntax",
            "(\"<code>name=value</code>\", separated by '&amp;' characters).",
            "If this value does not end in either a '?' or a '&amp;',",
            "one will be added as appropriate.",
            "</p>",
            "<p>See <ref id='coneService'/> for discussion of how to locate",
            "service URLs corresponding to given datasets.",
            "</p>",
        } );

        serviceParam_ =
            new ChoiceParameter<ServiceType>( "servicetype", serviceTypes );
        serviceParam_.setPrompt( "Search service type" );
        StringBuffer typesDescrip = new StringBuffer();
        for ( int i = 0; i < serviceTypes.length; i++ ) {
            ServiceType stype = serviceTypes[ i ];
            typesDescrip.append( "<li>" )
                        .append( "<code>" )
                        .append( stype )
                        .append( "</code>:\n" )
                        .append( stype.getDescription() )
                        .append( "</li>" )
                        .append( "\n" );
        }
        serviceParam_.setDescription( new String[] {
            "<p>Selects the type of data access service to contact.",
            "Most commonly this will be the Cone Search service itself,",
            "but there are one or two other possibilities:",
            "<ul>",
            typesDescrip.toString(),
            "</ul>",
            "</p>",
        } );
        serviceParam_.setDefaultOption( serviceTypes[ 0 ] );

        verbParam_ =
            new ChoiceParameter<String>( "verb",
                                         new String[] { "1", "2", "3", } );
        verbParam_.setNullPermitted( true );
        verbParam_.setPrompt( "Verbosity level of search responses (1..3)" );
        verbParam_.setDescription( new String[] {
            "<p>Verbosity level of the tables returned by the query service.",
            "A value of 1 indicates the bare minimum and",
            "3 indicates all available information.",
            "</p>",
        } );

        believeemptyParam_ = new BooleanParameter( BELIEVE_EMPTY_NAME );
        believeemptyParam_.setBooleanDefault( true );
        believeemptyParam_.setPrompt( "Believe metadata from empty results?" );
        believeemptyParam_.setDescription( new String[] {
            "<p>Whether the table metadata which is returned from a search",
            "result with zero rows is to be believed.",
            "According to the spirit, though not the letter, of the",
            "cone search standard, a cone search service which returns no data",
            "ought nevertheless to return the correct column headings.",
            "Unfortunately this is not always the case.",
            "If this parameter is set <code>true</code>, it is assumed",
            "that the service behaves properly in this respect; if it does not",
            "an error may result.  In that case, set this parameter",
            "<code>false</code>.  A consequence of setting it false is that",
            "in the event of no results being returned, the task will",
            "return no table at all, rather than an empty one.",
            "</p>",
        } );

        codingParam_ = new ContentCodingParameter();

        formatParam_ = new StringParameter( "dataformat" );
        formatParam_.setPrompt( "Data format type for DAL outputs" );
        formatParam_.setNullPermitted( true );
        StringBuffer formatsDescrip = new StringBuffer();
        for ( int i = 0; i < serviceTypes.length; i++ ) {
            ServiceType stype = serviceTypes[ i ];
            formatsDescrip
               .append( "<li>" )
               .append( "<code>" )
               .append( serviceParam_.getName() )
               .append( "=" )
               .append( stype )
               .append( "</code>:\n" )
               .append( stype.getFormatDescription() )
               .append( "</li>" )
               .append( "\n" );
        }
        formatParam_.setDescription( new String[] {
            "<p>Indicates the format of data objects described in the",
            "returned table.",
            "The meaning of this is dependent on the value of the",
            "<code>" + serviceParam_.getName() + "</code>",
            "parameter:",
            "<ul>",
            formatsDescrip.toString(),
            "</ul>",
            "</p>",
        } );
    }

    /**
     * Returns "ICRS", which is the system defined to be used by the
     * Cone Search specification.
     */
    public String getSkySystem() {
        return "ICRS";
    }

    public Parameter<?>[] getParameters() {
        return new Parameter<?>[] {
            serviceParam_,
            urlParam_,
            verbParam_,
            formatParam_,
            believeemptyParam_,
            codingParam_,
        };
    }

    public void configureParams( Environment env, Parameter<?> srParam )
            throws TaskException {
        serviceParam_.objectValue( env ).configureParams( srParam );
    }

    public boolean useDistanceFilter( Environment env )
            throws TaskException {
        return serviceParam_.objectValue( env ).useDistanceFilter();
    }

    public ConeSearcher createSearcher( Environment env, boolean bestOnly )
            throws TaskException {
        ServiceType serviceType = serviceParam_.objectValue( env );
        URL url = urlParam_.objectValue( env );
        boolean believeEmpty = believeemptyParam_.booleanValue( env );
        StarTableFactory tfact = LineTableEnvironment.getTableFactory( env );
        ContentCoding coding = codingParam_.codingValue( env );
        return serviceType
              .createSearcher( env, url.toString(), believeEmpty, tfact,
                               coding );
    }

    public Coverage getCoverage( Environment env ) throws TaskException {
        ServiceType serviceType = serviceParam_.objectValue( env );
        URL url = urlParam_.objectValue( env );
        return serviceType.getCoverage( url, nside_ );
    }

    /**
     * Sets the NSIDE parameter for MOC coverage maps.
     * Defaults to -1, which means no settting (up to service).
     *
     * @param  nside  HEALPix NSIDE parameter for MOCs
     */
    public void setNside( int nside ) {
        nside_ = nside;
    }

    /**
     * Describes the type of DAL service which is the basis of the cone.
     */
    private abstract class ServiceType {
        private final String name_;

        /**
         * Constructor.
         *
         * @param  informal, short name
         */
        ServiceType( String name ) {
            name_ = name;
        }

        /**
         * Returns XML description of this service type.
         *
         * @return  description
         */
        abstract String getDescription();

        /**
         * Returns XML documentation of the use of the format parameter
         * for this service type.
         *
         * @return  formats info
         */
        abstract String getFormatDescription();

        /**
         * Provides this object with a chance to perform custom configuration
         * on general cone search parameters.
         *
         * @param  srParam   search radius parameter
         */
        abstract void configureParams( Parameter<?> srParam );

        /**
         * Indicates whether the result table should be subjected
         * to additional filtering to ensure that only rows in the
         * specified search radius are included in the final output.
         *
         * @return  true iff post-query filtering on distance is to be performed
         */
        abstract boolean useDistanceFilter();

        /**
         * Constructs a ConeSearcher instance suitable for this service type.
         *
         * @param  env  execution environment
         * @param  url  service URL
         * @param  believeEmpty  whether to take seriously metadata from
         *         zero-length tables
         * @param  tfact  table factory
         * @param  coding  controls HTTP-level byte stream compression;
         *                 implementations may choose to ignore this hint
         * @return  cone searcher object
         */
        abstract ConeSearcher createSearcher( Environment env, String url,
                                              boolean believeEmpty,
                                              StarTableFactory tfact,
                                              ContentCoding coding )
                throws TaskException;

        /**
         * Returns a coverage footprint for use with the service specified.
         *
         * @param  url  cone search service URL
         * @param  nside  MOC nside parameter
         * @return  coverage footprint, or null
         */
        abstract Coverage getCoverage( URL url, int nside );

        public String toString() {
            return name_;
        }

        /**
         * Utility method to parse the verbosity level parameter.
         *
         * @param  env  execution environment
         * @return  verbosity level
         */
        int getVerbosity( Environment env ) throws TaskException {
            String sverb = verbParam_.stringValue( env );
            if ( sverb == null ) {
                return -1;
            }
            else {
                try {
                    return Integer.parseInt( sverb );
                }
                catch ( NumberFormatException e ) {
                    assert false;
                    throw new ParameterValueException( verbParam_,
                                                       e.getMessage(), e );
                }
            }
        }
    }

    /**
     * ServiceType implementation for Cone Search protocol.
     */
    private class ConeServiceType extends ServiceType {
        ConeServiceType() {
            super( "cone" );
        }

        String getDescription() {
            return new StringBuffer()
               .append( "Cone Search protocol " )
               .append( "- returns a table of objects found " )
               .append( "near each location.\n" )
               .append( "See <webref url='" )
               .append( "http://www.ivoa.net/Documents/latest/ConeSearch.html" )
               .append( "'>Cone Search standard</webref>." )
               .toString();
        }

        String getFormatDescription() {
            return "not used";
        }

        public void configureParams( Parameter<?> srParam ) {
            srParam.setNullPermitted( false );
        }

        public boolean useDistanceFilter() {
            return true;
        }

        public ConeSearcher createSearcher( Environment env, String url,
                                            final boolean believeEmpty,
                                            StarTableFactory tfact,
                                            ContentCoding coding )
                throws TaskException {
            return new ServiceConeSearcher( new ConeSearch( url, coding ),
                                            getVerbosity( env ),
                                            believeEmpty, tfact ) {
                @Override
                protected String getInconsistentEmptyAdvice() {
                    return INCONSISTENT_EMPTY_ADVICE;
                }
            };
        }

        public Coverage getCoverage( URL url, int nside ) {
            return UrlMocCoverage.getServiceMoc( url, nside );
        }
    }

    /**
     * ServiceType implementation for Simple Image Access.
     */
    private class SiaServiceType extends ServiceType {
        private final SiaVersion version_;

        /**
         * Constructor.
         *
         * @param  version  version of the SIA protocol to use
         */
        SiaServiceType( SiaVersion version ) {
            super( "sia" + version.getMajorVersion() );
            version_ = version;
        }

        String getDescription() {
            return new StringBuffer()
               .append( "Simple Image Access protocol version " )
               .append( version_.getMajorVersion() )
               .append( " - returns a table of images near each location.\n" )
               .append( "See <webref url='" )
               .append( version_.getDocumentUrl() )
               .append( "'>SIA " )
               .append( version_ )
               .append( " standard</webref>." )
               .toString();
        }

        String getFormatDescription() {
            StringBuffer sbuf = new StringBuffer()
               .append( "gives the MIME type required for images/resources\n" )
               .append( "referenced in the output table,\n" )
               .append( "corresponding to the SIA FORMAT parameter.\n" )
               .append( "The special values " )
               .append( "\"<code>GRAPHIC</code>\" (all graphics formats) and " )
               .append( "\"<code>ALL</code>\" (no restriction)\n" )
               .append( "as defined by SIAv1 are also permissible.\n" );
            if ( version_.getMajorVersion() == 1 ) {
                sbuf.append( "For SIA version 1 only, this defaults to " )
                    .append( "<code>\"image/fits\"</code>." );
            }
            return sbuf.toString();
        }

        public void configureParams( Parameter<?> srParam ) {

            /* SIZE = 0 has a special meaning for SIA: it means any image
             * containing the given image.  This is a sensible default in 
             * most cases. */
            srParam.setStringDefault( "0" );
            srParam.setNullPermitted( false );
        }

        public boolean useDistanceFilter() {
            return false;
        }

        public ConeSearcher createSearcher( Environment env, String url,
                                            final boolean believeEmpty,
                                            StarTableFactory tfact,
                                            ContentCoding coding )
                throws TaskException {
            if ( version_.getMajorVersion() == 1 ) {
                formatParam_.setStringDefault( "image/fits" );
            }
            String formatTxt = formatParam_.stringValue( env );
            SiaFormatOption format = SiaFormatOption.fromObject( formatTxt );
            return new SiaConeSearcher( url, version_, format, believeEmpty,
                                        tfact, coding ) {
                @Override
                protected String getInconsistentEmptyAdvice() {
                    return INCONSISTENT_EMPTY_ADVICE;
                }
            };
        }

        public Coverage getCoverage( URL url, int nside ) {
            return null;
        }
    }

    /**
     * ServiceType implementation for Simple Spectral Access.
     */
    private class SsaServiceType extends ServiceType {
        SsaServiceType() {
            super( "ssa" );
        }

        String getDescription() {
            return new StringBuffer()
               .append( "Simple Spectral Access protocol " )
               .append( " - returns a table of spectra near each location.\n" )
               .append( "See <webref url='" )
               .append( "http://www.ivoa.net/Documents/latest/SSA.html" )
               .append( "'>SSA standard</webref>." )
               .toString();
        }

        String getFormatDescription() {
            return new StringBuffer()
               .append( "gives the MIME type of spectra referenced in the " )
               .append( "output table, also special values " )
               .append( "\"<code>votable</code>\", " )
               .append( "\"<code>fits</code>\", " )
               .append( "\"<code>compliant</code>\", " )
               .append( "\"<code>graphic</code>\", " )
               .append( "\"<code>all</code>\", and others\n" )
               .append( "(value of the SSA FORMAT parameter)." )
               .toString();
        }

        public void configureParams( Parameter<?> srParam ) {

            /* SIZE param may be omitted in an SSA query; the service should
             * use some appropriate default value. */
            srParam.setNullPermitted( true );
        }

        public boolean useDistanceFilter() {
            return true;
        }

        public ConeSearcher createSearcher( Environment env, String url,
                                            final boolean believeEmpty,
                                            StarTableFactory tfact,
                                            ContentCoding coding )
                throws TaskException {
            String format = formatParam_.stringValue( env );
            return new SsaConeSearcher( url, format, believeEmpty, tfact,
                                        coding ) {
                @Override
                protected String getInconsistentEmptyAdvice() {
                    return INCONSISTENT_EMPTY_ADVICE;
                };
            };
        }

        public Coverage getCoverage( URL url, int nside ) {
            return null;
        }
    } 
}
