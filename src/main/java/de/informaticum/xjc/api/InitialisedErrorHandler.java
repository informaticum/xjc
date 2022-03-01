package de.informaticum.xjc.api;

import org.xml.sax.ErrorHandler;

public abstract interface InitialisedErrorHandler {

    public default ErrorHandler errorHandler() {
        throw new IllegalStateException("The current 'ErrorHandler' instance has not yet been initialised!");
    }

}