package de.informaticum.xjc.api;

import static java.util.Arrays.asList;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;

public final class BarPlugin
extends BasePlugin {

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>("bar", "Some bar infomation.");
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(
            new CommandLineArgument("aaaaaa", "Some aaaaaa information. Default: aaaaaa"),
            new CommandLineArgument("bb", "Some bb information. Default: bb")
        );
    }

}
