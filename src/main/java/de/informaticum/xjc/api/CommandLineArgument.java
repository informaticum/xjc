package de.informaticum.xjc.api;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import com.sun.tools.xjc.Options;

/**
 * Specific kind of a {@link XjcOption}, intended to be controlled as a command line parameter for custom {@link BasePlugin XJC plugins}.
 */
public final class CommandLineArgument
implements XjcOption {

    private final String argument;

    private final String description;

    private boolean activated;

    /**
     * Creates a new {@linkplain #isActivated() inactive} instance.
     *
     * @param argument
     *            the ID/the argument name of this XJC option (must be unique in the execution context)
     * @param description
     *            some short explanation of this XJC option
     */
    public CommandLineArgument(final String argument, final String description) {
        this.argument = requireNonNull(argument).startsWith("-") ? argument : "-" + argument;
        this.description = requireNonNull(description);
        this.activated = false;
    }

    @Override
    public final String getArgument() {
        return this.argument;
    }

    /**
     * @return short explanation of this XJC option
     */
    public final String getDescription() {
        return this.description;
    }

    @Override
    public final boolean isActivated() {
        return this.activated;
    }

    /**
     * Parses an option {@code arguments[index]} and augment the {@code options} object appropriately, then return the number of consumed tokens.
     *
     * @param options
     *            the options to augment
     * @param arguments
     *            the array of argument
     * @param index
     *            the index of the argument to parse
     * @return the number of tokens consumed
     */
    public final int parseArgument(final Options options, final String[] arguments, final int index) {
        assertThat(this.argument).isEqualTo(arguments[index]);
        this.activated = true;
        return 1;
    }

    /**
     * @param affected
     *            further XJC options to be activated if this XJC option {@link #isActivated() is activated itself}
     */
    public final void activates(final CommandLineArgument... affected) {
        this.set(true, affected);
    }

    /**
     * @param affected
     *            further XJC options to be deactivated if this XJC option {@link #isActivated() is activated itself}
     */
    public final void deactivates(final CommandLineArgument... affected) {
        this.set(false, affected);
    }

    private final void set(final boolean state, final CommandLineArgument... affected) {
        if (this.activated) {
            asList(affected).forEach(arg -> arg.activated = state);
        }
    }

    /**
     * @return the ID/the argument name of this XJC option
     */
    @Override
    public final String toString() {
        return this.argument;
    }

}
