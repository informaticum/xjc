package de.informaticum.xjc.util;

import static java.lang.String.format;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JMethod;
import de.informaticum.xjc.resources.ResourceBundleKeys;

public enum CodeRetrofit {
    ;

    static final String BODY_FIELD = "body";

    public static final JMethod eraseBody(final JMethod $method) {
        try {
            final var internalBodyField = JMethod.class.getDeclaredField(BODY_FIELD);
            internalBodyField.setAccessible(true);
            internalBodyField.set($method, null);
            return $method;
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
            throw new RuntimeException(seriousProblem);
        }
    }

    public static final void javadocDelimiter(final JDocComment $javadoc) {
        if (!$javadoc.isEmpty()) {
            $javadoc.append(format("%n%n<p>"));
        }
    }

    public static final void javadocInheritdoc(final JDocComment $javadoc) {
        $javadoc.append($javadoc.isEmpty() ? "{@inheritDoc}" : format("%n%n{@inheritDoc}"));
    }

    public static final void javadocAppendSection(final JDocCommentable $target, final ResourceBundleKeys key, final Object... arguments) {
        javadocAppendSection($target.javadoc(), key, arguments);
    }

    public static final void javadocAppendSection(final JDocComment $javadoc, final ResourceBundleKeys key, final Object... arguments) {
        javadocDelimiter($javadoc);
        $javadoc.append(key.apply(arguments));
    }

}
