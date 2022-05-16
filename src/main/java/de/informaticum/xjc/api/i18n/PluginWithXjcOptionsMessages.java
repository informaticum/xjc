package de.informaticum.xjc.api.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.PluginWithXjcOptions;
import de.informaticum.xjc.api.ResourceBundleEntry;

/**
 * Enumeration of all i18n messages keys used by the {@link PluginWithXjcOptions}.
 */
public enum PluginWithXjcOptionsMessages
implements ResourceBundleEntry {

    ADDITIONAL_ARGUMENTS_INTRODUCTION,

    ;

    private static final ResourceBundle RB = getBundle(PluginWithXjcOptionsMessages.class.getName().replace(".i18n.", ".l10n."));

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
