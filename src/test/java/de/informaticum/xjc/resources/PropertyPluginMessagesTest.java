package de.informaticum.xjc.resources;

import static java.util.Arrays.asList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PropertyPluginMessagesTest
extends ResourceBundleKeyTest<PropertyPluginMessages> {

    @Parameters(name = "{0}")
    public static Iterable<PropertyPluginMessages> keys() {
        return asList(PropertyPluginMessages.values());
    }

    @Parameter(0)
    public PropertyPluginMessages key;

    @Override
    protected PropertyPluginMessages getSUT() {
        return this.key;
    }

}
