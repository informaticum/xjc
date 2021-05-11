package de.informaticum.xjc;

import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugin.TargetCode.$null;
import static de.informaticum.xjc.plugin.TargetCode.$this;
import static de.informaticum.xjc.util.CollectionAnalysis.accordingDefaultFactoryFor;
import static de.informaticum.xjc.util.CollectionAnalysis.accordingEmptyFactoryFor;
import static de.informaticum.xjc.util.CollectionAnalysis.accordingUnmodifiableViewFactoryFor;
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
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import de.informaticum.xjc.util.CollectionAnalysis;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class PropertyPlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(PropertyPlugin.class);

    private static final String OPTION_NAME = "informaticum-xjc-properties";
    private static final CommandLineArgument PRIVATE_FIELDS                = new CommandLineArgument("properties-private-fields",       "Modifies the visibility of the generated fields onto 'private'. Default: false");
    private static final CommandLineArgument FINAL_FIELDS                  = new CommandLineArgument("properties-final-fields",         "Modifies the generated fields onto 'final' (automatically enables option '-properties-straight-getters'). Default: false");
    private static final CommandLineArgument GENERATE_STRAIGHT_GETTERS     = new CommandLineArgument("properties-straight-getters",     "Refactor collection fields' getter methods with immediate return statement, i.e., without previous implicit field assigment in case of an actual 'null' value. Default: false");
    private static final CommandLineArgument GENERATE_UNMODIFIABLE_GETTERS = new CommandLineArgument("properties-unmodifiable-getters", "Replace return value for collection fields' getter methods with an unmodifiable view. Default: false");
    private static final CommandLineArgument GENERATE_OPTIONAL_GETTERS     = new CommandLineArgument("properties-optional-getters",     "Replace return type [T] of non-required non-collection fields' getter methods with [OptionalDouble]/[OptionalInt]/[OptionalLong]/[Optional<T>]. Default: false");
    private static final CommandLineArgument REMOVE_SETTERS                = new CommandLineArgument("properties-remove-setters",       "Removes the property setters. Default: false");

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, "Refactors the code of type fields (i.e., origin field, according getter or setter).");
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return Arrays.asList(PRIVATE_FIELDS, FINAL_FIELDS, GENERATE_STRAIGHT_GETTERS, GENERATE_UNMODIFIABLE_GETTERS, GENERATE_OPTIONAL_GETTERS, REMOVE_SETTERS);
    }

    @Override
    public void onActivated(final Options options)
    throws BadCommandLineException {
        // TODO: Create and set custom field renderer factory with immediate result similar to the following generator code
        // final var originFieldRendererFactory = options.getFieldRendererFactory();
        // options.setFieldRendererFactory(originFieldRendererFactory, this);
        // TODO: Create and set custom name converter?
        // final var originNameConverter = options.getNameConverter();
        // options.setNameConverter(originNameConverter, this);
        super.onActivated(options);
    }

    @Override
    public final boolean prepareRun(final Outline outline, final Options options, final ErrorHandler errorHandler)
    throws SAXException {
        FINAL_FIELDS.alsoActivate(GENERATE_STRAIGHT_GETTERS);
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        PRIVATE_FIELDS.doOnActivation(this::setFieldsPrivate, clazz);
        FINAL_FIELDS.doOnActivation(this::setFieldsFinal, clazz);
        GENERATE_STRAIGHT_GETTERS.doOnActivation(this::generateStraightGetter, clazz);
        GENERATE_UNMODIFIABLE_GETTERS.doOnActivation(this::generateUnmodifiableGetter, clazz);
        GENERATE_OPTIONAL_GETTERS.doOnActivation(this::generateOptionalGetter, clazz);
        REMOVE_SETTERS.doOnActivation(this::removeSetter, clazz);
        return true;
    }

    private final void setFieldsPrivate(final ClassOutline clazz) {
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            LOG.info("Set accessibility of property [{}#{}] onto [private].", fullName(clazz), $property.name());
            $property.mods().setPrivate();
        }
    }

    private final void setFieldsFinal(final ClassOutline clazz) {
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            LOG.info("Set mutability of property [{}#{}] onto [final].", fullName(clazz), $property.name());
            $property.mods().setFinal(true);
        }
    }

    private final void generateStraightGetter(final ClassOutline clazz) {
        final var properties = generatedPropertiesOf(clazz);
        for (final var getter : generatedGettersOf(clazz).entrySet()) {
            final var property = getter.getKey();
            assertThat(properties).containsKey(property);
            final var $getter = getter.getValue();
            if (!property.getPropertyInfo().isCollection()) {
                assertThat($getter).matches(not(CollectionAnalysis::isCollectionMethod));
                LOG.debug("Skip creation of straight getter for [{}#{}()] because attribute is not a collection type.", fullName(clazz), $getter.name());
            } else {
                assertThat($getter).matches(CollectionAnalysis::isCollectionMethod);
                LOG.info("Replace original implementation body [{}#{}()] with a similar but straight version.", fullName(clazz), $getter.name());
                final var $property = properties.get(property);
                final var $class = clazz.implClass;
                final var info = property.getPropertyInfo();
                final var $OriginalType = $getter.type();
                // 1/3: Create
                final var $straightGetter = $class.method($getter.mods().getValue(), $OriginalType, $getter.name());
                // 2/3: JavaDocument
                $straightGetter.javadoc().addAll($getter.javadoc());
                $straightGetter.javadoc().append("")
                                         .append("@implNote In opposite to the origin getter implementation, <a href=\"https://github.com/informaticum/xjc\">this implementation</a> does not assign the field with a default value in case of an actual null value.");
                $straightGetter.javadoc().addReturn().append(format("the value of the attribute '%s'", info.getName(true)));
                // 3/3: Implement
                final var $factory = accordingDefaultFactoryFor($OriginalType);
                $straightGetter.body()._return(cond($this.ref($property).eq($null), $factory, $this.ref($property)));
                // Subsequently (!) remove the original getter method
                $class.methods().remove($getter);
            }
        }

    }

    private final void generateUnmodifiableGetter(final ClassOutline clazz) {
        for (final var getter : generatedGettersOf(clazz).entrySet()) {
            final var attribute = getter.getKey();
            final var $getter = getter.getValue();
            if (!attribute.getPropertyInfo().isCollection()) {
                assertThat($getter).matches(not(CollectionAnalysis::isCollectionMethod));
                LOG.debug("Skip creation of unmodifiable view getter for [{}#{}()] because attribute is not a collection type.", fullName(clazz), $getter.name());
            } else {
                assertThat($getter).matches(CollectionAnalysis::isCollectionMethod);
                LOG.info("Replace return value of [{}#{}()] with an according unmodifiable view version.", fullName(clazz), $getter.name());
                final var $class = clazz.implClass;
                final var info = attribute.getPropertyInfo();
                final var $OriginalType = $getter.type();
                // 1/3: Create
                final var $unmodifiableGetter = $class.method($getter.mods().getValue(), $OriginalType, $getter.name());
                // 2/3: JavaDocument
                $unmodifiableGetter.javadoc().append("@implNote In opposite to the origin getter implementation, <a href=\"https://github.com/informaticum/xjc\">this implementation</a> returns an unmodifiable view of the current value.");
                $unmodifiableGetter.javadoc().addReturn().append(format("an unmodifiable view of the value of the attribute '%s'", info.getName(true)));
                // 3/3: Implement
                final var $empty = accordingEmptyFactoryFor($OriginalType);
                final var $factory = accordingUnmodifiableViewFactoryFor($OriginalType);
                final var $delegation = $this.invoke($getter);
                final var $value = $unmodifiableGetter.body().decl(FINAL, $OriginalType, "value", $delegation);
                $unmodifiableGetter.body()._return(cond($value.invoke("isEmpty"), $empty, $factory.arg($value)));
                // Subsequently (!) modify the original getter method
                $getter.mods().setPrivate();
                $getter.mods().setFinal(true);
                $getter.name("_mutable_" + $getter.name());
            }
        }
    }

    private final void generateOptionalGetter(final ClassOutline clazz) {
        for (final var getter : generatedGettersOf(clazz).entrySet()) {
            final var attribute = getter.getKey();
            final var $getter = getter.getValue();
            if (isRequired(attribute)) {
                LOG.debug("Skip creation of optional getter for [{}#{}()] because attribute is required.", fullName(clazz), $getter.name());
            } else if (attribute.getPropertyInfo().isCollection()) {
                LOG.debug("Skip creation of optional getter for [{}#{}()] because attribute is a collection (and, thus, will be represented by an empty collection if missing).", fullName(clazz), $getter.name());
            } else if (isOptionalMethod($getter)) {
                LOG.warn("Skip creation of optional getter for [{}#{}()] because such method already exists.", fullName(clazz), $getter.name());
            } else {
                LOG.info("Replace return type X of [{}#{}()] with an according OptionalDouble, OptionalInt, OptionalLong, or Optional<X> type.", fullName(clazz), $getter.name());
                final var $class = clazz.implClass;
                final var info = attribute.getPropertyInfo();
                final var $OriginalType = $getter.type();
                // 1/3: Create
                final var $OptionalType = accordingOptionalTypeFor($OriginalType);
                final var $optionalGetter = $class.method($getter.mods().getValue(), $OptionalType, $getter.name());
                // 2/3: JavaDocument
                $optionalGetter.javadoc().append("@implNote In opposite to the origin getter implementation, <a href=\"https://github.com/informaticum/xjc\">this implementation</a> returns an optional view of the current value.");
                $optionalGetter.javadoc().addReturn().append(format("the value of the optional attribute '%s'", info.getName(true)));
                // 3/3: Implement
                final var $delegation = $this.invoke($getter);
                if ($OriginalType.isPrimitive()) {
                    $optionalGetter.body()._return($OptionalType.staticInvoke("of").arg($delegation));
                } else {
                    final var $value = $optionalGetter.body().decl(FINAL, $OriginalType, "value", $delegation);
                    $optionalGetter.body()._return(cond($value.eq($null), $OptionalType.staticInvoke("empty"), $OptionalType.staticInvoke("of").arg($value)));
                }
                // Subsequently (!) modify the original getter method
                $getter.mods().setPrivate();
                $getter.mods().setFinal(true);
                $getter.name("_nonoptional_" + $getter.name());
            }
        }
    }

    private final void removeSetter(final ClassOutline clazz) {
        for (final var $setter : generatedSettersOf(clazz).values()) {
            LOG.info("Remove property setter [{}#{}(...)].", fullName(clazz), $setter.name());
            final var $class = clazz.implClass;
            $class.methods().remove($setter);
        }
    }

}
