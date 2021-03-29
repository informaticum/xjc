package de.informaticum.xjc;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class BarTest {

    @Test
    public void testName()
    throws Exception {
        final var plugin = new FooPlugin();
        // System.out.println(plugin.getUsage());
        assertThat(plugin.getUsage()).hasToString(format("  -foo :  Some foo infomation.%n  -a  :  Some a information. Default: a%n  -bb :  Some bb information. Default: bb%n"));
    }

}
