package de.informaticum.xjc.plugins;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.not;
import static de.informaticum.xjc.plugins.BoilerplatePluginMessages.EQUALS_IMPLNOTE;
import static de.informaticum.xjc.plugins.BoilerplatePluginMessages.GENERATE_EQUALS_DESCRIPTION;
import static de.informaticum.xjc.plugins.BoilerplatePluginMessages.GENERATE_HASHCODE_DESCRIPTION;
import static de.informaticum.xjc.plugins.BoilerplatePluginMessages.GENERATE_TOSTRING_DESCRIPTION;
import static de.informaticum.xjc.plugins.BoilerplatePluginMessages.HASHCODE_IMPLNOTE;
import static de.informaticum.xjc.plugins.BoilerplatePluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.plugins.BoilerplatePluginMessages.TOSTRING_IMPLNOTE;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.$super;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.tools.xjc.outline.ClassOutline;
import de.informaticum.xjc.api.BasePlugin;
import de.informaticum.xjc.api.CommandLineArgument;
import org.slf4j.Logger;

public final class BoilerplatePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(BoilerplatePlugin.class);
    protected static final String GENERATE_METHOD = "Generate method [{}#{}].";
    protected static final String SKIP_METHOD = "Skip creation of method [{}#{}] because {}.";
    protected static final String BECAUSE_METHOD_ALREADY_EXISTS = "such method already exists";

    private static final String equals = "equals";
    private static final String EQUALS_SIGNATURE = format("#%s(Object)", equals);
    private static final String hashCode = "hashCode";
    private static final String HASHCODE_SIGNATURE = format("#%s()", hashCode);
    private static final String toString = "toString";
    private static final String TOSTRING_SIGNATURE = format("#%s()", toString);

    private static final String OPTION_NAME = "informaticum-xjc-boilerplate";
    private static final CommandLineArgument GENERATE_EQUALS   = new CommandLineArgument("boilerplate-equals",   GENERATE_EQUALS_DESCRIPTION  .format(EQUALS_SIGNATURE,   "-boilerplate-hashCode"));
    private static final CommandLineArgument GENERATE_HASHCODE = new CommandLineArgument("boilerplate-hashCode", GENERATE_HASHCODE_DESCRIPTION.format(HASHCODE_SIGNATURE, "-boilerplate-equals"));
    private static final CommandLineArgument GENERATE_TOSTRING = new CommandLineArgument("boilerplate-toString", GENERATE_TOSTRING_DESCRIPTION.format(TOSTRING_SIGNATURE));

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(GENERATE_EQUALS, GENERATE_HASHCODE, GENERATE_TOSTRING);
    }

    @Override
    public final boolean prepareRun() {
        GENERATE_EQUALS  .activates(GENERATE_HASHCODE);
        GENERATE_HASHCODE.activates(GENERATE_EQUALS);
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        GENERATE_EQUALS  .doOnActivation(this::generateEquals,   clazz);
        GENERATE_HASHCODE.doOnActivation(this::generateHashCode, clazz);
        GENERATE_TOSTRING.doOnActivation(this::generateToString, clazz);
        return true;
    }

    private final void generateEquals(final ClassOutline clazz) {
        // 0/2: Preliminary
        if (getMethod(clazz, equals, Object.class).isPresent()) {
            LOG.warn(SKIP_METHOD, fullNameOf(clazz), EQUALS_SIGNATURE, BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        // 1/2: Create
        LOG.info(GENERATE_METHOD, fullNameOf(clazz), EQUALS_SIGNATURE);
        final var $ImplClass = clazz.implClass;
        final var $equals = $ImplClass.method(PUBLIC, boolean.class, equals);
        final var $Object = this.reference(Object.class);
        final var $other = $equals.param(FINAL, $Object, "other");
        $equals.annotate(Override.class);
        javadocSection($equals).append(EQUALS_IMPLNOTE.format($other.name())); // No further method/@param Javadoc; will be inherited instead
        // 2/2: Implement
        $equals.body()._if($other.eq($null))._then()._return(lit(false));
        $equals.body()._if($this.eq($other))._then()._return(lit(true));
        $equals.body()._if(not($this.invoke("getClass").invoke(equals).arg($other.invoke("getClass"))))._then()._return(lit(false));
        final var $Arrays = this.reference(Arrays.class);
        final var $Objects = this.reference(Objects.class);
        final var $comparisons = new ArrayList<JExpression>();
        if (clazz.getSuperClass() != null) {
            $comparisons.add($super.invoke(equals).arg($other));
        }
        final var $properties = generatedPropertiesOf(clazz).values();
        if (!$properties.isEmpty()) {
            final var $that = $equals.body().decl(FINAL, $ImplClass, "that", cast($ImplClass, $other));
            for (final var $property : $properties) {
                if ($property.type().isPrimitive()) {
                    // compare primitive fields directly
                    $comparisons.add($this.ref($property).eq($that.ref($property)));
                } else if ($property.type().isArray() && $property.type().elementType().isPrimitive()) {
                    // invoke Arrays#equals(primitive[],primitive[]) comparison for arrays of primitive types
                    $comparisons.add($Arrays.staticInvoke("equals").arg($this.ref($property)).arg($that.ref($property)));
                } else if ($property.type().isArray()) {
                    // invoke Arrays#deepEquals(Object[],Object[]) comparison for arrays of non-primitive types
                    assertThat($property.type().elementType().isPrimitive()).isFalse();
                    $comparisons.add($Arrays.staticInvoke("deepEquals").arg($this.ref($property)).arg($that.ref($property)));
                } else {
                    // invoke Objects#equals(Object,Object) in any other case
                    $comparisons.add($Objects.staticInvoke("equals").arg($this.ref($property)).arg($that.ref($property)));
                }
            }
        }
        $equals.body()._return($comparisons.stream().reduce(JExpression::cand).orElse(lit(true)));
    }

    private final void generateHashCode(final ClassOutline clazz) {
        // 0/2: Preliminary
        if (getMethod(clazz, hashCode).isPresent()) {
            LOG.warn(SKIP_METHOD, fullNameOf(clazz), HASHCODE_SIGNATURE, BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        // 1/2: Create
        LOG.info(GENERATE_METHOD, fullNameOf(clazz), HASHCODE_SIGNATURE);
        final var $ImplClass = clazz.implClass;
        final var $hashCode = $ImplClass.method(PUBLIC, int.class, hashCode);
        $hashCode.annotate(Override.class);
        javadocSection($hashCode).append(HASHCODE_IMPLNOTE.text()); // No further method Javadoc; will be inherited instead
        // 2/2: Implement
        final var $Arrays = this.reference(Arrays.class);
        final var $hashes = new ArrayList<JInvocation>();
        if (clazz.getSuperClass() != null) {
            $hashes.add($super.invoke(hashCode));
        }
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            if ($property.type().isPrimitive()) {
                // invoke Primitivewrapper#hashCode(primitive) calculation for primitive fields
                $hashes.add($property.type().boxify().staticInvoke("hashCode").arg($this.ref($property)));
            } else if ($property.type().isArray() && $property.type().elementType().isPrimitive()) {
                // invoke Arrays#hashCode(primitive[]) calculation for arrays of primitive types
                $hashes.add($Arrays.staticInvoke("hashCode").arg($this.ref($property)));
            } else if ($property.type().isArray()) {
                // invoke Arrays#deepHashCode(Object[]) calculation for arrays of non-primitive types
                assertThat($property.type().elementType().isPrimitive()).isFalse();
                $hashes.add($Arrays.staticInvoke("deepHashCode").arg($this.ref($property)));
            } else {
                // invoke Object#hashCode() in any other case
                $hashes.add($this.ref($property).invoke(hashCode));
            }
        }
        if ($hashes.isEmpty()) {
            // if there is no hashCode source at all, calculate the class' hashCode
            $hashCode.body()._return($this.invoke("getClass").invoke(hashCode));
        } else if ($hashes.size() == 1) {
            // if there is one calculation only, return this value immediately
            $hashCode.body()._return($hashes.get(0));
        } else {
            // invoke Arrays#hashCode(int[]) in any other case
            final var intArray = $hashes.stream().reduce(_new(this.codeModel().INT.array()), JInvocation::arg);
            $hashCode.body()._return($Arrays.staticInvoke("hashCode").arg(intArray));
        }
    }

    private final void generateToString(final ClassOutline clazz) {
        // 0/2: Preliminary
        if (getMethod(clazz, toString).isPresent()) {
            LOG.warn(SKIP_METHOD, fullNameOf(clazz), TOSTRING_SIGNATURE, BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        // 1/2: Create
        LOG.info(GENERATE_METHOD, fullNameOf(clazz), TOSTRING_SIGNATURE);
        final var $ImplClass = clazz.implClass;
        final var $toString = $ImplClass.method(PUBLIC, String.class, toString);
        $toString.annotate(Override.class);
        javadocSection($toString).append(TOSTRING_IMPLNOTE.text()); // No further method Javadoc; will be inherited instead
        // 2/2: Implement
        final var $Arrays = this.reference(Arrays.class);
        final var $Objects = this.reference(Objects.class);
        final var $StringJoiner = this.reference(StringJoiner.class);
        final var $pieces = new ArrayList<JExpression>();
        for (final var property : generatedPropertiesOf(clazz).entrySet() /* TODO: Also consider constant fields */) {
            final var attribute = property.getKey();
            final var info = attribute.getPropertyInfo();
            final var $property = property.getValue();
            if ($property.type().isPrimitive()) {
                // invoke Primitivewrapper#toString(primitive) calculation for primitive fields
                $pieces.add(lit(info.getName(true) + ": ").plus($property.type().boxify().staticInvoke("toString").arg($this.ref($property))));
            } else if ($property.type().isArray() && $property.type().elementType().isPrimitive()) {
                // invoke Arrays#toString(Object[]) calculation for arrays of primitive types
                $pieces.add(lit(info.getName(true) + ": ").plus($Arrays.staticInvoke("toString").arg($this.ref($property))));
            } else if ($property.type().isArray()) {
                // invoke Arrays#deepToString(Object[]) calculation for arrays of non-primitive types
                assertThat($property.type().elementType().isPrimitive()).isFalse();
                $pieces.add(lit(info.getName(true) + ": ").plus($Arrays.staticInvoke("deepToString").arg($this.ref($property))));
            } else {
                // invoke Objects#toString(Object) in any other case
                $pieces.add(lit(info.getName(true) + ": ").plus($Objects.staticInvoke("toString").arg($this.ref($property))));
            }
        }
        if (clazz.getSuperClass() != null) {
            $pieces.add(lit("Super: ").plus($super.invoke(toString)));
        }
        final var $joiner = _new($StringJoiner).arg(", ").arg($ImplClass.name() + "[").arg("]");
        $toString.body()._return($pieces.stream().reduce($joiner, ($partial, $segement) -> $partial.invoke("add").arg($segement)).invoke("toString"));
    }

}
