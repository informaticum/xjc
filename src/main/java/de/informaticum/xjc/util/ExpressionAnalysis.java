package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static de.informaticum.xjc.util.CollectionAnalysis.copyFactoryFor;
import static de.informaticum.xjc.util.CollectionAnalysis.emptyImmutableInstanceOf;
import static de.informaticum.xjc.util.CollectionAnalysis.emptyModifiableInstanceOf;
import static de.informaticum.xjc.util.CollectionAnalysis.unmodifiableViewFactoryFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Optional;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.FieldOutline;
import org.slf4j.Logger;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions according to default value expressions.
 */
public enum ExpressionAnalysis {
    ;

    private static final Logger LOG = getLogger(ExpressionAnalysis.class);
    private static final String ILLEGAL_DEFAULT_VALUE = "Lexical representation of the existing default value for [{}] is [{}]!";

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
     * <dt>for any collection type</dt>
     * <dd>if requested (see parameter {@code initCollections}), the according {@linkplain CollectionAnalysis#emptyModifiableInstanceOf(JType) modifiable} or
     * {@linkplain CollectionAnalysis#emptyImmutableInstanceOf(JType) unmodifiable} empty instance will be chosen (see parameter {@code unmodifiableCollections}),</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param attribute
     *            the field to analyse
     * @param initCollections
     *            either to initialise collections or not
     * @param unmodifiableCollections
     *            if collections are initialised this specifies either to return an unmodifiable or a modifiable collection
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultExpressionFor(final FieldOutline attribute, final boolean initCollections, final boolean unmodifiableCollections) {
        final var outline = attribute.parent().parent();
        final var codeModel = outline.getCodeModel();
        final var property = attribute.getPropertyInfo();
        if (property.defaultValue != null) {
            assertThat(property.isCollection()).isFalse();
            final var $default = property.defaultValue.compute(outline);
            if ($default != null) { return Optional.of($default); }
            else { LOG.error(ILLEGAL_DEFAULT_VALUE, property.getName(false), null); }
        }
        final var raw = attribute.getRawType();
        // TODO: Checken, ob es einen Fall gibt, wo einem Non-Primitive-Boolean (etc.) ein false zugewiesen wird, ohne
        //       dass ein Default-Wert existiert. Das darf nicht passieren. Ein "Boolean" ist initial "null".
        // TODO: Consider property.isUnboxable()? What to do in that case?
        // TODO: Consider property.isOptionalPrimitive()? What to do in that case?
        if      (raw.equals(codeModel.BOOLEAN)) { return Optional.of(lit(DEFAULT_BOOLEAN)); }
        else if (raw.equals(codeModel.BYTE   )) { return Optional.of(lit(DEFAULT_BYTE   )); }
        else if (raw.equals(codeModel.CHAR   )) { return Optional.of(lit(DEFAULT_CHAR   )); }
        else if (raw.equals(codeModel.DOUBLE )) { return Optional.of(lit(DEFAULT_DOUBLE )); }
        else if (raw.equals(codeModel.FLOAT  )) { return Optional.of(lit(DEFAULT_FLOAT  )); }
        else if (raw.equals(codeModel.INT    )) { return Optional.of(lit(DEFAULT_INT    )); }
        else if (raw.equals(codeModel.LONG   )) { return Optional.of(lit(DEFAULT_LONG   )); }
        else if (raw.equals(codeModel.SHORT  )) { return Optional.of(lit(DEFAULT_SHORT  )); }
        else if (property.isCollection() && initCollections) {
            return Optional.of(unmodifiableCollections ? emptyImmutableInstanceOf(attribute.getRawType()) : emptyModifiableInstanceOf(attribute.getRawType()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * This method returns the clone expression for the given type and for the given actual expression if such clone expression exists. In detail, this means (in order):
     * <dl>
     * <dt>for any array</dt>
     * <dd>a shallow {@linkplain Object#clone() clone} but not a deep clone (multi-dimensional arrays are not cloned in deep, neither are the arrays's elements),</dd>
     * <dt>for any {@link Cloneable} type</dt>
     * <dd>a clone of this instance (either shallow or deep clone, depending on the specific internal {@link Object#clone()} implementation),</dd>
     * <dt>for any collection type</dt>
     * <dd>a {@linkplain CollectionAnalysis#copyFactoryFor(JType) modifiable} or {@linkplain CollectionAnalysis#unmodifiableViewFactoryFor(JType) unmodifiable} collection copy (see
     * parameter {@code unmodifiableCollections})</dd>
     * <dd>note, the copy most likely won't be a real clone as the collection type may differ and collection elements won't be cloned),</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param $Type
     *            the type to analyse
     * @param $expression
     *            the actual expression
     * @return an {@link Optional} holding the clone expression for the actual expression if such clone expression exists; the {@linkplain Optional#empty() empty Optional}
     *         otherwise
     */
    public static final Optional<JExpression> cloneExpressionFor(final JType $Type, final JExpression $expression, final boolean unmodifiableCollections) {
        if ($Type.isArray()) {
            // TODO: Deep copy (instead of shallow copy) for multi-dimensional arrays; Or even further, cloning the array's elements in general (a.k.a. deep clone)
            return Optional.of(cast($Type, $expression.invoke("clone")));
        } else if ($Type.owner().ref(Cloneable.class).isAssignableFrom($Type.boxify())) {
            // TODO: Get deep clone (instead of shallow copy) even if the origin type does not? (for example ArrayList#clone() only returns a shallow copy)
            return Optional.of(cast($Type, $expression.invoke("clone")));
        } else if (CollectionAnalysis.isCollectionType($Type)) {
            // TODO: Cloning the collection's elements (a.k.a. deep clone instead of shallow copy)
            return Optional.of(unmodifiableCollections ? unmodifiableViewFactoryFor($Type).arg($expression) : copyFactoryFor($Type).arg($expression));
        // TODO } else if (copy-constructor?) {
        // TODO } else if (copy-factory-method (in some util class)?) {
        }
        return Optional.empty();
    }

}
