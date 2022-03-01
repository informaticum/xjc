package de.informaticum.xjc.plugin;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFAULTED_OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFAULTED_REQUIRED_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFENSIVE_COPIES_DESCRIPTION;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.FIELD_INITIALISATION;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.ILLEGAL_VALUE;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.INITIALISATION_BEGIN;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.INITIALISATION_END;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.NOTNULL_COLLECTIONS_DESCRIPTION;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.PRIMITVE_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.REQUIRED_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.UNMODIFIABLE_COLLECTIONS_DESCRIPTION;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.cloneExpressionFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.doesThrow;
import static de.informaticum.xjc.util.CodeModelAnalysis.render;
import static de.informaticum.xjc.util.CodeRetrofit.javadocBreak;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.util.CodeModelAnalysis;
import de.informaticum.xjc.util.OutlineAnalysis;

public abstract class AssignmentPlugin
extends BasePlugin {

    protected static final CommandLineArgument NOTNULL_COLLECTIONS      = new CommandLineArgument("general-initialise-collections",   NOTNULL_COLLECTIONS_DESCRIPTION.text());
    protected static final CommandLineArgument UNMODIFIABLE_COLLECTIONS = new CommandLineArgument("general-unmodifiable-collections", UNMODIFIABLE_COLLECTIONS_DESCRIPTION.text());
    protected static final CommandLineArgument DEFENSIVE_COPIES         = new CommandLineArgument("general-defensive-copies",         DEFENSIVE_COPIES_DESCRIPTION.text());

    /**
     * @implNote If you extend this {@link AssignmentPlugin} you probably override this method. If you do so, you must
     *           <ul>
     *           <li>include {@link #NOTNULL_COLLECTIONS} if you reuse {@link #defaultExpressionFor(FieldOutline)},
     *           {@link #accordingAssignmentAndJavadoc(Entry, JMethod, JExpression)}, or {@link #accordingInitialisationAndJavadoc(Map, JMethod)};</li>
     *           <li>include {@link #DEFENSIVE_COPIES} if you reuse {@link #effectiveExpressionForNonNull(JType, JExpression)} or
     *           {@link #accordingAssignmentAndJavadoc(Entry, JMethod, JExpression)};</li>
     *           <li>include {@link #UNMODIFIABLE_COLLECTIONS} if you reuse {@link #defaultExpressionFor(FieldOutline)}, {@link #effectiveExpressionForNonNull(JType, JExpression)},
     *           {@link #accordingAssignmentAndJavadoc(Entry, JMethod, JExpression)}, {@link #accordingInitialisationAndJavadoc(Map, JMethod)}.</li>
     *           </ul>
     */
    @Override
    public List<CommandLineArgument> getPluginArguments() {
        return asList(NOTNULL_COLLECTIONS, DEFENSIVE_COPIES, UNMODIFIABLE_COLLECTIONS);
    }

    /**
     * Returns {@linkplain de.informaticum.xjc.util.OutlineAnalysis#defaultExpressionFor(FieldOutline, boolean, boolean) the default value for the given field if such value
     * exists}. The collection initialisation behaviour therefore is controlled by {@link #NOTNULL_COLLECTIONS} and {@link #UNMODIFIABLE_COLLECTIONS}.
     *
     * @param attribute
     *            the field to analyse
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see de.informaticum.xjc.util.OutlineAnalysis#defaultExpressionFor(FieldOutline, boolean, boolean)
     * @see #NOTNULL_COLLECTIONS
     * @see #UNMODIFIABLE_COLLECTIONS
     */
    protected static final Optional<JExpression> defaultExpressionFor(final FieldOutline attribute) {
        return OutlineAnalysis.defaultExpressionFor(attribute, NOTNULL_COLLECTIONS.getAsBoolean(), UNMODIFIABLE_COLLECTIONS.getAsBoolean());
    }

    /**
     * If {@link #DEFENSIVE_COPIES} is activated, this method returns {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#cloneExpressionFor(JType, JExpression, boolean) the
     * clone expression for the given type and the given actual expression if such clone expression exists}. The collection copy behaviour therefore is controlled by
     * {@link #UNMODIFIABLE_COLLECTIONS}. If no such clone expression exists or if {@link #DEFENSIVE_COPIES} is not activated, the actual expression is returned (without any
     * modification). Note: The generated expression most likely cannot deal a {@code null} argument accordingly.
     *
     * @param $type
     *            the type to analyse
     * @param $expression
     *            the actual expression
     * @return the clone expression for the actual expression if {@link #DEFENSIVE_COPIES} is activated and such clone expression exists; the actual expression otherwise
     * @see #UNMODIFIABLE_COLLECTIONS
     * @see de.informaticum.xjc.util.CodeModelAnalysis#cloneExpressionFor(JType, JExpression, boolean)
     */
    protected static final JExpression effectiveExpressionForNonNull(final JType $type, final JExpression $expression) {
        return DEFENSIVE_COPIES.getAsBoolean() ? cloneExpressionFor($type, $expression, UNMODIFIABLE_COLLECTIONS.getAsBoolean()).orElse($expression) : $expression;
    }

    /**
     * Creates all property assignment statements within the given setter {@linkplain JMethod method}, and also appends the according Javadoc messages. In detail, all properties
     * are initialised with their according {@linkplain #defaultExpressionFor(FieldOutline) default expression} or, if no such default expression exists, with
     * {@link de.informaticum.xjc.util.CodeModelAnalysis#$null null}.
     *
     * @param properties
     *            all properties to consider
     * @param $setter
     *            the method to put the assignment statements into
     */
    protected static final void accordingInitialisationAndJavadoc(final Map<? extends FieldOutline, ? extends JFieldVar> properties, final JMethod $setter) {
        javadocSection($setter).append(INITIALISATION_BEGIN.text());
        for (final var property : properties.entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var $value = defaultExpressionFor(attribute).orElse($null);
            $setter.body().assign($this.ref($property), $value);
            javadocBreak($setter).append(FIELD_INITIALISATION.format($property.name(), render($value)));
        }
        javadocBreak($setter).append(INITIALISATION_END.text());
    }

    /**
     * Creates all property assignment statements within the given setter {@linkplain JMethod method}, and also appends the according Javadoc messages. In detail, the property is
     * used as an argument for the given {@code super} invocation.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method to put the assignment statements into
     * @param $super
     *            the invocation of the {@code super} constructor
     * @param $expression
     *            the current expression to use for the property assignment
     */
    protected static final void accordingSuperAssignmentAndJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter, final JInvocation $super, final JExpression $expression) {
        $super.arg($expression);
        accordingAssignmentJavadoc(property, $setter);
    }

    /**
     * Creates the property assignment statement within the given setter {@linkplain JMethod method}, and also appends the according Javadoc messages.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method to put the assignment statements into
     * @param $expression
     *            the current expression to use for the property assignment
     */
    protected static final void accordingAssignmentAndJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter, final JExpression $expression) {
        accordingAssignment(property, $setter, $expression, defaultExpressionFor(property.getKey()), effectiveExpressionForNonNull(property.getValue().type(), $expression));
        accordingAssignmentJavadoc(property, $setter);
    }

    /**
     * Creates the property assignment statement within the given setter {@linkplain JMethod method}.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method to put the assignment statements into
     * @param $expression
     *            the current expression to use for the property assignment
     * @param $default the default value the given expression represents {@link CodeModelAnalysis#$null} or is {@code null} at runtime
     * @param $nonNull the effective expression to represent the non-null-case
     */
    protected static final void accordingAssignment(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter,
                                                    final JExpression $expression, final Optional<JExpression> $default, final JExpression $nonNull) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $model = attribute.parent().parent().getCodeModel();
        if ($property.type().isPrimitive()) {
            // TODO: Handle primitive $property with non-primitive $expression (that may be 'null')
            $setter.body().assign($this.ref($property), $expression);
        } else if ($default.isPresent()) {
            if (render($expression).equals(render($null))) {
                // in this case "this.$property = ($expression == null) ? $default : $nonNull;" is effectively similar to: "this.$property = default;"
                $setter.body().assign($this.ref($property), $default.get());
            } else {
                // in this default case null-check is performed: "this.$property = ($expression == null) ? $default : $nonNull;"
                $setter.body().assign($this.ref($property), cond($expression.eq($null), $default.get(), $nonNull));
            }
        } else if (isRequired(attribute)) {
            assertThat($default).isNotPresent();
            assertThat($expression).isNotEqualTo($null);
            $setter._throws(IllegalArgumentException.class);
            assertThat(doesThrow($setter, $model.ref(IllegalArgumentException.class))).isTrue();
            final var $condition = $setter.body()._if($expression.eq($null));
            $condition._then()._throw(_new($model.ref(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), $nonNull);
        } else {
            assertThat($default).isNotPresent();
            assertThat(isOptional(attribute)).isTrue();
            // This is the target expression: "this.$property = ($expression == $null) ? $null : $nonNull;" ...
            if (render($expression).equals(render($null))) {
                // ... but in this case, the target expression is effectively similar to: "this.$property = $null;"
                $setter.body().assign($this.ref($property), $null);
            } else if (render($expression).equals(render($nonNull))) {
                // ... but in this case, the target expression is effectively similar to: "this.$property = $nonNull;"
                $setter.body().assign($this.ref($property), $nonNull);
            } else {
                // ... and in any other case, the target expression will be used without any optimisation/modification.
                $setter.body().assign($this.ref($property), cond($expression.eq($null), $null, $nonNull));
            }
        }
    }

    /**
     * Appends the according Javadoc messages.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method to append the Javadoc
     */
    protected static final void accordingAssignmentJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter) {
        // TODO: Javadoc information about either defensive copy or live reference (with according side effects!)
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $default = defaultExpressionFor(attribute);
        if ($property.type().isPrimitive()) {
            javadocSection($setter.javadoc().addParam(property.getValue())).append(PRIMITVE_ARGUMENT.format(property.getValue().name()));
        } else if ($default.isPresent()) {
            // TODO: Different Javadoc message for collection types?
            final var DEFAULTED_ARGUMENT = isRequired(property.getKey()) ? DEFAULTED_REQUIRED_ARGUMENT : DEFAULTED_OPTIONAL_ARGUMENT;
            javadocSection($setter.javadoc().addParam(property.getValue())).append(DEFAULTED_ARGUMENT.format(property.getValue().name(), render($default.get())));
        } else if (isRequired(attribute)) {
            assertThat($default).isNotPresent();
            javadocSection($setter.javadoc().addParam(property.getValue())).append(REQUIRED_ARGUMENT.format(property.getValue().name()));
            if ($setter.javadoc().addThrows(IllegalArgumentException.class).isEmpty()) {
                javadocSection($setter.javadoc().addThrows(IllegalArgumentException.class)).append(ILLEGAL_VALUE.text());
            }
        } else {
            assertThat($default).isNotPresent();
            assertThat(isOptional(attribute)).isTrue();
            javadocSection($setter.javadoc().addParam(property.getValue())).append(OPTIONAL_ARGUMENT.format(property.getValue().name()));
        }
    }

}
