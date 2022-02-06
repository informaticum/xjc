package de.informaticum.xjc.util;

import static java.lang.String.format;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JMethod;
import de.informaticum.xjc.resources.ResourceBundleEntry;

public enum CodeRetrofit {
    ;

    static final String BODY_FIELD = "body";

    public static final JBlock eraseBody(final JMethod $method) {
        try {
            final var internalBodyField = JMethod.class.getDeclaredField(BODY_FIELD);
            internalBodyField.setAccessible(true);
            internalBodyField.set($method, null);
            return $method.body();
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
            throw new RuntimeException(seriousProblem);
        }
    }

    public static final void javadocDelimiter(final JCommentPart $javadoc) {
        if (!$javadoc.isEmpty()) {
            $javadoc.add(format("%n%n<p>"));
        }
    }

    public static final void javadocInheritdoc(final JCommentPart $javadoc) {
        $javadoc.add($javadoc.isEmpty() ? "{@inheritDoc}" : format("%n%n{@inheritDoc}"));
    }

    public static final void javadocAppendSection(final JDocCommentable $target, final ResourceBundleEntry key, final Object... arguments) {
        javadocAppendSection($target.javadoc(), key, arguments);
    }

    public static final void javadocAppendSection(final JCommentPart $javadoc, final ResourceBundleEntry key, final Object... arguments) {
        javadocDelimiter($javadoc);
        $javadoc.add(key.format(arguments));
    }

}
