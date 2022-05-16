package de.informaticum.xjc.api;

import static org.assertj.core.api.Assertions.assertThat;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
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
     * @param fullyQualifiedClassName
     *            the fully qualified name of the Java class to refer to
     * @return a {@linkplain JClass reference} to an existing class from its name
     */
    public default JClass reference(final String fullyQualifiedClassName) {
        return this.codeModel().ref(fullyQualifiedClassName);
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
