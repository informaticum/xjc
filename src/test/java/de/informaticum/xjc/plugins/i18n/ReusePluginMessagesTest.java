package de.informaticum.xjc.plugins.i18n;

import static java.util.Arrays.asList;
import de.informaticum.xjc.api.ResourceBundleKeyTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReusePluginMessagesTest
extends ResourceBundleKeyTest<ReusePluginMessages> {

    @Parameters(name = "{0}")
    public static Iterable<ReusePluginMessages> keys() {
        return asList(ReusePluginMessages.values());
    }

    @Parameter(0)
    public ReusePluginMessages key;

    @Override
    protected ReusePluginMessages getSUT() {
        return this.key;
    }

}
