package de.informaticum.xjc;

import java.util.LinkedHashMap;

public class BarPlugin
extends AbstractPlugin {

    @Override
    public String getOptionName() {
        return "bar";
    }

    @Override
    public String getOptionDescription() {
        return "Some bar infomation.";
    }

    @Override
    public LinkedHashMap<String, String> getPluginOptions() {
        final var options = new LinkedHashMap<String, String>();
        options.put("-aaaaaa", "Some aaaaaa information. Default: aaaaaa");
        options.put("-bb", "Some bb information. Default: bb");
        return options;
    }

}
