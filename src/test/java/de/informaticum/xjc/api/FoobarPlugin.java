package de.informaticum.xjc.api;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

public final class FoobarPlugin
extends BasePlugin {

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>("foobar", "Some foobar infomation.");
    }

}
