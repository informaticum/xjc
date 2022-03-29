package de.informaticum.xjc.plugins.i18n;

import static java.util.Arrays.asList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ConstructionPluginMessagesTest
extends ResourceBundleKeyTest<ConstructionPluginMessages> {

    @Parameters(name = "{0}")
    public static Iterable<ConstructionPluginMessages> keys() {
        return asList(ConstructionPluginMessages.values());
    }

    @Parameter(0)
    public ConstructionPluginMessages key;

    @Override
    protected ConstructionPluginMessages getSUT() {
        return this.key;
    }

}
