package de.informaticum.xjc.api;

import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.CustomizableOutline;
import com.sun.tools.xjc.outline.PackageOutline;

/**
 * Abstract definition of a XJC option, mainly with support for various conditional code executions.
 */
public abstract interface XjcOption {

    /**
     * @return {@code true} iff this XJC option has been activated
     */
    public abstract boolean isActivated();

    /**
     * @return the ID/the argument name of this XJC option (must be unique in the execution context)
     */
    public abstract String getArgument();

    /**
     * @param other
     *            the other XJC option
     * @return a logical OR'ed XJC option that is active if either {@code this} or the {@code other} XJC option is active
     */
    public default XjcOption or(final XjcOption other) {
        return new XjcOption() {
            @Override public final String getArgument() {
                return XjcOption.this.getArgument() + "||" + other.getArgument();
            }
            @Override public final boolean isActivated() {
                return XjcOption.this.isActivated() || other.isActivated();
            }
        };
    }

    /**
     * @param other
     *            the other XJC option
     * @return a logical AND'ed XJC option that is active if either {@code this} and the {@code other} XJC option is active
     */
    public default XjcOption and(final XjcOption other) {
        return new XjcOption() {
            @Override public final String getArgument() {
                return XjcOption.this.getArgument() + "&&" + other.getArgument();
            }
            @Override public final boolean isActivated() {
                return XjcOption.this.isActivated() && other.isActivated();
            }
        };
    }

    /**
     * @param execution
     *            any action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     */
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

    /**
     * @param execution
     *            any action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param clazz
     *            the execution's argument
     * @param <CO>
     *            the type of the execution's argument
     */
    public default <CO extends CustomizableOutline> void doOnActivation(final Consumer<? super CO> execution, final CO clazz) {
        this.doOnActivation(execution, clazz, fullNameOf(clazz));
    }

    /**
     * @param execution
     *            any action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param pakkage
     *            the execution's argument
     * @param <PO>
     *            the type of the execution's argument
     */
    public default <PO extends PackageOutline> void doOnActivation(final Consumer<? super PO> execution, final PO pakkage) {
        this.doOnActivation(execution, pakkage, fullNameOf(pakkage));
    }

    /**
     * @param execution
     *            any action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param $type
     *            the execution's argument
     * @param <JT>
     *            the type of the execution's argument
     */
    public default <JT extends JType> void doOnActivation(final Consumer<? super JT> execution, final JT $type) {
        this.doOnActivation(execution, $type, $type.fullName());
    }

    private <T, U> void doOnActivation(final BiConsumer<? super T, ? super U> execution, final T arg1, final U arg2, final String name) {
        if (this.isActivated()) {
            execution.accept(arg1, arg2);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), name);
        }
    }

    /**
     * @param execution
     *            any action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param clazz
     *            the execution's first argument
     * @param <CO>
     *            the type of the execution's first argument
     * @param param
     *            the execution's second argument
     * @param <P>
     *            the type of the execution's second argument
     */
    public default <CO extends CustomizableOutline, P> void doOnActivation(final BiConsumer<? super CO, ? super P> execution, final CO clazz, final P param) {
        this.doOnActivation(execution, clazz, param, fullNameOf(clazz));
    }

    /**
     * @param execution
     *            any action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param pakkage
     *            the execution's first argument
     * @param <PO>
     *            the type of the execution's first argument
     * @param param
     *            the execution's second argument
     * @param <P>
     *            the type of the execution's second argument
     */
    public default <PO extends PackageOutline, P> void doOnActivation(final BiConsumer<? super PO, ? super P> execution, final PO pakkage, final P param) {
        this.doOnActivation(execution, pakkage, param, fullNameOf(pakkage));
    }

    /**
     * @param execution
     *            any action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param $type
     *            the execution's first argument
     * @param <JT>
     *            the type of the execution's first argument
     * @param param
     *            the execution's second argument
     * @param <P>
     *            the type of the execution's second argument
     */
    public default <JT extends JType, P> void doOnActivation(final BiConsumer<? super JT, ? super P> execution, final JT $type, final P param) {
        this.doOnActivation(execution, $type, param, $type.fullName());
    }

}
