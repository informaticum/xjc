package de.informaticum.xjc.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import org.junit.Test;

public abstract class ResourceBundleKeyTest {

    protected abstract ResourceBundleEntry getSUT();

    @Test
    public void verifySUT() {
        final var key = this.getSUT();
        assertThat(key).isNotNull();
    }

    @Test
    public void testText() {
        final var key = this.getSUT();
        assumeThat(key).isNotNull();
        assertThat(key.text()).isNotNull().isNotEmpty().isEqualTo(key.format());
    }

    @Test
    public void testFormattedTextOfZeroArguments() {
        final var key = this.getSUT();
        assumeThat(key).isNotNull();
        assertThat(key.format()).isNotNull().isNotEmpty().isEqualTo(key.text());
    }

}
