package uk.ac.starlink.ttools.plot2.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import uk.ac.starlink.ttools.plot2.ReportMap;

/**
 * Config key for boolean values.
 *
 * @author   Mark Taylor
 * @since    22 Feb 2013
 */
public class BooleanConfigKey extends ConfigKey<Boolean> {

    private static final Set<String> TRUE_STRINGS =
            new HashSet<String>( Arrays.asList( new String[] {
        "true", "t", "yes", "y", "1",
    } ) );
    private static final Set<String> FALSE_STRINGS =
            new HashSet<String>( Arrays.asList( new String[] {
        "false", "f", "no", "n", "0",
    } ) );

    /**
     * Constructs a key with explicit default.
     *
     * @param  meta  metadata
     * @param  dflt  default value
     */
    public BooleanConfigKey( ConfigMeta meta, boolean dflt ) {
        super( meta, Boolean.class, Boolean.valueOf( dflt ) );
        if ( meta.getStringUsage() == null ) {
            meta.setStringUsage( "true|false" );
        }
    }

    /**
     * Constructs a key with a false default.
     *
     * @param  meta  metadata
     */
    public BooleanConfigKey( ConfigMeta meta ) {
        this( meta, false );
    }

    public Boolean stringToValue( String txt ) throws ConfigException {
        if ( isTrue( txt ) ) {
            return Boolean.TRUE;
        }
        else if ( isFalse( txt ) ) {
            return Boolean.FALSE;
        }
        else {
            throw new ConfigException( this, "\"" + txt + "\" not boolean" );
        }
    }

    public String valueToString( Boolean value ) {
        return value.toString();
    }

    public Specifier<Boolean> createSpecifier() {
        return new SpecifierPanel<Boolean>( false ) {
            final JCheckBox checkBox_;
            /** Constructor. */ {
                checkBox_ = new JCheckBox();
                checkBox_.setSelected( Boolean.TRUE
                                      .equals( getDefaultValue() ) );
                checkBox_.addActionListener( getActionForwarder() );
            }
            public JComponent createComponent() {
                return checkBox_;
            }
            public Boolean getSpecifiedValue() {
                return Boolean.valueOf( checkBox_.isSelected() );
            }
            public void setSpecifiedValue( Boolean v ) {
                checkBox_.setSelected( Boolean.TRUE.equals( v ) );
                fireAction();
            }
            public void submitReport( ReportMap report ) {
            }
        };
    }

    /**
     * Indicates whether the given string represents the value True.
     *
     * @param  txt  string
     * @return   true iff txt means True
     */
    public static boolean isTrue( String txt ) {
        return txt != null && TRUE_STRINGS.contains( txt.toLowerCase() );
    }

    /**
     * Indicates whether the given string represents the value False.
     *
     * @param  txt  string
     * @return   true iff txt means False
     */
    public static boolean isFalse( String txt ) {
        return txt != null && FALSE_STRINGS.contains( txt.toLowerCase() );
    }
}
