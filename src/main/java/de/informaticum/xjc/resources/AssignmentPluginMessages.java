package de.informaticum.xjc.resources;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.PropertyPlugin;

public enum AssignmentPluginMessages
implements ResourceBundleEntry {

    FIELD_INITIALISATION,
    PRIMITVE_ARGUMENT,
    OPTIONAL_ARGUMENT,
    REQUIRED_ARGUMENT,
    DEFAULTED_OPTIONAL_ARGUMENT,
    DEFAULTED_REQUIRED_ARGUMENT,
    ILLEGAL_VALUE,
    ;

    private static final ResourceBundle RB = getBundle(PropertyPlugin.class.getName() + "Messages");

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}