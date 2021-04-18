package de.informaticum.xjc;

import static java.util.Arrays.asList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;

public class FooPlugin
extends BasePlugin {

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>("foo", "Some foo infomation.");
    }

    @Override
    public List<CommandLineArgument> getPluginArguments() {
        return asList(
            new CommandLineArgument("-a", "Some a information. Default: a"),
            new CommandLineArgument("-bb", "Some bb information. Default: bb")
        );
    }

}
