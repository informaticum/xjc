package de.informaticum.xjc.plugins.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.ResourceBundleEntry;
import de.informaticum.xjc.plugins.AssignmentPlugin;

/**
 * Enumeration of all i18n messages keys used by the {@link AssignmentPlugin}.
 */
public enum AssignmentPluginMessages
implements ResourceBundleEntry {

    /* A. Description of the XJC Plugin */

    /* B. Description of the XJC Plugin's Options */
    PECS_PARAMETERS_DESCRIPTION,
    NOTNULL_COLLECTIONS_DESCRIPTION,
    UNMODIFIABLE_COLLECTIONS_DESCRIPTION,
    DEFENSIVE_COPIES_DESCRIPTION,

    /* C. Main Javadoc Contents and Javadoc Supply (@param, @throws, etc.) */
    INITIALISATION_BEGIN,
    FIELD_INITIALISATION,
    INITIALISATION_END,
    PRIMITVE_FIELD,
    DEFAULTED_FIELD,
    OPTIONAL_FIELD,
    REQUIRED_FIELD,
    ILLEGAL_ARGUMENT,

    ;

    private static final ResourceBundle RB = getBundle(AssignmentPluginMessages.class.getName().replace(".i18n.", ".l10n."));

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
