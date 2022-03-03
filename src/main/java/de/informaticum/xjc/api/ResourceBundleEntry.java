package de.informaticum.xjc.api;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Convenient declaration of a locale-specific message ({@link String}-based), intended to be adopted in {@code enum} scenarios. Using this interface, the {@linkplain Enum#name()
 * enum constant name} will be used as the resource bundle lookup key when returning a formatted message (either {@linkplain #format(Object...) with parameters} or
 * {@linkplain #text() without}. Further, the format pattern must follow the {@link MessageFormat} conventions.
 */
public abstract interface ResourceBundleEntry {

    /**
     * The name of this single entry, used when returning a formatted message (either {@linkplain #format(Object...) with parameters} or {@linkplain #text() without}. This method
     * name is intentional similar with {@link Enum#name()}.
     *
     * @return name of this single entry
     */
    public abstract String name();

    /**
     * @return resource bundle to query this entry from
     */
    public abstract ResourceBundle bundle();

    /**
     * @implNote Before the message is formatted, the current implementation compresses all space-sequence within the origin resource bundle entry's {@link String} value (i.e.,
     *           replaces all {@code "\u0020+"} with {@code "\u0020"}).
     * @param arguments
     *            all arguments to be used for {@link MessageFormat#format(String, Object...)}.
     * @return the formatted message, based on this resource bundle entry's string value and formatted according to the conventions of {@link MessageFormat}
     */
    public default String format(final Object... arguments) {
        final var pattern = this.bundle().getString(this.name()).replaceAll(" +", " ");
        return MessageFormat.format(pattern, arguments);
    }

    /**
     * @implNote The current implementation refers to {@link #format(Object...)} without specifying an argument.
     * @return the formatted message, based on this resource bundle entry's string value and formatted according to the conventions of {@link MessageFormat}
     */
    public default String text() {
        return this.format();
    }

}
