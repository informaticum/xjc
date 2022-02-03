package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr.lit;
import static de.informaticum.xjc.util.CollectionAnalysis.defaultInstanceOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Optional;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.CommandLineArgument;
import org.slf4j.Logger;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to default value expressions.
 */
public enum DefaultAnalysis {
    ;

    private static final Logger LOG = getLogger(DefaultAnalysis.class);

    /**
     * Returns the the default value for the given field if such value exists. In detail, this means (in order):
     * <dl>
     * <dt>for any XSD attribute with a given lexical value</dt>
     * <dd>{@linkplain com.sun.tools.xjc.model.CDefaultValue#compute(com.sun.tools.xjc.outline.Outline) the according Java expression} is chosen if it can be computed,</dd>
     * <dt>for any primitive type</dt>
     * <dd><a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">the according Java default value</a> is chosen,</dd>
     * <dt>for any known collection type</dt>
     * <dd>{@linkplain CollectionAnalysis#defaultInstanceOf(com.sun.codemodel.JType) the according default instance} is chosen,</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param field
     *            the field to analyse
     * @param initCollections
     *            either not {@link CommandLineArgument#isActivated() activated} to initialise collection types with {@code null} or {@link CommandLineArgument#isActivated()
     *            activated} to initialise with the according empty instance
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultValueFor(final FieldOutline field, final CommandLineArgument initCollections) {
        return defaultValueFor(field, initCollections.isActivated());
    }

    /**
     * Returns the the default value for the given field if such value exists. In detail, this means (in order):
     * <dl>
     * <dt>for any XSD attribute with a given lexical value</dt>
     * <dd>{@linkplain com.sun.tools.xjc.model.CDefaultValue#compute(com.sun.tools.xjc.outline.Outline) the according Java expression} is chosen if it can be computed,</dd>
     * <dt>for any primitive type</dt>
     * <dd><a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">the according Java default value</a> is chosen,</dd>
     * <dt>for any known collection type</dt>
     * <dd>{@linkplain CollectionAnalysis#defaultInstanceOf(com.sun.codemodel.JType) the according default instance} is chosen,</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param field
     *            the field to analyse
     * @param initCollections
     *            either {@code false} to initialise collection types with {@code null} or {@code true} to initialise with the according empty instance
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultValueFor(final FieldOutline field, final boolean initCollections) {
        final var outline = field.parent().parent();
        final var property = field.getPropertyInfo();
        if (property.defaultValue != null) {
            final var $default = property.defaultValue.compute(outline);
            if ($default != null) { return Optional.of($default); }
            else { LOG.error("Lexical representation of the existing default value for [{}] is [null]!", property.getName(false)); }
        }
        final var raw = field.getRawType();
        final var codeModel = outline.getCodeModel();
        // TODO: Checken, ob es einen Fall gibt, wo einem Non-Primitive-Boolean (etc.) ein false zugewiesen wird, ohne
        //       dass ein Default-Wert existiert. Das darf nicht passieren. Ein "Boolean" ist initial "null".
        // TODO: Consider property.isOptionalPrimitive()? What to do in that case?
        if (raw.equals(codeModel.BOOLEAN)) return Optional.of(lit(false));
        if (raw.equals(codeModel.BYTE))    return Optional.of(lit(0));
        if (raw.equals(codeModel.CHAR))    return Optional.of(lit('\u0000'));
        if (raw.equals(codeModel.DOUBLE))  return Optional.of(lit(0.0d));
        if (raw.equals(codeModel.FLOAT))   return Optional.of(lit(0.0f));
        if (raw.equals(codeModel.INT))     return Optional.of(lit(0));
        if (raw.equals(codeModel.LONG))    return Optional.of(lit(0L));
        if (raw.equals(codeModel.SHORT))   return Optional.of(lit(0));        
        if (property.isCollection())       return initCollections ? Optional.of(defaultInstanceOf(field.getRawType())) : Optional.empty();
        return Optional.empty();
    }

}
