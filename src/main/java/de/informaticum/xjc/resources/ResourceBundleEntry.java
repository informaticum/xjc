package de.informaticum.xjc.resources;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract interface ResourceBundleEntry {

    public abstract String name();

    public abstract ResourceBundle bundle();

    public default String format(final Object... arguments) {
        final var pattern = this.bundle().getString(this.name()).replaceAll(" +", " ");
        return MessageFormat.format(pattern, arguments);
    }

    public default String text() {
        return this.format();
    }

}
