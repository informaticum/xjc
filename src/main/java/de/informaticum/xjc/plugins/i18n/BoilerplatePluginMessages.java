package de.informaticum.xjc.plugins.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.ResourceBundleEntry;
import de.informaticum.xjc.plugins.BoilerplatePlugin;

/**
 * Enumeration of all i18n messages used by the {@link BoilerplatePlugin}.
 */
public enum BoilerplatePluginMessages
implements ResourceBundleEntry {

    /* A. Description of the XJC Plugin */
    OPTION_DESCRIPTION,

    /* B. Description of the XJC Plugin's Options */
    GENERATE_EQUALS_DESCRIPTION,
    GENERATE_HASHCODE_DESCRIPTION,
    GENERATE_TOSTRING_DESCRIPTION,

    /* C.1. Main Javadoc Contents */
    EQUALS_IMPLNOTE,
    HASHCODE_IMPLNOTE,
    TOSTRING_IMPLNOTE,
    /* C.2. Javadoc Supply (@param, @throws, etc.) */

    ;

    private static final ResourceBundle RB = getBundle(BoilerplatePluginMessages.class.getName().replace(".i18n.", ".l10n."));

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
