package de.informaticum.xjc.plugins.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.ResourceBundleEntry;
import de.informaticum.xjc.plugins.TracePlugin;

/**
 * Enumeration of all i18n messages used by the {@link TracePlugin}.
 */
public enum TracePluginMessages
implements ResourceBundleEntry {

    /* A. Description of the XJC Plugin */
    OPTION_DESCRIPTION,

    /* B. Description of the XJC Plugin's Options */

    /* C.1. Main Javadoc Contents */
    /* C.2. Javadoc Supply (@param, @throws, etc.) */

    ;

    private static final ResourceBundle RB = getBundle(TracePluginMessages.class.getName());

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
