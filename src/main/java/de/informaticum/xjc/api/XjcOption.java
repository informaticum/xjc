package de.informaticum.xjc.api;

import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
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
     * @return a logical AND'ed XJC option that is active if both {@code this} and the {@code other} XJC option is active
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
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     */
    public default void doOnActivation(final Runnable execution) {
        if (this.isActivated()) {
            execution.run();
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}], because it has not been activated.", this.getArgument());
        }
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param <R>
     *            the type of the action's result
     * @return the result of the action if {@code this} XJC option is activated, an {@link Optional#empty() empty Optional} otherwise
     */
    public default <R> Optional<R> doOnActivation(final Supplier<? extends R> execution) {
        if (this.isActivated()) {
            return Optional.ofNullable(execution.get());
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}], because it has not been activated.", this.getArgument());
            return Optional.empty();
        }
    }

    private <T, R> Optional<R> doOnActivation(final Function<? super T, ? extends R> execution, final T arg, final String name) {
        if (this.isActivated()) {
            return Optional.ofNullable(execution.apply(arg));
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), name);
            return Optional.empty();
        }
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param clazz
     *            the execution's argument
     * @param <CO>
     *            the type of the action's argument
     * @param <R>
     *            the type of the action's result
     * @return the result of the action if {@code this} XJC option is activated, an {@link Optional#empty() empty Optional} otherwise
     */
    public default <CO extends CustomizableOutline, R> Optional<R> doOnActivation(final Function<? super CO, ? extends R> execution, final CO clazz) {
        return this.doOnActivation(execution, clazz, fullNameOf(clazz));
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param pakkage
     *            the execution's argument
     * @param <PO>
     *            the type of the action's argument
     * @param <R>
     *            the type of the action's result
     * @return the result of the action if {@code this} XJC option is activated, an {@link Optional#empty() empty Optional} otherwise
     */
    public default <PO extends PackageOutline, R> Optional<R> doOnActivation(final Function<? super PO, ? extends R> execution, final PO pakkage) {
        return this.doOnActivation(execution, pakkage, fullNameOf(pakkage));
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param $type
     *            the execution's argument
     * @param <JT>
     *            the type of the action's argument
     * @param <R>
     *            the type of the action's result
     * @return the result of the action if {@code this} XJC option is activated, an {@link Optional#empty() empty Optional} otherwise
     */
    public default <JT extends JType, R> Optional<R> doOnActivation(final Function<? super JT, ? extends R> execution, final JT $type) {
        return this.doOnActivation(execution, $type, $type.fullName());
    }

    private <T, P, R> Optional<R> doOnActivation(final BiFunction<? super T, ? super P, ? extends R> execution, final T arg, final P param, final String name) {
        if (this.isActivated()) {
            return Optional.ofNullable(execution.apply(arg, param));
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), name);
            return Optional.empty();
        }
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param clazz
     *            the execution's first argument
     * @param <CO>
     *            the type of the action's first argument
     * @param param
     *            the execution's second argument
     * @param <P>
     *            the type of the action's second argument
     * @param <R>
     *            the type of the action's result
     * @return the result of the action if {@code this} XJC option is activated, an {@link Optional#empty() empty Optional} otherwise
     */
    public default <CO extends CustomizableOutline, P, R> Optional<R> doOnActivation(final BiFunction<? super CO, ? super P, ? extends R> execution, final CO clazz, final P param) {
        return this.doOnActivation(execution, clazz, param, fullNameOf(clazz));
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param pakkage
     *            the execution's first argument
     * @param <PO>
     *            the type of the action's first argument
     * @param param
     *            the execution's second argument
     * @param <P>
     *            the type of the action's second argument
     * @param <R>
     *            the type of the action's result
     * @return the result of the action if {@code this} XJC option is activated, an {@link Optional#empty() empty Optional} otherwise
     */
    public default <PO extends PackageOutline, P, R> Optional<R> doOnActivation(final BiFunction<? super PO, ? super P, ? extends R> execution, final PO pakkage, final P param) {
        return this.doOnActivation(execution, pakkage, param, fullNameOf(pakkage));
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param $type
     *            the execution's first argument
     * @param <JT>
     *            the type of the action's first argument
     * @param param
     *            the execution's second argument
     * @param <P>
     *            the type of the action's second argument
     * @param <R>
     *            the type of the action's result
     * @return the result of the action if {@code this} XJC option is activated, an {@link Optional#empty() empty Optional} otherwise
     */
    public default <JT extends JType, P, R> Optional<R> doOnActivation(final BiFunction<? super JT, ? super P, ? extends R> execution, final JT $type, final P param) {
        return this.doOnActivation(execution, $type, param, $type.fullName());
    }

}
