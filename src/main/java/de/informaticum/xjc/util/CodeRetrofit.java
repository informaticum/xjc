package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeModelAnalysis.allThrows;
import static java.lang.String.format;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JMethod;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions for refactoring the generated code.
 */
public enum CodeRetrofit {
    ;

    /*pkg*/ static final String BODY_FIELD = "body";

    private static final String JAVADOC_HARD_BREAK = format("%n%n");
    private static final String JAVADOC_SOFT_BREAK = format("%n");

    /**
     * Erases the existing Javadoc elements. This is a live operation and will clear the given Javadoc directly.
     *
     * @param <JCP>
     *            the specific subtype of {@link JCommentPart}
     * @param $javadoc
     *            the Javadoc to clear
     * @return the cleared Javadoc
     */
    public static final <JCP extends JCommentPart> JCP eraseJavadoc(final JCP $javadoc) {
        // TODO: even replace with custom JDocComment to support @implNote?
        $javadoc.clear();
        return $javadoc;
    }

    /**
     * Adds a new section within the given Javadoc. (Actually, it adds two newlines iff there is at least one element within the given Javadoc.)
     *
     * @param $javadoc
     *            the Javadoc to deal with
     * @return the complemented Javadoc
     */
    public static final JDocComment javadocSection(final JDocCommentable $javadoc) {
        return javadocSection($javadoc.javadoc());
    }

    /**
     * Adds a new section within the given Javadoc. (Actually, it adds two newlines iff there is at least one element within the given Javadoc.)
     *
     * @param $javadoc
     *            the Javadoc to deal with
     * @return the complemented Javadoc
     */
    public static final JCommentPart javadocSection(final JCommentPart $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_HARD_BREAK);
    }

    /**
     * Adds a new section within the given Javadoc. (Actually, it adds two newlines iff there is at least one element within the given Javadoc.)
     *
     * @param $javadoc
     *            the Javadoc to deal with
     * @return the complemented Javadoc
     */
    public static final JDocComment javadocSection(final JDocComment $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_HARD_BREAK);
    }

    /**
     * Adds a break within the given Javadoc. (Actually, it adds one newlines iff there is at least one element within the given Javadoc.)
     *
     * @param $javadoc
     *            the Javadoc to deal with
     * @return the complemented Javadoc
     */
    public static final JDocComment javadocBreak(final JDocCommentable $javadoc) {
        return javadocBreak($javadoc.javadoc());
    }

    /**
     * Adds a break within the given Javadoc. (Actually, it adds one newlines iff there is at least one element within the given Javadoc.)
     *
     * @param $javadoc
     *            the Javadoc to deal with
     * @return the complemented Javadoc
     */
    public static final JCommentPart javadocBreak(final JCommentPart $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_SOFT_BREAK);
    }

    /**
     * Adds a break within the given Javadoc. (Actually, it adds one newlines iff there is at least one element within the given Javadoc.)
     *
     * @param $javadoc
     *            the Javadoc to deal with
     * @return the complemented Javadoc
     */
    public static final JDocComment javadocBreak(final JDocComment $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_SOFT_BREAK);
    }

    /**
     * Erases all existing body statement. This is a live operation and will clear the given method's body directly.
     *
     * @param $method
     *            the method/constructor to clear
     * @return the cleared body
     */
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

    /**
     * Adds {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#allThrows(JMethod) all thrown exception types} of the origin method/constructor into the calling
     * method/constructor.
     *
     * @param $origin
     *            the origin method/constructor
     * @param $caller
     *            the calling method/constructor
     * @see #relayThrows(JMethod, JMethod, String)
     */
    public static final void relayThrows(final JMethod $origin, final JMethod $caller) {
        relayThrows($origin, $caller, null);
    }

    /**
     * Adds {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#allThrows(JMethod) all thrown exception types} of the origin method/constructor into the calling
     * method/constructor.
     *
     * @param $origin
     *            the origin method/constructor
     * @param $caller
     *            the calling method/constructor
     * @param reason
     *            the Javadoc message to append for each relayed exception type (may be {@code null} to skip Javadoc appending)
     */
    public static final void relayThrows(final JMethod $origin, final JMethod $caller, final String reason) {
        for (final var $throwable : allThrows($origin)) {
            $caller._throws($throwable);
            if (reason != null) {
                $caller.javadoc().addThrows($throwable).append(reason);
            }
        }
    }

}
