package de.informaticum.xjc;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import de.informaticum.xjc.plugin.BasePlugin;

public class BarPlugin
extends BasePlugin {

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>("bar", "Some bar infomation.");
    }

    @Override
    public LinkedHashMap<String, String> getPluginOptions() {
        final var options = new LinkedHashMap<String, String>();
        options.put("-aaaaaa", "Some aaaaaa information. Default: aaaaaa");
        options.put("-bb", "Some bb information. Default: bb");
        return options;
    }

}
