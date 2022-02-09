package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr.lit;
import static de.informaticum.xjc.util.CollectionAnalysis.emptyModifiableInstanceOf;
import static de.informaticum.xjc.util.CollectionAnalysis.emptyImmutableInstanceOf;
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

    /* Do not (!) assign the following values. Instead, let Java do the initialisation. */
    /* In result, each field's value will be defaulted as specified by the JLS.         */
    /* --> https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html     */
    private static boolean DEFAULT_BOOLEAN; /* no assignment, defaulted by Java instead */
    private static byte    DEFAULT_BYTE   ; /* no assignment, defaulted by Java instead */
    private static char    DEFAULT_CHAR   ; /* no assignment, defaulted by Java instead */
    private static double  DEFAULT_DOUBLE ; /* no assignment, defaulted by Java instead */
    private static float   DEFAULT_FLOAT  ; /* no assignment, defaulted by Java instead */
    private static int     DEFAULT_INT    ; /* no assignment, defaulted by Java instead */
    private static long    DEFAULT_LONG   ; /* no assignment, defaulted by Java instead */
    private static short   DEFAULT_SHORT  ; /* no assignment, defaulted by Java instead */

    /**
     * Returns the the default value for the given field if such value exists. In detail, this means (in order):
     * <dl>
     * <dt>for any XSD attribute with a given lexical value</dt>
     * <dd>{@linkplain com.sun.tools.xjc.model.CDefaultValue#compute(com.sun.tools.xjc.outline.Outline) the according Java expression} is chosen if it can be computed,</dd>
     * <dt>for any primitive type</dt>
     * <dd><a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">the according Java default value</a> is chosen,</dd>
     * <dt>for any known collection type</dt>
     * <dd>either {@code null} or {@linkplain CollectionAnalysis#emptyModifiableInstanceOf(com.sun.codemodel.JType) the according modifiable/unmodifiable empty instance} is
     * chosen,</dd>
     * <dd></dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param field
     *            the field to analyse
     * @param initCollections
     *            either not {@linkplain CommandLineArgument#isActivated() activated} to initialise collection types with {@code null} or
     *            {@linkplainCommandLineArgument#isActivated() activated} to initialise with the according modifiable/unmodifiable empty instance
     * @param unmodifiable
     *            either not {@linkplain CommandLineArgument#isActivated() activated} to create a modifiable empty collection instance or
     *            {@linkplain CommandLineArgument#isActivated() activated} to create an unmodifiable empty collection instance
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultValueFor(final FieldOutline field, final CommandLineArgument initCollections, final CommandLineArgument unmodifiable) {
        return defaultValueFor(field, initCollections.isActivated(), unmodifiable.isActivated());
    }

    /**
     * Returns the the default value for the given field if such value exists. In detail, this means (in order):
     * <dl>
     * <dt>for any XSD attribute with a given lexical value</dt>
     * <dd>{@linkplain com.sun.tools.xjc.model.CDefaultValue#compute(com.sun.tools.xjc.outline.Outline) the according Java expression} is chosen if it can be computed,</dd>
     * <dt>for any primitive type</dt>
     * <dd><a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">the according Java default value</a> is chosen,</dd>
     * <dt>for any known collection type</dt>
     * <dd>either {@code null} or {@linkplain CollectionAnalysis#emptyModifiableInstanceOf(com.sun.codemodel.JType) the according modifiable/unmodifiable empty instance} is
     * chosen,</dd>
     * <dd>and further, the default instance may be modifiable or unmodifiable</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param field
     *            the field to analyse
     * @param initCollections
     *            either {@code false} to initialise collection types with {@code null} or {@code true} to initialise with the according modifiable/unmodifiable empty instance
     * @param unmodifiable
     *            in case an empty collection instance is initialised, that instance may be either modifiable (pass {@code false} value) or may be unmodifiable (pass {@code true}
     *            value)
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultValueFor(final FieldOutline field, final boolean initCollections, final boolean unmodifiable) {
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
        // TODO: Consider property.isUnboxable()? What to do in that case?
        // TODO: Consider property.isOptionalPrimitive()? What to do in that case?
        if (raw.equals(codeModel.BOOLEAN)) return Optional.of(lit(DEFAULT_BOOLEAN));
        if (raw.equals(codeModel.BYTE   )) return Optional.of(lit(DEFAULT_BYTE   ));
        if (raw.equals(codeModel.CHAR   )) return Optional.of(lit(DEFAULT_CHAR   ));
        if (raw.equals(codeModel.DOUBLE )) return Optional.of(lit(DEFAULT_DOUBLE ));
        if (raw.equals(codeModel.FLOAT  )) return Optional.of(lit(DEFAULT_FLOAT  ));
        if (raw.equals(codeModel.INT    )) return Optional.of(lit(DEFAULT_INT    ));
        if (raw.equals(codeModel.LONG   )) return Optional.of(lit(DEFAULT_LONG   ));
        if (raw.equals(codeModel.SHORT  )) return Optional.of(lit(DEFAULT_SHORT  ));
        if (property.isCollection() && initCollections &&  unmodifiable) return Optional.of(emptyImmutableInstanceOf( field.getRawType()));
        if (property.isCollection() && initCollections && !unmodifiable) return Optional.of(emptyModifiableInstanceOf(field.getRawType()));
        return Optional.empty();
    }

}
