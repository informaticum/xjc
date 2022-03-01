package de.informaticum.xjc;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.not;
import static de.informaticum.xjc.resources.BoilerplatePluginMessages.EQUALS_IMPLNOTE;
import static de.informaticum.xjc.resources.BoilerplatePluginMessages.GENERATE_EQUALS_DESCRIPTION;
import static de.informaticum.xjc.resources.BoilerplatePluginMessages.GENERATE_HASHCODE_DESCRIPTION;
import static de.informaticum.xjc.resources.BoilerplatePluginMessages.GENERATE_TOSTRING_DESCRIPTION;
import static de.informaticum.xjc.resources.BoilerplatePluginMessages.HASHCODE_IMPLNOTE;
import static de.informaticum.xjc.resources.BoilerplatePluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.resources.BoilerplatePluginMessages.TOSTRING_IMPLNOTE;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.$super;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
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
        GENERATE_EQUALS.activates(GENERATE_HASHCODE);
        GENERATE_HASHCODE.activates(GENERATE_EQUALS);
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        GENERATE_EQUALS.doOnActivation(this::generateEquals, clazz);
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
        final var $Objects = this.reference(Objects.class);
        $equals.body()._if($other.eq($null))._then()._return(lit(false));
        $equals.body()._if($this.eq($other))._then()._return(lit(true));
        $equals.body()._if(not($this.invoke("getClass").invoke("equals").arg($other.invoke("getClass"))))._then()._return(lit(false));
        final var $comparisons = new ArrayList<JExpression>();
        if (clazz.getSuperClass() != null) {
            $comparisons.add($super.invoke(equals).arg($other));
        }
        final var $properties = generatedPropertiesOf(clazz).values();
        if (!$properties.isEmpty()) {
            final var $that = $equals.body().decl(FINAL, $ImplClass, "that", cast($ImplClass, $other));
            for (final var $property : $properties) {
                if ($property.type().isPrimitive()) {
                    $comparisons.add($this.ref($property).eq($that.ref($property)));
                } else {
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
        final var $Objects = this.reference(Objects.class);
        final var $hash = $Objects.staticInvoke("hash");
        if (clazz.getSuperClass() != null) {
            $hash.arg($super.invoke(hashCode));
        }
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            $hash.arg($this.ref($property));
        }
        $hashCode.body()._return($hash.listArgs().length > 0 ? $hash : $this.invoke("getClass").invoke("hashCode"));
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
        final var $Objects = this.reference(Objects.class);
        final var $StringJoiner = this.reference(StringJoiner.class);
        final var $segments = new ArrayList<JExpression>();
        for (final var property : generatedPropertiesOf(clazz).entrySet() /* TODO: Also consider constant fields */) {
            final var attribute = property.getKey();
            final var info = attribute.getPropertyInfo();
            final var $property = property.getValue();
            $segments.add(lit(info.getName(true) + ": ").plus($Objects.staticInvoke("toString").arg($this.ref($property))));
        }
        if (clazz.getSuperClass() != null) {
            $segments.add(lit("Super: ").plus($super.invoke(toString)));
        }
        final var $joiner = _new($StringJoiner).arg(", ").arg($ImplClass.name() + "[").arg("]");
        $toString.body()._return($segments.stream().reduce($joiner, ($partial, $segement) -> $partial.invoke("add").arg($segement)).invoke("toString"));
    }

}
