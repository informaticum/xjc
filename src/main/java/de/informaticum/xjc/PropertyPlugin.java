package de.informaticum.xjc;

import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.BoilerplatePlugin.BECAUSE_METHOD_ALREADY_EXISTS;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.resources.PropertyPluginMessages.COLLECTION_INIT_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.COLLECTION_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.COLLECTION_SETTERS_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.DEFAULTED_OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.PropertyPluginMessages.DEFAULTED_REQUIRED_ARGUMENT;
import static de.informaticum.xjc.resources.PropertyPluginMessages.DEFENSIVE_COPIES_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.FINAL_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.FINAL_FIELD_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_DEFAULTED_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_DEFAULTED_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_DEFENSIVE_COPY_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_EMPTY_COLLECTION_CONTAINER;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_LIVE_REFERENCE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_NULLABLE_VALUE;
import static de.informaticum.xjc.resources.PropertyPluginMessages.HINT_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.ILLEGAL_NULL_VALUE;
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
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_ARGUMENT;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_GETTER_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTIONAL_VALUE_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.PRIVATE_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.PRIVATE_FIELD_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.REFACTORED_GETTER_INTRO;
import static de.informaticum.xjc.resources.PropertyPluginMessages.REFACTORED_GETTER_OUTRO;
import static de.informaticum.xjc.resources.PropertyPluginMessages.REMOVE_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.REQUIRED_ARGUMENT;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_DEFAULTED_VALUE_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_GETTER_JAVADOC;
import static de.informaticum.xjc.resources.PropertyPluginMessages.STRAIGHT_VALUE_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY;
import static de.informaticum.xjc.resources.PropertyPluginMessages.UNMODIFIABLE_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.resources.PropertyPluginMessages.UNMODIFIABLE_GETTER_JAVADOC;
import static de.informaticum.xjc.util.CodeRetrofit.eraseBody;
import static de.informaticum.xjc.util.CodeRetrofit.eraseJavadoc;
import static de.informaticum.xjc.util.CodeRetrofit.javadocAppendSection;
import static de.informaticum.xjc.util.CollectionAnalysis.unmodifiableViewFactoryFor;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
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
import java.util.function.BooleanSupplier;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.CommandLineArgument;
import de.informaticum.xjc.resources.ResourceBundleEntry;
import de.informaticum.xjc.util.CollectionAnalysis;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

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
    private static final String SKIP_SETTER = "Skip creation of setter method [{}#{}({})] for property [{}] because {}.";
    private static final String REMOVE_SETTER = "Remove setter method [{}#{}(...)].";

    private static final String OPTION_NAME = "informaticum-xjc-properties";
    private static final CommandLineArgument COLLECTION_INIT      = new CommandLineArgument("properties-initialise-collections", COLLECTION_INIT_DESCRIPTION.text());
    private static final CommandLineArgument DEFENSIVE_COPIES     = new CommandLineArgument("properties-defensive-copies",       DEFENSIVE_COPIES_DESCRIPTION.text());
    private static final CommandLineArgument STRAIGHT_GETTERS     = new CommandLineArgument("properties-straight-getters",       STRAIGHT_GETTERS_DESCRIPTION.text());
    private static final CommandLineArgument OPTIONAL_GETTERS     = new CommandLineArgument("properties-optional-getters",       OPTIONAL_GETTERS_DESCRIPTION.format(STRAIGHT_GETTERS));
    private static final CommandLineArgument UNMODIFIABLE_GETTERS = new CommandLineArgument("properties-unmodifiable-getters",   UNMODIFIABLE_GETTERS_DESCRIPTION.format(STRAIGHT_GETTERS));
    private static final CommandLineArgument COLLECTION_SETTERS   = new CommandLineArgument("properties-collection-setters",     COLLECTION_SETTERS_DESCRIPTION.text());
    private static final CommandLineArgument REMOVE_SETTERS       = new CommandLineArgument("properties-remove-setters",         REMOVE_SETTERS_DESCRIPTION.format(COLLECTION_SETTERS));
    private static final CommandLineArgument PRIVATE_FIELDS       = new CommandLineArgument("properties-private-fields",         PRIVATE_FIELDS_DESCRIPTION.text());
    private static final CommandLineArgument FINAL_FIELDS         = new CommandLineArgument("properties-final-fields",           FINAL_FIELDS_DESCRIPTION.format(STRAIGHT_GETTERS));
    // TODO: What about unsetter?

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(PRIVATE_FIELDS, FINAL_FIELDS, COLLECTION_INIT, DEFENSIVE_COPIES, STRAIGHT_GETTERS, OPTIONAL_GETTERS, UNMODIFIABLE_GETTERS, COLLECTION_SETTERS, REMOVE_SETTERS);
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
    public final boolean prepareRun()
    throws SAXException {
        FINAL_FIELDS.activates(STRAIGHT_GETTERS);
        OPTIONAL_GETTERS.activates(STRAIGHT_GETTERS);
        UNMODIFIABLE_GETTERS.activates(STRAIGHT_GETTERS);
        REMOVE_SETTERS.deactivates(COLLECTION_SETTERS);
        return true;
    }

    @Override
    protected BooleanSupplier initCollections() {
        return COLLECTION_INIT;
    }

    @Override
    protected BooleanSupplier createDefensiveCopies() {
        return DEFENSIVE_COPIES;
    }

    @Override
    protected BooleanSupplier createUnmodifiableCollections() {
        return UNMODIFIABLE_GETTERS;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        PRIVATE_FIELDS.doOnActivation(this::setFieldsPrivate, clazz);
        FINAL_FIELDS.doOnActivation(this::setFieldsFinal, clazz);
        STRAIGHT_GETTERS.or(UNMODIFIABLE_GETTERS).or(OPTIONAL_GETTERS).doOnActivation(this::refactorGetter, clazz);
        COLLECTION_SETTERS.doOnActivation(this::addCollectionSetter, clazz);
        REMOVE_SETTERS.doOnActivation(this::removeSetter, clazz);
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
            final var $default = defaultValueFor(attribute, COLLECTION_INIT, UNMODIFIABLE_GETTERS);
            final var $copy = this.potentialDefensiveCopy(attribute, $property, $prop);
            final var $view = unmodifiableViewFactoryFor($ReturnType).arg($prop);
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
                eraseBody($getter)._return($copy);
            } else if (attributeInfo.isCollection()) {
                assertThat($getter).matches(CollectionAnalysis::isCollectionMethod);
                assertThat(isOptionalMethod($getter)).isFalse();
                assertThat($ReturnType.isPrimitive()).isFalse();
                assertThat($ReturnType.isReference()).isTrue();
                final var NOTE_REFERENCE = DEFENSIVE_COPIES.getAsBoolean() ? NOTE_DEFENSIVE_COPY_COLLECTION : NOTE_LIVE_REFERENCE;
                final var NOTE_REFERENCE_CONTAINER = DEFENSIVE_COPIES.getAsBoolean() ? NOTE_DEFENSIVE_COPY_COLLECTION_CONTAINER : NOTE_LIVE_REFERENCE_CONTAINER;
                final var HINT_REFERENCE = DEFENSIVE_COPIES.getAsBoolean() ? HINT_DEFENSIVE_COPY_COLLECTION : HINT_LIVE_REFERENCE;
                if ($default.isPresent() && UNMODIFIABLE_GETTERS.getAsBoolean()) {
                    LOG.debug(REFACTOR_AS_UNMODIFIABLE_AND_DEFAULTED, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION, HINT_DEFAULTED_UNMODIFIABLE_COLLECTION, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION);
                    supersedeReturns(getter, $property, $ReturnType, UNMODIFIABLE_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $default.get(), $view));
                } else if ($default.isPresent() ) {
                    assertThat(UNMODIFIABLE_GETTERS.getAsBoolean()).isFalse();
                    LOG.debug(REFACTOR_AS_DEFAULTED, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_DEFAULTED_COLLECTION, HINT_DEFAULTED_COLLECTION, NOTE_REFERENCE, HINT_REFERENCE);
                    supersedeReturns(getter, $property, $ReturnType, STRAIGHT_COLLECTION_OR_EMPTY_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $default.get(), $copy));
                } else if (OPTIONAL_GETTERS.getAsBoolean() && isOptional(attribute) && UNMODIFIABLE_GETTERS.getAsBoolean()) {
                    LOG.debug(REFACTOR_AS_UNMODIFIABLE_AND_OPTIONAL, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $OptionalType, OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC, NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_UNMODIFIABLE_COLLECTION_CONTAINER, HINT_UNMODIFIABLE_COLLECTION);
                    supersedeReturns(getter, $property, $OptionalType, OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $optionalEmpty, $optionalOf.arg($view)));
                    $getter.type($OptionalType);
                } else if (OPTIONAL_GETTERS.getAsBoolean() && isOptional(attribute)) {
                    assertThat(UNMODIFIABLE_GETTERS.getAsBoolean()).isFalse();
                    LOG.debug(REFACTOR_AS_OPTIONAL, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $OptionalType, OPTIONAL_GETTER_JAVADOC, NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_REFERENCE_CONTAINER, HINT_REFERENCE);
                    supersedeReturns(getter, $property, $OptionalType, OPTIONAL_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $optionalEmpty, $optionalOf.arg($copy)));
                    $getter.type($OptionalType);
                } else if (UNMODIFIABLE_GETTERS.getAsBoolean()) {
                    LOG.debug(REFACTOR_AS_UNMODIFIABLE, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, UNMODIFIABLE_GETTER_JAVADOC, NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION);
                    supersedeReturns(getter, $property, $ReturnType, UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $null, $view));
                } else {
                    assertThat(UNMODIFIABLE_GETTERS.getAsBoolean()).isFalse();
                    LOG.debug(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_REFERENCE, HINT_REFERENCE);
                    supersedeReturns(getter, $property, $ReturnType, STRAIGHT_COLLECTION_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(($prop == $copy) ? $prop : cond($prop.eq($null), $null, $copy));
                }
            // } else if ($ReturnType.isArray()) { // TODO: handle array type similar to collections (defensive copies, non-modifiable, etc.)
            } else {
                assertThat($getter).matches(not(CollectionAnalysis::isCollectionMethod));
                assertThat(isOptionalMethod($getter)).isFalse();
                assertThat($ReturnType.isPrimitive()).isFalse();
                assertThat($ReturnType.isReference()).isTrue();
                if ($default.isPresent()) {
                    LOG.debug(REFACTOR_AS_DEFAULTED, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, render($default.get()), STRAIGHT_GETTER_JAVADOC, NOTE_DEFAULTED_VALUE);
                    supersedeReturns(getter, $property, render($default.get()), STRAIGHT_DEFAULTED_VALUE_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $default.get(), $copy));
                } else if (OPTIONAL_GETTERS.getAsBoolean() && isOptional(attribute)) {
                    assertThat(isOptionalMethod($getter)).withFailMessage("This case is not considered yet ;-(").isFalse();
                    LOG.debug(REFACTOR_AS_OPTIONAL, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $OptionalType, OPTIONAL_GETTER_JAVADOC, NOTE_EMPTY_CONTAINER);
                    supersedeReturns(getter, $property, $OptionalType, OPTIONAL_VALUE_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(cond($prop.eq($null), $optionalEmpty, $optionalOf.arg($copy)));
                    $getter.type($OptionalType);
                } else {
                    LOG.debug(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $getter.name());
                    supersedeJavadoc(getter, $property, $ReturnType, STRAIGHT_GETTER_JAVADOC, NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE);
                    supersedeReturns(getter, $property, $ReturnType, STRAIGHT_VALUE_JAVADOC_SUMMARY);
                    eraseBody($getter)._return(($prop == $copy) ? $prop : cond($prop.eq($null), $null, $copy));
                }
            }
            javadocAppendSection($getter.javadoc(), REFACTORED_GETTER_INTRO);
            $getter.javadoc().addAll(originJavadoc);
            $getter.javadoc().append(REFACTORED_GETTER_OUTRO.text());
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

    private static void supersedeReturns(final Entry<? extends FieldOutline, ? extends JMethod> getter,
                                         final JFieldVar $property, final JType $Type,
                                         final ResourceBundleEntry returnJavadoc) {
        supersedeReturns(getter, $property, $Type.erasure().name(), returnJavadoc);
    }

    private static void supersedeReturns(final Entry<? extends FieldOutline, ? extends JMethod> getter,
                                         final JFieldVar $property, final String noteParam,
                                         final ResourceBundleEntry returnJavadoc) {
        final var $return = getter.getValue().javadoc().addReturn();
        eraseJavadoc($return);
        $return.append(returnJavadoc.format($property.name(), noteParam));
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
            final var $setter = $Class.method(PUBLIC | FINAL, this.codeModel().VOID, setterName);
            // 3/3: Implement
            javadocAppendSection($setter.javadoc(), COLLECTION_SETTERS_JAVADOC, $property.name());
            final var $value = $setter.param(FINAL, $property.type(), $property.name());
            final var $valueDoc = $setter.javadoc().addParam($value);
            final var $default = defaultValueFor(attribute, COLLECTION_INIT, UNMODIFIABLE_GETTERS);
            if ($default.isPresent()) {
                javadocAppendSection($valueDoc, isRequired(attribute) ? DEFAULTED_REQUIRED_ARGUMENT : DEFAULTED_OPTIONAL_ARGUMENT, $property.name(), render($default.get()));
                this.accordingAssignment(attribute, $setter, $property, $value);
            } else if (isRequired(attribute)) {
                javadocAppendSection($valueDoc, REQUIRED_ARGUMENT, $property.name());
                javadocAppendSection($setter.javadoc().addThrows(IllegalArgumentException.class), ILLEGAL_NULL_VALUE);
                this.accordingAssignment(attribute, $setter, $property, $value);
            } else {
                javadocAppendSection($valueDoc, OPTIONAL_ARGUMENT, $property.name());
                this.accordingAssignment(attribute, $setter, $property, $value);
            }
        }
    }

    private final void removeSetter(final ClassOutline clazz) {
        final var $Class = clazz.implClass;
        for (final var $setter : generatedSettersOf(clazz).values()) {
            LOG.info(REMOVE_SETTER, fullNameOf(clazz), $setter.name());
            $Class.methods().remove($setter);
        }
    }

}
