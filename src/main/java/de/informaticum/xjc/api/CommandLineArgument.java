package de.informaticum.xjc.api;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;

/**
 * Specific kind of a {@link XjcOption}, intended to be controlled as a command line parameter for custom {@link BasePlugin XJC plug-ins}.
 */
public final class CommandLineArgument
implements XjcOption {

    private final String argument;

    private final String description;

    private final LinkedHashMap<String,String> optionParameters;

    private boolean activated;

    /**
     * Creates a new {@linkplain #isActivated() inactive} instance, expecting a specific number of option arguments.
     *
     * @param argument
     *            the ID/the argument name of this XJC option (must be unique in the execution context)
     * @param description
     *            some short explanation of this XJC option
     * @param optionParameters
     *            the names of all expected option arguments (if any)
     */
    public CommandLineArgument(final String argument, final String description, final String... optionParameters) {
        this(argument, description, asList(optionParameters));
    }

    /**
     * Creates a new {@linkplain #isActivated() inactive} instance, expecting a specific number of option arguments.
     *
     * @param argument
     *            the ID/the argument name of this XJC option (must be unique in the execution context)
     * @param description
     *            some short explanation of this XJC option
     * @param optionParameters
     *            the names of all expected option arguments (if any)
     */
    public CommandLineArgument(final String argument, final String description, final List<? extends String> optionParameters) {
        this.argument = requireNonNull(argument).startsWith("-") ? argument : "-" + argument;
        this.description = requireNonNull(description);
        this.optionParameters = new LinkedHashMap<>(optionParameters.size());
        optionParameters.forEach(p -> this.optionParameters.put(p, null));
        this.activated = false;
    }

    @Override
    public final String getArgument() {
        return this.argument;
    }

    @Override
    public final List<String> getParameters() {
        return new ArrayList<>(this.optionParameters.keySet());
    }

    @Override
    public final List<String> getParameterValues() {
        return new ArrayList<>(this.optionParameters.values());
    }

    /**
     * @return short explanation of this XJC option
     */
    @Override
    public final String getDescription() {
        return this.description;
    }

    @Override
    public final boolean isActivated() {
        return this.activated;
    }

    @Override
    public final int parseArgument(final Options options, final String[] arguments, int index)
    throws BadCommandLineException {
        assertThat(this.argument).isEqualTo(arguments[index]);
        this.activated = true;
        for (final var entry : this.optionParameters.entrySet()) {
            entry.setValue(options.requireArgument(this.argument, arguments, ++index));
        }
        return 1 + this.optionParameters.size();
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
