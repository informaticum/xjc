package de.informaticum.xjc.plugin;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFAULTED_OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFAULTED_REQUIRED_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFENSIVE_COPIES_DESCRIPTION;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.FIELD_INITIALISATION;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.ILLEGAL_VALUE;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.NOTNULL_COLLECTIONS_DESCRIPTION;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.PRIMITVE_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.REQUIRED_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.UNMODIFIABLE_COLLECTIONS_DESCRIPTION;
import static de.informaticum.xjc.util.CodeModelAnalysis.render;
import static de.informaticum.xjc.util.CodeRetrofit.javadocAppendSection;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map.Entry;
import java.util.Optional;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.util.CollectionAnalysis;
import de.informaticum.xjc.util.ExpressionAnalysis;

public abstract class AssignmentPlugin
extends BasePlugin {

    protected static final CommandLineArgument NOTNULL_COLLECTIONS      = new CommandLineArgument("general-initialise-collections",   NOTNULL_COLLECTIONS_DESCRIPTION.text());
    protected static final CommandLineArgument UNMODIFIABLE_COLLECTIONS = new CommandLineArgument("general-unmodifiable-collections", UNMODIFIABLE_COLLECTIONS_DESCRIPTION.text());
    protected static final CommandLineArgument DEFENSIVE_COPIES         = new CommandLineArgument("general-defensive-copies",         DEFENSIVE_COPIES_DESCRIPTION.text());

    /**
     * Returns the the default value for the given field if such value exists. In detail, this means (in order):
     * <dl>
     * <dt>for any XSD attribute with a given lexical value</dt>
     * <dd>{@linkplain com.sun.tools.xjc.model.CDefaultValue#compute(com.sun.tools.xjc.outline.Outline) the according Java expression} is chosen if it can be computed,</dd>
     * <dt>for any primitive type</dt>
     * <dd><a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">the according Java default value</a> is chosen,</dd>
     * <dt>for any collection type</dt>
     * <dd>if {@link #NOTNULL_COLLECTIONS} {@linkplain CommandLineArgument#getAsBoolean() is activated}, the {@linkplain #UNMODIFIABLE_COLLECTIONS according}
     * {@linkplain CollectionAnalysis#emptyModifiableInstanceOf(JType) modifiable} or
     * {@linkplain CollectionAnalysis#emptyImmutableInstanceOf(JType) unmodifiable} empty instance will be chosen,</dd>
     * <dt>in any other cases</dt>
     * <dd>the {@linkplain Optional#empty() empty Optional} is returned.</dd>
     * </dl>
     *
     * @param attribute
     *            the field to analyse
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">The Java™ Tutorials :: Primitive Data Types</a>
     */
    public static final Optional<JExpression> defaultExpressionFor(final FieldOutline attribute) {
        return ExpressionAnalysis.defaultExpressionFor(attribute, NOTNULL_COLLECTIONS.getAsBoolean(), UNMODIFIABLE_COLLECTIONS.getAsBoolean());
    }

    /**
     * If {@link #DEFENSIVE_COPIES} is activated, this method returns the clone expression for the given type and the given actual expression. In detail, this means (in order):
     * <dl>
     * <dt>for any array</dt>
     * <dd>a shallow {@linkplain Object#clone() clone} but not a deep clone (multi-dimensional arrays are not cloned in deep, neither are the arrays's elements),</dd>
     * <dt>for any {@link Cloneable} type</dt>
     * <dd>a {@linkplain Object#clone() clone} of this instance (either shallow or deep clone, depending on the specific internal clone implementation),</dd>
     * <dt>for any collection type</dt>
     * <dd>an {@linkplain #UNMODIFIABLE_COLLECTIONS according} {@linkplain CollectionAnalysis#copyFactoryFor(JType) modifiable} or
     * {@linkplain CollectionAnalysis#unmodifiableViewFactoryFor(JType) unmodifiable} collection copy (not a real clone as the collection type may differ and collection elements
     * won't be cloned),</dd>
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
    public static final Optional<JExpression> cloneExpressionFor(final JType $Type, final JExpression $expression) {
        return DEFENSIVE_COPIES.getAsBoolean() ? ExpressionAnalysis.cloneExpressionFor($Type, $expression, UNMODIFIABLE_COLLECTIONS.getAsBoolean()) : Optional.empty();
    }

    public static final JExpression effectiveExpressionForNonNull(final JType $Type, final JExpression $expression) {
        return cloneExpressionFor($Type, $expression).orElse($expression);
    }

    protected static final void accordingInitialisation(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter) {
        accordingInitialisationStatement(property, $setter);
        accordingInitialisationJavadoc(property, $setter);
    }

    protected static final void accordingInitialisationStatement(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $value = defaultExpressionFor(attribute).orElse($null);
        $setter.body().assign($this.ref($property), $value);
    }

    protected static final void accordingInitialisationJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $value = defaultExpressionFor(attribute).orElse($null);
        javadocAppendSection($setter.javadoc(), FIELD_INITIALISATION, $property.name(), render($value));
    }

    protected static final void accordingSuperAssignment(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter, final JInvocation $super, final JExpression $expression) {
        accordingSuperAssignmentStatement($super, $expression);
        accordingAssignmentJavadoc(property, $setter);
    }

    protected static final void accordingSuperAssignmentStatement(final JInvocation $super, final JExpression $expression) {
        $super.arg($expression);
    }

    protected static final void accordingAssignment(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter, final JExpression $expression) {
        accordingAssignmentStatement(property, $setter, $expression);
        accordingAssignmentJavadoc(property, $setter);
    }

    protected static final void accordingAssignmentStatement(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter, final JExpression $expression) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var codeModel = attribute.parent().parent().getCodeModel();
        final var $default = defaultExpressionFor(attribute);
        final var $nonNull = effectiveExpressionForNonNull($property.type(), $expression);
        if ($property.type().isPrimitive()) {
            // TODO: Handle primitive $property with non-primitive $expression (that may be 'null')
            $setter.body().assign($this.ref($property), $expression);
        } else if ($default.isPresent()) {
            if ($expression.equals($null)) {
                // in this case "this.$property = ($expression == null) ? $default : $nonNull;" is effectively similar to: "this.$property = default;"
                $setter.body().assign($this.ref($property), $default.get());
            } else {
                // in this default case null-check is performed: "this.$property = ($expression == null) ? $default : $nonNull;"
                $setter.body().assign($this.ref($property), cond($expression.eq($null), $default.get(), $nonNull));
            }
        } else if (isRequired(attribute)) {
            assertThat($expression).isNotEqualTo($null);
            $setter._throws(IllegalArgumentException.class);
            final var $condition = $setter.body()._if($expression.eq($null));
            $condition._then()._throw(_new(codeModel.ref(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), $nonNull);
        } else {
            assertThat(isOptional(attribute)).isTrue();
            if ($expression.equals($null)) {
                // in this case "this.$property = ($expression == null) ? null : $nonNull;" is effectively similar to: "this.$property = null;"
                $setter.body().assign($this.ref($property), $expression);
            } else if ($nonNull == $expression) {
                // in this case "this.$property = ($expression == null) ? null : $nonNull;" is effectively similar to: "this.$property = $expression;"
                $setter.body().assign($this.ref($property), $expression);
            } else {
                // in this default case null-check is performed: "this.$property = ($expression == null) ? null : $nonNull;"
                $setter.body().assign($this.ref($property), cond($expression.eq($null), $null, $nonNull));
            }
        }
    }

    protected static final void accordingAssignmentJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $default = defaultExpressionFor(attribute);
        if ($property.type().isPrimitive()) {
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), PRIMITVE_ARGUMENT, property.getValue().name());
        } else if ($default.isPresent()) {
            // TODO: Different Javadoc message for collection types?
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), isRequired(property.getKey()) ? DEFAULTED_REQUIRED_ARGUMENT : DEFAULTED_OPTIONAL_ARGUMENT, property.getValue().name(), render($default.get()));
        } else if (isRequired(attribute)) {
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), REQUIRED_ARGUMENT, property.getValue().name());
            if ($setter.javadoc().addThrows(IllegalArgumentException.class).isEmpty()) {
                javadocAppendSection($setter.javadoc().addThrows(IllegalArgumentException.class), ILLEGAL_VALUE);
            }
        } else {
            assertThat(isOptional(attribute)).isTrue();
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), OPTIONAL_ARGUMENT, property.getValue().name());
        }
    }

}
