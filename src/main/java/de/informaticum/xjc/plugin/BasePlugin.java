package de.informaticum.xjc.plugin;

import static java.lang.String.format;
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

    public static final String GENERATE_METHOD = "Generate [{}] method for [{}].";
    public static final String SKIP_METHOD = "Skip creation of [{}] method for [{}] because {}.";
    public static final String BECAUSE_METHOD_ALREADY_EXISTS = "such method already exists";

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
        this.sayHi();
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

    protected void sayHi() {
        final var HI             = "### Hey JAX-B/JAX-WS user,";
        final var USING          = "### you are using one of the informaticum's XJC plugins, i.e., the:";
        final var CURRENT = format("###   - %s (%s),", this.getOption().getKey(), this.getOption().getValue());
        final var APPRECIATING   = "### If you appreciate it, let me know at:";
        final var CONTACT        = "###   - mailto:xjc@informaticum.de";
        final var INPROVING      = "### If you have any improvement or feature suggestion, feel free to add these at:";
        final var REPOSITORY     = "###   - https://github.com/informaticum/xjc";
        if (LOG.isInfoEnabled()) {
            LOG.info(HI);
            LOG.info(USING);
            LOG.info(CURRENT);
            LOG.info(APPRECIATING);
            LOG.info(CONTACT);
            LOG.info(INPROVING);
            LOG.info(REPOSITORY);
        } else if (LOG.isWarnEnabled()) {
            LOG.warn(HI);
            LOG.warn(USING);
            LOG.warn(CURRENT);
            LOG.warn(APPRECIATING);
            LOG.warn(CONTACT);
            LOG.warn(INPROVING);
            LOG.warn(REPOSITORY);
        } else if (LOG.isErrorEnabled()) {
            LOG.error(HI);
            LOG.error(USING);
            LOG.error(CURRENT);
            LOG.error(APPRECIATING);
            LOG.error(CONTACT);
            LOG.error(INPROVING);
            LOG.error(REPOSITORY);
        }
        System.out.println(HI);
        System.out.println(USING);
        System.out.println(CURRENT);
        System.out.println(APPRECIATING);
        System.out.println(CONTACT);
        System.out.println(INPROVING);
        System.out.println(REPOSITORY);

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
