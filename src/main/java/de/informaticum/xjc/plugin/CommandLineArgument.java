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
    public final boolean isActivated() {
        return this.activated;
    }

    public int parseArgument(final Options options, final String[] arguments, final int index) {
        assertThat(this.argument).isEqualTo(arguments[index]);
        this.activated = true;
        return 1;
    }

    public final void alsoActivate(final CommandLineArgument... activateImplicitly) {
        if (this.activated) {
            asList(activateImplicitly).forEach(arg -> arg.activated = true);
        }
    }

}
