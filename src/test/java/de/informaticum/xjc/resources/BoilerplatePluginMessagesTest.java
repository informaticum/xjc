package de.informaticum.xjc.resources;

import static java.util.Arrays.asList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BoilerplatePluginMessagesTest
extends ResourceBundleKeyTest<BoilerplatePluginMessages> {

    @Parameters(name = "{0}")
    public static Iterable<BoilerplatePluginMessages> keys() {
        return asList(BoilerplatePluginMessages.values());
    }

    @Parameter(0)
    public BoilerplatePluginMessages key;

    @Override
    protected BoilerplatePluginMessages getSUT() {
        return this.key;
    }

}
