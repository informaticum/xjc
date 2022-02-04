package de.informaticum.xjc.resources;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.ReusePlugin;

public enum ReusePluginMessages
implements ResourceBundleEntry {

    OPTION_DESCRIPTION,

    REUSE_QNAMES_DESCRIPTION,

    PUBLIC_QNAMES_JAVADOC,

    ;

    private static final ResourceBundle RB = getBundle(ReusePlugin.class.getName() + "Messages");

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}