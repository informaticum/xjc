package de.informaticum.xjc;

import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedSettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructor;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.Printify.fullName;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessFactoryName;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import org.slf4j.Logger;

public final class ImmutablePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(ImmutablePlugin.class);

    private static final String HIDE_DEFAULT_CONSTRUCTOR = "Hide default constructor [{}#{}()].";
    private static final String SKIP_HIDE_DEFAULT_CONSTRUCTOR = "Skip hiding of default constructor for [{}] because {}.";

    private static final String REMOVE_DEFAULT_FACTORY = "Remove default factory [{}#{}()].";
    private static final String SKIP_REMOVE_DEFAULT_FACTORY = "Skip removal of default factory method for [{}] because {}.";

    private static final String REMOVE_PROPERTY_SETTERS = "Remove property setters [{}#{}({})].";
    private static final String SKIP_REMOVE_PROPERTY_SETTERS = "Skip removal of property setters for [{}] because {}.";

    public static final String OPTION_NAME = "ITBSG-xjc-immutable";
    private static final String HIDE_DEFAULT_CONSTRUCTORS_NAME = "-immutable-hideDefaultConstructors";
    private static final CommandLineArgument HIDE_DEFAULT_CONSTRUCTORS = new CommandLineArgument(HIDE_DEFAULT_CONSTRUCTORS_NAME, "Hides default constructors. Default: false");
    private static final String REMOVE_DEFAULT_FACTORIES_NAME = "-immutable-removeDefaultFactories";
    private static final CommandLineArgument REMOVE_DEFAULT_FACTORIES = new CommandLineArgument(REMOVE_DEFAULT_FACTORIES_NAME, "Removes default factory methods. Default: false");
    private static final String REMOVE_SETTERS_NAME = "-immutable-removeSetters";
    private static final CommandLineArgument REMOVE_SETTERS = new CommandLineArgument(REMOVE_SETTERS_NAME, "Removes the property setters. Default: false");
    private static final String PRIVATE_FIELDS_NAME = "-immutable-privateFields";
    private static final CommandLineArgument PRIVATE_FIELDS = new CommandLineArgument(PRIVATE_FIELDS_NAME, "Modifies the visibility of the generated fields onto 'private'. Default: false");
    private static final String FINAL_FIELDS_NAME = "-immutable-finalFields";
    private static final CommandLineArgument FINAL_FIELDS = new CommandLineArgument(FINAL_FIELDS_NAME, "Modifies the generated fields onto 'final'. Default: false");

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, "Let's make the generated code immutable.");
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(HIDE_DEFAULT_CONSTRUCTORS, REMOVE_DEFAULT_FACTORIES, REMOVE_SETTERS, PRIVATE_FIELDS, FINAL_FIELDS);
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        // TODO: Defensive-copy of Lists in constructors/builders
        this.considerHideDefaultConstructor(clazz);
        this.considerRemoveDefaultFactory(clazz);
        this.considerRemoveSetters(clazz);
        // TODO: Defensive-copy of Lists in property getters
        this.considerPrivateFields(clazz);
        this.considerFinalFields(clazz);
        return true;
    }

    private final void considerHideDefaultConstructor(final ClassOutline clazz) {
        if (!HIDE_DEFAULT_CONSTRUCTORS.isActivated()) {
            LOG.trace(SKIP_HIDE_DEFAULT_CONSTRUCTOR, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getConstructor(clazz) == null) {
            // TODO: Log the absence of the default constructor
        } else {
            LOG.info(HIDE_DEFAULT_CONSTRUCTOR, fullName(clazz), fullName(clazz));
            assertThat(getConstructor(clazz)).isNotNull();
            this.hideDefaultConstructor(clazz);
        }
    }

    private final void hideDefaultConstructor(final ClassOutline clazz) {
        final var $constructor = getConstructor(clazz);
        $constructor.mods().setProtected();
        $constructor.javadoc().append(format("%n%nThis constructor has been intentionally set on {@code protected} visibility to be not used anymore."))
                    .append(format("%nInstead in order to create instances of this class, use the all-values constructor"));
        final var $Builder = stream(clazz.implClass.listClasses()).filter(nested -> "Builder".equals(nested.name())).findFirst();
        if ($Builder.isPresent()) {
            $constructor.javadoc().append(" or utilise the nested ").append($Builder.get());
        }
        $constructor.javadoc().append(".");
        $constructor.javadoc().append(format("%n%nSince JAX-B's reflective instantiation bases on a default constructor, it has not been removed."))
                    .append(format("(As an aside, it cannot be set to {@code private} because the similarly kept sub-classes' default constructors must have access to this constructor.)"));
    }

    private final void considerRemoveDefaultFactory(final ClassOutline clazz) {
        if (!REMOVE_DEFAULT_FACTORIES.isActivated()) {
            LOG.trace(SKIP_REMOVE_DEFAULT_FACTORY, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            final var $objectFactory = clazz._package().objectFactory();
            final var $factory = getMethod($objectFactory, guessFactoryName(clazz));
            if ($factory == null) {
                //
            } else {
                LOG.info(REMOVE_DEFAULT_FACTORY, fullName($objectFactory), $factory.name());
                this.removeDefaultFactory($objectFactory, $factory);
                assertThat(getMethod($objectFactory, guessFactoryName(clazz))).isNull();
            }
        }
    }

    private final void removeDefaultFactory(final JDefinedClass $objectFactory, final JMethod $factory) {
        $objectFactory.methods().remove($factory);
    }


    private final void considerRemoveSetters(final ClassOutline clazz) {
        if (!REMOVE_SETTERS.isActivated()) {
            LOG.trace(SKIP_REMOVE_PROPERTY_SETTERS, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            for (final var setter : generatedSettersOf(clazz).entrySet()) {
                final var $setter = setter.getValue();
                LOG.info(REMOVE_PROPERTY_SETTERS, fullName(clazz), $setter.name(), $setter.type().fullName());
                this.removeSetter(clazz, setter);
            }
        }
    }

    private final void removeSetter(final ClassOutline clazz, final Entry<FieldOutline, JMethod> setter) {
        clazz.implClass.methods().remove(setter.getValue());
    }

    private final void considerPrivateFields(final ClassOutline clazz) {
        if (!PRIVATE_FIELDS.isActivated()) {
            //
        } else {
            this.setFieldsPrivate(clazz);
        }
    }

    private final void setFieldsPrivate(final ClassOutline clazz) {
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            $property.mods().setPrivate();
        }
    }

    private final void considerFinalFields(final ClassOutline clazz) {
        if (!FINAL_FIELDS.isActivated()) {
            //
        } else {
            this.setFieldsFinal(clazz);
        }
    }

    private final void setFieldsFinal(final ClassOutline clazz) {
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            if (property.getKey().getPropertyInfo().isCollection()) {
                // TODO: Handle Collection types -- these are re-assigned (!) within the current getters
            } else {
                final var $property = property.getValue();
                $property.mods().setFinal(true);
            }
        }
    }

}
