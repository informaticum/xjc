package de.informaticum.xjc.api;

import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.CustomizableOutline;
import com.sun.tools.xjc.outline.PackageOutline;

/**
 * Abstract definition of a XJC option, mainly with support for various conditional code executions.
 */
public abstract interface XjcOption {

    /**
     * @implSpec Any implementation must return the activation state as identified by {@link #parseArgument(Options, String[], int)}.
     * @return {@code true} iff this XJC option has been activated
     * @see #getParameterValues()
     */
    public abstract boolean isActivated();

    /**
     * @return the ID/the argument name of this XJC option (must be unique in the execution context)
     */
    public abstract String getArgument();

    /**
     * @return the list of parameters required by this XJC option
     */
    public default List<String> getParameters() {
        return emptyList();
    }

    /**
     * @implSpec Any implementation must return the exact same amount of values as required by {@link #getParameters()}, and all these values have to be identified by
     *           {@link #parseArgument(Options, String[], int)}.
     * @return the list of parameter values iff this XJC option has been activated
     * @see #isActivated()
     */
    public default List<String> getParameterValues() {
        return emptyList();
    }

    /**
     * @return short explanation of this XJC option
     */
    public abstract String getDescription();

    /**
     * Parses an option {@code arguments[index]} (augments the {@code options} object appropriately if applicable), then returns the number of consumed tokens.
     *
     * @implSpec Any implementation must activate this XJC option iff the arguments at position {@code index} equals the {@link #getArgument() option's argument name}, and further
     *           any implementation must parse the exact amount of additional parameters as required by {@link #getParameters()}. In result the number of consumed tokens must equal
     *           {@code 1} plus the number of additional parameters.
     * @param options
     *            the options to augment
     * @param arguments
     *            the array of argument
     * @param index
     *            the index of the argument to parse
     * @return the number of tokens consumed
     * @see #getParameters()
     * @see #isActivated()
     * @see #getParameterValues()
     */
    public abstract int parseArgument(final Options options, final String[] arguments, int index)
    throws BadCommandLineException;

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

    private <T> void doOnActivation(final Consumer<? super T> execution, final T arg, final String name) {
        if (this.isActivated()) {
            execution.accept(arg);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), name);
        }
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param clazz
     *            the execution's argument
     * @param <CO>
     *            the type of the action's argument
     */
    public default <CO extends CustomizableOutline> void doOnActivation(final Consumer<? super CO> execution, final CO clazz) {
        this.doOnActivation(execution, clazz, fullNameOf(clazz));
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param pakkage
     *            the execution's argument
     * @param <PO>
     *            the type of the action's argument
     */
    public default <PO extends PackageOutline> void doOnActivation(final Consumer<? super PO> execution, final PO pakkage) {
        this.doOnActivation(execution, pakkage, fullNameOf(pakkage));
    }

    /**
     * @param execution
     *            an action to be executed if {@code this} XJC option {@linkplain #isActivated() is activated}
     * @param $type
     *            the execution's argument
     * @param <JT>
     *            the type of the action's argument
     */
    public default <JT extends JType> void doOnActivation(final Consumer<? super JT> execution, final JT $type) {
        this.doOnActivation(execution, $type, $type.fullName());
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

    private <T, P, R> void doOnActivation(final BiConsumer<? super T, ? super P> execution, final T arg0, final P arg1, final String name) {
        if (this.isActivated()) {
            execution.accept(arg0, arg1);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), name);
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
     */
    public default <CO extends CustomizableOutline, P> void doOnActivation(final BiConsumer<? super CO, ? super P> execution, final CO clazz, final P param) {
        this.doOnActivation(execution, clazz, param, fullNameOf(clazz));
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
     */
    public default <PO extends PackageOutline, P> void doOnActivation(final BiConsumer<? super PO, ? super P> execution, final PO pakkage, final P param) {
        this.doOnActivation(execution, pakkage, param, fullNameOf(pakkage));
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
     */
    public default <JT extends JType, P> void doOnActivation(final BiConsumer<? super JT, ? super P> execution, final JT $type, final P param) {
        this.doOnActivation(execution, $type, param, $type.fullName());
    }

    private <T, P, R> Optional<R> doOnActivation(final BiFunction<? super T, ? super P, ? extends R> execution, final T arg0, final P arg1, final String name) {
        if (this.isActivated()) {
            return Optional.ofNullable(execution.apply(arg0, arg1));
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
