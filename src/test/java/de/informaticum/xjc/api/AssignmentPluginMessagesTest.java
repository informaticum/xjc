package de.informaticum.xjc.api;

import static java.util.Arrays.asList;
import de.informaticum.xjc.plugins.ResourceBundleKeyTest;
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
