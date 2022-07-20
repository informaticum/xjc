package de.informaticum.xjc.api;

import static de.informaticum.xjc.util.IterationUtil.iterableOf;
import static de.informaticum.xjc.util.IterationUtil.streamOf;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;

/**
 * Convenient actions, based on a given {@link #outline() Outline}.
 */
public abstract interface InitialisedOutline {

    /**
     * @return the current {@link Outline} instance
     */
    public default Outline outline() {
        throw new IllegalStateException("The current 'Outline' instance has not yet been initialised!");
    }

    /**
     * @return the {@link Outline#getModel() model} of the current {@link #outline() Outline}
     */
    public default Model model() {
        return this.outline().getModel();
    }

    /**
     * @return the {@link Outline#getCodeModel() code model} of the current {@link #outline() Outline}
     */
    public default JCodeModel codeModel() {
        return this.outline().getCodeModel();
    }

    /**
     * Searches and returns either the (a) currently created or (b) existing Java class/type identified by the given fully qualified class name.
     *
     * @implNote If the FQCN refers to a currently created class, an according {@link JDefinedClass} instance will be returned. Otherwise, the result of
     *           {@link JCodeModel#ref(String)} will be returned.
     * @param fullyQualifiedClassName
     *            the fully qualified name of the (a) currently created or (b) existing Java class/type to refer to
     * @return a {@linkplain JType reference} to a currently created or to an existing class/type from its name
     * @see JCodeModel#ref(Class)
     */
    public default JType reference(final String fullyQualifiedClassName) {
        try {
            return JType.parse(this.codeModel(), fullyQualifiedClassName);
        } catch (final IllegalArgumentException ignoreNonPrimitiveClass) {
            if (fullyQualifiedClassName.startsWith("[[") && fullyQualifiedClassName.endsWith(";")) {
                /**
                 * Multi-dimensional array of some type according to the binary name as specified by {@link Class.class#getName()}.
                 */
                return this.reference(fullyQualifiedClassName.substring(1)).array();
            } else if (fullyQualifiedClassName.startsWith("[L") && fullyQualifiedClassName.endsWith(";")) {
                /**
                 * One-dimensional array of some type according to the binary name as specified by {@link Class.class#getName()}.
                 */
                return this.reference(fullyQualifiedClassName.substring(2, fullyQualifiedClassName.length() - 1)).array();
            } else if (fullyQualifiedClassName.endsWith("[]")) {
                /**
                 * Array of some type according to the binary name as specified by {@link com.sun.codemodel.JArrayClass#binaryName()}.
                 */
                return this.reference(fullyQualifiedClassName.substring(0, fullyQualifiedClassName.length() - 2)).array();
            } else if (SourceVersion.isName(fullyQualifiedClassName)) {
                for (final var pakkage : iterableOf(this.codeModel().packages())) {
                    if (fullyQualifiedClassName.startsWith(pakkage.name() + ".")) {
                        final var hierarchySuffix = fullyQualifiedClassName.substring((pakkage.name() + ".").length());
                        final var hierarchyNesting = hierarchySuffix.split("\\.");
                        final var lookup = this.find(pakkage, hierarchyNesting);
                        if (lookup != null) {
                            return lookup;
                        }
                    }
                }
            }
            return this.codeModel().ref(fullyQualifiedClassName);
        }
    }

    private JDefinedClass find(final JPackage pck, final String[] hierarchyNesting) {
        assert pck != null;
        assert hierarchyNesting != null;
        final var nesting = new ArrayList<>(of(hierarchyNesting));
        if (nesting.isEmpty()) {
            return null;
        } else {
            final var hit = pck._getClass(nesting.get(0));
            if (hit == null) {
                return null;
            } else {
                nesting.remove(0);
                return this.find(hit, nesting);
            }
        }
    }

    private JDefinedClass find(final JDefinedClass clazz, final List<String> hierarchyNesting) {
        assert clazz != null;
        assert hierarchyNesting != null;
        if (hierarchyNesting.isEmpty()) {
            return clazz;
        } else {
            final var hit = streamOf(clazz.classes()).filter(c -> c.name().equals(hierarchyNesting.get(0))).findFirst();
            if (hit.isPresent()) {
                hierarchyNesting.remove(0);
                return this.find(hit.get(), hierarchyNesting);
            } else {
                return null;
            }
        }
    }

    /**
     * @param clazz
     *            the Java class to refer to
     * @return a {@linkplain JClass reference} to an existing class from its {@link Class} object
     */
    public default JClass reference(final Class<?> clazz) {
        assertThat(clazz.isPrimitive()).isFalse();
        return this.codeModel().ref(clazz);
    }

}
