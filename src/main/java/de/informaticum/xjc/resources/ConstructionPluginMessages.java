package de.informaticum.xjc.resources;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.ConstructionPlugin;

public enum ConstructionPluginMessages
implements ResourceBundleEntry {

    OPTION_DESCRIPTION,

    DEFAULT_CONSTRUCTOR_DESCRIPTION,
    VALUES_CONSTRUCTOR_DESCRIPTION,
    BASIC_CONSTRUCTOR_DESCRIPTION,
    COPY_CONSTRUCTOR_DESCRIPTION,
    HIDDEN_CONSTRUCTOR_DESCRIPTION,
    GENERATE_CLONE_DESCRIPTION,

    CONSTRUCTOR_INTRO,
    INJECT_SUPER_CONSTRUCTOR,
    ASSIGN_ALL_FIELDS,
    BLUEPRINT_ARGUMENT,
    INVALID_ARGUMENT_HANDLING,

    PROTECTED_CONSTRUCTOR_JAVADOC,
    ALTERNATIVES_BEGIN,
    ALTERNATIVES_CONSTRUCTOR,
    ALTERNATIVES_BUILDER,
    ALTERNATIVES_FACTORY,
    ALTERNATIVES_END,

    ;

    private static final ResourceBundle RB = getBundle(ConstructionPlugin.class.getName() + "Messages");

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
