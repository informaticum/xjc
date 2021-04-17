package de.informaticum.xjc.plugin;

import static org.slf4j.LoggerFactory.getLogger;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class BasePlugin
extends CommandLineArgumentedPlugin
implements RunningPlugin, InitialisedOutline {

    private static final Logger LOG = getLogger(BasePlugin.class);

    public static final String GENERATE_CODE = "Generate {} for [{}]";
    public static final String SKIP_CODE = "Skip creation of {} for [{}] because {}.";
    public static final String BECAUSE_CODE_ALREADY_EXISTS = "such code already exists";
    public static final String BECAUSE_OPTION_IS_DISABLED = "according option has not been selected";

    private Outline currentOutline = null;

    private Options currentOptions = null;

    private ErrorHandler currentErrorHandler = null;

    @Override
    public final Outline outline() {
        return this.currentOutline != null ? this.currentOutline : RunningPlugin.super.outline();
    }

    @Override
    public final Options options() {
        return this.currentOptions != null ? this.currentOptions : RunningPlugin.super.options();
    }

    @Override
    public final ErrorHandler errorHandler() {
        return this.currentErrorHandler != null ? this.currentErrorHandler : RunningPlugin.super.errorHandler();
    }

    @Override
    public boolean run(final Outline outline, final Options options, final ErrorHandler errorHandler)
    throws SAXException {
        this.currentOutline = outline;
        this.currentOptions = options;
        this.currentErrorHandler = errorHandler;
        try {
            var result = true;
            for (final var pakkage : outline.getAllPackageContexts()) {
                result &= this.runPackage(pakkage);
                result &= this.runObjectFactory(pakkage.objectFactory());
            }
            for (final var clazz : outline.getClasses()) {
                result &= this.runClass(clazz);
            }
            for (final var enumeration : outline.getEnums()) {
                result &= this.runEnum(enumeration);
            }
            return result;
        } catch (final RuntimeException any) {
            LOG.error("Plugin cannot recover from error.", any);
            try {
                final var failure = new SAXParseException(any.getMessage(), null, any);
                errorHandler.fatalError(failure);
            } catch (final RuntimeException ignore) {}
            throw any;
        } catch (final SAXException any) {
            LOG.error("Plugin cannot recover from error.", any);
            throw any;
        } catch (final Exception any) {
            LOG.error("Plugin cannot recover from error.", any);
            try {
                final var failure = new SAXParseException(any.getMessage(), null, any);
                errorHandler.fatalError(failure);
                throw failure;
            } catch (final RuntimeException ignore) {
                throw new RuntimeException(any.getMessage(), any);
            }
        }
    }

    protected boolean runPackage(final PackageOutline pakkage)
    throws SAXException, Exception {
        return true;
    }

    protected boolean runObjectFactory(final JDefinedClass $factory)
    throws SAXException, Exception {
        return true;
    }

    protected boolean runClass(final ClassOutline clazz)
    throws SAXException, Exception {
        return true;
    }

    protected boolean runEnum(final EnumOutline enumeration)
    throws SAXException, Exception {
        return true;
    }

}
