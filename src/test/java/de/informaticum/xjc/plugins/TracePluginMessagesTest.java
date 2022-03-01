package de.informaticum.xjc.plugins;

import static java.util.Arrays.asList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TracePluginMessagesTest
extends ResourceBundleKeyTest<TracePluginMessages> {

    @Parameters(name = "{0}")
    public static Iterable<TracePluginMessages> keys() {
        return asList(TracePluginMessages.values());
    }

    @Parameter(0)
    public TracePluginMessages key;

    @Override
    protected TracePluginMessages getSUT() {
        return this.key;
    }

}
