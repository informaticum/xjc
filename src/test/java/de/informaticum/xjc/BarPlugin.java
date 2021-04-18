package de.informaticum.xjc;

import static java.util.Arrays.asList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;

public class BarPlugin
extends BasePlugin {

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>("bar", "Some bar infomation.");
    }

    @Override
    public List<CommandLineArgument> getPluginArguments() {
        return asList(
            new CommandLineArgument("-aaaaaa", "Some aaaaaa information. Default: aaaaaa"),
            new CommandLineArgument("-bb", "Some bb information. Default: bb")
        );
    }

}
