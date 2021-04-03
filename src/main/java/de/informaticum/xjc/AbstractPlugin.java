package de.informaticum.xjc;

import static java.lang.String.format;
import java.io.IOException;
import java.util.LinkedHashMap;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class AbstractPlugin
extends Plugin {

    public static final JType[] NO_ARG = new JType[0];

    public static final Class<?>[] DIAMOND = new Class<?>[0];

    public static final JExpression $null = JExpr._null();

    public static final JExpression $super = JExpr._super();

    public static final JExpression $this = JExpr._this();

    private Outline currentOutline = null;

    private Options currentOptions = null;

    private ErrorHandler currentErrorHandler = null;

    public final Outline getCurrentOutline() {
        if (this.currentOutline == null) {
            throw new IllegalStateException("Do not query current 'Outline' before '#run(...)' is called!");
        }
        return this.currentOutline;
    }

    public final Model model() {
        return this.currentOutline.getModel();
    }

    public final JCodeModel codeModel() {
        return this.currentOutline.getCodeModel();
    }

    public final JClass reference(final Class<?> clazz) {
        return this.currentOutline.getCodeModel().ref(clazz);
    }

    public final Options options() {
        if (this.currentOptions == null) {
            throw new IllegalStateException("Do not query current 'Options' before '#run(...)' is called!");
        }
        return this.currentOptions;
    }

    public final ErrorHandler errorHandler() {
        if (this.currentErrorHandler == null) {
            throw new IllegalStateException("Do not query current 'ErrorHandler' before '#run(...)' is called!");
        }
        return this.currentErrorHandler;
    }

    public abstract String getOptionDescription();

    public LinkedHashMap<String, String> getPluginOptions() {
        return new LinkedHashMap<>();
    }

    @Override
    public int parseArgument(final Options options, final String[] arguments, final int index)
    throws BadCommandLineException, IOException {
        return 0; // no option recognized
    }

    @Override
    public String getUsage() {
        final var options = this.getPluginOptions();
        final var width = options.keySet().stream().mapToInt(String::length).max().orElse(this.getOptionName().length());
        final var usage = new StringBuilder();
        usage.append(format("  %1$s :  %2$s%n", "-" + this.getOptionName(), this.getOptionDescription()));
        options.entrySet().forEach(o -> usage.append(format("  %1$-" + width + "s :  %2$s%n", o.getKey(), o.getValue())));
        return usage.toString();
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
            final var failure = new SAXParseException(any.getMessage(), null, any);
            try {
                errorHandler.fatalError(failure);
            } catch (final RuntimeException ignore) {}
            throw any;
        } catch (final SAXException any) {
            throw any;
        } catch (final Exception any) {
            final var failure = new SAXParseException(any.getMessage(), null, any);
            try {
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
