package de.informaticum.xjc;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;

public final class TracePlugin
extends AbstractPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(TracePlugin.class);

    @Override
    public final String getOptionName() {
        return "ITBSG-xjc-trace";
    }

    @Override
    public final String getOptionDescription() {
        return "Traces all generated sources, mainly intended to enable debugging purposes.";
    }

    @Override
    protected final boolean runPackage(final Outline outline, final Options options, final ErrorHandler errorHandler, final PackageOutline pakkage) {
        LOG.info("Current Package is [{}].", pakkage._package().name());
        return true;
    }

    @Override
    protected final boolean runObjectFactory(final Outline outline, final Options options, final ErrorHandler errorHandler, final JDefinedClass factory) {
        LOG.info("Current Object-Factory is [{}].", factory.fullName());
        return true;
    }

    @Override
    protected final boolean runClass(final Outline outline, final Options options, final ErrorHandler errorHandler, final ClassOutline clazz) {
        LOG.info("Current Class is [{}].", clazz.getImplClass().fullName());
        return true;
    }

    @Override
    protected final boolean runEnum(final Outline outline, final Options options, final ErrorHandler errorHandler, final EnumOutline enumeration) {
        LOG.info("Current Enum is [{}].", enumeration.getImplClass().fullName());
        return true;
    }

}
