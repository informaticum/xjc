package de.informaticum.xjc.api;

import com.sun.tools.xjc.Options;

/**
 * Convenient actions, based on the given {@link #options() Options}.
 */
public abstract interface InitialisedOptions {

    /**
     * @return the current {@link Options} instance
     */
    public default Options options() {
        throw new IllegalStateException("The current 'Options' instance has not yet been initialised!");
    }

}
