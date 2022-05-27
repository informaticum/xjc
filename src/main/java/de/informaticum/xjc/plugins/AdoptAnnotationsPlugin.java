package de.informaticum.xjc.plugins;

import static com.sun.codemodel.JExpr.direct;
import static com.sun.codemodel.JExpr.lit;
import static de.informaticum.xjc.util.CodeModelAnalysis.render;
import static de.informaticum.xjc.util.CodeRetrofit.copyAnnotation;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import java.util.List;
import java.util.Optional;
import javax.annotation.processing.Generated;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JAnnotationUse;
import com.sun.tools.xjc.Plugin;
import de.informaticum.xjc.api.BasePlugin;
import de.informaticum.xjc.api.CommandLineArgument;
import de.informaticum.xjc.api.XjcOption;

/**
 * Base class for custom {@linkplain Plugin XJC plug-ins} for the purpose of adopting the {@link Generated}-annotation behaviour of the
 * {@linkplain com.sun.tools.xjc.addon.at_generated.PluginImpl AtGenerated-Plugin}. In detail, it adopts the XJC options {@link #ADOPT_GENERATED}, {@link #ADOPT_GENERATED_NODATE},
 * and {@link #ADOPT_GENERATED_CLASS}. Depending on whether they are activated or not, the code will similarly create such annotations (or will copy and adopt these if already
 * present).
 * 
 * @implNote This plug-in adopts the {@link Generated}-annotation behaviour of the {@linkplain com.sun.tools.xjc.addon.at_generated.PluginImpl AtGenerated-Plugin}. Thus, the
 *           arguments of this plug-in must be listed as {@linkplain #getCadgerArguments() cadger arguments}.
 */
public abstract class AdoptAnnotationsPlugin
extends BasePlugin {

    /**
     * The fully-qualified name of the default {@code Generated} annotation. It copies the origin default value of {@link com.sun.tools.xjc.addon.at_generated.PluginImpl}.
     *
     * @implNote This value represents the {@code @Generated} annotation class of Java 1.6. Many of the JavaEE code has been outsourced into the Jakarta project and, thus, the
     *           successor class is {@code jakarta.annotation.Generated}. (Actually Java 9 moved this annotation into module {@code java.xml.ws.annotation} (see
     *           <a href="http://openjdk.java.net/jeps/320#Java-EE-modules">JEP 320</a>) and within Java 11 this annotation has been removed in preference to the Jakarta project
     *           where this class has been relocated into another package.) However, Java 9 comes along with a new {@code Generated} annotation, placed within the package
     *           {@code javax.annotation.processing} and within the module {@code java.compiler}. As you can see, the choice is yours.
     */
    private static final String JAVAX_ANNOTATION_GENERATED = "javax.annotation.Generated";

    /**
     * The option name of the origin {@code Generated}-plug-in: {@link com.sun.tools.xjc.addon.at_generated.PluginImpl#getOptionName()}.
     */
    protected static final CommandLineArgument ADOPT_GENERATED = new CommandLineArgument("mark-generated", "mark the generated code as @" + JAVAX_ANNOTATION_GENERATED);

    /**
     * The additional argument of the origin {@code Generated}-plug-in to decide whether or not to include a date field:
     * {@link com.sun.tools.xjc.addon.at_generated.PluginImpl#getUsage()}.
     */
    protected static final CommandLineArgument ADOPT_GENERATED_NODATE = new CommandLineArgument("noDate", "do not add date");

    /**
     * The additional argument of the origin {@code Generated}-plug-in to specify an alternate {@code Generated} annotation class:
     * {@link com.sun.tools.xjc.addon.at_generated.PluginImpl#getUsage()}.
     */
    protected static final CommandLineArgument ADOPT_GENERATED_CLASS = new CommandLineArgument("Xann", "generate <annotation> instead of @" + JAVAX_ANNOTATION_GENERATED, "annotation");

    /**
     * If there is no template date, this singleton timestamp will be used.
     * 
     * @see com.sun.tools.xjc.addon.at_generated.PluginImpl#date
     * @see com.sun.tools.xjc.addon.at_generated.PluginImpl#getISO8601Date()
     */
    private static final String TIMESTAMP = now().withNano(0).format(ISO_OFFSET_DATE_TIME);

    @Override
    public List<XjcOption> getCadgerArguments() {
        return asList(ADOPT_GENERATED, ADOPT_GENERATED_NODATE, ADOPT_GENERATED_CLASS);
    }

    /**
     * Appends a specific comment into the existing {@code Generated} annotation of a given target. If no such annotation exists, the annotation will be
     * {@linkplain de.informaticum.xjc.util.CodeRetrofit#copyAnnotation(com.sun.codemodel.JAnnotationUse, JAnnotatable) copied} from the given template component. If there is no
     * such template annotation, a new empty annotation will be attached.
     *
     * @implNote This method does not set/alter the {@code value} field of the {@code Generated} annotation. It neither sets/alters the {@code date} field.
     * @param $template
     *            the template component to copy the {@code Generated} annotation from (if necessary and if present)
     * @param $target
     *            the target of the {@code Generated} annotation
     * @param comment
     *            the comment to put into the {@code Generated} annotation
     * @return the annotation instance for further actions
     */
    protected final Optional<JAnnotationUse> appendGeneratedAnnotation(final JAnnotatable $template, final JAnnotatable $target, final String comment) {
        return ADOPT_GENERATED.doOnActivation(() -> this.getOrCopyOrAttachAnnotation($template, $target, comment))
                              .map($a -> setDateIfMissing($a))
                              .map($a -> appendComment($a, comment));
    }

    /**
     * Hijacks an existing {@code Generated} annotation of a given target. If no such annotation exists, the annotation will be
     * {@linkplain de.informaticum.xjc.util.CodeRetrofit#copyAnnotation(com.sun.codemodel.JAnnotationUse, JAnnotatable) copied} from the given template component. If there is no
     * such template annotation, a new empty annotation will be attached.
     * 
     * @implNote This method sets the {@code date} field if there is no current date value and unless {@link #ADOPT_GENERATED_NODATE date is suppressed}.
     * @param $template
     *            the template component to copy the {@code Generated} annotation from (if necessary and if present)
     * @param $target
     *            the target of the {@code Generated} annotation
     * @param driver
     *            the XJC code generator class to be named as the origin of the {@code Generated} annotation
     * @param comment
     *            the comment to put into the {@code Generated} annotation
     * @return the annotation instance for further actions
     */
    protected final Optional<JAnnotationUse> hijackGeneratedAnnotation(final JAnnotatable $template, final JAnnotatable $target, final Class<?> driver, final String comment) {
        return ADOPT_GENERATED.doOnActivation(() -> this.getOrCopyOrAttachAnnotation($template, $target, comment))
                              .map($a -> setDriver($a, driver))
                              .map($a -> setDateIfMissing($a))
                              .map($a -> setComment($a, comment));
    }

    private final JAnnotationUse getOrCopyOrAttachAnnotation(final JAnnotatable $template, final JAnnotatable $target, final String comment) {
        final var annotationClassName = ofNullable(ADOPT_GENERATED_CLASS.getParameterValues().get(0)).orElse(JAVAX_ANNOTATION_GENERATED);
        final var $annotationClass = this.reference(annotationClassName);
        return $target.annotations().stream()
                                    // either (a) find an existing annotation
                                    .filter($a -> $a.getAnnotationClass().compareTo($annotationClass) == 0).findFirst()
                                    .orElseGet(() -> $template.annotations().stream()
                                                              // or (b) copy an annotation if present
                                                              .filter($a -> $a.getAnnotationClass().compareTo($annotationClass) == 0).findFirst().map($a -> copyAnnotation($a, $target))
                                                              // or (c) create an annotation
                                                              .orElseGet(() -> $target.annotate($annotationClass)));
    }

    private static final JAnnotationUse setDriver(final JAnnotationUse $annotation, final Class<?> driver) {
        return $annotation.param("value", driver.getName());
    }

    private static final JAnnotationUse setDateIfMissing(final JAnnotationUse $annotation) {
        return ((!ADOPT_GENERATED_NODATE.isActivated()) && ($annotation.getAnnotationMembers().get("date") == null)) ? $annotation.param("date", TIMESTAMP) : $annotation;
    }

    private static final JAnnotationUse setComment(final JAnnotationUse $annotation, final String comment) {
        return $annotation.param("comments", comment);
    }

    private static final JAnnotationUse appendComment(final JAnnotationUse $annotation, final String addendum) {
        final var $currentComments = $annotation.getAnnotationMembers().get("comments");
        final var currentCommentsValue = ($currentComments == null) ? "" : render($currentComments);
        if (currentCommentsValue.isBlank()) {
            return $annotation.param("comments", addendum);
        } else {
            return $annotation.param("comments", direct(currentCommentsValue).plus(lit(" " + addendum)));
        }
    }

}
