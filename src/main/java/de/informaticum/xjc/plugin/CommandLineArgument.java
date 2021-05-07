package de.informaticum.xjc.plugin;

import static de.informaticum.xjc.util.Printify.fullName;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.function.Consumer;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.PackageOutline;
import org.slf4j.Logger;

public class CommandLineArgument {

    private static final Logger LOG = getLogger(CommandLineArgument.class);

    private final String argument;

    private final String description;

    private boolean activated;

    public CommandLineArgument(final String argument) {
        this(argument, "%TBD description%");
    }

    public CommandLineArgument(final String argument, final String description) {
        this.argument = requireNonNull(argument).startsWith("-") ? argument : "-" + argument;
        this.description = requireNonNull(description);
        this.activated = false;
    }

    public final String getArgument() {
        return this.argument;
    }

    public final String getDescription() {
        return this.description;
    }

    public final boolean isActivated() {
        return this.activated;
    }

    public int parseArgument(final Options options, final String[] arguments, final int index) {
        assertThat(this.argument).isEqualTo(arguments[index]);
        this.activated = true;
        return 1;
    }

    public final void alsoActivate(final CommandLineArgument... activateImplicitly) {
        if (this.activated) {
            asList(activateImplicitly).forEach(arg -> arg.activated = true);
        }
    }

    public final void doOnActivation(final Runnable execution) {
        if (this.activated) {
            execution.run();
        } else {
            LOG.trace("Skip execution of XJC option [{}], because it has not been activated.", this.argument);
        }
    }

    public final void doOnActivation(final Consumer<? super PackageOutline> execution, final PackageOutline pakkage) {
        if (this.activated) {
            execution.accept(pakkage);
        } else {
            LOG.trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.argument, fullName(pakkage));
        }
    }

    public final void doOnActivation(final Consumer<? super JDefinedClass> execution, final JDefinedClass $factory) {
        if (this.activated) {
            execution.accept($factory);
        } else {
            LOG.trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.argument, fullName($factory));
        }
    }

    public final void doOnActivation(final Consumer<? super ClassOutline> execution, final ClassOutline clazz) {
        if (this.activated) {
            execution.accept(clazz);
        } else {
            LOG.trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.argument, fullName(clazz));
        }
    }

    public final void doOnActivation(final Consumer<? super EnumOutline> execution, final EnumOutline enumeration) {
        if (this.activated) {
            execution.accept(enumeration);
        } else {
            LOG.trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.argument, fullName(enumeration));
        }
    }

}
