package de.informaticum.xjc.api;

import org.xml.sax.ErrorHandler;

/**
 * Convenient actions, based on the given {@link #errorHandler() ErrorHandler}.
 */
public abstract interface InitialisedErrorHandler {

    /**
     * @return the current {@link ErrorHandler} instance
     */
    public default ErrorHandler errorHandler() {
        throw new IllegalStateException("The current 'ErrorHandler' instance has not yet been initialised!");
    }

}
