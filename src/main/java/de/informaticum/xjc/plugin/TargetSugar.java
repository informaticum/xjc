package de.informaticum.xjc.plugin;

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr._super;
import static com.sun.codemodel.JExpr._this;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;

public enum TargetSugar {
    ;

    public static final JExpression $super = _super();

    public static final JExpression $this = _this();

    public static final JExpression $null = _null();

    public static final JPrimitiveType $void = new com.sun.codemodel.JCodeModel().VOID;

}
