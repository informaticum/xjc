package de.informaticum.xjc.plugin;

import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;

public abstract interface RunningPlugin {

    public default Outline outline() {
        throw new IllegalStateException("Do not query current 'Outline' before '#run(...)' is called!");
    }

    public default Options options() {
        throw new IllegalStateException("Do not query current 'Options' before '#run(...)' is called!");
    }

    public default ErrorHandler errorHandler() {
        throw new IllegalStateException("Do not query current 'ErrorHandler' before '#run(...)' is called!");
    }

}
