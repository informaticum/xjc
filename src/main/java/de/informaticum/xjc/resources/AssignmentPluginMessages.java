package de.informaticum.xjc.resources;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.AssignmentPlugin;

public enum AssignmentPluginMessages
implements ResourceBundleEntry {

    NOTNULL_COLLECTIONS_DESCRIPTION,
    UNMODIFIABLE_COLLECTIONS_DESCRIPTION,
    DEFENSIVE_COPIES_DESCRIPTION,

    NOTES_BEGIN,
    FIELD_INITIALISATION,
    NOTES_END,
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