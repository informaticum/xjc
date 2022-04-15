package de.informaticum.xjc.plugins;

import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static de.informaticum.xjc.plugins.BoilerplatePlugin.BECAUSE_METHOD_ALREADY_EXISTS;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.COLLECTION_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.COLLECTION_SETTER_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.COLLECTION_SETTER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.FINAL_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.FINAL_FIELD_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.FINAL_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.FINAL_GETTER_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.FINAL_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.FINAL_SETTER_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTIONAL_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTIONAL_ORDEFAULT_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.ORBUILTIN_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.ORBUILTIN_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.ORBUILTIN_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.ORDEFAULT_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.ORDEFAULT_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.ORDEFAULT_PARAM;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.ORDEFAULT_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.PRIVATE_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.PRIVATE_FIELD_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.REFACTORED_GETTER_IMPLNOTE_INTRO;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.REFACTORED_GETTER_IMPLNOTE_OUTRO;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.REMOVED_SETTERS_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.REMOVE_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.STRAIGHT_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.deoptionalisedTypeFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.isCollectionMethod;
import static de.informaticum.xjc.util.CodeModelAnalysis.isOptionalMethod;
import static de.informaticum.xjc.util.CodeModelAnalysis.javadocNameOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.render;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static de.informaticum.xjc.util.OutlineAnalysis.filter;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedGettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedSettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.guessSetterName;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.javadocNameOf;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.api.CommandLineArgument;
import de.informaticum.xjc.util.CodeModelAnalysis;
import de.informaticum.xjc.util.OutlineAnalysis;
import org.slf4j.Logger;

public final class PropertyPlugin
extends AssignmentPlugin {

    private static final Logger LOG = getLogger(PropertyPlugin.class);
    private static final String MODIFY_PROPERTY = "Modify {} of property [{}#{}] to [{}].";
    private static final String REFACTOR_JUST_STRAIGHT                 = "Refactor [{}#{}()]: return value straightly";
    private static final String REFACTOR_AS_DEFAULTED                  = "Refactor [{}#{}()]: return default value if 'null'";
    private static final String REFACTOR_AS_OPTIONAL                   = "Refactor [{}#{}()]: return OptionalDouble, OptionalInt, OptionalLong, or Optional<X> of optional attribute";
    private static final String REFACTOR_AS_UNMODIFIABLE               = "Refactor [{}#{}()]: return unmodifiable view"; // currently only for collections
    private static final String REFACTOR_AS_UNMODIFIABLE_AND_DEFAULTED = "Refactor [{}#{}()]: return unmodifiable view or default value if 'null'"; // currently only for collections
    private static final String REFACTOR_AS_UNMODIFIABLE_AND_OPTIONAL  = "Refactor [{}#{}()]: return Optional<X> of unmodifiable view of optional attribute"; // currently only for collections
    private static final String MODIFY_METHOD = "Modify {} of method [{}#{}] to [{}].";
    private static final String GENERATE_ORDEFAULT = "Generate getter method [{}#{}({})] for property [{}].";
    private static final String SKIP_ORDEFAULT = "Skip creation of getter method [{}#{}({})] for property [{}] because {}.";
    private static final String BECAUSE_NO_DEFAULT_EXISTS = "there is no according default value";
    private static final String GENERATE_SETTER = "Generate setter method [{}#{}({})] for property [{}].";
    private static final String SKIP_SETTER = "Skip creation of setter method [{}#{}({})] for property [{}] because {}.";
    private static final String REMOVE_SETTER = "Remove setter method [{}#{}(...)].";

    private static final String OPTION_NAME = "informaticum-xjc-properties";
    private static final CommandLineArgument FINAL_GETTERS      = new CommandLineArgument("properties-final-getters",      FINAL_GETTERS_DESCRIPTION.text());
    private static final CommandLineArgument FINAL_SETTERS      = new CommandLineArgument("properties-final-setters",      FINAL_SETTERS_DESCRIPTION.text());
    private static final CommandLineArgument STRAIGHT_GETTERS   = new CommandLineArgument("properties-straight-getters",   STRAIGHT_GETTERS_DESCRIPTION.text());
    private static final CommandLineArgument OPTIONAL_GETTERS   = new CommandLineArgument("properties-optional-getters",   OPTIONAL_GETTERS_DESCRIPTION.format(STRAIGHT_GETTERS));
    private static final CommandLineArgument OPTIONAL_ORDEFAULT = new CommandLineArgument("properties-optional-ordefault", OPTIONAL_ORDEFAULT_DESCRIPTION.format(OPTIONAL_GETTERS));
    private static final CommandLineArgument COLLECTION_SETTERS = new CommandLineArgument("properties-collection-setters", COLLECTION_SETTERS_DESCRIPTION.text());
    private static final CommandLineArgument REMOVE_SETTERS     = new CommandLineArgument("properties-remove-setters",     REMOVE_SETTERS_DESCRIPTION.format(FINAL_SETTERS, COLLECTION_SETTERS));
    private static final CommandLineArgument PRIVATE_FIELDS     = new CommandLineArgument("properties-private-fields",     PRIVATE_FIELDS_DESCRIPTION.text());
    private static final CommandLineArgument FINAL_FIELDS       = new CommandLineArgument("properties-final-fields",       FINAL_FIELDS_DESCRIPTION.format(STRAIGHT_GETTERS));
    // TODO: What about unsetter?

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        final var args = asList(PRIVATE_FIELDS, FINAL_FIELDS,                                          // field options
                                STRAIGHT_GETTERS, OPTIONAL_GETTERS, OPTIONAL_ORDEFAULT, FINAL_GETTERS, // getter options
                                COLLECTION_SETTERS, FINAL_SETTERS, REMOVE_SETTERS);                    // setter options
        return new ArrayList<>(super.getPluginArguments()){{this.addAll(args);}};
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
        // For all collection fields, the out-of-the-box-getters create modifiable empty collection instances if there
        // is no value assigned. That becomes impossible if fields are final and, thus, {@link #STRAIGHT_GETTERS} must
        // be enabled too. Similar, the out-of-the-box-getters' implementation is inconsistent with the idea of optional
        // getters and, thus, {@link #STRAIGHT_GETTERS} must be enabled too.
        FINAL_FIELDS.activates(STRAIGHT_GETTERS);
        OPTIONAL_GETTERS.activates(STRAIGHT_GETTERS);
        // Skip {@link #FINAL_SETTERS} and {@link #COLLECTION_SETTERS} if setter methods shall be removed.
        REMOVE_SETTERS.deactivates(FINAL_SETTERS, COLLECTION_SETTERS);
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        PRIVATE_FIELDS.doOnActivation(this::setFieldsPrivate, clazz);
        FINAL_FIELDS.doOnActivation(this::setFieldsFinal, clazz);
        STRAIGHT_GETTERS.doOnActivation(this::refactorGetter, clazz);
        FINAL_GETTERS.doOnActivation(this::setGettersFinal, clazz); // finalise after refactoring (or Javadoc gets messed up) but before optional getters (modifiers will be copied)
        OPTIONAL_ORDEFAULT.doOnActivation(this::generateOrDefaultGetters, clazz);
        REMOVE_SETTERS.doOnActivation(this::removeSetter, clazz);
        COLLECTION_SETTERS.doOnActivation(this::generateCollectionSetters, clazz);
        FINAL_SETTERS.doOnActivation(this::setSettersFinal, clazz); // finalise after collection-setter creation
        return true;
    }

    private final LinkedHashMap<FieldOutline, JFieldVar> setFieldsPrivate(final ClassOutline clazz) {
        final var properties = generatedPropertiesOf(clazz);
        for (final var $property : properties.values()) {
            LOG.info(MODIFY_PROPERTY, "accessibility", fullNameOf(clazz), $property.name(), "private");
            javadocSection($property).append(PRIVATE_FIELD_IMPLNOTE.text());
            $property.mods().setPrivate();
        }
        return properties;
    }

    private final LinkedHashMap<FieldOutline, JFieldVar> setFieldsFinal(final ClassOutline clazz) {
        final var properties = generatedPropertiesOf(clazz);
        for (final var $property : properties.values()) {
            LOG.info(MODIFY_PROPERTY, "mutability", fullNameOf(clazz), $property.name(), "final");
            javadocSection($property).append(FINAL_FIELD_IMPLNOTE.text());
            $property.mods().setFinal(true);
        };
        return properties;
    }

    private final LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>> setGettersFinal(final ClassOutline clazz) {
        final var getters = generatedGettersOf(clazz);
        for (final var getter : getters.values()) {
            final var $getter = getter.getValue();
            LOG.info(MODIFY_METHOD, "mutability", fullNameOf(clazz), $getter, "final");
            javadocSection($getter).append(FINAL_GETTER_IMPLNOTE.text());
            $getter.mods().setFinal(true);
        };
        return getters;
    }

    private final LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>> refactorGetter(final ClassOutline clazz) {
        final var getters = generatedGettersOf(clazz);
        for (final var getter : getters.entrySet()) {
            this.refactorGetter(new GetterBricks(new PropertyAccessor(getter)));
        }
        return getters;
    }

    private final void refactorGetter(final GetterBricks bricks) {
        // 1/3: Dump current Javadoc
        final var originJavadoc = new ArrayList<>(bricks.$getter.javadoc());
        // 2/3: Refactor getter method
        if (bricks.$property.type().isPrimitive()) {
            assertThat(bricks.$getter).matches(not(CodeModelAnalysis::isCollectionMethod));
            assertThat(isOptionalMethod(bricks.$getter)).isFalse();
            assertThat(bricks.$returnType.isPrimitive()).isTrue();
            assertThat(bricks.$returnType.isReference()).isFalse();
            LOG.info(REFACTOR_JUST_STRAIGHT, fullNameOf(bricks.clazz), bricks.$getter.name());
            GetterRefactoring.PRIMITIVE_PROPERTY.supersedeGetter(bricks);
        } else if (bricks.attributeInfo.isCollection()) {
            assertThat(bricks.$getter).matches(CodeModelAnalysis::isCollectionMethod);
            assertThat(isOptionalMethod(bricks.$getter)).isFalse();
            assertThat(bricks.$returnType.isPrimitive()).isFalse();
            assertThat(bricks.$returnType.isReference()).isTrue();
            if (bricks.$default.isPresent() && UNMODIFIABLE_COLLECTIONS.isActivated()) {
                LOG.info(REFACTOR_AS_UNMODIFIABLE_AND_DEFAULTED, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.DEFAULTED_UNMODIFIABLE_COLLECTION_PROPERTY.supersedeGetter(bricks);
            } else if (bricks.$default.isPresent()) {
                assertThat(UNMODIFIABLE_COLLECTIONS.isActivated()).isFalse();
                LOG.info(REFACTOR_AS_DEFAULTED, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.DEFAULTED_MODIFIABLE_COLLECTION_PROPERTY.supersedeGetter(bricks);
            } else if (OPTIONAL_GETTERS.isActivated() && isOptional(bricks.attribute) && UNMODIFIABLE_COLLECTIONS.isActivated()) {
                LOG.info(REFACTOR_AS_UNMODIFIABLE_AND_OPTIONAL, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.OPTIONAL_UNMODIFIABLE_COLLECTION_PROPERTY.supersedeGetter(bricks);
            } else if (OPTIONAL_GETTERS.isActivated() && isOptional(bricks.attribute)) {
                assertThat(UNMODIFIABLE_COLLECTIONS.isActivated()).isFalse();
                LOG.info(REFACTOR_AS_OPTIONAL, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.OPTIONAL_MODIFIABLE_COLLECTION_PROPERTY.supersedeGetter(bricks);
            } else if (UNMODIFIABLE_COLLECTIONS.isActivated()) {
                LOG.info(REFACTOR_AS_UNMODIFIABLE, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.UNMODIFIABLE_COLLECTION_PROPERTY.supersedeGetter(bricks);
            } else {
                assertThat(UNMODIFIABLE_COLLECTIONS.isActivated()).isFalse();
                LOG.info(REFACTOR_JUST_STRAIGHT, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.MODIFIABLE_COLLECTION_PROPERTY.supersedeGetter(bricks);
            }
        // } else if ($.$returnType.isArray()) { // TODO: handle array type similar to collections (defensive copies, non-modifiable, etc.)
        } else {
            assertThat(bricks.$getter).matches(not(CodeModelAnalysis::isCollectionMethod));
            assertThat(isOptionalMethod(bricks.$getter)).isFalse();
            // assertThat($.$returnType.isPrimitive()).isFalse(); // TODO: return type may be primitive, even if property is not
            // assertThat($.$returnType.isReference()).isTrue();  // TODO: return type may be primitive, even if property is not
            if (bricks.$default.isPresent()) {
                LOG.info(REFACTOR_AS_DEFAULTED, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.DEFAULTED_PROPERTY.supersedeGetter(bricks);
            } else if (OPTIONAL_GETTERS.isActivated() && isOptional(bricks.attribute)) {
                assertThat(isOptionalMethod(bricks.$getter)).withFailMessage("This case is not considered yet ;-(").isFalse(/* TODO: Handle getters that already return Optional */);
                LOG.info(REFACTOR_AS_OPTIONAL, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.OPTIONAL_PROPERTY.supersedeGetter(bricks);
            } else {
                LOG.info(REFACTOR_JUST_STRAIGHT, fullNameOf(bricks.clazz), bricks.$getter.name());
                GetterRefactoring.STRAIGHT_PROPERTY.supersedeGetter(bricks);
            }
        }
        // 3/3: Update Javadoc
        javadocSection(bricks.$getter).append(REFACTORED_GETTER_IMPLNOTE_INTRO.text());
        bricks.$getter.javadoc().addAll(originJavadoc);
        bricks.$getter.javadoc().append(REFACTORED_GETTER_IMPLNOTE_OUTRO.text());
    }

    private final List<JMethod> generateOrDefaultGetters(final ClassOutline clazz) {
        final var getters = generatedGettersOf(clazz);
        getters.values().removeIf(getter -> !isOptionalMethod(getter.getValue()));
        final var $generated = getters.entrySet().stream().map(PropertyAccessor::new).flatMap(getter -> {
            final var $getOrDefault = this.ensureOrDefaultGetter(getter);
            final var $getOrBuiltin = this.ensureOrBuiltinGetter(getter, $getOrDefault);
            return concat(Stream.of($getOrDefault), $getOrBuiltin.stream());
        });
        return $generated.collect(toList());
    }

    private final JMethod ensureOrDefaultGetter(final PropertyAccessor accessor) {
        final var methodName = accessor.$method.name() + "OrDefault";
        final var $argType = deoptionalisedTypeFor(accessor.$method.type().boxify()).orElse(accessor.$property.type());
        final var $getOrDefault = getMethod(accessor.clazz, methodName, $argType);
        $getOrDefault.ifPresent($m -> LOG.warn(SKIP_ORDEFAULT, fullNameOf(accessor.clazz), $m.name(), $argType, accessor.$property.name(), BECAUSE_METHOD_ALREADY_EXISTS));
        return $getOrDefault.orElse(this.generateOrDefaultGetter(accessor));
    }

    private final JMethod generateOrDefaultGetter(final PropertyAccessor accessor) {
        final var methodName = accessor.$method.name() + "OrDefault";
        final var $type = deoptionalisedTypeFor(accessor.$method.type().boxify()).orElse(accessor.$property.type());
        LOG.info(GENERATE_ORDEFAULT, fullNameOf(accessor.clazz), methodName, $type, accessor.$property.name());
        // 1/3: Declare
        final var methodMods = accessor.$method.mods().getValue();
        final var $getOrDefault = accessor.$ImplClass.method(methodMods, $type, methodName);
        final var $defaultValue = $getOrDefault.param(FINAL, $type, "defaultValue");
        // 2/3: Document
        javadocSection($getOrDefault).append(ORDEFAULT_JAVADOC.format(javadocNameOf(accessor.clazz), javadocNameOf(accessor.$method), $defaultValue.name()));
        javadocSection($getOrDefault).append(ORDEFAULT_IMPLNOTE.text());
        javadocSection($getOrDefault.javadoc().addParam($defaultValue)).append(ORDEFAULT_PARAM.format(javadocNameOf(accessor.clazz), javadocNameOf(accessor.$method)));
        javadocSection($getOrDefault.javadoc().addReturn()).append(ORDEFAULT_RETURN.format(javadocNameOf(accessor.clazz), javadocNameOf(accessor.$method), $defaultValue.name()));
        // 3/3: Implement
        $getOrDefault.body()._return($this.invoke(accessor.$method).invoke("orElse").arg($defaultValue));
        return $getOrDefault;
    }

    private final Optional<JMethod> ensureOrBuiltinGetter(final PropertyAccessor accessor, final JMethod $getOrDefault) {
        final var methodName = isCollectionMethod($getOrDefault) ? accessor.$method.name() + "OrEmpty" : $getOrDefault.name();
        final var $getOrBuiltin = getMethod(accessor.clazz, methodName);
        $getOrBuiltin.ifPresent($m -> LOG.warn(SKIP_ORDEFAULT, fullNameOf(accessor.clazz), $m.name(), "", accessor.$property.name(), BECAUSE_METHOD_ALREADY_EXISTS));
        return $getOrBuiltin.or(() -> this.generateOrBuiltinGetter(accessor, $getOrDefault));
    }

    private final Optional<JMethod> generateOrBuiltinGetter(final PropertyAccessor accessor, final JMethod $getOrDefault) {
        final var methodName = isCollectionMethod($getOrDefault) ? accessor.$method.name() + "OrEmpty" : $getOrDefault.name();
        final var $builtin = OutlineAnalysis.defaultExpressionFor(accessor.attribute, true, UNMODIFIABLE_COLLECTIONS.isActivated());
        if ($builtin.isEmpty()) {
            // 0/3: Skip
            LOG.info(SKIP_ORDEFAULT, fullNameOf(accessor.clazz), methodName, "", accessor.$property.name(), BECAUSE_NO_DEFAULT_EXISTS);
            return Optional.empty();
        }
        return $builtin.map($b -> {
            LOG.info(GENERATE_ORDEFAULT, fullNameOf(accessor.clazz), methodName, "", accessor.$property.name());
            // 1/3: Declare
            final var methodMods = $getOrDefault.mods().getValue();
            final var $returnType = $getOrDefault.type();
            final var $getOrBuiltin = accessor.$ImplClass.method(methodMods, $returnType, methodName);
            // 2/3: Document
            javadocSection($getOrBuiltin).append(ORBUILTIN_JAVADOC.format(javadocNameOf(accessor.clazz), javadocNameOf(accessor.$method), render($b)));
            javadocSection($getOrBuiltin).append(ORBUILTIN_IMPLNOTE.text());
            javadocSection($getOrBuiltin.javadoc().addReturn()).append(ORBUILTIN_RETURN.format(javadocNameOf(accessor.clazz), javadocNameOf(accessor.$method), render($b)));
            // 3/3: Implement
            $getOrBuiltin.body()._return($this.invoke($getOrDefault).arg($b));
            return $getOrBuiltin;
        });
    }

    private final List<JMethod> generateCollectionSetters(final ClassOutline clazz) {
        final var properties = generatedPropertiesOf(clazz);
        final var collectionProperties = filter(properties, k -> k.getPropertyInfo().isCollection());
        final var $setters = collectionProperties.entrySet().stream().map(this::ensureCollectionSetter);
        return $setters.collect(toList());
    }

    private final JMethod ensureCollectionSetter(final Entry<? extends FieldOutline, ? extends JFieldVar> property) {
        final var clazz = property.getKey().parent();
        final var attribute = property.getKey();
        assertThat(attribute.getPropertyInfo().isCollection()).isTrue();
        assertThat(attribute.getPropertyInfo().defaultValue).isNull();
        final var $property = property.getValue();
        assertThat($property.type().isPrimitive()).isFalse();
        final var setterName = guessSetterName(attribute);
        final var $setter = getMethod(clazz, setterName, $property.type());
        $setter.ifPresent($s -> LOG.warn(SKIP_SETTER, fullNameOf(clazz), $s.name(), $property.type(), $property.name(), BECAUSE_METHOD_ALREADY_EXISTS));
        return $setter.orElse(this.generateCollectionSetter(property));
    }

    private final JMethod generateCollectionSetter(final Entry<? extends FieldOutline, ? extends JFieldVar> property) {
        final var clazz = property.getKey().parent();
        final var attribute = property.getKey();
        assertThat(attribute.getPropertyInfo().isCollection()).isTrue();
        assertThat(attribute.getPropertyInfo().defaultValue).isNull();
        final var $property = property.getValue();
        assertThat($property.type().isPrimitive()).isFalse();
        final var setterName = guessSetterName(attribute);
        LOG.info(GENERATE_SETTER, fullNameOf(clazz), setterName, $property.type(), $property.name());
        // 1/3: Declare
        final var $ImplClass = clazz.getImplClass();
        final var $setter = $ImplClass.method(PUBLIC, this.codeModel().VOID, setterName);
        final var $param = $setter.param(FINAL, $property.type(), $property.name());
        // 2/3: Document
        javadocSection($setter).append(COLLECTION_SETTER_JAVADOC.format(javadocNameOf($property)));
        javadocSection($setter).append(COLLECTION_SETTER_IMPLNOTE.text());
        // 3/3: Implement
        accordingAssignmentAndJavadoc(property, $setter, $param);
        return $setter;
    }

    private final LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>> setSettersFinal(final ClassOutline clazz) {
        final var setters = generatedSettersOf(clazz);
        for (final var setter : setters.values()) {
            final var $setter = setter.getValue();
            LOG.info(MODIFY_METHOD, "mutability", fullNameOf(clazz), $setter, "final");
            javadocSection($setter).append(FINAL_SETTER_IMPLNOTE.text());
            $setter.mods().setFinal(true);
        };
        return setters;
    }

    private final LinkedHashMap<FieldOutline, Entry<JFieldVar, JMethod>> removeSetter(final ClassOutline clazz) {
        final var $ImplClass = clazz.getImplClass();
        final var setters = generatedSettersOf(clazz);
        if (!setters.isEmpty()) {
            javadocSection($ImplClass).append(REMOVED_SETTERS_IMPLNOTE.text());
        }
        for (final var setter : setters.values()) {
            final var $setter = setter.getValue();
            LOG.info(REMOVE_SETTER, fullNameOf(clazz), $setter.name());
            $ImplClass.methods().remove($setter);
        };
        return setters;
    }

}
