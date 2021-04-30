package de.informaticum.xjc.util;

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.lit;
import static de.informaticum.xjc.util.CollectionAnalysis.accordingDefaultFactoryFor;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Optional;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.outline.FieldOutline;
import org.slf4j.Logger;

public enum DefaultAnalysis {
    ;

    private static final Logger LOG = getLogger(DefaultAnalysis.class);

    /* see: https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html */
    public static final Optional<JExpression> defaultValueFor(final FieldOutline field) {
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
        if (raw.equals(codeModel.BOOLEAN)) return Optional.of(FALSE);
        if (raw.equals(codeModel.BYTE))    return Optional.of(lit(0));
        if (raw.equals(codeModel.CHAR))    return Optional.of(lit('\u0000'));
        if (raw.equals(codeModel.DOUBLE))  return Optional.of(lit(0.0d));
        if (raw.equals(codeModel.FLOAT))   return Optional.of(lit(0.0f));
        if (raw.equals(codeModel.INT))     return Optional.of(lit(0));
        if (raw.equals(codeModel.LONG))    return Optional.of(lit(0L));
        if (raw.equals(codeModel.SHORT))   return Optional.of(lit(0));        
        if (property.isCollection()) return Optional.of(accordingDefaultFactoryFor(field.getRawType()));
        return Optional.empty();
    }

}
