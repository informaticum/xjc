package de.informaticum.xjc.plugins.i18n;

import static java.util.ResourceBundle.getBundle;
import java.util.ResourceBundle;
import de.informaticum.xjc.api.ResourceBundleEntry;
import de.informaticum.xjc.plugins.PropertyPlugin;

/**
 * Enumeration of all i18n messages keys used by the {@link PropertyPlugin}.
 */
public enum PropertyPluginMessages
implements ResourceBundleEntry {

    /* A. Description of the XJC Plugin */
    OPTION_DESCRIPTION,

    /* B. Description of the XJC Plugin's Options */
    PRIVATE_FIELDS_DESCRIPTION,
    FINAL_FIELDS_DESCRIPTION,
    STRAIGHT_GETTERS_DESCRIPTION,
    OPTIONAL_GETTERS_DESCRIPTION,
    OPTIONAL_ORDEFAULT_DESCRIPTION,
    FINAL_GETTERS_DESCRIPTION,
    COLLECTION_SETTERS_DESCRIPTION,
    REMOVE_SETTERS_DESCRIPTION,
    FINAL_SETTERS_DESCRIPTION,

    /* C. Main Javadoc Contents and Javadoc Supply (@param, @throws, etc.) */
    PRIVATE_FIELD_IMPLNOTE,
    FINAL_FIELD_IMPLNOTE,
    FINAL_GETTER_IMPLNOTE,
    FINAL_SETTER_IMPLNOTE,
    REMOVED_SETTERS_IMPLNOTE,
    STRAIGHT_GETTER_JAVADOC_BEGIN,
    OPTIONAL_GETTER_JAVADOC_BEGIN,
    UNMODIFIABLE_GETTER_JAVADOC_BEGIN,
    OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC_BEGIN,
    NOTE_REQUIRED_VALUE,
    NOTE_OPTIONAL_VALUE,
    NOTE_NULLABLE_VALUE,
    NOTE_DEFAULTED_VALUE,
    NOTE_DEFAULTED_COLLECTION,
    NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION,
    NOTE_EMPTY_CONTAINER,
    NOTE_LIVE_REFERENCE,
    NOTE_LIVE_REFERENCE_CONTAINER,
    NOTE_DEFENSIVE_COPY_COLLECTION,
    NOTE_UNMODIFIABLE_COLLECTION,
    NOTE_DEFENSIVE_COPY_COLLECTION_CONTAINER,
    NOTE_UNMODIFIABLE_COLLECTION_CONTAINER,
    HINT_NULLABLE_VALUE,
    HINT_DEFAULTED_COLLECTION,
    HINT_DEFAULTED_UNMODIFIABLE_COLLECTION,
    HINT_EMPTY_COLLECTION_CONTAINER,
    HINT_LIVE_REFERENCE,
    HINT_DEFENSIVE_COPY_COLLECTION,
    HINT_UNMODIFIABLE_COLLECTION,
    GETTER_JAVADOC_END,
    REFACTORED_GETTER_IMPLNOTE_INTRO,
    REFACTORED_GETTER_IMPLNOTE_OUTRO,
    ORDEFAULT_JAVADOC,
    ORDEFAULT_IMPLNOTE,
    ORBUILTIN_JAVADOC,
    ORBUILTIN_IMPLNOTE,
    COLLECTION_SETTER_JAVADOC,
    COLLECTION_SETTER_IMPLNOTE,

    STRAIGHT_VALUE_RETURN,
    STRAIGHT_DEFAULTED_VALUE_RETURN,
    STRAIGHT_COLLECTION_RETURN,
    STRAIGHT_COLLECTION_OR_EMPTY_RETURN,
    UNMODIFIABLE_COLLECTION_RETURN,
    UNMODIFIABLE_COLLECTION_OR_EMPTY_RETURN,
    OPTIONAL_VALUE_RETURN,
    OPTIONAL_COLLECTION_RETURN,
    OPTIONAL_UNMODIFIABLE_COLLECTION_RETURN,
    ORDEFAULT_PARAM,
    ORDEFAULT_RETURN,
    ORBUILTIN_RETURN,

    ;

    private static final ResourceBundle RB = getBundle(PropertyPluginMessages.class.getName().replace(".i18n.", ".l10n."));

    @Override
    public final ResourceBundle bundle() {
        return RB;
    }

}
