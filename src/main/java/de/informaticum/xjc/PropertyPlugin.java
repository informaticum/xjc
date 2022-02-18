package de.informaticum.xjc;

import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.NONE;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.BoilerplatePlugin.BECAUSE_METHOD_ALREADY_EXISTS;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.resources.PropertyPluginMessages.COLLECTION_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.COLLECTION_SETTERS_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.FINAL_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.FINAL_FIELD_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.FINAL_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.FINAL_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_DEFAULTED_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_DEFAULTED_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_DEFENSIVE_COPY_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_EMPTY_COLLECTION_CONTAINER;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_LIVE_REFERENCE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_NULLABLE_VALUE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTES_BEGIN;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTES_END;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_DEFAULTED_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_DEFAULTED_VALUE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_DEFENSIVE_COPY_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_DEFENSIVE_COPY_COLLECTION_CONTAINER;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_EMPTY_CONTAINER;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_LIVE_REFERENCE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_LIVE_REFERENCE_CONTAINER;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_NULLABLE_VALUE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_OPTIONAL_VALUE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_REQUIRED_VALUE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.NOTE_UNMODIFIABLE_COLLECTION_CONTAINER;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_GETTER_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_ORDEFAULT_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_VALUE_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.PRIVATE_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.PRIVATE_FIELD_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.REFACTORED_GETTER_INTRO;
import static de.informaticum.xjc.resources.PropertyPluginMessages.REFACTORED_GETTER_OUTRO;
import static de.informaticum.xjc.resources.PropertyPluginMessages.REMOVE_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_DEFAULTED_VALUE_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_GETTER_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_VALUE_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.UNMODIFIABLE_GETTER_JAVADOC;
import static de.informaticum.xjc.util.CodeRetrofit.eraseBody;
import static de.informaticum.xjc.util.CodeRetrofit.eraseJavadoc;
import static de.informaticum.xjc.util.CodeRetrofit.javadocAppendSection;
import static de.informaticum.xjc.util.CollectionAnalysis.isCollectionMethod;
import static de.informaticum.xjc.util.CollectionAnalysis.unmodifiableViewFactoryFor;
import static de.informaticum.xjc.util.OptionalAnalysis.deoptionalisedTypeFor;
import static de.informaticum.xjc.util.OptionalAnalysis.isOptionalMethod;
import static de.informaticum.xjc.util.OptionalAnalysis.optionalTypeFor;
import static de.informaticum.xjc.util.OutlineAnalysis.filter;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedGettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedSettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.Printify.render;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessSetterName;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.CommandLineArgument;
import de.informaticum.xjc.resources.PropertyPluginMessages;
import de.informaticum.xjc.resources.ResourceBundleEntry;
import de.informaticum.xjc.util.CollectionAnalysis;
import de.informaticum.xjc.util.DefaultAnalysis;
import org.slf4j.Logger;

public final class PropertyPlugin
extends AssignmentPlugin {

    private static final Logger LOG = getLogger(PropertyPlugin.class);
    private static final String MODIFY_PROPERTY = "Set {} of property [{}#{}] onto [{}].";
    private static final String REFACTOR_JUST_STRAIGHT                 = "Refactor [{}#{}()]: return value straightly";
    private static final String REFACTOR_AS_DEFAULTED                  = "Refactor [{}#{}()]: return default value if 'null'";
    private static final String REFACTOR_AS_OPTIONAL                   = "Refactor [{}#{}()]: return OptionalDouble, OptionalInt, OptionalLong, or Optional<X> of optional attribute";
    private static final String REFACTOR_AS_UNMODIFIABLE               = "Refactor [{}#{}()]: return unmodifiable view"; // currently only for collections
    private static final String REFACTOR_AS_UNMODIFIABLE_AND_DEFAULTED = "Refactor [{}#{}()]: return unmodifiable view or default value if 'null'"; // currently only for collections
    private static final String REFACTOR_AS_UNMODIFIABLE_AND_OPTIONAL  = "Refactor [{}#{}()]: return Optional<X> of unmodifiable view of optional attribute"; // currently only for collections
    private static final String GENERATE_SETTER = "Generate setter method [{}#{}({})] for property [{}].";
    private static final String MODIFY_METHOD = "Set {} of method [{}#{}] onto [{}].";
    private static final String SKIP_SETTER = "Skip creation of setter method [{}#{}({})] for property [{}] because {}.";
    private static final String REMOVE_SETTER = "Remove setter method [{}#{}(...)].";

    private static final String OPTION_NAME = "informaticum-xjc-properties";
    /*pkg*/ static final CommandLineArgument STRAIGHT_GETTERS   = new CommandLineArgument("properties-straight-getters",   STRAIGHT_GETTERS_DESCRIPTION.text());
    private static final CommandLineArgument OPTIONAL_GETTERS   = new CommandLineArgument("properties-optional-getters",   OPTIONAL_GETTERS_DESCRIPTION.format(STRAIGHT_GETTERS));
    private static final CommandLineArgument OPTIONAL_ORDEFAULT = new CommandLineArgument("properties-optional-ordefault", OPTIONAL_ORDEFAULT_DESCRIPTION.text());
    private static final CommandLineArgument COLLECTION_SETTERS = new CommandLineArgument("properties-collection-setters", COLLECTION_SETTERS_DESCRIPTION.text());
    private static final CommandLineArgument REMOVE_SETTERS     = new CommandLineArgument("properties-remove-setters",     REMOVE_SETTERS_DESCRIPTION.format(COLLECTION_SETTERS));
    private static final CommandLineArgument PRIVATE_FIELDS     = new CommandLineArgument("properties-private-fields",     PRIVATE_FIELDS_DESCRIPTION.text());
    private static final CommandLineArgument FINAL_FIELDS       = new CommandLineArgument("properties-final-fields",       FINAL_FIELDS_DESCRIPTION.format(STRAIGHT_GETTERS));
    private static final CommandLineArgument FINAL_GETTERS      = new CommandLineArgument("properties-final-getters",      FINAL_GETTERS_DESCRIPTION.text());
    private static final CommandLineArgument FINAL_SETTERS      = new CommandLineArgument("properties-final-setters",      FINAL_SETTERS_DESCRIPTION.text());
    // TODO: What about unsetter?

    private static final BiFunction<JExpression, JExpression, PropertyPluginMessages> NOTE_REFERENCE = (x,y) -> x==y ? NOTE_LIVE_REFERENCE : NOTE_DEFENSIVE_COPY_COLLECTION;
    private static final BiFunction<JExpression, JExpression, PropertyPluginMessages> HINT_REFERENCE = (x,y) -> x==y ? HINT_LIVE_REFERENCE : HINT_DEFENSIVE_COPY_COLLECTION;
    private static final BiFunction<JExpression, JExpression, PropertyPluginMessages> NOTE_REFERENCE_CONTAINER = (x,y) -> x==y ? NOTE_LIVE_REFERENCE_CONTAINER : NOTE_DEFENSIVE_COPY_COLLECTION_CONTAINER;

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(NOTNULL_COLLECTIONS, DEFENSIVE_COPIES, UNMODIFIABLE_COLLECTIONS,
                      PRIVATE_FIELDS, FINAL_FIELDS,
                      STRAIGHT_GETTERS, OPTIONAL_GETTERS, OPTIONAL_ORDEFAULT,
                      COLLECTION_SETTERS, REMOVE_SETTERS,
                      FINAL_GETTERS, FINAL_SETTERS);
    }

    @Override
    public final void onActivated(final Options options)
    throws BadCommandLineException {
        // TODO: Create and set custom field renderer factory with immediate result similar to the following generator code
        //       > final var originFieldRendererFactory = options.getFieldRendererFactory();
        //       > options.setFieldRendererFactory(originFieldRendererFactory, this);
        // TODO: Create and set custom name converter?
        //       > final var originNameConverter = options.getNameConverter();
        //       > options.setNameConverter(originNameConverter, this);
        super.onActivated(options);
    }

    @Override
    public final boolean prepareRun() {
        // For all collection fields, the out-of-the-box-getters create modifiable empty collection instances if there is no value assigned.
        // That becomes impossible if fields are final and, thus, {@link #STRAIGHT_GETTERS} must be enabled too.
        FINAL_FIELDS.activates(STRAIGHT_GETTERS);
        // Similar, the out-of-the-box-getters compromises the idea of optional getters and, thus, {@link #STRAIGHT_GETTERS} must be enabled too.
        OPTIONAL_GETTERS.activates(STRAIGHT_GETTERS);
        // Skip {@link #COLLECTION_SETTERS} if setter methods shall be removed
        REMOVE_SETTERS.deactivates(COLLECTION_SETTERS);
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        PRIVATE_FIELDS.doOnActivation(this::setFieldsPrivate, clazz);
        FINAL_FIELDS.doOnActivation(this::setFieldsFinal, clazz);
        STRAIGHT_GETTERS.or(UNMODIFIABLE_COLLECTIONS).or(OPTIONAL_GETTERS).doOnActivation(this::refactorGetter, clazz);
        OPTIONAL_ORDEFAULT.doOnActivation(this::generateOrDefaultGetters, clazz);
        COLLECTION_SETTERS.doOnActivation(this::addCollectionSetter, clazz);
        REMOVE_SETTERS.doOnActivation(this::removeSetter, clazz);
        FINAL_GETTERS.doOnActivation(this::setGettersFinal, clazz);
        FINAL_SETTERS.doOnActivation(this::setSettersFinal, clazz);
        return true;
    }

    private final void setFieldsPrivate(final ClassOutline clazz) {
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            LOG.info(MODIFY_PROPERTY, "accessibility", fullNameOf(clazz), $property.name(), "private");
            javadocAppendSection($property, PRIVATE_FIELD_JAVADOC);
            $property.mods().setPrivate();
        }
    }

    private final void setFieldsFinal(final ClassOutline clazz) {
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            LOG.info(MODIFY_PROPERTY, "mutability", fullNameOf(clazz), $property.name(), "final");
            javadocAppendSection($property, FINAL_FIELD_JAVADOC);
            $property.mods().setFinal(true);
        }
    }

    private final void refactorGetter(final ClassOutline clazz) {
        assertThat(STRAIGHT_GETTERS.getAsBoolean()).isTrue();
        final var properties = generatedPropertiesOf(clazz);
        for (final var getter : generatedGettersOf(clazz).entrySet()) {
            final var attribute = getter.getKey();
            final var attributeInfo = attribute.getPropertyInfo();
            assertThat(properties).containsKey(attribute);
            final var $property = properties.get(attribute);
            final var $getter = getter.getValue();
            final var originJavadoc = new ArrayList<>($getter.javadoc());
            final var $ReturnType = $getter.type();
            final var $OptionalType = optionalTypeFor($ReturnType);
            final var $prop = $this.ref($property);
            final var $default = defaultExpressionFor(attribute);
            final var $nonNull = effectiveExpressionForNonNull($property.type(), $prop);
            final var $optionalEmpty = $OptionalType.erasure().staticInvoke("empty");
            final var $optionalOf = $OptionalType.erasure().staticInvoke("of");

            if ($property.type().isPrimitive()) {
                assertThat($getter).matches(not(CollectionAnalysis::isCollectionMethod));
                assertThat(isOptionalMethod($getter)).isFalse();
                assertThat($ReturnType.isPrimitive()).isTrue();
                assertThat($ReturnType.isReference()).isFalse();
                LOG.debug(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $getter.name());
                supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC);
                supersedeReturns(getter, $property, $ReturnType, STRAIGHT_VALUE_JAVADOC_SUMMARY);
                eraseBody($getter)._return($nonNull);
            } else if (attributeInfo.isCollection()) {
                assertThat($getter).matches(CollectionAnalysis::isCollectionMethod);
                assertThat(isOptionalMethod($getter)).isFalse();
                assertThat($ReturnType.isPrimitive()).isFalse();
                assertThat($ReturnType.isReference()).isTrue();
                // TODO: if $copy is deep copy, the collection view should wrap $copy 
                final var $view = unmodifiableViewFactoryFor($ReturnType).arg($prop);
                if ($default.isPresent() && UNMODIFIABLE_COLLECTIONS.getAsBoolean()) {
                    LOG.debug(REFACTOR_AS_UNMODIFIABLE_AND_DEFAULTED, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION, HINT_DEFAULTED_UNMODIFIABLE_COLLECTION, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION);
                    supersedeReturns(getter, $property, $ReturnType, UNMODIFIABLE_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $default.get(), $view));
                } else if ($default.isPresent() ) {
                    assertThat(UNMODIFIABLE_COLLECTIONS.getAsBoolean()).isFalse();
                    LOG.debug(REFACTOR_AS_DEFAULTED, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_DEFAULTED_COLLECTION, HINT_DEFAULTED_COLLECTION, NOTE_REFERENCE.apply($prop, $nonNull), HINT_REFERENCE.apply($prop, $nonNull));
                    supersedeReturns(getter, $property, $ReturnType, STRAIGHT_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $default.get(), $nonNull));
                } else if (OPTIONAL_GETTERS.getAsBoolean() && isOptional(attribute) && UNMODIFIABLE_COLLECTIONS.getAsBoolean()) {
                    LOG.debug(REFACTOR_AS_UNMODIFIABLE_AND_OPTIONAL, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $OptionalType, OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC, NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_UNMODIFIABLE_COLLECTION_CONTAINER, HINT_UNMODIFIABLE_COLLECTION);
                    supersedeReturns(getter, $property, $OptionalType, OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $optionalEmpty, $optionalOf.arg($view)));
                    $getter.type($OptionalType);
                } else if (OPTIONAL_GETTERS.getAsBoolean() && isOptional(attribute)) {
                    assertThat(UNMODIFIABLE_COLLECTIONS.getAsBoolean()).isFalse();
                    LOG.debug(REFACTOR_AS_OPTIONAL, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $OptionalType, OPTIONAL_GETTER_JAVADOC, NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_REFERENCE_CONTAINER.apply($prop, $nonNull), HINT_REFERENCE.apply($prop, $nonNull));
                    supersedeReturns(getter, $property, $OptionalType, OPTIONAL_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $optionalEmpty, $optionalOf.arg($nonNull)));
                    $getter.type($OptionalType);
                } else if (UNMODIFIABLE_COLLECTIONS.getAsBoolean()) {
                    LOG.debug(REFACTOR_AS_UNMODIFIABLE, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, UNMODIFIABLE_GETTER_JAVADOC, NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION);
                    supersedeReturns(getter, $property, $ReturnType, UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $null, $view));
                } else {
                    assertThat(UNMODIFIABLE_COLLECTIONS.getAsBoolean()).isFalse();
                    LOG.debug(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_REFERENCE.apply($prop, $nonNull), HINT_REFERENCE.apply($prop, $nonNull));
                    supersedeReturns(getter, $property, $ReturnType, STRAIGHT_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(($prop == $nonNull) ? $prop : cond($prop.eq($null), $null, $nonNull));
                }
            // } else if ($ReturnType.isArray()) { // TODO: handle array type similar to collections (defensive copies, non-modifiable, etc.)
            } else {
                assertThat($getter).matches(not(CollectionAnalysis::isCollectionMethod));
                assertThat(isOptionalMethod($getter)).isFalse();
                // assertThat($ReturnType.isPrimitive()).isFalse(); // TODO: return type may be primitive, even if property is not
                // assertThat($ReturnType.isReference()).isTrue();  // TODO: return type may be primitive, even if property is not
                if ($default.isPresent()) {
                    LOG.debug(REFACTOR_AS_DEFAULTED, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, render($default.get()), STRAIGHT_GETTER_JAVADOC, NOTE_DEFAULTED_VALUE);
                    supersedeReturns(getter, $property, render($default.get()), STRAIGHT_DEFAULTED_VALUE_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $default.get(), $nonNull));
                } else if (OPTIONAL_GETTERS.getAsBoolean() && isOptional(attribute)) {
                    assertThat(isOptionalMethod($getter)).withFailMessage("This case is not considered yet ;-(").isFalse();
                    LOG.debug(REFACTOR_AS_OPTIONAL, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $OptionalType, OPTIONAL_GETTER_JAVADOC, NOTE_EMPTY_CONTAINER);
                    supersedeReturns(getter, $property, $OptionalType, OPTIONAL_VALUE_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $optionalEmpty, $optionalOf.arg($nonNull)));
                    $getter.type($OptionalType);
                } else {
                    LOG.debug(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE);
                    supersedeReturns(getter, $property, $ReturnType, STRAIGHT_VALUE_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(($prop == $nonNull) ? $prop : cond($prop.eq($null), $null, $nonNull));
                }
            }
            javadocAppendSection($getter.javadoc(), REFACTORED_GETTER_INTRO);
            $getter.javadoc().addAll(originJavadoc);
            $getter.javadoc().append(REFACTORED_GETTER_OUTRO.text());
        }
    }

    private final void generateOrDefaultGetters(final ClassOutline clazz) {
        final var getters = generatedGettersOf(clazz);
        getters.entrySet().removeIf(e -> !isOptionalMethod(e.getValue()));
        for (final var getter : getters.entrySet()) {
            final var $getOrDefault = this.generateOrDefaultGetter(clazz, getter);
            this.generateOrDefaultGetter(clazz, getter, $getOrDefault);
        }
    }

    private final JMethod generateOrDefaultGetter(final ClassOutline clazz, final Entry<? extends FieldOutline, ? extends JMethod> getter) {
        // TODO: Skip if method already exists
        // TODO: Javadoc
        final var properties = generatedPropertiesOf(clazz);
        final var $Class = clazz.implClass;
        final var attribute = getter.getKey();
        assertThat(properties).containsKey(attribute);
        final var $property = properties.get(attribute);
        final var $getter = getter.getValue();
        final var modifiers = $getter.mods().getValue() | (FINAL_GETTERS.getAsBoolean() ? FINAL : NONE);
        final var $getOrDefault = $Class.method(modifiers, $property.type(), $getter.name() + "OrDefault");
        final var $defaultValue = $getOrDefault.param(FINAL, deoptionalisedTypeFor($getter.type().boxify()).orElse($property.type()), "defaultValue");
        $getOrDefault.body()._return($this.invoke($getter).invoke("orElse").arg($defaultValue));
        return $getOrDefault;
    }

    private final void generateOrDefaultGetter(final ClassOutline clazz, final Entry<? extends FieldOutline, ? extends JMethod> getter, final JMethod $delegation) {
        // TODO: Skip if method already exists
        // TODO: Javadoc
        final var $Class = clazz.implClass;
        final var attribute = getter.getKey();
        final var $getter = getter.getValue();
        final var $defaultFallback = DefaultAnalysis.defaultExpressionFor(attribute, true, UNMODIFIABLE_COLLECTIONS.getAsBoolean());
        if ($defaultFallback.isPresent()) {
            final var $getOrDefault = $Class.method($delegation.mods().getValue(), $delegation.type(), isCollectionMethod($delegation) ? $getter.name() + "OrEmpty" : $delegation.name());
            $getOrDefault.body()._return($this.invoke($delegation).arg($defaultFallback.get()));
        }
    }

    private final void setGettersFinal(final ClassOutline clazz) {
        for (final var $getter : generatedGettersOf(clazz).values()) {
            LOG.info(MODIFY_METHOD, "mutability", fullNameOf(clazz), $getter, "final");
            // TODO: Javadoc
            $getter.mods().setFinal(true);
        }
    }

    private final void addCollectionSetter(final ClassOutline clazz) {
        for (final var collectionProperty : filter(generatedPropertiesOf(clazz), k -> k.getPropertyInfo().isCollection()).entrySet()) {
            final var attribute = collectionProperty.getKey();
            assertThat(attribute.getPropertyInfo().defaultValue).isNull();
            final var $property = collectionProperty.getValue();
            assertThat($property.type().isPrimitive()).isFalse();
            final var setterName = guessSetterName(attribute);
            // 1/3: Prepare
            if (getMethod(clazz, setterName, $property.type()) != null) {
                LOG.error(SKIP_SETTER, fullNameOf(clazz), setterName, $property.type(), $property.name(), BECAUSE_METHOD_ALREADY_EXISTS);
                continue;
            }
            LOG.info(GENERATE_SETTER, fullNameOf(clazz), setterName, $property.type(), $property.name());
            // 2/3: Create
            final var $Class = clazz.implClass;
            final var modifiers = PUBLIC | (FINAL_SETTERS.getAsBoolean() ? FINAL : NONE);
            final var $setter = $Class.method(modifiers, this.codeModel().VOID, setterName);
            // 3/3: Implement
            javadocAppendSection($setter.javadoc(), COLLECTION_SETTERS_JAVADOC, $property.name());
            final var $value = $setter.param(FINAL, $property.type(), $property.name());
            accordingAssignment(collectionProperty, $setter, $value);
        }
    }

    private final void removeSetter(final ClassOutline clazz) {
        final var $Class = clazz.implClass;
        for (final var $setter : generatedSettersOf(clazz).values()) {
            LOG.info(REMOVE_SETTER, fullNameOf(clazz), $setter.name());
            $Class.methods().remove($setter);
            // TODO: Add class javadoc to list all deleted setter methods
        }
    }

    private final void setSettersFinal(final ClassOutline clazz) {
        for (final var $setter : generatedSettersOf(clazz).values()) {
            LOG.info(MODIFY_METHOD, "mutability", fullNameOf(clazz), $setter, "final");
            // TODO: Javadoc
            $setter.mods().setFinal(true);
        }
    }

    private static final void supersedeJavadoc(final Entry<? extends FieldOutline, ? extends JMethod> getter,
                                               final JFieldVar $property, final JType $Type,
                                               final ResourceBundleEntry methodJavadoc, final ResourceBundleEntry... notes) {
        supersedeJavadoc(getter, $property, $Type.erasure().name(), methodJavadoc, notes);
    }

    private static final void supersedeJavadoc(final Entry<? extends FieldOutline, ? extends JMethod> getter,
                                               final JFieldVar $property, final String noteParam,
                                               final ResourceBundleEntry methodJavadoc, final ResourceBundleEntry... notes) {
        final var $javadoc = getter.getValue().javadoc();
        eraseJavadoc($javadoc);
        $javadoc.append(methodJavadoc.format($property.name()));
        $javadoc.append(NOTES_BEGIN.text());
        $javadoc.append((isRequired(getter.getKey()) ? NOTE_REQUIRED_VALUE : NOTE_OPTIONAL_VALUE).text());
        for (final var note : notes) {
            $javadoc.append(note.format(noteParam));
        }
        $javadoc.append(NOTES_END.text());
    }

    private static void supersedeReturns(final Entry<? extends FieldOutline, ? extends JMethod> getter, final JFieldVar $property, final JType $Type, final ResourceBundleEntry returnJavadoc) {
        supersedeReturns(getter, $property, $Type.erasure().name(), returnJavadoc);
    }

    private static void supersedeReturns(final Entry<? extends FieldOutline, ? extends JMethod> getter, final JFieldVar $property, final String noteParam, final ResourceBundleEntry returnJavadoc) {
        final var $return = getter.getValue().javadoc().addReturn();
        eraseJavadoc($return);
        $return.append(returnJavadoc.format($property.name(), noteParam));
    }

}
