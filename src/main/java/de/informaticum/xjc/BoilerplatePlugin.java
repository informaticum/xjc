package de.informaticum.xjc;

import static com.sun.codemodel.JExpr.FALSE;
import static com.sun.codemodel.JExpr.TRUE;
import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.ABSTRACT;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.NONE;
import static com.sun.codemodel.JMod.PROTECTED;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;
import static com.sun.codemodel.JOp.cond;
import static com.sun.codemodel.JOp.not;
import static de.informaticum.xjc.JavaDoc.DEFAULT_CONSTRUCTOR_JAVADOC_FIELDS;
import static de.informaticum.xjc.JavaDoc.DEFAULT_CONSTRUCTOR_JAVADOC_INTRO;
import static de.informaticum.xjc.JavaDoc.DEFAULT_CONSTRUCTOR_JAVADOC_SUPER;
import static de.informaticum.xjc.JavaDoc.DEFAULT_FIELD_ASSIGNMENT;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_OPTIONAL;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_PRIMITIVE;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_REQUIRED;
import static de.informaticum.xjc.JavaDoc.PARAM_WITH_DEFAULT_MULTI_VALUE;
import static de.informaticum.xjc.JavaDoc.PARAM_WITH_DEFAULT_SINGLE_VALUE;
import static de.informaticum.xjc.JavaDoc.RETURN_OPTIONAL_VALUE;
import static de.informaticum.xjc.JavaDoc.THROWS_IAE_BY_NULL;
import static de.informaticum.xjc.JavaDoc.VALUES_CONSTRUCTOR_JAVADOC_FIELDS;
import static de.informaticum.xjc.JavaDoc.VALUES_CONSTRUCTOR_JAVADOC_INTRO;
import static de.informaticum.xjc.JavaDoc.VALUES_CONSTRUCTOR_JAVADOC_SUPER;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
import static de.informaticum.xjc.util.OptionalAnalysis.accordingOptionalTypeFor;
import static de.informaticum.xjc.util.OptionalAnalysis.isOptionalMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedGettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructor;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.OutlineAnalysis.superAndGeneratedPropertiesOf;
import static de.informaticum.xjc.util.Printify.fullName;
import static de.informaticum.xjc.util.Printify.render;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessFactoryName;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessWitherName;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.StringJoiner;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import org.slf4j.Logger;

public final class BoilerplatePlugin
extends AbstractPlugin {

    private static final Logger LOG = getLogger(BoilerplatePlugin.class);

    private static final String GENERATE_DEFAULTCONSTRUCTOR = "-boilerplate-defaultConstructor";
    private static final String GENERATE_DEFAULTCONSTRUCTOR_DESC = "Generate default constructor. Default: false";
    private boolean generateDefaultConstructor = false;

    private static final String GENERATE_VALUESCONSTRUCTOR = "-boilerplate-valuesConstructor";
    private static final String GENERATE_VALUESCONSTRUCTOR_DESC = "Generate all-values constructor. Default: false";
    private boolean generateValueConstructor = false;

    private static final String GENERATE_VALUESBUILDER = "-boilerplate-valuesBuilder";
    private static final String GENERATE_VALUESBUILDER_DESC = "Generate all-values builder. Default: false";
    private boolean generateValueBuilder = false;

    private static final String GENERATE_OPTIONALGETTERS = "-boilerplate-optionalGetters";
    private static final String GENERATE_OPTIONALGETTERS_DESC = "Replace return type [T] of non-required fields' getter methods with [OptionalDouble]/[OptionalInt]/[OptionalLong]/[Optional<T>]. Default: false";
    private boolean generateOptionalGetters = false;

    private static final String GENERATE_EQUALS = "-boilerplate-equals";
    private static final String GENERATE_EQUALS_DESC = "Generate #equals(Object) method. Default: false";
    private boolean generateEquals = false;

    private static final String GENERATE_HASHCODE = "-boilerplate-hashCode";
    private static final String GENERATE_HASHCODE_DESC = "Generate [#hashCode()] method. Default: false";
    private boolean generateHashcode = false;

    private static final String GENERATE_TOSTRING = "-boilerplate-toString";
    private static final String GENERATE_TOSTRING_DESC = "Generate [#toString()] method. Default: false";
    private boolean generateToString = false;

    private static final String GENERATE_BUILDER = "Generate all-values builder for [{}]";
    private static final String GENERATE_CONSTRUCTOR = "Generate {} constructor for [{}]";
    private static final String GENERATE_METHOD = "Generate [{}] method for [{}]";
    private static final String GENERATE_OPTIONAL_GETTER = "Replace return type X of [{}#{}()] with an according OptionalDouble, OptionalInt, OptionalLong, or Optional<X> type";
    private static final String SKIP_BUILDER = "Skip creation of builder for [{}] because {}.";
    private static final String SKIP_CONSTRUCTOR = "Skip creation of {} constructor for [{}] because {}.";
    private static final String SKIP_METHOD = "Skip creation of [{}] method for [{}] because {}.";
    private static final String SKIP_OPTIONAL_GETTER = "Skip creation of optional getter for [{}] of [{}] because {}.";
    private static final String SKIP_OPTIONAL_GETTERS = "Skip creation of optional getters for [{}] because {}.";
    private static final String BECAUSE_ATTRIBUTE_IS_REQUIRED = "attribute is required";
    private static final String BECAUSE_BUILDER_EXISTS = "such builder already exists";
    private static final String BECAUSE_CONSTRUCTOR_EXISTS = "such constructor already exists";
    private static final String BECAUSE_EFFECTIVELY_SIMILAR = "it is effectively similar to default-constructor";
    private static final String BECAUSE_METHOD_EXISTS = "such method already exists";
    private static final String BECAUSE_OPTION_IS_DISABLED = "according option has not been selected";

    @Override
    public final String getOptionName() {
        return "ITBSG-xjc-boilerplate";
    }

    @Override
    public final String getOptionDescription() {
        return "Generates common boilerplate code.";
    }

    @Override
    public final LinkedHashMap<String, String> getPluginOptions() {
        return new LinkedHashMap<>(ofEntries(
            entry(GENERATE_DEFAULTCONSTRUCTOR, GENERATE_DEFAULTCONSTRUCTOR_DESC),
            entry(GENERATE_VALUESCONSTRUCTOR, GENERATE_VALUESCONSTRUCTOR_DESC),
            entry(GENERATE_VALUESBUILDER, GENERATE_VALUESBUILDER_DESC),
            entry(GENERATE_OPTIONALGETTERS, GENERATE_OPTIONALGETTERS_DESC),
            entry(GENERATE_EQUALS, GENERATE_EQUALS_DESC),
            entry(GENERATE_HASHCODE, GENERATE_HASHCODE_DESC),
            entry(GENERATE_TOSTRING, GENERATE_TOSTRING_DESC)
        ));
    }

    @Override
    public final int parseArgument(final Options options, final String[] arguments, final int index) {
        switch (arguments[index]) {
            case GENERATE_DEFAULTCONSTRUCTOR:
                this.generateDefaultConstructor = true;
                return 1;
            case GENERATE_VALUESCONSTRUCTOR:
                this.generateValueConstructor = true;
                return 1;
            case GENERATE_VALUESBUILDER:
                this.generateValueBuilder = true;
                return 1;
            case GENERATE_OPTIONALGETTERS:
                this.generateOptionalGetters = true;
                return 1;
            case GENERATE_EQUALS:
                this.generateEquals = true;
                return 1;
            case GENERATE_HASHCODE:
                this.generateHashcode = true;
                return 1;
            case GENERATE_TOSTRING:
                this.generateToString = true;
                return 1;
            default:
                return 0;
        }
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        this.considerDefaultConstructor(clazz);
        this.considerValuesConstructor(clazz);
        this.considerValuesBuilder(clazz);
        this.considerOptionalGetters(clazz);
        this.considerEquals(clazz);
        this.considerHashCode(clazz);
        this.considerToString(clazz);
        return true;
    }

    private final void considerDefaultConstructor(final ClassOutline clazz) {
        if (!this.generateDefaultConstructor) {
            LOG.trace(SKIP_CONSTRUCTOR, "default", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getConstructor(clazz) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "default", fullName(clazz), BECAUSE_CONSTRUCTOR_EXISTS);
        } else {
            LOG.info(GENERATE_CONSTRUCTOR, "default", fullName(clazz));
            this.generateDefaultConstructor(clazz);
            assertThat(getConstructor(clazz)).isNotNull();
        }
    }

    private final void generateDefaultConstructor(final ClassOutline clazz) {
        // 1/3: Create
        final var $constructor = clazz.getImplClass().constructor(PUBLIC);
        // 2/3: JavaDocument
        $constructor.javadoc().append(format(DEFAULT_CONSTRUCTOR_JAVADOC_INTRO));
        // 3/3: Implement
        if (clazz.getSuperClass() != null) {
            $constructor.javadoc().append(format(DEFAULT_CONSTRUCTOR_JAVADOC_SUPER));
            $constructor.body().invoke("super");
        }
        $constructor.javadoc().append(format(DEFAULT_CONSTRUCTOR_JAVADOC_FIELDS));
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var $value = defaultValueFor(attribute).orElse($null);
            $constructor.javadoc().append(format(DEFAULT_FIELD_ASSIGNMENT, $property.name(), render($value)));
            $constructor.body().assign($this.ref($property), $value);
        }
    }

    private final void considerValuesConstructor(final ClassOutline clazz) {
        if (!this.generateValueConstructor) {
            LOG.trace(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (superAndGeneratedPropertiesOf(clazz).isEmpty() && this.generateDefaultConstructor) {
            LOG.info(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_EFFECTIVELY_SIMILAR);
        } else if (getConstructor(clazz, superAndGeneratedPropertiesOf(clazz)) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_CONSTRUCTOR_EXISTS);
        } else {
            LOG.info(GENERATE_CONSTRUCTOR, "all-values", fullName(clazz));
            final var $constructor = this.generateValuesConstructor(clazz);
            assertThat(getConstructor(clazz, superAndGeneratedPropertiesOf(clazz))).isNotNull();
            if (clazz.getImplClass().isAbstract()) {
                LOG.info("Skip adoption of all-values constructor for [{}] because this class is abstract.", fullName(clazz));
            } else if (clazz._package().objectFactory() == null) {
                LOG.error("Skip adoption of all-values constructor for [{}] because there is no according package's ObjectFactory.", fullName(clazz));
            } else if (getMethod(clazz._package().objectFactory(), guessFactoryName(clazz)) == null) {
                LOG.error("Skip adoption of all-values constructor for [{}] because according package's ObjectFactory does not contain a predefined factory method for this class.", fullName(clazz));
            } else {
                LOG.info("Adopt all-values constructor for [{}] in according package's ObjectFactory.", fullName(clazz));
                this.generateValuesConstructorFactory(clazz._package().objectFactory(), clazz, $constructor);
                assertThat(getMethod(clazz._package().objectFactory(), guessFactoryName(clazz))).isNotNull();
                if (!this.generateDefaultConstructor) {
                    LOG.info("Remove default factory [{}] in according package's ObjectFactory because implicit default constructor no longer exists and has not been generated explicitly.", fullName(clazz));
                    this.removeDefaultConstructorFactory(clazz._package().objectFactory(), clazz);
                }
            }
        }
    }

    private final JMethod generateValuesConstructor(final ClassOutline clazz) {
        // 1/3: Create
        final var $constructor = clazz.getImplClass().constructor(PUBLIC);
        // 2/3: JavaDocument
        $constructor.javadoc().append(format(VALUES_CONSTRUCTOR_JAVADOC_INTRO));
        // TODO: @throws nur, wenn wirklich möglich (Super-Konstruktor beachten)
        $constructor.javadoc().addThrows(IllegalArgumentException.class).append(format(THROWS_IAE_BY_NULL));
        // 3/3: Implement
        if (clazz.getSuperClass() != null) {
            $constructor.javadoc().append(format(VALUES_CONSTRUCTOR_JAVADOC_SUPER));
            final var $super = $constructor.body().invoke("super");
            for (final var property : superAndGeneratedPropertiesOf(clazz.getSuperClass()).entrySet()) {
                final var attribute = property.getKey();
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                appendParameterJavaDoc($constructor.javadoc(), attribute, $parameter);
                $super.arg($parameter);
            }
        }
        $constructor.javadoc().append(format(VALUES_CONSTRUCTOR_JAVADOC_FIELDS));
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
            this.accordingAssignment(attribute, $constructor, $property, $parameter);
        }
        return $constructor;
    }

    private static final void appendParameterJavaDoc(final JDocComment javadoc, final FieldOutline attribute, final JVar $parameter) {
        final var info = attribute.getPropertyInfo();
        final var name = info.getName(true);
        final var $default = defaultValueFor(attribute);
        if ($parameter.type().isPrimitive()) {
            javadoc.addParam($parameter).append(format(PARAM_THAT_IS_PRIMITIVE, name));
        } else if (isOptional(attribute) && $default.isEmpty()) {
            javadoc.addParam($parameter).append(format(PARAM_THAT_IS_OPTIONAL, name));
        } else if (isRequired(attribute) && $default.isEmpty()) {
            javadoc.addParam($parameter).append(format(PARAM_THAT_IS_REQUIRED, name));
        } else {
            assertThat($default).isPresent();
            javadoc.addParam($parameter).append(format(info.isCollection() ? PARAM_WITH_DEFAULT_MULTI_VALUE : PARAM_WITH_DEFAULT_SINGLE_VALUE, name));
        }
    }

    private final void accordingAssignment(final FieldOutline attribute, final JMethod $method, final JFieldVar $property, final JExpression $parameter) {
        this.accordingAssignment(attribute, $method, $property, $parameter, true);
    }

    private final void accordingAssignment(final FieldOutline attribute, final JMethod $method, final JFieldVar $property, final JExpression $parameter, final boolean javadoc) {
        if (javadoc) {
            appendParameterJavaDoc($method.javadoc(), attribute, $property);
        }
        final var $default = defaultValueFor(attribute);
        if ($property.type().isPrimitive()) {
            $method.body().assign($this.ref($property), $parameter);
        } else if (isOptional(attribute) && $default.isEmpty()) {
            $method.body().assign($this.ref($property), $parameter);
        } else if (isRequired(attribute) && $default.isEmpty()) {
            $method._throws(IllegalArgumentException.class);
            final var $condition = $method.body()._if($parameter.eq($null));
            $condition._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), $parameter);
        } else {
            assertThat($default).isPresent();
            $method.body().assign($this.ref($property), cond($parameter.eq($null), $default.get(), $parameter));
        }
    }

    private final void generateValuesConstructorFactory(final JDefinedClass $objectFactory, final ClassOutline clazz, final JMethod $constructor) {
        final var $defaultFactory = getMethod($objectFactory, guessFactoryName(clazz));
        // 1/3: Create
        final var $allValuesFactory = $objectFactory.method($defaultFactory.mods().getValue(), $defaultFactory.type(), $defaultFactory.name());
        // TODO: Re-throw declared exceptions of constructor
        // 2/3: JavaDocument
        $allValuesFactory.javadoc().addAll($defaultFactory.javadoc());
        // TODO: @return-JavaDoc?!
        // 3/3: Implement
        final var $instantiation = _new(clazz.getImplClass());
        for (final var $constructorParameter : $constructor.listParams()) {
            final var $factoryParameter = $allValuesFactory.param(FINAL, $constructorParameter.type(), $constructorParameter.name());
            // TODO: Generate JavaDoc similar to all-values constructor
            $allValuesFactory.javadoc().addParam($factoryParameter).append("See all-values constructor of ").append(clazz.getImplClass());
            $instantiation.arg($factoryParameter);
        }
        $allValuesFactory.body()._return($instantiation);
    }

    private final void removeDefaultConstructorFactory(final JDefinedClass $objectFactory, final ClassOutline clazz) {
        // 1/2: Identify
        final var $defaultFactory = getMethod($objectFactory, guessFactoryName(clazz));
        // 2/2: Remove
        $objectFactory.methods().remove($defaultFactory);
    }

    private final void considerValuesBuilder(final ClassOutline clazz) {
        if (!this.generateValueBuilder) {
            LOG.trace(SKIP_BUILDER, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            // TODO: LOG.warn(SKIP_BUILDER, "default", fullName(clazz), BECAUSE_BUILDER_EXISTS);
            LOG.info(GENERATE_BUILDER, fullName(clazz));
            this.generateValuesBuilder(clazz);
        }
    }

    private final JClass generateValuesBuilder(final ClassOutline clazz) {
        final var $clazz = clazz.getImplClass();
        try {
            final var isAbstract = $clazz.isAbstract();
            final var isFinal = ($clazz.mods().getValue() & FINAL) != 0;
            final var builderModifiers = PUBLIC | STATIC | (isAbstract ? ABSTRACT : NONE) | (isFinal ? FINAL : NONE);
            // 1/3: Create
            final var $Builder = $clazz._class(builderModifiers, "Builder", ClassType.CLASS);
            // 2/3: JavaDocument
            $Builder.javadoc().append("Builder for (enclosing) class ").append($clazz).append(".");
            // 3/3: Implement
            final var $defaultConstructor = $Builder.constructor(PROTECTED);
            $defaultConstructor.javadoc().append("Default constructor.");
            final var $blueprintConstructor = $Builder.constructor(PROTECTED);
            $blueprintConstructor.javadoc().append("Blueprint constructor.");
            final var $blueprint = $blueprintConstructor.param(FINAL, $clazz, "blueprint");
            $blueprintConstructor.javadoc().addParam($blueprint).append("the blueprint ").append($clazz).append(" instance to get all initial values from");
            if (clazz.getSuperClass() != null) {
                $Builder._extends(this.generateValuesBuilder(clazz.getSuperClass()));
                $defaultConstructor.body().invoke("super");
                $blueprintConstructor.body().invoke("super").arg($blueprint);
            }
            if (!isAbstract) {
                assertThat(builderModifiers & ABSTRACT).isEqualTo(0);
                final var $builder = $clazz.method(builderModifiers, $Builder, "builder");
                $builder.body()._return(_new($Builder));
                final var $toBuilder = $clazz.method(PUBLIC, $Builder, "toBuilder");
                $toBuilder.javadoc().addReturn().append("a new ").append($Builder).append(" with all properties initialised with the current values of {@code this} instance");
                $toBuilder.body()._return(_new($Builder).arg($this));
            }
            for (final var blueprintProperty : generatedPropertiesOf(clazz).entrySet()) {
                final var attribute = blueprintProperty.getKey();
                final var $blueprintProperty = blueprintProperty.getValue();
                final var $builderProperty = $Builder.field(PROTECTED, $blueprintProperty.type(), $blueprintProperty.name(), defaultValueFor(attribute).orElse($null));
                this.accordingAssignment(attribute, $blueprintConstructor, $builderProperty, $blueprint.ref($blueprintProperty), false);
                final var $wither = $Builder.method(PUBLIC | FINAL, $Builder, guessWitherName(attribute));
                final var $parameter = $wither.param(FINAL, $builderProperty.type(), $builderProperty.name());
                this.accordingAssignment(attribute, $wither, $builderProperty, $parameter);
                $wither.javadoc().addReturn().append("{@code this} builder instance for fluent API style");
                $wither.body()._return($this);
            }
            final var $build = $Builder.method(PUBLIC | (isAbstract ? ABSTRACT : NONE), $clazz, "build");
            if (!isAbstract) {
                final var $instantiation = _new($clazz);
                for (final var $blueprintProperty : superAndGeneratedPropertiesOf(clazz).values()) {
                    // The according builder property is either a field of the current builder class' ...
                    // /* final var $builderProperty = $Builder.fields().get($blueprintProperty.name()); */
                    // ... or is specified in any of the builder's super classes.
                    // /* if ($builderProperty == null) { ??? } */
                    // Fortunately, the name is sufficient enough to identify the accordingly named property.
                    final var $builderProperty = $blueprintProperty.name();
                    $instantiation.arg($this.ref($builderProperty));
                }
                $build.javadoc().addReturn().append("a new instance of ").append($clazz);
                $build.body()._return($instantiation);
            }
            return $Builder;
        } catch (final JClassAlreadyExistsException alreadyExists) {
            return stream($clazz.listClasses()).filter(nested -> "Builder".equals(nested.name())).findFirst()
                                               .orElseThrow(() -> new RuntimeException("Nested class 'Builder' already exists but cannot be found!", alreadyExists));
        }
    }

    private final void considerOptionalGetters(final ClassOutline clazz) {
        if (!this.generateOptionalGetters) {
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
                    this.generateOptionalGetters(clazz, getter);
                }
            }
        }
    }

    private final void generateOptionalGetters(final ClassOutline clazz, final Entry<? extends FieldOutline, ? extends JMethod> original) {
        final var attribute = original.getKey();
        final var info = attribute.getPropertyInfo();
        final var $originalGetter = original.getValue();
        final var originalType = $originalGetter.type();
        // 1/3: Create
        final var optionalType = accordingOptionalTypeFor(originalType);
        final var $optionalGetter = clazz.getImplClass().method($originalGetter.mods().getValue(), optionalType, $originalGetter.name());
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
        $originalGetter.name("_" + $originalGetter.name());
    }

    private final void considerEquals(final ClassOutline clazz) {
        if (!this.generateEquals) {
            LOG.trace(SKIP_METHOD, "#equals(Object)", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, "equals", Object.class) != null) {
            LOG.warn(SKIP_METHOD, "#equals(Object)", fullName(clazz), BECAUSE_METHOD_EXISTS);
        } else {
            LOG.info(GENERATE_METHOD, "#equals(Object)", fullName(clazz));
            this.generateEquals(clazz);
            assertThat(getMethod(clazz, "equals", Object.class)).isNotNull();
        }
    }

    private final void generateEquals(final ClassOutline clazz) {
        // 1/3: Create
        final var $equals = clazz.getImplClass().method(PUBLIC, boolean.class, "equals");
        // 2/3: Annotate
        $equals.annotate(Override.class);
        // 3/3: Implement
        final var $other = $equals.param(FINAL, this.reference(Object.class), "other");
        $equals.body()._if($other.eq($null))._then()._return(FALSE);
        $equals.body()._if($this.eq($other))._then()._return(TRUE);
        $equals.body()._if(not($this.invoke("getClass").invoke("equals").arg($other.invoke("getClass"))))._then()._return(FALSE);
        final var comparisons = new ArrayList<JExpression>();
        if (clazz.getSuperClass() != null) {
            comparisons.add($super.invoke("equals").arg($other));
        }
        final var properties = generatedPropertiesOf(clazz);
        if (!properties.isEmpty()) {
            final var $Objects = this.reference(Objects.class);
            final var $that = $equals.body().decl(FINAL, clazz.getImplClass(), "that", cast(clazz.getImplClass(), $other));
            for (final var $property : properties.values()) {
                comparisons.add($Objects.staticInvoke("equals").arg($this.ref($property)).arg($that.ref($property)));
            }
        }
        $equals.body()._return(comparisons.stream().reduce(JExpression::cand).orElse(TRUE));
    }

    private final void considerHashCode(final ClassOutline clazz) {
        if (!this.generateHashcode) {
            LOG.trace(SKIP_METHOD, "#hashCode()", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, "hashCode") != null) {
            LOG.warn(SKIP_METHOD, "#hashCode()", fullName(clazz), BECAUSE_METHOD_EXISTS);
        } else {
            LOG.info(GENERATE_METHOD, "#hashCode()", fullName(clazz));
            this.addHashCode(clazz);
            assertThat(getMethod(clazz, "hashCode")).isNotNull();
        }
    }

    private final void addHashCode(final ClassOutline clazz) {
        // 1/3: Create
        final var $hashCode = clazz.getImplClass().method(PUBLIC, int.class, "hashCode");
        // 2/3: Annotate
        $hashCode.annotate(Override.class);
        // 3/3: Implement
        final var calculation = this.reference(Objects.class).staticInvoke("hash");
        if (clazz.getSuperClass() != null) {
            calculation.arg($super.invoke("hashCode"));
        }
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            calculation.arg($this.ref($property));
        }
        $hashCode.body()._return(calculation.listArgs().length > 0 ? calculation : $this.invoke("getClass").invoke("hashCode"));
    }

    private final void considerToString(final ClassOutline clazz) {
        if (!this.generateToString) {
            LOG.trace(SKIP_METHOD, "#toString()", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, "toString") != null) {
            LOG.warn(SKIP_METHOD, "#toString()", fullName(clazz), BECAUSE_METHOD_EXISTS);
        } else {
            LOG.info(GENERATE_METHOD, "#toString()", fullName(clazz));
            this.addToString(clazz);
            assertThat(getMethod(clazz, "toString")).isNotNull();
        }
    }

    private final void addToString(final ClassOutline clazz) {
        // 1/3: Create
        final var $toString = clazz.getImplClass().method(PUBLIC, String.class, "toString");
        // 2/3: Annotate
        $toString.annotate(Override.class);
        // 3/3: Implement
        final var parts = new ArrayList<JExpression>();
        final var $Objects = this.reference(Objects.class);
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var info = attribute.getPropertyInfo();
            final var $property = property.getValue();
            parts.add(lit(info.getName(true) + ": ").plus($Objects.staticInvoke("toString").arg($this.ref($property))));
        }
        if (clazz.getSuperClass() != null) {
            parts.add(lit("Super: ").plus($super.invoke("toString")));
        }
        final var $joiner = _new(this.reference(StringJoiner.class)).arg(", ").arg(clazz.getImplClass().name() + "[").arg("]");
        // TODO: InsurantIdType#ROOT in toString()-Ausgabe aufnehmen
        $toString.body()._return(parts.stream().reduce($joiner, (join, next) -> join.invoke("add").arg(next)).invoke("toString"));
    }

}
