package de.informaticum.xjc.api;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class FooTest {

    @Test
    public void testUsage()
    throws Exception {
        final var plugin = new BarPlugin();
        // System.out.println(plugin.getUsage());
        assertThat(plugin.getUsage()).hasToString(format("  -bar :  Some bar infomation.%n  -aaaaaa :  Some aaaaaa information. Default: aaaaaa%n  -bb     :  Some bb information. Default: bb%n"));
    }

}
