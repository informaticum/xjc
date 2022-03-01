package de.informaticum.xjc.api;

import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.CustomizableOutline;
import com.sun.tools.xjc.outline.PackageOutline;

public abstract interface XjcOption
extends BooleanSupplier {

    public abstract String getArgument();

    public default XjcOption or(final XjcOption other) {
        return new XjcOption() {
            @Override public final String getArgument() {
                return XjcOption.this.getArgument() + "||" + other.getArgument();
            }
            @Override public final boolean getAsBoolean() {
                return XjcOption.this.getAsBoolean() || other.getAsBoolean();
            }
        };
    }

    public default XjcOption and(final XjcOption other) {
        return new XjcOption() {
            @Override public final String getArgument() {
                return XjcOption.this.getArgument() + "&&" + other.getArgument();
            }
            @Override public final boolean getAsBoolean() {
                return XjcOption.this.getAsBoolean() && other.getAsBoolean();
            }
        };
    }

    public default void doOnActivation(final Runnable execution) {
        if (this.getAsBoolean()) {
            execution.run();
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}], because it has not been activated.", this.getArgument());
        }
    }

    private <T> void doOnActivation(final Consumer<? super T> execution, final T arg, final String name) {
        if (this.getAsBoolean()) {
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
        this.doOnActivation(execution, $type, $type.fullName());
    }

    private <T, U> void doOnActivation(final BiConsumer<? super T, ? super U> execution, final T arg1, final U arg2, final String name) {
        if (this.getAsBoolean()) {
            execution.accept(arg1, arg2);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), name);
        }
    }

    public default <CO extends CustomizableOutline, P> void doOnActivation(final BiConsumer<? super CO, ? super P> execution, final CO clazz, final P param) {
        this.doOnActivation(execution, clazz, param, fullNameOf(clazz));
    }

    public default <PO extends PackageOutline, P> void doOnActivation(final BiConsumer<? super PO, ? super P> execution, final PO pakkage, final P param) {
        this.doOnActivation(execution, pakkage, param, fullNameOf(pakkage));
    }

    public default <JT extends JType, P> void doOnActivation(final BiConsumer<? super JT, ? super P> execution, final JT $type, final P param) {
        this.doOnActivation(execution, $type, param, $type.fullName());
    }

}
