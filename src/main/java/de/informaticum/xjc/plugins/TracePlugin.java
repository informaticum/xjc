package de.informaticum.xjc.plugins;

import static de.informaticum.xjc.plugins.i18n.TracePluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.PackageOutline;
import de.informaticum.xjc.api.BasePlugin;
import org.slf4j.Logger;

public final class TracePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(TracePlugin.class);
    private static final String CURRENT_ENTITY = "Current {} is [{}].";

    private static final String OPTION_NAME = "informaticum-xjc-trace";

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    protected final boolean runPackage(final PackageOutline pakkage) {
        LOG.trace(CURRENT_ENTITY, "Package", fullNameOf(pakkage));
        return true;
    }

    @Override
    protected final boolean runObjectFactory(final JDefinedClass $Factory) {
        LOG.trace(CURRENT_ENTITY, "Object-Factory", $Factory.fullName());
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        LOG.trace(CURRENT_ENTITY, "Class", fullNameOf(clazz));
        return true;
    }

    @Override
    protected final boolean runEnum(final EnumOutline enumeration) {
        LOG.trace(CURRENT_ENTITY, "Enum", fullNameOf(enumeration));
        return true;
    }

}
