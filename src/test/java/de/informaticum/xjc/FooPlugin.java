package de.informaticum.xjc;

import java.util.LinkedHashMap;

public class FooPlugin
extends AbstractPlugin {

    @Override
    public String getOptionName() {
        return "foo";
    }

    @Override
    public String getOptionDescription() {
        return "Some foo infomation.";
    }

    @Override
    public LinkedHashMap<String, String> getPluginOptions() {
        final var options = new LinkedHashMap<String, String>();
        options.put("-a", "Some a information. Default: a");
        options.put("-bb", "Some bb information. Default: bb");
        return options;
    }

}
