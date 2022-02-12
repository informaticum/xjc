package de.informaticum.xjc;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFAULTED_OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.DEFAULTED_REQUIRED_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.FIELD_INITIALISATION;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.ILLEGAL_VALUE;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.PRIMITVE_ARGUMENT;
import static de.informaticum.xjc.resources.AssignmentPluginMessages.REQUIRED_ARGUMENT;
import static de.informaticum.xjc.util.CodeRetrofit.javadocAppendSection;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
import static de.informaticum.xjc.util.DefaultAnalysis.defensiveCopyFor;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.Printify.render;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.BasePlugin;

public abstract class AssignmentPlugin
extends BasePlugin {

    protected abstract BooleanSupplier initCollections();

    protected abstract BooleanSupplier createUnmodifiableCollections();

    protected abstract BooleanSupplier createDefensiveCopies();

    protected final void accordingInitialisation(final Entry<FieldOutline, JFieldVar> property, final JMethod $setter) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $value = defaultValueFor(attribute, this.initCollections(), this.createUnmodifiableCollections()).orElse($null);
        $setter.body().assign($this.ref($property), $value);
        javadocAppendSection($setter.javadoc(), FIELD_INITIALISATION, property.getValue().name(), render($value));
    }

    protected final void accordingSuperAssignment(final Entry<FieldOutline, JFieldVar> property, final JMethod $setter, final JInvocation $super, final JExpression $expression) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $default = defaultValueFor(attribute, this.initCollections(), this.createUnmodifiableCollections());
        $super.arg($expression);
        if ($property.type().isPrimitive()) {
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), PRIMITVE_ARGUMENT, property.getValue().name());
        } else if ($default.isPresent()) {
            // TODO: Different Javadoc message for collection types?
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), isRequired(property.getKey()) ? DEFAULTED_REQUIRED_ARGUMENT : DEFAULTED_OPTIONAL_ARGUMENT, property.getValue().name(), render($default.get()));
        } else if (isRequired(attribute)) {
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), REQUIRED_ARGUMENT, property.getValue().name());
            javadocAppendSection($setter.javadoc().addThrows(IllegalArgumentException.class), ILLEGAL_VALUE);
        } else {
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), OPTIONAL_ARGUMENT, property.getValue().name());
        }
    }

    protected final void accordingAssignment(final Entry<FieldOutline, JFieldVar> property, final JMethod $setter, final JExpression $expression) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $default = defaultValueFor(attribute, this.initCollections(), this.createUnmodifiableCollections());
        final var $defensiveCopy = defensiveCopyFor(attribute, $property, $expression, this.createDefensiveCopies());
        if ($property.type().isPrimitive()) {
            // TODO: Handle primitive $property with non-primitive $expression (that may be 'null')
            $setter.body().assign($this.ref($property), $expression);
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), PRIMITVE_ARGUMENT, property.getValue().name());
        } else if ($default.isPresent()) {
            if ($expression.equals($null)) {
                $setter.body().assign($this.ref($property), $default.get());
            } else {
                $setter.body().assign($this.ref($property), cond($expression.eq($null), $default.get(), $defensiveCopy));
            }
            // TODO: Different Javadoc message for collection types?
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), isRequired(property.getKey()) ? DEFAULTED_REQUIRED_ARGUMENT : DEFAULTED_OPTIONAL_ARGUMENT, property.getValue().name(), render($default.get()));
        } else if (isRequired(attribute)) {
            assertThat($expression).isNotEqualTo($null);
            $setter._throws(IllegalArgumentException.class);
            final var $condition = $setter.body()._if($expression.eq($null));
            $condition._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), $defensiveCopy);
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), REQUIRED_ARGUMENT, property.getValue().name());
            javadocAppendSection($setter.javadoc().addThrows(IllegalArgumentException.class), ILLEGAL_VALUE);
        } else {
            if (($defensiveCopy == $expression) || $expression.equals($null)) {
                $setter.body().assign($this.ref($property), $expression);
            } else {
                $setter.body().assign($this.ref($property), cond($expression.eq($null), $null, $defensiveCopy));
            }
            javadocAppendSection($setter.javadoc().addParam(property.getValue()), OPTIONAL_ARGUMENT, property.getValue().name());
        }
    }

}
