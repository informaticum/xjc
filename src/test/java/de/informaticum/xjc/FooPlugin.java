package de.informaticum.xjc;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import de.informaticum.xjc.plugin.BasePlugin;

public class FooPlugin
extends BasePlugin {

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>("foo", "Some foo infomation.");
    }

    @Override
    public LinkedHashMap<String, String> getPluginOptions() {
        final var options = new LinkedHashMap<String, String>();
        options.put("-a", "Some a information. Default: a");
        options.put("-bb", "Some bb information. Default: bb");
        return options;
    }

}
