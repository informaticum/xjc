package de.informaticum.xjc;

import static java.lang.String.format;
import java.io.IOException;
import java.util.LinkedHashMap;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public abstract class AbstractPlugin
extends Plugin {

    public static final JType[] NO_ARG = new JType[0];

    public static final JType[] argTypes(final JType... args) {
        return args;
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
        try {
            var result = true;
            for (final var pakkage : outline.getAllPackageContexts()) {
                result &= this.runPackage(outline, options, errorHandler, pakkage);
                result &= this.runObjectFactory(outline, options, errorHandler, pakkage.objectFactory());
            }
            for (final var clazz : outline.getClasses()) {
                result &= this.runClass(outline, options, errorHandler, clazz);
            }
            for (final var enumeration : outline.getEnums()) {
                result &= this.runEnum(outline, options, errorHandler, enumeration);
            }
            return result;
        } catch (final Exception any) {
            outline.getErrorReceiver().error(any);
            return false;
        }
    }

    protected boolean runPackage(final Outline outline, final Options options, final ErrorHandler errorHandler, final PackageOutline pakkage)
    throws Exception {
        return true;
    }

    protected boolean runObjectFactory(final Outline outline, final Options options, final ErrorHandler errorHandler, final JDefinedClass objectFactory)
    throws Exception {
        return true;
    }

    protected boolean runClass(final Outline outline, final Options options, final ErrorHandler errorHandler, final ClassOutline clazz)
    throws Exception {
        return true;
    }

    protected boolean runEnum(final Outline outline, final Options options, final ErrorHandler errorHandler, final EnumOutline enumeration)
    throws Exception {
        return true;
    }

}
