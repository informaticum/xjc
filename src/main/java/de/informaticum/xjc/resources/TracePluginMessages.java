package de.informaticum.xjc.resources;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.TracePlugin;

public enum TracePluginMessages
implements ResourceBundleKeys {

    OPTION_DESCRIPTION,

    ;

    static final ResourceBundle RB = getBundle(TracePlugin.class.getName() + "Messages");

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

    @Override
    public final String toString() {
        return this.apply();
    }

}