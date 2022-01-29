package de.informaticum.xjc;

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.not;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$super;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.ClassOutline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import org.slf4j.Logger;

public final class BoilerplatePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(BoilerplatePlugin.class);

    private static final String equals = "equals";
    private static final String EQUALS_SIGNATURE = format("#%s(Object)", equals);
    private static final String hashCode = "hashCode";
    private static final String HASHCODE_SIGNATURE = format("#%s()", hashCode);
    private static final String toString = "toString";
    private static final String TOSTRING_SIGNATURE = format("#%s()", toString);

    private static final String OPTION_NAME = "informaticum-xjc-boilerplate";
    private static final String OPTION_DESC = "Generates common boilerplate code.";
    private static final CommandLineArgument GENERATE_EQUALS   = new CommandLineArgument("boilerplate-equals",   format("Generate [%s] method (automatically enables option '-boilerplate-hashCode'). Default: false", EQUALS_SIGNATURE));
    private static final CommandLineArgument GENERATE_HASHCODE = new CommandLineArgument("boilerplate-hashCode", format("Generate [%s] method (automatically enables option '-boilerplate-equals'). Default: false", HASHCODE_SIGNATURE));
    private static final CommandLineArgument GENERATE_TOSTRING = new CommandLineArgument("boilerplate-toString", format("Generate [%s] method. Default: false", TOSTRING_SIGNATURE));

    protected static final String GENERATE_METHOD = "Generate [{}] method for [{}].";
    protected static final String SKIP_METHOD = "Skip creation of [{}] method for [{}] because {}.";
    protected static final String BECAUSE_METHOD_ALREADY_EXISTS = "such method already exists";

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESC);
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(GENERATE_EQUALS, GENERATE_HASHCODE, GENERATE_TOSTRING);
    }

    @Override
    public boolean prepareRun() {
        GENERATE_EQUALS.alsoActivate(GENERATE_HASHCODE);
        GENERATE_HASHCODE.alsoActivate(GENERATE_EQUALS);
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        GENERATE_EQUALS.doOnActivation(this::generateEquals, clazz);
        GENERATE_HASHCODE.doOnActivation(this::addHashCode, clazz);
        GENERATE_TOSTRING.doOnActivation(this::addToString, clazz);
        return true;
    }

    private final void generateEquals(final ClassOutline clazz) {
        // 1/4: Prepare
        if (getMethod(clazz, equals, Object.class) != null) {
            LOG.warn(SKIP_METHOD, EQUALS_SIGNATURE, fullNameOf(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_METHOD, EQUALS_SIGNATURE, fullNameOf(clazz));
        // 2/4: Create
        final var $class = clazz.implClass;
        final var $equals = $class.method(PUBLIC, boolean.class, equals);
        // 3/4: JavaDocument/Annotate
        $equals.javadoc().append(format("{@inheritDoc}%n"))
                         .append(format("%n@implNote <a href=\"https://github.com/informaticum/xjc\">This generated {@code equals} method</a> compares each field of {@code this} instance with the according field of the {@code other} instance."));
        $equals.annotate(Override.class);
        // 4/4: Implement
        final var $other = $equals.param(FINAL, this.reference(Object.class), "other");
        $equals.body()._if($other.eq($null))._then()._return(FALSE);
        $equals.body()._if($this.eq($other))._then()._return(TRUE);
        $equals.body()._if(not($this.invoke("getClass").invoke(equals).arg($other.invoke("getClass"))))._then()._return(FALSE);
        final var comparisons = new ArrayList<JExpression>();
        if (clazz.getSuperClass() != null) {
            comparisons.add($super.invoke(equals).arg($other));
        }
        final var properties = generatedPropertiesOf(clazz);
        if (!properties.isEmpty()) {
            final var $Objects = this.reference(Objects.class);
            final var $that = $equals.body().decl(FINAL, $class, "that", cast($class, $other));
            for (final var $property : properties.values()) {
                comparisons.add($Objects.staticInvoke(equals).arg($this.ref($property)).arg($that.ref($property)));
            }
        }
        $equals.body()._return(comparisons.stream().reduce(JExpression::cand).orElse(TRUE));
    }

    private final void addHashCode(final ClassOutline clazz) {
        // 1/4: Prepare
        if (getMethod(clazz, hashCode) != null) {
            LOG.warn(SKIP_METHOD, HASHCODE_SIGNATURE, fullNameOf(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_METHOD, HASHCODE_SIGNATURE, fullNameOf(clazz));
        // 2/4: Create
        final var $class = clazz.implClass;
        final var $hashCode = $class.method(PUBLIC, int.class, hashCode);
        // 3/4: JavaDocument/Annotate
        $hashCode.javadoc().append(format("{@inheritDoc}%n"))
                           .append(format("%n@implNote <a href=\"https://github.com/informaticum/xjc\">This generated {@code hashCode} method</a> considers the hash-code of each field of {@code this} instance to compute the overall return result."))
                           .append(format("%n(If there is no field at all, the hash-code of {@code this} instance's {@linkplain #getClass() class} is returned instead.)"));
        $hashCode.annotate(Override.class);
        // 4/4: Implement
        final var $Objects = this.reference(Objects.class);
        final var $hash = $Objects.staticInvoke("hash");
        if (clazz.getSuperClass() != null) {
            $hash.arg($super.invoke(hashCode));
        }
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            $hash.arg($this.ref($property));
        }
        $hashCode.body()._return($hash.listArgs().length > 0 ? $hash : $this.invoke("getClass").invoke(hashCode));
    }

    private final void addToString(final ClassOutline clazz) {
        // 1/4: Prepare
        if (getMethod(clazz, toString) != null) {
            LOG.warn(SKIP_METHOD, TOSTRING_SIGNATURE, fullNameOf(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_METHOD, TOSTRING_SIGNATURE, fullNameOf(clazz));
        // 2/4: Create
        final var $class = clazz.implClass;
        final var $toString = $class.method(PUBLIC, String.class, toString);
        // 3/4: JavaDocument/Annotate
        $toString.javadoc().append(format("{@inheritDoc}%n"))
                           .append(format("%n@implNote <a href=\"https://github.com/informaticum/xjc\">This generated {@code toString} method</a> returns a human readable list of all fields of {@code this} instance, each mapping to its own string representation."));
        $toString.annotate(Override.class);
        // 4/4: Implement
        final var $Objects = this.reference(Objects.class);
        final var segments = new ArrayList<JExpression>();
        for (final var property : generatedPropertiesOf(clazz).entrySet() /* TODO: Also consider constant fields */) {
            final var attribute = property.getKey();
            final var info = attribute.getPropertyInfo();
            final var $property = property.getValue();
            segments.add(lit(info.getName(true) + ": ").plus($Objects.staticInvoke(toString).arg($this.ref($property))));
        }
        if (clazz.getSuperClass() != null) {
            segments.add(lit("Super: ").plus($super.invoke(toString)));
        }
        final var $joiner = _new(this.reference(StringJoiner.class)).arg(", ").arg($class.name() + "[").arg("]");
        $toString.body()._return(segments.stream().reduce($joiner, (partial, segement) -> partial.invoke("add").arg(segement)).invoke(toString));
    }

}
