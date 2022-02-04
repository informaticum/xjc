package de.informaticum.xjc.resources;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.BoilerplatePlugin;

public enum BoilerplatePluginMessages
implements ResourceBundleEntry {

    OPTION_DESCRIPTION,

    GENERATE_EQUALS_DESCRIPTION,
    GENERATE_HASHCODE_DESCRIPTION,
    GENERATE_TOSTRING_DESCRIPTION,

    EQUALS_IMPLNOTE,
    HASHCODE_IMPLNOTE,
    TOSTRING_IMPLNOTE,

    ;

    private static final ResourceBundle RB = getBundle(BoilerplatePlugin.class.getName() + "Messages");

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}