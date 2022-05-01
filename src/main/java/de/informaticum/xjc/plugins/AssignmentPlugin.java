package de.informaticum.xjc.plugins;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.DEFAULTED_FIELD;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.DEFENSIVE_COPIES_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.FIELD_INITIALISATION;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.ILLEGAL_ARGUMENT;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.INITIALISATION_BEGIN;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.INITIALISATION_END;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.NOTNULL_COLLECTIONS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.OPTIONAL_FIELD;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.PECS_PARAMETERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.PRIMITVE_FIELD;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.REQUIRED_FIELD;
import static de.informaticum.xjc.plugins.i18n.AssignmentPluginMessages.UNMODIFIABLE_COLLECTIONS_DESCRIPTION;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.cloneExpressionFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.doesThrow;
import static de.informaticum.xjc.util.CodeModelAnalysis.javadocNameOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.pecsProducerTypeOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.render;
import static de.informaticum.xjc.util.CodeRetrofit.javadocBreak;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.OutlineAnalysis.javadocNameOf;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.api.BasePlugin;
import de.informaticum.xjc.api.CommandLineArgument;
import de.informaticum.xjc.util.OutlineAnalysis;

/**
 * Base class for custom {@linkplain Plugin XJC plugins} for the purpose of generating (field) assignments. It supports three XJC options, namely {@link #NOTNULL_COLLECTIONS},
 * {@link #UNMODIFIABLE_COLLECTIONS}, and {@link #DEFENSIVE_COPIES}. Depending on whether they are activated or not, the code will be generated accordingly.
 */
public abstract class AssignmentPlugin
extends BasePlugin {

    /**
     * The intention of this XJC option is:
     * <ul>
     * <li>each time a collection instance is initialised, the code generated by the informaticum's XJC plugins shall instantiate an empty collection instance (instead of leaving
     * it {@code null}),</li>
     * <li>each time a collection field will be assigned to some given value/parameter, the code generated by the informaticum's XJC plugins shall check for {@code null} values and
     * use an empty collection instance instead.</li>
     * </ul>
     */
    protected static final CommandLineArgument NOTNULL_COLLECTIONS = new CommandLineArgument("general-initialise-collections", NOTNULL_COLLECTIONS_DESCRIPTION.text());

    /**
     * The intention of this XJC option is:
     * <ul>
     * <li>whenever possible, the code generated by the informaticum's XJC plugins shall treat collections as unmodifiable.</li>
     * </ul>
     */
    protected static final CommandLineArgument UNMODIFIABLE_COLLECTIONS = new CommandLineArgument("general-unmodifiable-collections", UNMODIFIABLE_COLLECTIONS_DESCRIPTION.text());

    /**
     * The intention of this XJC option is:
     * <ul>
     * <li>whenever possible, the code generated by the informaticum's XJC plugins shall create defensive copies,</li>
     * <li>though due to the Java way of copying/cloning instances, the current generic code may not return perfect deep copies for some fields (e.g., multidimensional
     * arrays).</li>
     * </ul>
     */
    protected static final CommandLineArgument DEFENSIVE_COPIES = new CommandLineArgument("general-defensive-copies", DEFENSIVE_COPIES_DESCRIPTION.text());

    /**
     * The intention of this XJC option is:
     * <ul>
     * <li>Provide more generic method signatures according to the PECS principle (i.e., covariance acceptance).</li>
     * </ul>
     */
    protected static final CommandLineArgument PECS_PARAMETERS = new CommandLineArgument("general-pecs-parameters", PECS_PARAMETERS_DESCRIPTION.format(DEFENSIVE_COPIES));

    /**
     * @implNote If you extend this {@link AssignmentPlugin} you probably override this method. If you do so, you must
     *           <ul>
     *           <li>include {@link #NOTNULL_COLLECTIONS} if you reuse {@link #defaultExpressionFor(FieldOutline)},
     *           {@link #accordingAssignmentAndJavadoc(Entry, JMethod, JExpression)}, or {@link #accordingInitialisationAndJavadoc(Map, JMethod)};</li>
     *           <li>include {@link #DEFENSIVE_COPIES} if you reuse {@link #effectiveExpressionForNonNull(JType, JExpression)}, or
     *           {@link #accordingAssignmentAndJavadoc(Entry, JMethod, JExpression)};</li>
     *           <li>include {@link #UNMODIFIABLE_COLLECTIONS} if you reuse {@link #defaultExpressionFor(FieldOutline)}, {@link #effectiveExpressionForNonNull(JType, JExpression)},
     *           {@link #accordingAssignmentAndJavadoc(Entry, JMethod, JExpression)}, or {@link #accordingInitialisationAndJavadoc(Map, JMethod)}.</li>
     *           </ul>
     */
    @Override
    public List<CommandLineArgument> getPluginArguments() {
        return asList(PECS_PARAMETERS, NOTNULL_COLLECTIONS, DEFENSIVE_COPIES, UNMODIFIABLE_COLLECTIONS);
    }

    @Override
    protected boolean prepareRun() {
        // Collection PECS wildcard parameters cannot be assigned directly to the non-wildcard fields:
        //   > incompatible types: java.util.List<capture#1 of ? extends X> cannot be converted to java.util.List<X>
        // Instead, they must be adopted by its copy statements. Thus, {@link #DEFENSIVE_COPIES} must be activated.
        PECS_PARAMETERS.activates(DEFENSIVE_COPIES);
        return true;
    }

    public static final JType parameterTypeOf(final JType $type) {
        return PECS_PARAMETERS.isActivated() ? pecsProducerTypeOf($type) : $type;
    }

    public static final JType parameterTypeOf(final JVar $var) {
        return parameterTypeOf($var.type());
    }

    public static final JType[] parameterTypesOf(final Collection<? extends JVar> $types) {
        return $types.stream().map(AssignmentPlugin::parameterTypeOf).toArray(JType[]::new);
    }

    /**
     * Returns {@linkplain de.informaticum.xjc.util.OutlineAnalysis#defaultExpressionFor(FieldOutline, boolean, boolean) the default value for the given field if such value
     * exists}. The collection initialisation behaviour therefore is controlled by {@link #NOTNULL_COLLECTIONS} and {@link #UNMODIFIABLE_COLLECTIONS}.
     *
     * @param field
     *            the field to analyse
     * @return an {@link Optional} holding the default value for the given field if such value exists; the {@linkplain Optional#empty() empty Optional} otherwise
     * @see #NOTNULL_COLLECTIONS
     * @see #UNMODIFIABLE_COLLECTIONS
     * @see de.informaticum.xjc.util.OutlineAnalysis#defaultExpressionFor(FieldOutline, boolean, boolean)
     */
    protected static final Optional<JExpression> defaultExpressionFor(final FieldOutline field) {
        return OutlineAnalysis.defaultExpressionFor(field, NOTNULL_COLLECTIONS.isActivated(), UNMODIFIABLE_COLLECTIONS.isActivated());
    }

    /**
     * If {@link #DEFENSIVE_COPIES} is activated, this method returns {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#cloneExpressionFor(JType, JExpression, boolean) the
     * clone expression for the given type and the given actual expression if such clone expression exists}. The collection copy behaviour therefore is controlled by
     * {@link #UNMODIFIABLE_COLLECTIONS}. If no such clone expression exists or if {@link #DEFENSIVE_COPIES} is not activated, the actual expression is returned (without any
     * modification). Note: The generated expression most likely cannot deal a {@code null} parameter accordingly, so you better generate a {@code null} check before executing this
     * expression.
     *
     * @param $type
     *            the type to analyse
     * @param $expression
     *            the actual expression
     * @return the clone expression for the actual expression if {@link #DEFENSIVE_COPIES} is activated and such clone expression exists; the actual expression otherwise
     * @see #DEFENSIVE_COPIES
     * @see #UNMODIFIABLE_COLLECTIONS
     * @see de.informaticum.xjc.util.CodeModelAnalysis#cloneExpressionFor(JType, JExpression, boolean)
     */
    protected static final JExpression effectiveExpressionForNonNull(final JType $type, final JExpression $expression) {
        return DEFENSIVE_COPIES.isActivated() ? cloneExpressionFor($type, $expression, UNMODIFIABLE_COLLECTIONS.isActivated()).orElse($expression) : $expression;
    }

    /**
     * Creates all property initialisations within the given {@linkplain JMethod setter method/constructor}, and also appends the according Javadoc messages. In detail, all
     * properties are initialised with their according {@linkplain #defaultExpressionFor(FieldOutline) default expression} or, if no such default expression exists, with
     * {@link de.informaticum.xjc.util.CodeModelAnalysis#$null null}.
     *
     * @param properties
     *            all properties to consider
     * @param $setter
     *            the method/constructor to put the assignment statements into
     */
    protected static final void accordingInitialisationAndJavadoc(final Map<? extends FieldOutline, ? extends JFieldVar> properties, final JMethod $setter) {
        javadocSection($setter).append(INITIALISATION_BEGIN.text());
        for (final var property : properties.entrySet()) {
            final var field = property.getKey();
            final var $field = property.getValue();
            final var $value = defaultExpressionFor(field).orElse($null);
            $setter.body().assign($this.ref($field), $value);
            javadocBreak($setter).append(FIELD_INITIALISATION.format(javadocNameOf(field.parent()), javadocNameOf($field), render($value)));
        }
        javadocBreak($setter).append(INITIALISATION_END.text());
    }

    /**
     * Creates a single property assignment statement within the given {@linkplain JMethod setter method/constructor}, and also appends the according Javadoc messages.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method/constructor to put the assignment statements into
     * @param $expression
     *            the current expression to use for the property assignment
     */
    protected static final void accordingAssignmentAndJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter,
                                                              final JExpression $expression) {
        final var $default = defaultExpressionFor(property.getKey());
        final var $nonNull = effectiveExpressionForNonNull(property.getValue().type(), $expression);
        accordingAssignmentAndJavadoc(property, $setter, $expression, $default, $nonNull);
    }

    /**
     * Creates a single property assignment statement within the given {@linkplain JMethod setter method/constructor}, and also appends the according Javadoc messages.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method/constructor to put the assignment statements into
     * @param $expression
     *            the current expression to use for the property assignment
     * @param $default
     *            the fallback default value
     * @param $nonNull
     *            the effective expression to represent the non-{@code null}-case
     */
    protected static final void accordingAssignmentAndJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter,
                                                              final JExpression $expression, final Optional<? extends JExpression> $default, final JExpression $nonNull) {
        accordingAssignment(property, $setter, $expression, $default, $nonNull);
        accordingAssignmentJavadoc(property, $setter, $default);
    }

    /**
     * Creates a single property assignment statement within the given {@linkplain JMethod setter method/constructor}.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method/constructor to put the assignment statements into
     * @param $expression
     *            the current expression to use for the property assignment
     * @param $default
     *            the fallback default value
     * @param $nonNull
     *            the effective expression to represent the non-{@code null}-case
     */
    protected static final void accordingAssignment(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter,
                                                    final JExpression $expression, final Optional<? extends JExpression> $default, final JExpression $nonNull) {
        final var field = property.getKey();
        final var $field = property.getValue();
        if ($field.type().isPrimitive()) {
            // TODO: Handle primitive $field with non-primitive $expression (that may be 'null')
            $setter.body().assign($this.ref($field), $expression);
        } else if ($default.isPresent()) {
            if (render($expression).equals(render($null))) {
                // in this case "this.$field = ($expression == null) ? $default : $nonNull;" is effectively similar to: "this.$field = default;"
                $setter.body().assign($this.ref($field), $default.get());
            } else {
                // in this default case null-check is performed: "this.$field = ($expression == null) ? $default : $nonNull;"
                $setter.body().assign($this.ref($field), cond($expression.eq($null), $default.get(), $nonNull));
            }
        } else if (isRequired(field)) {
            assertThat($default).isNotPresent();
            assertThat(render($expression)).isNotEqualTo(render($null));
            final var $IllegalArgumentException = field.parent().parent().getCodeModel().ref(IllegalArgumentException.class);
            $setter._throws($IllegalArgumentException);
            assertThat(doesThrow($setter, $IllegalArgumentException)).isTrue();
            final var $condition = $setter.body()._if($expression.eq($null));
            $condition._then()._throw(_new($IllegalArgumentException).arg(lit("Required field '" + $field.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($field), $nonNull);
        } else {
            assertThat($default).isNotPresent();
            assertThat(isOptional(field)).isTrue();
            // This is the target expression: "this.$field = ($expression == $null) ? $null : $nonNull;" ...
            if (render($expression).equals(render($null))) {
                // ... but in this case, the target expression is effectively similar to: "this.$field = $null;"
                $setter.body().assign($this.ref($field), $null);
            } else if (render($expression).equals(render($nonNull))) {
                // ... but in this case, the target expression is effectively similar to: "this.$field = $nonNull;"
                $setter.body().assign($this.ref($field), $nonNull);
            } else {
                // ... and in any other case, the target expression will be used without any optimisation/modification.
                $setter.body().assign($this.ref($field), cond($expression.eq($null), $null, $nonNull));
            }
        }
    }

    /**
     * Appends the according property-assignment's Javadoc messages.
     *
     * @param property
     *            the property to consider
     * @param $setter
     *            the method/constructor to append the Javadoc
     * @param $default
     *            the fallback default value
     */
    private static final void accordingAssignmentJavadoc(final Entry<? extends FieldOutline, ? extends JFieldVar> property, final JMethod $setter,
                                                         final Optional<? extends JExpression> $default) {
        final var field = property.getKey();
        final var $field = property.getValue();
        final String javadoc;
        if ($field.type().isPrimitive()) {
            javadoc = PRIMITVE_FIELD.format(javadocNameOf(field.parent()), javadocNameOf($field));
        } else if ($default.isPresent()) {
            javadoc = DEFAULTED_FIELD.format(javadocNameOf(field.parent()), javadocNameOf($field), render($default.get()));
        } else if (isRequired(field)) {
            assertThat($default).isNotPresent();
            javadoc = REQUIRED_FIELD.format(javadocNameOf(field.parent()), javadocNameOf($field));
            final var $IllegalArgumentException = field.parent().parent().getCodeModel().ref(IllegalArgumentException.class);
            assertThat(doesThrow($setter, $IllegalArgumentException)).isTrue();
            if (!$setter.javadoc().addThrows($IllegalArgumentException).contains(ILLEGAL_ARGUMENT.text())) {
                javadocSection($setter.javadoc().addThrows($IllegalArgumentException)).append(ILLEGAL_ARGUMENT.text());
            }
        } else {
            assertThat($default).isNotPresent();
            assertThat(isOptional(field)).isTrue();
            javadoc = OPTIONAL_FIELD.format(javadocNameOf(field.parent()), javadocNameOf($field));
        }
        javadocSection($setter.javadoc().addParam($field)).append(javadoc);
    }

    /**
     * Checks whether or not a given amount of fields contains a candidate causing an {@link IllegalArgumentException} when generating the according fields assignment code. Since
     * the generated code may or may not be aware of default values, this behaviour has to be specified as an additional parameter.
     *
     * @param properties
     *            the amount of fields to analyse
     * @param enableDefault
     *            a flag enabling/disabling the existence of default values
     * @return the subset of all fields that are able to cause an {@link IllegalArgumentException} within its according generated field assignment code
     */
    protected static final Map<FieldOutline, JFieldVar> filterIllegalArgumentExceptionCandidates(final Map<? extends FieldOutline, ? extends JFieldVar> properties, final boolean enableDefault) {
        final var candidates = new LinkedHashMap<FieldOutline, JFieldVar>();
        for (final Entry<? extends FieldOutline, ? extends JFieldVar> property : properties.entrySet()) {
            final var field = property.getKey();
            final var $field = property.getValue();
            final var $default = enableDefault ? defaultExpressionFor(field) : Optional.empty();
            if ($field.type().isPrimitive()) {
                // not a candidate
            } else if ($default.isPresent()) {
                // not a candidate
            } else if (isRequired(field)) {
                assertThat($default).isNotPresent();
                // this is a candidate
                candidates.put(field, $field);
            } else {
                assertThat($default).isNotPresent();
                assertThat(isOptional(field)).isTrue();
                // not a candidate
            }
        }
        return candidates;
    }

}
