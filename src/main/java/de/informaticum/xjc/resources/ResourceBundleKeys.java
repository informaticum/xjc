package de.informaticum.xjc.resources;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.function.Function;

public abstract interface ResourceBundleKeys
extends Function<Object[], String>, CharSequence {

    public abstract String name();

    public abstract ResourceBundle bundle();

    @Override
    public default String apply(final Object... arguments) {
        return MessageFormat.format(this.bundle().getString(this.name()), arguments);
    }

    @Override
    public default int length() {
        return this.toString().length();
    }

    @Override
    public default char charAt(final int index) {
        return this.toString().charAt(index);
    }

    @Override
    default CharSequence subSequence(final int beginIndex, final int endIndex) {
        return this.toString().subSequence(beginIndex, endIndex);
    }

}
