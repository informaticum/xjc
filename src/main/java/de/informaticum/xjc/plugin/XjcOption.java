package de.informaticum.xjc.plugin;

import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.function.Consumer;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.PackageOutline;

public abstract interface XjcOption {

    public abstract String getArgument();

    public abstract boolean isActivated();

    public default boolean isNotActivated() {
        return !this.isActivated();
    }

    public default XjcOption or(final XjcOption other) {
        return new XjcOption() {
            @Override
            public final String getArgument() {
                return XjcOption.this.getArgument() + "/" + other.getArgument();
            }

            @Override
            public final boolean isActivated() {
                return XjcOption.this.isActivated() || other.isActivated();
            }
        };
    }

    public default void doOnActivation(final Runnable execution) {
        if (this.isActivated()) {
            execution.run();
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}], because it has not been activated.", this.getArgument());
        }
    }

    public default void doOnActivation(final Consumer<? super PackageOutline> execution, final PackageOutline pakkage) {
        if (this.isActivated()) {
            execution.accept(pakkage);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), fullNameOf(pakkage));
        }
    }

    public default void doOnActivation(final Consumer<? super JDefinedClass> execution, final JDefinedClass $factory) {
        if (this.isActivated()) {
            execution.accept($factory);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), $factory.fullName());
        }
    }

    public default void doOnActivation(final Consumer<? super ClassOutline> execution, final ClassOutline clazz) {
        if (this.isActivated()) {
            execution.accept(clazz);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), fullNameOf(clazz));
        }
    }

    public default void doOnActivation(final Consumer<? super EnumOutline> execution, final EnumOutline enumeration) {
        if (this.isActivated()) {
            execution.accept(enumeration);
        } else {
            getLogger(XjcOption.class).trace("Skip execution of XJC option [{}] for [{}], because it has not been activated.", this.getArgument(), fullNameOf(enumeration));
        }
    }

}
