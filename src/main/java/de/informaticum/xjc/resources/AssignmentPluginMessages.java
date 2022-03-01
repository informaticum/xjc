package de.informaticum.xjc.resources;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.AssignmentPlugin;

public enum AssignmentPluginMessages
implements ResourceBundleEntry {

    /* A. Description of the XJC Plugin */

    /* B. Description of the XJC Plugin's Options */
    NOTNULL_COLLECTIONS_DESCRIPTION,
    UNMODIFIABLE_COLLECTIONS_DESCRIPTION,
    DEFENSIVE_COPIES_DESCRIPTION,

    /* C.1. Main Javadoc Contents */
    INITIALISATION_BEGIN,
    FIELD_INITIALISATION,
    INITIALISATION_END,
    /* C.2. Javadoc Supply (@param, @throws, etc.) */
    PRIMITVE_ARGUMENT,
    OPTIONAL_ARGUMENT,
    REQUIRED_ARGUMENT,
    DEFAULTED_OPTIONAL_ARGUMENT,
    DEFAULTED_REQUIRED_ARGUMENT,
    ILLEGAL_VALUE,

    ;

    private static final ResourceBundle RB = getBundle(AssignmentPlugin.class.getName() + "Messages");

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
