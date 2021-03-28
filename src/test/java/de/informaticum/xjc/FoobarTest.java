package de.informaticum.xjc;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class FoobarTest {

    @Test
    public void testName()
    throws Exception {
        final var plugin = new FoobarPlugin();
        System.out.println(plugin.getUsage());
        assertThat(plugin.getUsage()).hasToString(format("  -foobar :  Some foobar infomation.%n"));
    }

}
