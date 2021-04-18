package de.informaticum.xjc.plugin;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;

public abstract class CommandLineArgumentedPlugin
extends Plugin {

    /**
     * Gets the option name to turn on this add-on; And gets the add-on's description. The strong coupling of both
     * attributes becomes manifest in usage of the the {@link Entry} data structure.
     *
     * @see #getOptionName()
     */
    public abstract Entry<String, String> getOption();

    /**
     * @implNote The current implementation returns the key attribute of {@link #getOption()}.
     */
    @Override
    public final String getOptionName() {
        return this.getOption().getKey();
    }

    /**
     * Is there any plug-in argument? Just return all of them here and you will get
     * {@linkplain #parseArgument(Options, String[], int) a default argument parsing} for free.
     *
     * @return all plug-in's command line arguments
     */
    public List<CommandLineArgument> getPluginArguments() {
        return emptyList();
    }

    @Override
    public final String getUsage() {
        final var pluginArgs = this.getPluginArguments();
        final var width = pluginArgs.stream().mapToInt(arg -> arg.getArgument().length()).max().orElse(this.getOptionName().length());
        final var usage = new StringBuilder();
        usage.append(format("  %1$s :  %2$s%n", "-" + this.getOptionName(), this.getOption().getValue()));
        pluginArgs.forEach(arg -> usage.append(format("  %1$-" + width + "s :  %2$s%n", arg.getArgument(), arg.getDescription())));
        return usage.toString();
    }

    /**
     * @implNote The current implementation parses the arguments and -- if an according argument has been supplied by
     *           {@link #getPluginArguments()} -- the specific
     *           {@linkplain CommandLineArgument#parseArgument(Options, String[], int) argument parsing method} will be
     *           called.
     */
    @Override
    public int parseArgument(final Options options, final String[] arguments, final int index)
    throws BadCommandLineException, IOException {
        final var possiblyMatchedArgument = this.getPluginArguments().stream().filter(arg -> arg.getArgument().equals(arguments[index])).findFirst();
        return possiblyMatchedArgument.map(arg -> arg.parseArgument(options, arguments, index)).orElse(0);
    }

}
