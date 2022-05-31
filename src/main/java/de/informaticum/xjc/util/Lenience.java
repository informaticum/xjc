package de.informaticum.xjc.util;

import static de.informaticum.xjc.util.i18n.LenienceMessages.SERIOUS_PROBLEM;
import static java.lang.Boolean.parseBoolean;
import static java.util.Optional.ofNullable;

public enum Lenience {
    ;

    private static final String LENIENCE_PROPERTY = "informaticum.xjc.lenience";

    public static final boolean LENIENT = parseBoolean(ofNullable(System.getProperty(LENIENCE_PROPERTY)).orElse("").strip());

    public static final String LENIENT_BREAKPOINT_MESSAGE = SERIOUS_PROBLEM.format(LENIENCE_PROPERTY);

}
