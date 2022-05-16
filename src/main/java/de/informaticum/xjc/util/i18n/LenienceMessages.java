package de.informaticum.xjc.util.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.ResourceBundleEntry;

/**
 * Enumeration of all i18n messages keys used by the lenience-assertions.
 */
public enum LenienceMessages
implements ResourceBundleEntry {

    SERIOUS_PROBLEM,

    ;

    private static final ResourceBundle RB = getBundle(LenienceMessages.class.getName().replace(".i18n.", ".l10n."));

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
