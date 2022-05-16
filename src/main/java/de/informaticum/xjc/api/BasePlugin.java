package de.informaticum.xjc.api;

import static de.informaticum.xjc.util.CustomizableOutlineComparator.sorted;
import static de.informaticum.xjc.util.PackageOutlineComparator.sorted;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.function.Consumer;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Base class for custom {@linkplain Plugin XJC plug-ins}.
 */
public abstract class BasePlugin
extends CommandLineArgumentedPlugin
implements InitialisedOutline, InitialisedOptions, InitialisedErrorHandler {

    private static final Logger LOG = getLogger(BasePlugin.class);

    private Outline currentOutline = null;

    private Options currentOptions = null;

    private ErrorHandler currentErrorHandler = null;

    @Override
    public final Outline outline() {
        return this.currentOutline != null ? this.currentOutline : InitialisedOutline.super.outline();
    }

    @Override
    public final Options options() {
        return this.currentOptions != null ? this.currentOptions : InitialisedOptions.super.options();
    }

    @Override
    public final ErrorHandler errorHandler() {
        return this.currentErrorHandler != null ? this.currentErrorHandler : InitialisedErrorHandler.super.errorHandler();
    }

    @Override
    public void postProcessModel(final Model model, final ErrorHandler errorHandler) {
        super.postProcessModel(model, errorHandler);
        // TODO: consider model manipulation ... (e.g., CPropertyInfo#setName(boolean, String)
    }

    /**
     * @see #sayHi() 1. say hi
     * @see #run() 2. execute the specific add-on's implementation
     */
    @Override
    public final boolean run(final Outline outline, final Options options, final ErrorHandler errorHandler)
    throws SAXException {
        // TODO: do something with the options (e.g., change the #sayHi() behaviour)
        try {
            this.currentOutline = outline;
            this.currentOptions = options;
            this.currentErrorHandler = errorHandler;
            this.sayHi();
            return this.run();
        } catch (final SAXException any) {
            LOG.error("Plugin cannot recover from error.", any);
            throw any;
        } catch (final RuntimeException any) {
            LOG.error("Plugin cannot recover from error.", any);
            try { errorHandler.fatalError(new SAXParseException(any.getMessage(), null, any)); }
            catch (final RuntimeException ignore) { any.addSuppressed(ignore); }
            throw any;
        } catch (final Exception any) {
            LOG.error("Plugin cannot recover from error.", any);
            try { errorHandler.fatalError(new SAXParseException(any.getMessage(), null, any)); }
            catch (final RuntimeException ignore) { any.addSuppressed(ignore); }
            throw new RuntimeException(any.getMessage(), any);
        }
    }

    private final void sayHi() {
        if      (LOG.isInfoEnabled())  { this.sayHi(LOG::info);  }
        else if (LOG.isWarnEnabled())  { this.sayHi(LOG::warn);  }
        else if (LOG.isErrorEnabled()) { this.sayHi(LOG::error); }
        this.sayHi(System.out::println);
    }

    private final void sayHi(final Consumer<? super String> sink) {
        sink.accept(       "########################################################################");
        sink.accept(       "### Hey JAXB/JAX-WS user,"                                               );
        sink.accept(       "### you are using one of the informaticum's XJC plug-ins, i.e., the:"    );
        sink.accept(format("###   - %s (%s),", this.getOptionName(), this.getOptionDescription())    );
        sink.accept(       "### If you appreciate it, let me know at:"                               );
        sink.accept(       "###   - mailto:xjc@informaticum.de"                                      );
        sink.accept(       "### If you have any improvement or feature suggestion, feel free to"     );
        sink.accept(       "### add these at:"                                                       );
        sink.accept(       "###   - https://github.com/informaticum/xjc"                             );
        sink.accept(       "########################################################################");
    }

    /**
     * This method is called by {@link #run(Outline, Options, ErrorHandler)}. Can be overridden.
     *
     * @see #prepareRun() 1. prepare the run
     * @see #runPackage(PackageOutline) 2.a. run each package (in lexicological order of the package name)
     * @see #runObjectFactory(JDefinedClass) 2.b. run each object factory (in lexicological order of the according package name)
     * @see #runClass(ClassOutline) 3. run each class (in order of the class hierarchy)
     * @see #runEnum(EnumOutline) 4. run each enum class (in order of the class hierarchy, effectively in lexicological order of the class name)
     * @return {@code true} if the add-on executes successfully; {@code false} if it detects some errors but those are reported and recovered gracefully
     * @throws SAXException
     *             indication of a fatal irrecoverable error, thrown by any of the sub-methods
     * @throws Exception
     *             indication of a fatal irrecoverable error, thrown by any of the sub-methods
     */
    protected boolean run()
    throws SAXException, Exception {
        var result = this.prepareRun();
        for (final var pakkage : sorted(this.currentOutline.getAllPackageContexts())) {
            result &= this.runPackage(pakkage);
            result &= this.runObjectFactory(pakkage.objectFactory());
        }
        for (final var clazz : sorted(this.currentOutline.getClasses() /* sorted list guarantees execution in hierarchical order */ )) {
            result &= this.runClass(clazz);
        }
        for (final var enumeration : sorted(this.currentOutline.getEnums())) {
            result &= this.runEnum(enumeration);
        }
        return result;
    }

    /**
     * This method is called by {@link #run()} (step 1 of 4). Can be overridden.
     *
     * @return {@code true} if this method executes successfully; {@code false} if it detects some errors but those are reported and recovered gracefully
     * @throws SAXException
     *             indication of a fatal irrecoverable error
     * @throws Exception
     *             indication of a fatal irrecoverable error
     */
    protected boolean prepareRun()
    throws SAXException, Exception {
        return true;
    }

    /**
     * This method is called by {@link #run()} (step 2.a of 4). Can be overridden.
     *
     * @param pakkage
     *            the package-outline to consider
     * @return {@code true} if this method executes successfully; {@code false} if it detects some errors but those are reported and recovered gracefully
     * @throws SAXException
     *             indication of a fatal irrecoverable error
     * @throws Exception
     *             indication of a fatal irrecoverable error
     */
    protected boolean runPackage(final PackageOutline pakkage)
    throws SAXException, Exception {
        return true;
    }

    /**
     * This method is called by {@link #run()} (step 2.b of 4). Can be overridden.
     *
     * @param $Factory
     *            the object-factory to consider
     * @return {@code true} if this method executes successfully; {@code false} if it detects some errors but those are reported and recovered gracefully
     * @throws SAXException
     *             indication of a fatal irrecoverable error
     * @throws Exception
     *             indication of a fatal irrecoverable error
     */
    protected boolean runObjectFactory(final JDefinedClass $Factory)
    throws SAXException, Exception {
        return true;
    }

    /**
     * This method is called by {@link #run()} (step 3 of 4). Can be overridden.
     *
     * @param clazz
     *            the class-outline to consider
     * @return {@code true} if this method executes successfully; {@code false} if it detects some errors but those are reported and recovered gracefully
     * @throws SAXException
     *             indication of a fatal irrecoverable error
     * @throws Exception
     *             indication of a fatal irrecoverable error
     */
    protected boolean runClass(final ClassOutline clazz)
    throws SAXException, Exception {
        return true;
    }

    /**
     * This method is called by {@link #run()} (step 4 of 4). Can be overridden.
     *
     * @param enumeration
     *            the enum-outline to consider
     * @return {@code true} if this method executes successfully; {@code false} if it detects some errors but those are reported and recovered gracefully
     * @throws SAXException
     *             indication of a fatal irrecoverable error
     * @throws Exception
     *             indication of a fatal irrecoverable error
     */
    protected boolean runEnum(final EnumOutline enumeration)
    throws SAXException, Exception {
        return true;
    }

}
