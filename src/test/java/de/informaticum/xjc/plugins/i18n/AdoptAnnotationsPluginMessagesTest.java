package de.informaticum.xjc.plugins.i18n;

import static java.util.Arrays.asList;
import de.informaticum.xjc.api.ResourceBundleKeyTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AdoptAnnotationsPluginMessagesTest
extends ResourceBundleKeyTest<AdoptAnnotationsPluginMessages> {

    @Parameters(name = "{0}")
    public static Iterable<AdoptAnnotationsPluginMessages> keys() {
        return asList(AdoptAnnotationsPluginMessages.values());
    }

    @Parameter(0)
    public AdoptAnnotationsPluginMessages key;

    @Override
    protected AdoptAnnotationsPluginMessages getSUT() {
        return this.key;
    }

}
