package de.informaticum.xjc.plugins.i18n;

import static java.util.Arrays.asList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LenienceMessagesTest
extends ResourceBundleKeyTest<LenienceMessages> {

    @Parameters(name = "{0}")
    public static Iterable<LenienceMessages> keys() {
        return asList(LenienceMessages.values());
    }

    @Parameter(0)
    public LenienceMessages key;

    @Override
    protected LenienceMessages getSUT() {
        return this.key;
    }

}
