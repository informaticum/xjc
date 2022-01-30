package de.informaticum.xjc.resources;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TracePluginMessagesTest {

    @Parameters(name = "{0}")
    public static Iterable<TracePluginMessages> keys() {
        return asList(TracePluginMessages.values());
    }

    @Parameter(0)
    public TracePluginMessages key;

    @Test
    public void testApplicability() {
        assertThat(this.key.toString()).isNotNull().isNotEmpty();
    }

}
