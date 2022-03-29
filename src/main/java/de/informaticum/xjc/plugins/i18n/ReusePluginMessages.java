package de.informaticum.xjc.plugins.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.ResourceBundleEntry;
import de.informaticum.xjc.plugins.ReusePlugin;

/**
 * Enumeration of all i18n messages used by the {@link ReusePlugin}.
 */
public enum ReusePluginMessages
implements ResourceBundleEntry {

    /* A. Description of the XJC Plugin */
    OPTION_DESCRIPTION,

    /* B. Description of the XJC Plugin's Options */
    REUSE_QNAMES_DESCRIPTION,

    /* C.1. Main Javadoc Contents */
    PUBLIC_QNAMES_IMPLNOTE,
    /* C.2. Javadoc Supply (@param, @throws, etc.) */

    ;

    private static final ResourceBundle RB = getBundle(ReusePluginMessages.class.getName());

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
