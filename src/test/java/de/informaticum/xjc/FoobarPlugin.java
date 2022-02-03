package de.informaticum.xjc;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import de.informaticum.xjc.plugin.BasePlugin;

public final class FoobarPlugin
extends BasePlugin {

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>("foobar", "Some foobar infomation.");
    }

}
