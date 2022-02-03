package de.informaticum.xjc.resources;

import static java.util.Arrays.asList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReusePluginMessagesTest
extends ResourceBundleKeyTest {

    @Parameters(name = "{0}")
    public static Iterable<ReusePluginMessages> keys() {
        return asList(ReusePluginMessages.values());
    }

    @Parameter(0)
    public ReusePluginMessages key;

    @Override
    protected ResourceBundleEntry getSUT() {
        return this.key;
    }

}
