package de.informaticum.xjc;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr._super;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.PUBLIC;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;

public final class BoilerplatePlugin
extends AbstractPlugin {

    private static final Logger LOG = getLogger(BoilerplatePlugin.class);

    private static final String GENERATE_HASHCODE = "-boilerplate-hashCode";

    private static final String GENERATE_EQUALS = "-boilerplate-equals";

    private static final String GENERATE_TOSTRING = "-boilerplate-toString";

    private boolean generateEquals = false;

    private boolean generateHashcode = false;

    private boolean generateToString = false;

    @Override
    public final String getOptionName() {
        return "ITBSG-xjc-boilerplate";
    }

    @Override
    public final String getOptionDescription() {
        return "Generates common boilerplate code.";
    }

    @Override
    public final LinkedHashMap<String, String> getPluginOptions() {
        return new LinkedHashMap<>(ofEntries(entry(GENERATE_EQUALS, "Generate #equals(Object) method. Default: false"),
                                             entry(GENERATE_HASHCODE, "Generate [#hashCode()] method. Default: false"),
                                             entry(GENERATE_TOSTRING, "Generate [#toString()] method. Default: false")));
    }

    @Override
    public final int parseArgument(final Options options, final String[] arguments, final int index) {
        switch (arguments[index]) {
            case GENERATE_EQUALS:
                this.generateEquals = true;
                return 1;
            case GENERATE_HASHCODE:
                this.generateHashcode = true;
                return 1;
            case GENERATE_TOSTRING:
                this.generateToString = true;
                return 1;
            default:
                return 0;
        }
    }

    @Override
    protected final boolean runClass(final Outline outline, final Options options, final ErrorHandler errorHandler, final ClassOutline clazz) {
        if (this.generateEquals) {
            if (clazz.getImplClass().getMethod("equals", argTypes(outline.getCodeModel().ref(Object.class))) != null) {
                LOG.warn("Skip [#equals(Object)] method for [{}] because such method already exists.", clazz.getImplClass().fullName());
            } else {
                this.equalsInto(clazz, outline);
            }
        }
        if (this.generateHashcode) {
            if (clazz.getImplClass().getMethod("hashCode", NO_ARG) != null) {
                LOG.warn("Skip [#hashCode()] method for [{}] because such method already exists.", clazz.getImplClass().fullName());
            } else {
                this.hashCodeInto(clazz, outline);
            }
        }
        if (this.generateToString) {
            if (clazz.getImplClass().getMethod("toString", NO_ARG) != null) {
                LOG.warn("Skip [#toString()] method for [{}] because such method already exists.", clazz.getImplClass().fullName());
            } else {
                this.toStringInto(clazz, outline);
            }
        }
        return true;
    }

    private final void equalsInto(final ClassOutline clazz, final Outline outline) {
        LOG.info("Generate [#equals(Object)] method for [{}]", clazz.getImplClass().fullName());
        final var $equals = clazz.getImplClass().method(PUBLIC, boolean.class, "equals");
        // TODO: hashcode.javadoc().append("TODO");
        $equals.annotate(Override.class);
        final var $other = $equals.param(outline.getCodeModel().ref(Object.class), "other");
        $other.mods().setFinal(true);

        $equals.body()._if($other.eq(_null()))._then()._return(lit(false));

        $equals.body()._if($other.eq(_this()))._then()._return(lit(true));

        $equals.body()._if(_this().invoke("getClass").ne($other.invoke("getClass")))._then()._return(lit(false));

        final var comparisons = new ArrayList<JExpression>();
        ofNullable(clazz.getSuperClass()).ifPresent(parent -> comparisons.add(_super().invoke("equals").arg($other)));
        final var fields = clazz.getImplClass().fields().keySet();
        if (!fields.isEmpty()) {
            final var $that = $equals.body().decl(clazz.getImplClass(), "that", cast(clazz.getImplClass(), $other));
            $that.mods().setFinal(true);
            final var $Objects = outline.getCodeModel().ref(Objects.class);
            fields.forEach(field -> comparisons.add($Objects.staticInvoke("equals").arg(_this().ref(field)).arg($that.ref(field))));
        }
        final var fallback = lit(true);

        $equals.body()._return(comparisons.stream().reduce(JExpression::cand).orElse(fallback));
    }

    private final void hashCodeInto(final ClassOutline clazz, final Outline outline) {
        LOG.info("Generate [#hashCode()] method for [{}]", clazz.getImplClass().fullName());
        final var $hashCode = clazz.getImplClass().method(PUBLIC, int.class, "hashCode");
        // TODO: hashcode.javadoc().append("TODO");
        $hashCode.annotate(Override.class);

        final var hashes = new ArrayList<JExpression>();
        ofNullable(clazz.getSuperClass()).ifPresent(parent -> hashes.add(_super().invoke("hashCode")));
        clazz.getImplClass().fields().keySet().forEach(field -> hashes.add(_this().ref(field)));
        final var calculation = outline.getCodeModel().ref(Objects.class).staticInvoke("hash");
        hashes.forEach(calculation::arg);
        final var fallback = _this().invoke("getClass").invoke("hashCode");

        $hashCode.body()._return(hashes.isEmpty() ? fallback : calculation);
    }

    private void toStringInto(final ClassOutline clazz, final Outline outline) {
        LOG.info("Generate [#toString()] method for [{}]", clazz.getImplClass().fullName());
        final var $toString = clazz.getImplClass().method(PUBLIC, String.class, "toString");
        // TODO: hashcode.javadoc().append("TODO");
        $toString.annotate(Override.class);

        final var $StringJoiner = outline.getCodeModel().ref(StringJoiner.class);
        final var joiner = _new($StringJoiner).arg(", ").arg(clazz.getImplClass().name() + "[").arg("]");
        final var values = toStringAtoms(outline, clazz);
        $toString.body()._return(values.stream().reduce(joiner, (result, value) -> result.invoke("add").arg(value)).invoke("toString"));
    }

    private static List<JExpression> toStringAtoms(final Outline outline, final ClassOutline clazz) {
        final var atoms = new ArrayList<JExpression>();
        final var $Objects = outline.getCodeModel().ref(Objects.class);
        clazz.getImplClass().fields().entrySet().forEach(f -> atoms.add(lit(f.getKey() + ": ").plus($Objects.staticInvoke("toString").arg(_this().ref(f.getValue())))));
        ofNullable(clazz.getSuperClass()).ifPresent(s -> atoms.add(lit("super: ").plus(_super().invoke("toString"))));
        return atoms;
    }

}
