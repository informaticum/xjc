package de.informaticum.xjc.resources;

import static java.util.Arrays.asList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TracePluginMessagesTest
extends ResourceBundleKeyTest {

    @Parameters(name = "{0}")
    public static Iterable<TracePluginMessages> keys() {
        return asList(TracePluginMessages.values());
    }

    @Parameter(0)
    public TracePluginMessages key;

    @Override
    protected ResourceBundleEntry getSUT() {
        return this.key;
    }

}
