package de.informaticum.xjc.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JGenerable;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.CustomizableOutline;
import com.sun.tools.xjc.outline.PackageOutline;

public enum Printify {
    ;

    public static final String fullName(final PackageOutline pakkage) {
        return pakkage._package().name();
    }

    public static final String fullName(final CustomizableOutline outline) {
        return fullName(outline.getImplClass());
    }

    public static final String fullName(final JType type) {
        return type.fullName();
    }

    public static final String render(final JGenerable generable) {
        final var out = new StringWriter();
        generable.generate(new JFormatter(new PrintWriter(out)));
        return out.toString();
    }

}
