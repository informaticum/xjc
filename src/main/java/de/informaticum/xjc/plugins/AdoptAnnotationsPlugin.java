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
import javax.annotation.processing.Generated;
import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JDefinedClass;
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
     * {@linkplain de.informaticum.xjc.util.CodeRetrofit#copyAnnotation(com.sun.codemodel.JAnnotationUse, JAnnotatable) copied} from the given outer class. If there is no such
     * template annotation, an empty annotation will be attached.
     *
     * @implNote This method does not set/alter the {@code value} field of the {@code Generated} annotation. It neither sets/alters the {@code date} field.
     * @param $outer
     *            the outer class to copy the {@code Generated} annotation from (if necessary)
     * @param $target
     *            the target of the {@code Generated} annotation
     * @param comment
     *            the comment to put into the {@code Generated} annotation
     */
    protected final void appendGeneratedAnnotation(final JDefinedClass $outer, final JAnnotatable $target, final String comment) {
        if (ADOPT_GENERATED.isActivated()) {
            final var $annotationClass = this.reference(ofNullable(ADOPT_GENERATED_CLASS.getParameterValues().get(0)).orElse(JAVAX_ANNOTATION_GENERATED));
            final var $outerAnnotation = $outer.annotations().stream().filter(a -> a.getAnnotationClass().compareTo($annotationClass) == 0).findFirst();
            final var $annotation = $target.annotations().stream()
                                           .filter(a -> a.getAnnotationClass().compareTo($annotationClass) == 0).findFirst()
                                           .orElseGet(() -> $outerAnnotation.map($a -> copyAnnotation($a, $target)).orElseGet(() -> $target.annotate($annotationClass)));
            if (!ADOPT_GENERATED_NODATE.isActivated() && $annotation.getAnnotationMembers().get("date") == null) {
                $annotation.param("date", TIMESTAMP);
            }
            final var $currentComments = $annotation.getAnnotationMembers().get("comments");
            final var currentCommentsValue = ($currentComments == null) ? "" : render($currentComments);
            if (currentCommentsValue.isBlank()) {
                $annotation.param("comments", comment);
            } else {
                $annotation.param("comments", direct(currentCommentsValue).plus(lit(" " + comment)));
            }
        }
    }

    /**
     * Hijacks an existing {@code Generated} annotation of a given target. If no such annotation exists, the annotation will be
     * {@linkplain de.informaticum.xjc.util.CodeRetrofit#copyAnnotation(com.sun.codemodel.JAnnotationUse, JAnnotatable) copied} from the given outer class. If there is no such
     * template annotation, an empty annotation will be attached.
     * 
     * @implNote This method sets the {@code date} field if there is no current date value and unless {@link #ADOPT_GENERATED_NODATE date is suppressed}.
     * @param $outer
     *            the outer class to copy the {@code Generated} annotation from (if necessary)
     * @param $target
     *            the target of the {@code Generated} annotation
     * @param driver
     *            the XJC code generator class to be named as the origin of the {@code Generated} annotation
     * @param comment
     *            the comment to put into the {@code Generated} annotation
     */
    protected final void hijackGeneratedAnnotation(final JDefinedClass $outer, final JAnnotatable $target, final Class<?> driver, final String comment) {
        if (ADOPT_GENERATED.isActivated()) {
            final var $annotationClass = this.reference(ofNullable(ADOPT_GENERATED_CLASS.getParameterValues().get(0)).orElse(JAVAX_ANNOTATION_GENERATED));
            final var $outerAnnotation = $outer.annotations().stream().filter(a -> a.getAnnotationClass().compareTo($annotationClass) == 0).findFirst();
            final var $annotation = $target.annotations().stream()
                                           .filter(a -> a.getAnnotationClass().compareTo($annotationClass) == 0).findFirst()
                                           .orElseGet(() -> $outerAnnotation.map($a -> copyAnnotation($a, $target)).orElseGet(() -> $target.annotate($annotationClass)));
            $annotation.param("value", driver.getName());
            if (!ADOPT_GENERATED_NODATE.isActivated() && $annotation.getAnnotationMembers().get("date") == null) {
                $annotation.param("date", TIMESTAMP);
            }
            $annotation.param("comments", comment);
        }
    }

}
