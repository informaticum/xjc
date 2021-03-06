package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.CodeModelAnalysis.allJavadocParams;
import static de.informaticum.xjc.util.CodeModelAnalysis.allJavadocThrows;
import static de.informaticum.xjc.util.CodeModelAnalysis.allThrows;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.LinkedHashMap;
import java.util.Map;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;

/**
 * Util class (technically a non-instantiable enum container) to provide some helper functions for refactoring the generated code.
 */
public enum CodeRetrofit {
    ;

    /*pkg*/ static final String ANNOTATION_MEMBERS = "memberValues";
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

    public static final String COPY_JAVADOC = "###-Copy-Javadoc-Marker-###";

    /**
     * Adds {@linkplain de.informaticum.xjc.util.CodeModelAnalysis#allThrows(JMethod) all thrown exception types} of the origin method/constructor into the calling
     * method/constructor.
     *
     * @param $origin
     *            the origin method/constructor
     * @param $caller
     *            the calling method/constructor
     * @param reason
     *            the Javadoc message to append for each relayed exception type (may be {@code null} to copy the origin Javadoc message)
     */
    public static final void relayThrows(final JMethod $origin, final JMethod $caller, final String reason) {
        final var javadocThrows = allJavadocThrows($origin);
        for (final var $throwable : allThrows($origin)) {
            $caller._throws($throwable);
            if (reason == null) {
                // do not copy Javadoc
            } else if (COPY_JAVADOC.equals(reason)) {
                assertThat(javadocThrows).containsKey($throwable);
                $caller.javadoc().addThrows($throwable).addAll($origin.javadoc().addThrows($throwable));
            } else {
                $caller.javadoc().addThrows($throwable).append(reason);
            }
        }
    }

    /**
     * Adds a specific parameter's {@code @param}-Javadoc description of the origin method/constructor into the calling method/constructor.
     *
     * @param $origin
     *            the origin method/constructor
     * @param $caller
     *            the calling method/constructor
     * @param $param
     *            the parameter to look for
     */
    public static final void relayParamDoc(final JMethod $origin, final JMethod $caller, final JVar $param) {
        final var javadocParams = allJavadocParams($origin);
        assertThat(javadocParams).containsKey($param.name());
        $caller.javadoc().addParam($param).addAll($origin.javadoc().addParam($param));
    }

    /**
     * Attaches a given annotation onto a given target, including all annotation's values.
     * 
     * @param $annotation
     *            the given annotation to attach/copy
     * @param $target
     *            the target to annotate
     * @return the attached/copied annotation
     */
    public static final JAnnotationUse copyAnnotation(final JAnnotationUse $annotation, final JAnnotatable $target) {
        final var $copy = $target.annotate($annotation.getAnnotationClass());
        $copy.getAnnotationMembers(); // initialises the internal field
        try {
            final var internalMembersField = JAnnotationUse.class.getDeclaredField(ANNOTATION_MEMBERS);
            internalMembersField.setAccessible(true);
            var $members = (Map<String, JAnnotationValue>) internalMembersField.get($copy);
            if ($members == null) {
                internalMembersField.set($copy, new LinkedHashMap<String, JAnnotationValue>());
                $members = (Map<String, JAnnotationValue>) internalMembersField.get($copy);
            }
            assertThat($members).isNotNull();
            $members.putAll($annotation.getAnnotationMembers());
            return $copy;
        } catch (final IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException seriousProblem) {
            throw new RuntimeException(seriousProblem);
        }
    }

}
