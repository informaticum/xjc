package de.informaticum.xjc.util;

import static de.informaticum.xjc.plugins.i18n.LenienceMessages.SERIOUS_PROBLEM;
import static java.lang.Boolean.parseBoolean;

public enum Lenience {
    ;

    private static final String LENIENCE_PROPERTY = "informaticum.xjc.lenience";

    public static final boolean LENIENT = parseBoolean(System.getProperty(LENIENCE_PROPERTY).strip());

    public static final String LENIENT_BREAKPOINT_MESSAGE = SERIOUS_PROBLEM.format(LENIENCE_PROPERTY);

}
