package de.informaticum.xjc.plugins;

import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.optionalTypeFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.unmodifiableViewFactoryFor;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map.Entry;
import java.util.Optional;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.util.CodeModelAnalysis;

/*package*/ final class GetterBricks {

    /*package*/ final Entry<? extends FieldOutline, ? extends JMethod> getter;

    /*package*/ final JFieldVar $property;

    /*package*/ final FieldOutline attribute;

    /*package*/ final CPropertyInfo attributeInfo;

    /*package*/ final JMethod $getter;

    /*package*/ final JType $returnType;

    /*package*/ final JClass $OptionalType;

    /*package*/ final JFieldRef $prop;

    /*package*/ final Optional<JExpression> $default;

    /*package*/ final JExpression $nonNull;

    /*package*/ final JInvocation $optionalEmpty;

    /*package*/ final JInvocation $optionalOf;

    /*package*/ final JInvocation $view() {
        // no precalculation, on-demand only ($returnType might be non-collection type and that causes an IllegalArgumentException)
        assertThat(this.$returnType).matches(CodeModelAnalysis::isCollectionType);
        return unmodifiableViewFactoryFor(this.$returnType).arg(this.$prop);
    }

    /*package*/ GetterBricks(final Entry<? extends FieldOutline, ? extends JMethod> getter, final JFieldVar $property) {
        this.getter = getter;
        this.$property = $property;
        this.attribute = this.getter.getKey();
        this.attributeInfo = this.attribute.getPropertyInfo();
        this.$getter = this.getter.getValue();
        this.$returnType = this.$getter.type();
        this.$OptionalType = optionalTypeFor(this.$returnType);
        this.$prop = $this.ref(this.$property);
        this.$default = PropertyPlugin.defaultExpressionFor(this.attribute);
        this.$nonNull = PropertyPlugin.effectiveExpressionForNonNull(this.$property.type(), this.$prop);
        this.$optionalEmpty = this.$OptionalType.erasure().staticInvoke("empty");
        this.$optionalOf = this.$OptionalType.erasure().staticInvoke("of");
    }

}
