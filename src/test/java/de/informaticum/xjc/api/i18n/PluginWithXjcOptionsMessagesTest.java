package de.informaticum.xjc.api.i18n;

import static java.util.Arrays.asList;
import de.informaticum.xjc.api.ResourceBundleKeyTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PluginWithXjcOptionsMessagesTest
extends ResourceBundleKeyTest<PluginWithXjcOptionsMessages> {

    @Parameters(name = "{0}")
    public static Iterable<PluginWithXjcOptionsMessages> keys() {
        return asList(PluginWithXjcOptionsMessages.values());
    }

    @Parameter(0)
    public PluginWithXjcOptionsMessages key;

    @Override
    protected PluginWithXjcOptionsMessages getSUT() {
        return this.key;
    }

}
