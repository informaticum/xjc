package de.informaticum.xjc.plugin;

import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.function.Consumer;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.CustomizableOutline;
import com.sun.tools.xjc.outline.PackageOutline;

public abstract interface XjcOption {

    public abstract String getArgument();

    public abstract boolean isActivated();

    public default boolean isNotActivated() {
        return !this.isActivated();
    }

    public default XjcOption or(final XjcOption other) {
        return new XjcOption() {
            @Override
            public final String getArgument() {
                return XjcOption.this.getArgument() + "/" + other.getArgument();
            }

            @Override
            public final boolean isActivated() {
                return XjcOption.this.isActivated() || other.isActivated();
            }
        };
    }

    public default void doOnActivation(final Runnable execution) {
        if (this.isActivated()) {
            execution.run();
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}], because it has not been activated.", this.getArgument());
        }
    }

    private <T> void doOnActivation(final Consumer<? super T> execution, final T arg, final String name) {
        if (this.isActivated()) {
            execution.accept(arg);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), name);
        }
    }

    public default <CO extends CustomizableOutline> void doOnActivation(final Consumer<? super CO> execution, final CO clazz) {
        this.doOnActivation(execution, clazz, fullNameOf(clazz));
    }

    public default <PO extends PackageOutline> void doOnActivation(final Consumer<? super PO> execution, final PO pakkage) {
        this.doOnActivation(execution, pakkage, fullNameOf(pakkage));
    }

    public default <JT extends JType> void doOnActivation(final Consumer<? super JT> execution, final JT $type) {
        this.doOnActivation(execution, $type, fullNameOf($type));
    }

}
