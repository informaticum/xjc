package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeModelAnalysis.allThrows;
import static java.lang.String.format;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JMethod;

public enum CodeRetrofit {
    ;

    /*pkg*/ static final String BODY_FIELD = "body";

    private static final String JAVADOC_HARD_BREAK = format("%n%n");
    private static final String JAVADOC_SOFT_BREAK = format("%n");

    public static final <JCP extends JCommentPart> JCP eraseJavadoc(final JCP $javadoc) {
        // TODO: even replace with custom JDocComment to support @implNote?
        $javadoc.clear();
        return $javadoc;
    }

    public static final JDocComment javadocSection(final JDocCommentable $target) {
        return javadocSection($target.javadoc());
    }

    public static final JCommentPart javadocSection(final JCommentPart $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_HARD_BREAK);
    }

    public static final JDocComment javadocSection(final JDocComment $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_HARD_BREAK);
    }

    public static final JDocComment javadocBreak(final JDocCommentable $target) {
        return javadocBreak($target.javadoc());
    }

    public static final JCommentPart javadocBreak(final JCommentPart $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_SOFT_BREAK);
    }

    public static final JDocComment javadocBreak(final JDocComment $javadoc) {
        return ($javadoc.isEmpty()) ? $javadoc : $javadoc.append(JAVADOC_SOFT_BREAK);
    }

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
