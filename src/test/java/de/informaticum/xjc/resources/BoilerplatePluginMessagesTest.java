package de.informaticum.xjc.resources;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BoilerplatePluginMessagesTest {

    @Parameters(name = "{0}")
    public static Iterable<BoilerplatePluginMessages> keys() {
        return asList(BoilerplatePluginMessages.values());
    }

    @Parameter(0)
    public BoilerplatePluginMessages key;

    @Test
    public void testApplicability() {
        assertThat(this.key.toString()).isNotNull().isNotEmpty();
    }

}
