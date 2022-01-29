package de.informaticum.xjc.plugin;

import com.sun.tools.xjc.Options;

public abstract interface InitialisedOptions {

    public default Options options() {
        throw new IllegalStateException("The current 'Options' instance has not yet been initialised!");
    }

}
