package de.informaticum.xjc.api;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import java.util.List;
import java.util.Map.Entry;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;

/**
 * Enhanced {@linkplain Plugin XJC plug-in}, providing convenient methods to handle the plug-in's arguments.
 */
public abstract class CommandLineArgumentedPlugin
extends Plugin {

    /**
     * Gets the option name to use for XJC add-on activation; Plus, gets the add-on's description. The strong coupling of both attributes becomes manifest in the usage of the
     * {@link Entry} data structure.
     *
     * @return the name and the description of this XJC add-on
     * @see #getOptionName()
     * @see #getOptionDescription()
     */
    public abstract Entry<String, String> getOptionEntry();

    /**
     * @implNote The current implementation returns the key attribute of {@link #getOptionEntry()}.
     *
     * @see #getOptionEntry()
     * @see #getOptionDescription()
     */
    @Override
    public final String getOptionName() {
        return this.getOptionEntry().getKey();
    }

    /**
     * @implNote The current implementation returns the value attribute of {@link #getOptionEntry()}.
     *
     * @return the description of this XJC add-on
     * @see #getOptionEntry()
     * @see #getOptionName()
     */
    public final String getOptionDescription() {
        return this.getOptionEntry().getValue();
    }

    /**
     * Is there any plug-in argument? Just return all of them here and you will get {@linkplain #parseArgument(Options, String[], int) a default argument parsing} for free.
     *
     * @return all plug-in's command line arguments
     */
    public List<XjcOption> getPluginArguments() {
        return emptyList();
    }

    @Override
    public final String getUsage() {
        final var pluginArgs = this.getPluginArguments();
        final var width = pluginArgs.stream().mapToInt(arg -> arg.getArgument().length()).max().orElse(this.getOptionName().length());
        final var usage = new StringBuilder();
        usage.append(format("  %1$s :  %2$s%n", "-" + this.getOptionName(), this.getOptionDescription()));
        pluginArgs.forEach(arg -> {
            final var argNames = arg.getParameters().stream().map(s -> "<" + s + ">").collect(joining(" "));
            usage.append(format("  %1$-" + width + "s :  %2$s%n", arg.getArgument() + (argNames.isBlank() ? "" : " " + argNames), arg.getDescription()));
        });
        return usage.toString();
    }

    /**
     * @implNote The current implementation parses the arguments and -- if an according argument has been supplied by {@link #getPluginArguments()} -- the
     *           {@linkplain CommandLineArgument#parseArgument(Options, String[], int) specific argument parsing method} will be called.
     */
    @Override
    public final int parseArgument(final Options options, final String[] arguments, final int index)
    throws BadCommandLineException {
        // Consume the current argument if it's either equal to this plug-in's option name or if it matches any of this plug-in's arguments
        if (this.getOptionName().equals(arguments[index])) {
            return 1;
        } else {
            final var possiblyMatchedArgument = this.getPluginArguments().stream().filter(arg -> arg.getArgument().equals(arguments[index])).findFirst();
            return possiblyMatchedArgument.isPresent() ? possiblyMatchedArgument.get().parseArgument(options, arguments, index) : 0;
        }
    }

}
