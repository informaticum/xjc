package de.informaticum.xjc.plugins.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.ResourceBundleEntry;

/**
 * Enumeration of all i18n messages keys used by the {@link de.informaticum.xjc.plugins.AdoptAnnotationsPlugin}.
 */
public enum AdoptAnnotationsPluginMessages
implements ResourceBundleEntry {

    /* A. Description of the XJC Plugin */

    /* B. Description of the XJC Plugin's Options */
    ADOPT_GENERATED_DESCRIPTION,

    /* C. Main Javadoc Contents and Javadoc Supply (@param, @throws, etc.) */

    ;

    private static final ResourceBundle RB = getBundle(AdoptAnnotationsPluginMessages.class.getName().replace(".i18n.", ".l10n."));

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
