package de.informaticum.xjc.plugins.i18n;

import static java.util.Arrays.asList;
import de.informaticum.xjc.api.ResourceBundleKeyTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AssignmentPluginMessagesTest
extends ResourceBundleKeyTest<AssignmentPluginMessages> {

    @Parameters(name = "{0}")
    public static Iterable<AssignmentPluginMessages> keys() {
        return asList(AssignmentPluginMessages.values());
    }

    @Parameter(0)
    public AssignmentPluginMessages key;

    @Override
    protected AssignmentPluginMessages getSUT() {
        return this.key;
    }

}
