package de.informaticum.xjc.plugin;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import com.sun.tools.xjc.Options;

public final class CommandLineArgument
implements XjcOption {

    private final String argument;

    private final String description;

    private boolean activated;

    public CommandLineArgument(final String argument, final String description) {
        this.argument = requireNonNull(argument).startsWith("-") ? argument : "-" + argument;
        this.description = requireNonNull(description);
        this.activated = false;
    }

    @Override
    public final String getArgument() {
        return this.argument;
    }

    public final String getDescription() {
        return this.description;
    }

    @Override
    public final boolean getAsBoolean() {
        return this.activated;
    }

    public final int parseArgument(final Options options, final String[] arguments, final int index) {
        assertThat(this.argument).isEqualTo(arguments[index]);
        this.activated = true;
        return 1;
    }

    public final void activates(final CommandLineArgument... affected) {
        this.set(true, affected);
    }

    public final void deactivates(final CommandLineArgument... affected) {
        this.set(false, affected);
    }

    private final void set(final boolean state, final CommandLineArgument... affected) {
        if (this.activated) {
            asList(affected).forEach(arg -> arg.activated = state);
        }
    }

    @Override
    public final String toString() {
        return this.argument;
    }

}
