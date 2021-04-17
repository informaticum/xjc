package de.informaticum.xjc.plugin;

import static java.lang.String.format;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;

public abstract class CommandLineArgumentedPlugin
extends Plugin {

    public abstract Entry<String, String> getOption();

    @Override
    public final String getOptionName() {
        return this.getOption().getKey();
    }

    public LinkedHashMap<String, String> getPluginArguments() {
        return new LinkedHashMap<>();
    }

    @Override
    public final String getUsage() {
        final var options = this.getPluginArguments();
        final var width = options.keySet().stream().mapToInt(String::length).max().orElse(this.getOptionName().length());
        final var usage = new StringBuilder();
        usage.append(format("  %1$s :  %2$s%n", "-" + this.getOptionName(), this.getOption().getValue()));
        options.entrySet().forEach(o -> usage.append(format("  %1$-" + width + "s :  %2$s%n", o.getKey(), o.getValue())));
        return usage.toString();
    }

    private final Set<String> activeArguments = new HashSet<>();

    @Override
    public int parseArgument(final Options options, final String[] arguments, final int index)
    throws BadCommandLineException, IOException {
        if (this.getPluginArguments().containsKey(arguments[index])) {
            this.activeArguments.add(arguments[index]);
            return 1;
        } else {
            return super.parseArgument(options, arguments, index);
        }
    }

    public boolean isActive(final String argument) {
        return this.activeArguments.contains(argument);
    }
}
