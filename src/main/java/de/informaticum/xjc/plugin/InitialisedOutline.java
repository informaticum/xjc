package de.informaticum.xjc.plugin;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;

public abstract interface InitialisedOutline {

    public default Outline outline() {
        throw new IllegalStateException("The current 'Outline' instance has not yet been initialised!");
    }

    public default Model model() {
        return this.outline().getModel();
    }

    public default JCodeModel codeModel() {
        return this.outline().getCodeModel();
    }

    public default JClass reference(final Class<?> clazz) {
        return this.codeModel().ref(clazz);
    }

}
