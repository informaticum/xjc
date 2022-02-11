package de.informaticum.xjc;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
import static de.informaticum.xjc.util.DefaultAnalysis.defensiveCopyFor;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.BasePlugin;

public abstract class AssignmentPlugin
extends BasePlugin {

    protected abstract BooleanSupplier initCollections();

    protected abstract BooleanSupplier createUnmodifiableCollections();

    protected abstract BooleanSupplier createDefensiveCopies();

    protected final void accordingInitialisation(final Entry<FieldOutline, JFieldVar> property, final JMethod $method,
                                                 final BiConsumer<? super JMethod, ? super JExpression> onInitialisation) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $value = defaultValueFor(attribute, this.initCollections(), this.createUnmodifiableCollections()).orElse($null);
        $method.body().assign($this.ref($property), $value);
        onInitialisation.accept($method, $value);
    }

    protected final void accordingAssignment(final Entry<FieldOutline, JFieldVar> property, final JMethod $method, final JExpression $expression,
                                             final Consumer<? super JMethod> onPrimitive, final BiConsumer<? super JMethod, ? super JExpression> onDefault,
                                             final Consumer<? super JMethod> onRequired, final Consumer<? super JMethod> onFallback) {
        final var attribute = property.getKey();
        final var $property = property.getValue();
        final var $default = defaultValueFor(attribute, this.initCollections(), this.createUnmodifiableCollections());
        final var $defensiveCopy = defensiveCopyFor(attribute, $property, $expression, this.createDefensiveCopies());
        if ($property.type().isPrimitive()) {
            // TODO: Handle primitive $property with non-primitive $expression (that may be 'null')
            $method.body().assign($this.ref($property), $expression);
            onPrimitive.accept($method);
        } else if ($default.isPresent()) {
            // TODO: if ($expression.equals($null)) {
            //     $method.body().assign($this.ref($property), $default.get());
            // } else {
                $method.body().assign($this.ref($property), cond($expression.eq($null), $default.get(), $defensiveCopy));
                onDefault.accept($method, $default.get());
            // }
        } else if (isRequired(attribute)) {
            // TODO: assertThat($expression).isNotEqualTo($null);
            $method._throws(IllegalArgumentException.class);
            final var $condition = $method.body()._if($expression.eq($null));
            $condition._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), $defensiveCopy);
            onRequired.accept($method);
        } else {
            if ($defensiveCopy == $expression) {
                $method.body().assign($this.ref($property), $expression);
            // TODO: } else if ($expression.equals($null)) {
            //    $method.body().assign($this.ref($property), $expression);
            } else {
                $method.body().assign($this.ref($property), cond($expression.eq($null), $null, $defensiveCopy));
            }
            onFallback.accept($method);
        }
    }

}
