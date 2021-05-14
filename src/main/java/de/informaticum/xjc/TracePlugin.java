package de.informaticum.xjc;

import static de.informaticum.xjc.util.Printify.fullNameOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.PackageOutline;
import de.informaticum.xjc.plugin.BasePlugin;
import org.slf4j.Logger;

public final class TracePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(TracePlugin.class);

    private static final String OPTION_NAME = "informaticum-xjc-trace";

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, "Traces/Logs all generated sources, mainly intended to enable debugging purposes.");
    }

    @Override
    protected final boolean runPackage(final PackageOutline pakkage) {
        LOG.trace("Current Package is [{}].", fullNameOf(pakkage));
        return true;
    }

    @Override
    protected final boolean runObjectFactory(final JDefinedClass $factory) {
        LOG.trace("Current Object-Factory is [{}].", fullNameOf($factory));
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        LOG.trace("Current Class is [{}].", fullNameOf(clazz));
        return true;
    }

    @Override
    protected final boolean runEnum(final EnumOutline enumeration) {
        LOG.trace("Current Enum is [{}].", fullNameOf(enumeration));
        return true;
    }

}
