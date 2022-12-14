package uk.ac.starlink.ttools.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import uk.ac.starlink.ttools.plot.Shader;
import uk.ac.starlink.ttools.plot.Shaders;
import uk.ac.starlink.util.IconUtils;

/**
 * ListCellRenderer suitable for a combo box containing 
 * {@link uk.ac.starlink.ttools.plot.Shader}s.
 *
 * @author   Mark Taylor
 * @since    3 Apr 2008
 */
public class ShaderListCellRenderer implements ListCellRenderer<Shader> {

    private static final Map<Shader,Icon> rendererIconMap_ =
        new HashMap<Shader,Icon>();
    private BasicComboBoxRenderer baseRenderer_;

    /**
     * Constructs a renderer suitable for use with a combo box containing
     * shaders.  The renderer will listen to the supplied 
     * <code>comboBox</code>'s properties in order to update its
     * enabledness appropriately. 
     *
     * @param  comboBox  combo box to contain renderer
     */
    public ShaderListCellRenderer( final JComboBox<Shader> comboBox ) {
        baseRenderer_ = new BasicComboBoxRenderer();

        /* Message the renderer when the combo box is enabled/disabled,
         * which does not happen by default (though you might expect it to).
         * This enables it to repaint its icon in disabled (greyed out)
         * colours where appropriate. */
        comboBox.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent evt ) {
                if ( "enabled".equals( evt.getPropertyName() ) ) {
                    baseRenderer_.setEnabled( comboBox.isEnabled() );
                }
            }
        } );
    }

    public Component getListCellRendererComponent( JList<? extends Shader> list,
                                                   Shader shader, int index,
                                                   boolean isSel,
                                                   boolean hasFocus ) {
        Component comp =
            baseRenderer_.getListCellRendererComponent( list, shader, index,
                                                        isSel, hasFocus );
        if ( comp instanceof JLabel ) {
            JLabel label = (JLabel) comp;
            String text = shader.getName();
            if ( ! shader.isAbsolute() ) {
                text = "* " + text ;
            }
            label.setText( text );
            label.setIcon( getRendererIcon( shader ) );
        }
        return comp;
    }

    /**
     * Returns the icon associated with a shader.
     * Currently uses a map for caching.
     *
     * @param  shader  shader
     * @return  icon
     */
    private static Icon getRendererIcon( Shader shader ) {
        if ( ! rendererIconMap_.containsKey( shader ) ) {
            Icon icon = Shaders.createShaderIcon( shader, true, 48, 16, 4, 1 );

            /* Store the image icon based on the painted icon in the map.
             * This has two advantages: first it doesn't need to be
             * drawn each time, so probably this is more efficient.
             * Second, only ImageIcons can be greyed out
             * (Swing limitation). */
            rendererIconMap_.put( shader, IconUtils.toImageIcon( icon ) );
        }
        return rendererIconMap_.get( shader );
    }
}
