package de.informaticum.xjc;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.util.CollectionAnalysis.copyFactoryFor;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.function.BooleanSupplier;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.BasePlugin;
import org.slf4j.Logger;

public abstract class AssignmentPlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(AssignmentPlugin.class);

    protected abstract BooleanSupplier initCollections();

    protected abstract BooleanSupplier createUnmodifiableCollections();

    protected final void accordingAssignment(final FieldOutline attribute, final JMethod $method, final JFieldVar $property, final JExpression $expression) {
        final var $default = defaultValueFor(attribute, this.initCollections(), this.createUnmodifiableCollections());
        final var $defensiveCopy = this.potentialDefensiveCopy(attribute, $property, $expression);
        if ($property.type().isPrimitive()) {
            // TODO: Handle primitive $property with non-primitive $expression (that may be 'null')
            $method.body().assign($this.ref($property), $expression);
        } else if ($default.isPresent()) {
            // TODO: if ($expression.equals($null)) {
            //     $method.body().assign($this.ref($property), $default.get());
            // } else {
                $method.body().assign($this.ref($property), cond($expression.eq($null), $default.get(), $defensiveCopy));
            // }
        } else if (isRequired(attribute)) {
            // TODO: assertThat($expression).isNotEqualTo($null);
            $method._throws(IllegalArgumentException.class);
            final var $condition = $method.body()._if($expression.eq($null));
            $condition._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), $defensiveCopy);
        } else {
            if ($defensiveCopy == $expression) {
                $method.body().assign($this.ref($property), $expression);
            // TODO: } else if ($expression.equals($null)) {
            //    $method.body().assign($this.ref($property), $expression);
            } else {
                $method.body().assign($this.ref($property), cond($expression.eq($null), $null, $defensiveCopy));
            }
        }
    }

    protected abstract BooleanSupplier createDefensiveCopies();

    protected final JExpression potentialDefensiveCopy(final FieldOutline attribute, final JFieldVar $property, final JExpression $expression) {
        if (this.createDefensiveCopies().getAsBoolean()) {
            // TODO: use copy-constructor if exits
            if (attribute.getPropertyInfo().isCollection()) {
                // TODO: Cloning the collection's elements (a.k.a. deep clone)
                return copyFactoryFor($property.type()).arg($expression);
            } else if ($property.type().isArray()) {
                return cast($property.type(), $expression.invoke("clone"));
            } else if (this.reference(Cloneable.class).isAssignableFrom($property.type().boxify())) {
                // TODO (?): Skip cast if "clone()" already returns required type
                return cast($property.type(), $expression.invoke("clone"));
            } else {
                LOG.debug("Skip defensive copy for [{}] because [{}] is neither Collection, Array, nor Cloneable.", $property.name(), $property.type().boxify().erasure());
            }
        }
        return $expression;
    }

}
