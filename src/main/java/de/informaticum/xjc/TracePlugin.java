package de.informaticum.xjc;

import static de.informaticum.xjc.util.Printify.fullName;
import static org.slf4j.LoggerFactory.getLogger;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.slf4j.Logger;

public final class TracePlugin
extends AbstractPlugin {

    private static final Logger LOG = getLogger(TracePlugin.class);

    @Override
    public final String getOptionName() {
        return "ITBSG-xjc-trace";
    }

    @Override
    public final String getOptionDescription() {
        return "Traces all generated sources, mainly intended to enable debugging purposes.";
    }

    @Override
    protected final boolean runPackage(final PackageOutline pakkage) {
        LOG.trace("Current Package is [{}].", fullName(pakkage));
        return true;
    }

    @Override
    protected final boolean runObjectFactory(final JDefinedClass factory) {
        LOG.trace("Current Object-Factory is [{}].", fullName(factory));
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        LOG.trace("Current Class is [{}].", fullName(clazz));
        return true;
    }

    @Override
    protected final boolean runEnum(final EnumOutline enumeration) {
        LOG.trace("Current Enum is [{}].", fullName(enumeration));
        return true;
    }

}
