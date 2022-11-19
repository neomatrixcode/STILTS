package uk.ac.starlink.vo;

/**
 * Describes a capability of a registry service.
 *
 * @author   Mark Taylor
 * @since    3 Jul 2009
 */
public class Capability {

    private final String[] standardIds_;
    private final String xsiTypeTail_;

    /** Cone search capability. */
    public static final Capability CONE =
        new Capability( "ivo://ivoa.net/std/ConeSearch", "ConeSearch" );

    /** Simple Image Access capability. */
    public static final Capability SIA =
        new Capability( new String[] {
                           SiaVersion.V10.getStandardId(),
                           SiaVersion.V20.getStandardId(),
                        },
                        "SimpleImageAccess" );

    /** Simple Spectral Access capability. */
    public static final Capability SSA =
        new Capability( "ivo://ivoa.net/std/SSA", "SimpleSpectralAccess" );

    /** Table Access Protocol capability. */
    public static final Capability TAP =
        new Capability( "ivo://ivoa.net/std/TAP", "TableAccess" );

    /**
     * Constructs a capability with a unique standardID.
     *
     * @param  standardId  capability/@standardID value
     *                     identifying the capability
     * @param  xsiTypeTail  trailing part of the capability/@xsi:type for
     *         the capability
     */
    public Capability( String standardId, String xsiTypeTail ) {
        this( new String[] { standardId }, xsiTypeTail );
    }

    /**
     * Constructs a capability with multiple alternative standardIDs.
     *
     * @param  standardIds  array of capability/@standardID values
     *                      identifying the capability
     * @param  xsiTypeTail  trailing part of the capability/@xsi:type for
     *         the capability
     */
    public Capability( String[] standardIds, String xsiTypeTail ) {
        standardIds_ = standardIds.clone();
        xsiTypeTail_ = xsiTypeTail;
    }

    /**
     * Returns the capability/@standardID string for this capability.
     *
     * @return  ivorn for standard
     */
    public String[] getStandardIds() {
        return standardIds_.clone();
    }

    /**
     * Returns the final part of the capability/@xsi:type for this capability.
     *
     * @return   xsi:type tail
     */
    public String getXsiTypeTail() {
        return xsiTypeTail_;
    }

    @Override
    public String toString() {
        return xsiTypeTail_;
    }
}
