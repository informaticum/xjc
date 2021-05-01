package de.informaticum.xjc;

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.not;
import static de.informaticum.xjc.plugin.TargetCode.$null;
import static de.informaticum.xjc.plugin.TargetCode.$super;
import static de.informaticum.xjc.plugin.TargetCode.$this;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.Printify.fullName;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public final class BoilerplatePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(BoilerplatePlugin.class);

    private static final String equals = "equals";
    private static final String EQUALS_SIGNATURE = format("#%s(Object)", equals);
    private static final String hashCode = "hashCode";
    private static final String HASHCODE_SIGNATURE = format("#%s()", hashCode);
    private static final String toString = "toString";
    private static final String TOSTRING_SIGNATURE = format("#%s()", toString);

    private static final String OPTION_NAME = "ITBSG-xjc-boilerplate";
    private static final String GENERATE_EQUALS_NAME = "boilerplate-equals";
    private static final CommandLineArgument GENERATE_EQUALS = new CommandLineArgument(GENERATE_EQUALS_NAME, format("Generate [%s] method (automatically enables option '-boilerplate-hashCode').", EQUALS_SIGNATURE));
    private static final String GENERATE_HASHCODE_NAME = "boilerplate-hashCode";
    private static final CommandLineArgument GENERATE_HASHCODE = new CommandLineArgument(GENERATE_HASHCODE_NAME, format("Generate [%s] method (automatically enables option '-boilerplate-equals').", HASHCODE_SIGNATURE));
    private static final String GENERATE_TOSTRING_NAME = "boilerplate-toString";
    private static final CommandLineArgument GENERATE_TOSTRING = new CommandLineArgument(GENERATE_TOSTRING_NAME, format("Generate [%s] method.", TOSTRING_SIGNATURE));

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, "Generates common boilerplate code.");
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(GENERATE_EQUALS, GENERATE_HASHCODE, GENERATE_TOSTRING);
    }

    @Override
    public boolean run(final Outline outline, final Options options, final ErrorHandler errorHandler)
    throws SAXException {
        // activate implicit arguments
        GENERATE_EQUALS.alsoActivate(GENERATE_HASHCODE);
        GENERATE_HASHCODE.alsoActivate(GENERATE_EQUALS);
        // execute usual process
        return super.run(outline, options, errorHandler);
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        this.considerEquals(clazz);
        this.considerHashCode(clazz);
        this.considerToString(clazz);
        return true;
    }

    private final void considerEquals(final ClassOutline clazz) {
        if (!GENERATE_EQUALS.isActivated()) {
            LOG.trace(SKIP_METHOD, EQUALS_SIGNATURE, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, equals, Object.class) != null) {
            LOG.warn(SKIP_METHOD, EQUALS_SIGNATURE, fullName(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
        } else {
            LOG.info(GENERATE_METHOD, EQUALS_SIGNATURE, fullName(clazz));
            assertThat(getMethod(clazz, equals, Object.class)).as("check undefined method %s", EQUALS_SIGNATURE).isNull();
            this.generateEquals(clazz);
            assertThat(getMethod(clazz, equals, Object.class)).as("check generated method %s", EQUALS_SIGNATURE).isNotNull();
        }
    }

    private final void generateEquals(final ClassOutline clazz) {
        // 1/3: Create
        final var $Type = clazz.implClass;
        final var $equals = $Type.method(PUBLIC, boolean.class, equals);
        // 2/3: Annotate
        $equals.annotate(Override.class);
        // 3/3: Implement (without JavaDoc)
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
            final var $that = $equals.body().decl(FINAL, $Type, "that", cast($Type, $other));
            for (final var $property : properties.values()) {
                comparisons.add($Objects.staticInvoke(equals).arg($this.ref($property)).arg($that.ref($property)));
            }
        }
        $equals.body()._return(comparisons.stream().reduce(JExpression::cand).orElse(TRUE));
    }

    private final void considerHashCode(final ClassOutline clazz) {
        if (!GENERATE_HASHCODE.isActivated()) {
            LOG.trace(SKIP_METHOD, HASHCODE_SIGNATURE, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, hashCode) != null) {
            LOG.warn(SKIP_METHOD, HASHCODE_SIGNATURE, fullName(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
        } else {
            LOG.info(GENERATE_METHOD, HASHCODE_SIGNATURE, fullName(clazz));
            assertThat(getMethod(clazz, hashCode)).as("check undefined method %s", HASHCODE_SIGNATURE).isNull();
            this.addHashCode(clazz);
            assertThat(getMethod(clazz, hashCode)).as("check generated method %s", HASHCODE_SIGNATURE).isNotNull();
        }
    }

    private final void addHashCode(final ClassOutline clazz) {
        // 1/3: Create
        final var $Type = clazz.implClass;
        final var $hashCode = $Type.method(PUBLIC, int.class, hashCode);
        // 2/3: Annotate
        $hashCode.annotate(Override.class);
        // 3/3: Implement (without JavaDoc)
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

    private final void considerToString(final ClassOutline clazz) {
        if (!GENERATE_TOSTRING.isActivated()) {
            LOG.trace(SKIP_METHOD, TOSTRING_SIGNATURE, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, toString) != null) {
            LOG.warn(SKIP_METHOD, TOSTRING_SIGNATURE, fullName(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
        } else {
            LOG.info(GENERATE_METHOD, TOSTRING_SIGNATURE, fullName(clazz));
            assertThat(getMethod(clazz, toString)).as("check undefined method %s", TOSTRING_SIGNATURE).isNull();
            this.addToString(clazz);
            assertThat(getMethod(clazz, toString)).as("check generated method %s", TOSTRING_SIGNATURE).isNotNull();
        }
    }

    private final void addToString(final ClassOutline clazz) {
        // 1/3: Create
        final var $Type = clazz.implClass;
        final var $toString = $Type.method(PUBLIC, String.class, toString);
        // 2/3: Annotate
        $toString.annotate(Override.class);
        // 3/3: Implement (without JavaDoc)
        final var $Objects = this.reference(Objects.class);
        final var segments = new ArrayList<JExpression>();
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var info = attribute.getPropertyInfo();
            final var $property = property.getValue();
            segments.add(lit(info.getName(true) + ": ").plus($Objects.staticInvoke(toString).arg($this.ref($property))));
        }
        if (clazz.getSuperClass() != null) {
            segments.add(lit("Super: ").plus($super.invoke(toString)));
        }
        // TODO: InsurantIdType#ROOT in toString()-Ausgabe aufnehmen
        final var $joiner = _new(this.reference(StringJoiner.class)).arg(", ").arg($Type.name() + "[").arg("]");
        $toString.body()._return(segments.stream().reduce($joiner, (partial, segement) -> partial.invoke("add").arg(segement)).invoke(toString));
    }

}
