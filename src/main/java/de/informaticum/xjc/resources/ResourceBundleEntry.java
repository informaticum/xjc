package de.informaticum.xjc.resources;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract interface ResourceBundleEntry {

    public abstract String name();

    public abstract ResourceBundle bundle();

    public default String format(final Object... arguments) {
        return MessageFormat.format(this.bundle().getString(this.name()), arguments);
    }

    public default String text() {
        return this.format();
    }

}
