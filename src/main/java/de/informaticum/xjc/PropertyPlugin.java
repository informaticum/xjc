package de.informaticum.xjc;

import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.JavaDoc.RETURN_IMMUTABLE_VALUE;
import static de.informaticum.xjc.JavaDoc.RETURN_OPTIONAL_VALUE;
import static de.informaticum.xjc.plugin.TargetCode.$null;
import static de.informaticum.xjc.plugin.TargetCode.$this;
import static de.informaticum.xjc.util.CollectionAnalysis.accordingEmptyFactoryFor;
import static de.informaticum.xjc.util.CollectionAnalysis.accordingImmutableFactoryFor;
import static de.informaticum.xjc.util.OptionalAnalysis.accordingOptionalTypeFor;
import static de.informaticum.xjc.util.OptionalAnalysis.isOptionalMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedGettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedSettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.Printify.fullName;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import de.informaticum.xjc.util.CollectionAnalysis;
import org.slf4j.Logger;

public class PropertyPlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(PropertyPlugin.class);
    
    private static final String GENERATE_IMMUTABLE_GETTER = "Replace return value of [{}#{}()] with an according immutable version";
    private static final String SKIP_IMMUTABLE_GETTERS = "Skip creation of immutable getters for [{}] because {}.";
    private static final String SKIP_IMMUTABLE_GETTER = "Skip creation of immutable getter for [{}] of [{}] because {}.";
    private static final String BECAUSE_ATTRIBUTE_IS_NONCOLLECTION = "attribute is not a collection type";
    private static final String BECAUSE_METHOD_EXISTS = "such method already exists";

    private static final String GENERATE_OPTIONAL_GETTER = "Replace return type X of [{}#{}()] with an according OptionalDouble, OptionalInt, OptionalLong, or Optional<X> type";
    private static final String SKIP_OPTIONAL_GETTER = "Skip creation of optional getter for [{}] of [{}] because {}.";
    private static final String SKIP_OPTIONAL_GETTERS = "Skip creation of optional getters for [{}] because {}.";
    private static final String BECAUSE_ATTRIBUTE_IS_REQUIRED = "attribute is required";

    private static final String REMOVE_PROPERTY_SETTERS = "Remove property setters [{}#{}({})].";
    private static final String SKIP_REMOVE_PROPERTY_SETTERS = "Skip removal of property setters for [{}] because {}.";

    private static final String OPTION_NAME = "ITBSG-xjc-properties";
    private static final String PRIVATE_FIELDS_NAME = "-properties-private-fields";
    private static final CommandLineArgument PRIVATE_FIELDS = new CommandLineArgument(PRIVATE_FIELDS_NAME, "Modifies the visibility of the generated fields onto 'private'. Default: false");
    private static final String FINAL_FIELDS_NAME = "-properties-final-fields";
    private static final CommandLineArgument FINAL_FIELDS = new CommandLineArgument(FINAL_FIELDS_NAME, "Modifies the generated fields onto 'final'. Default: false");
    private static final String GENERATE_IMMUTABLEGETTERS_NAME = "-properties-immutable-getters";
    private static final CommandLineArgument GENERATE_IMMUTABLEGETTERS = new CommandLineArgument(GENERATE_IMMUTABLEGETTERS_NAME, "Replace return value for collection fields' getter methods with an immutable version. Default: false");
    private static final String GENERATE_OPTIONALGETTERS_NAME = "-properties-optional-getters";
    private static final CommandLineArgument GENERATE_OPTIONALGETTERS = new CommandLineArgument(GENERATE_OPTIONALGETTERS_NAME, "Replace return type [T] of non-required fields' getter methods with [OptionalDouble]/[OptionalInt]/[OptionalLong]/[Optional<T>]. Default: false");
    private static final String REMOVE_SETTERS_NAME = "-properties-remove-setters";
    private static final CommandLineArgument REMOVE_SETTERS = new CommandLineArgument(REMOVE_SETTERS_NAME, "Removes the property setters. Default: false");

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, "Generates common boilerplate code.");
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return Arrays.asList(PRIVATE_FIELDS, FINAL_FIELDS, GENERATE_IMMUTABLEGETTERS, GENERATE_OPTIONALGETTERS, REMOVE_SETTERS);
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        this.considerPrivateFields(clazz);
        this.considerFinalFields(clazz);
        this.considerImmutableGetters(clazz);
        this.considerOptionalGetters(clazz);
        this.considerRemoveSetters(clazz);
        return true;
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

    private final void considerImmutableGetters(final ClassOutline clazz) {
        if (!GENERATE_IMMUTABLEGETTERS.isActivated()) {
            LOG.trace(SKIP_IMMUTABLE_GETTERS, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            for (final var getter : generatedGettersOf(clazz).entrySet()) {
                final var attribute = getter.getKey();
                final var $getter = getter.getValue();
                if (!attribute.getPropertyInfo().isCollection()) {
                    assertThat($getter).matches(not(CollectionAnalysis::isCollectionMethod));
                    LOG.debug(SKIP_IMMUTABLE_GETTER, $getter.name(), fullName(clazz), BECAUSE_ATTRIBUTE_IS_NONCOLLECTION);
                } else {
                    assertThat($getter).matches(CollectionAnalysis::isCollectionMethod);
                    LOG.info(GENERATE_IMMUTABLE_GETTER, fullName(clazz), $getter.name());
                    this.generateImmutableGetter(clazz, getter);
                }
            }
        }
    }

    private final void generateImmutableGetter(final ClassOutline clazz, final Entry<? extends FieldOutline, ? extends JMethod> original) {
        final var $class = clazz.implClass;
        final var attribute = original.getKey();
        final var info = attribute.getPropertyInfo();
        final var $originalGetter = original.getValue();
        final var originalType = $originalGetter.type();
        // 1/3: Create
        final var immutableGetter = $class.method($originalGetter.mods().getValue(), originalType, $originalGetter.name());
        // 2/3: JavaDocument
        immutableGetter.javadoc().addReturn().append(format(RETURN_IMMUTABLE_VALUE, info.getName(true)));
        // 3/3: Implement
        final var $empty = accordingEmptyFactoryFor(originalType);
        final var $factory = accordingImmutableFactoryFor(originalType);
        final var $delegation = $this.invoke($originalGetter);
        final var $value = immutableGetter.body().decl(FINAL, originalType, "value", $delegation);
        immutableGetter.body()._return(cond($value.invoke("isEmpty"), $empty, $factory.arg($value)));
        // Subsequently (!) modify the original getter method
        $originalGetter.mods().setPrivate();
        $originalGetter.mods().setFinal(true);
        $originalGetter.name("_mutable_" + $originalGetter.name());
    }

    private final void considerOptionalGetters(final ClassOutline clazz) {
        if (!GENERATE_OPTIONALGETTERS.isActivated()) {
            LOG.trace(SKIP_OPTIONAL_GETTERS, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            for (final var getter : generatedGettersOf(clazz).entrySet()) {
                final var attribute = getter.getKey();
                final var $getter = getter.getValue();
                if (isRequired(attribute)) {
                    LOG.debug(SKIP_OPTIONAL_GETTER, $getter.name(), fullName(clazz), BECAUSE_ATTRIBUTE_IS_REQUIRED);
                } else if (isOptionalMethod($getter)) {
                    LOG.warn(SKIP_OPTIONAL_GETTER, $getter.name(), fullName(clazz), BECAUSE_METHOD_EXISTS);
                } else {
                    LOG.info(GENERATE_OPTIONAL_GETTER, fullName(clazz), $getter.name());
                    this.generateOptionalGetter(clazz, getter);
                }
            }
        }
    }

    private final void generateOptionalGetter(final ClassOutline clazz, final Entry<? extends FieldOutline, ? extends JMethod> original) {
        final var $class = clazz.implClass;
        final var attribute = original.getKey();
        final var info = attribute.getPropertyInfo();
        final var $originalGetter = original.getValue();
        final var originalType = $originalGetter.type();
        // 1/3: Create
        final var optionalType = accordingOptionalTypeFor(originalType);
        final var $optionalGetter = $class.method($originalGetter.mods().getValue(), optionalType, $originalGetter.name());
        // 2/3: JavaDocument
        $optionalGetter.javadoc().addReturn().append(format(RETURN_OPTIONAL_VALUE, info.getName(true)));
        // 3/3: Implement
        final var $OptionalClass = optionalType.erasure();
        final var $delegation = $this.invoke($originalGetter);
        if (originalType.isPrimitive()) {
            $optionalGetter.body()._return($OptionalClass.staticInvoke("of").arg($delegation));
        } else {
            final var $value = $optionalGetter.body().decl(FINAL, originalType, "value", $delegation);
            $optionalGetter.body()._return(cond($value.eq($null), $OptionalClass.staticInvoke("empty"), $OptionalClass.staticInvoke("of").arg($value)));
        }
        // Subsequently (!) modify the original getter method
        $originalGetter.mods().setPrivate();
        $originalGetter.mods().setFinal(true);
        $originalGetter.name("_nonoptional_" + $originalGetter.name());
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

}
