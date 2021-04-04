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
import static de.informaticum.xjc.JavaDoc.DEFAULT_CONSTRUCTOR_JAVADOC;
import static de.informaticum.xjc.JavaDoc.DEFAULT_FIELD_ASSIGNMENT;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_OPTIONAL;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_PRIMITIVE;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_REQUIRED;
import static de.informaticum.xjc.JavaDoc.PARAM_WITH_DEFAULT_MULTI_VALUE;
import static de.informaticum.xjc.JavaDoc.PARAM_WITH_DEFAULT_SINGLE_VALUE;
import static de.informaticum.xjc.JavaDoc.RETURN_OPTIONAL_VALUE;
import static de.informaticum.xjc.JavaDoc.THROWS_IAE_BY_NULL;
import static de.informaticum.xjc.JavaDoc.VALUES_CONSTRUCTOR_JAVADOC;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
import static de.informaticum.xjc.util.OptionalAnalysis.accordingOptionalFor;
import static de.informaticum.xjc.util.OptionalAnalysis.isOptionalMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedFieldsOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedGettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructor;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.OutlineAnalysis.superAndGeneratedFieldsOf;
import static de.informaticum.xjc.util.Printify.fullName;
import static de.informaticum.xjc.util.Printify.render;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessBuilderName;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessFactoryName;
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
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
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
            LOG.info("Generate default constructor for [{}]", fullName(clazz));
            this.generateDefaultConstructor(clazz);
            assertThat(getConstructor(clazz)).isNotNull();
        }
    }

    private final void generateDefaultConstructor(final ClassOutline clazz) {
        // 1/3: Create
        final var $constructor = clazz.getImplClass().constructor(PUBLIC);
        // 2/3: JavaDocument
        $constructor.javadoc().append(format(DEFAULT_CONSTRUCTOR_JAVADOC));
        // 3/3: Implement
        $constructor.body().invoke("super");
        for (final var field : generatedFieldsOf(clazz).entrySet()) {
            final var attribute = field.getKey();
            final var $parameter = field.getValue();
            final var $value = defaultValueFor(attribute).orElse($null);
            $constructor.javadoc().append(format(DEFAULT_FIELD_ASSIGNMENT, $parameter.name(), render($value)));
            $constructor.body().assign($this.ref($parameter), $value);
        }
    }

    private final void considerValuesConstructor(final ClassOutline clazz) {
        if (!this.generateValueConstructor) {
            LOG.trace(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (superAndGeneratedFieldsOf(clazz).isEmpty() && this.generateDefaultConstructor) {
            LOG.info(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_EFFECTIVELY_SIMILAR);
        } else if (getConstructor(clazz, superAndGeneratedFieldsOf(clazz)) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_CONSTRUCTOR_EXISTS);
        } else {
            LOG.info("Generate all-values constructor for [{}].", fullName(clazz));
            final var $constructor = this.generateValuesConstructor(clazz);
            assertThat(getConstructor(clazz, superAndGeneratedFieldsOf(clazz))).isNotNull();
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
        // TODO: JavaDoc anpassen, wenn kein Parameter vorhanden
        $constructor.javadoc().append(format(VALUES_CONSTRUCTOR_JAVADOC));
        // TODO: @throws nur, wenn wirklich möglich (Super-Konstruktor beachten)
        $constructor.javadoc().addThrows(IllegalArgumentException.class).append(format(THROWS_IAE_BY_NULL));
        // 3/3: Implement
        final var $super = $constructor.body().invoke("super");
        for (final var field : superAndGeneratedFieldsOf(clazz.getSuperClass()).entrySet()) {
            final var attribute = field.getKey();
            final var property = attribute.getPropertyInfo();
            final var name = property.getName(true);
            final var $parameter = field.getValue();
            final var $default = defaultValueFor(attribute);
            if ($parameter.type().isPrimitive()) {
                $constructor.javadoc().addParam($parameter).append(format(PARAM_THAT_IS_PRIMITIVE, name));
            } else if (isOptional(attribute) && $default.isEmpty()) {
                $constructor.javadoc().addParam($parameter).append(format(PARAM_THAT_IS_OPTIONAL, name));
            } else if (isRequired(attribute) && $default.isEmpty()) {
                $constructor.javadoc().addParam($parameter).append(format(PARAM_THAT_IS_REQUIRED, name));
            } else {
                assertThat($default).isPresent();
                $constructor.javadoc().addParam($parameter).append(format(property.isCollection() ? PARAM_WITH_DEFAULT_MULTI_VALUE : PARAM_WITH_DEFAULT_SINGLE_VALUE, name));
            }
            $constructor.param(FINAL, $parameter.type(), $parameter.name());
            $super.arg($parameter);
        }
        for (final var field : generatedFieldsOf(clazz).entrySet()) {
            final var attribute = field.getKey();
            final var $parameter = field.getValue();
            $constructor.param(FINAL, $parameter.type(), $parameter.name());
            this.accordingAssignment(attribute, $constructor, $parameter, $parameter);
        }
        return $constructor;
    }

    private final void accordingAssignment(final FieldOutline attribute, final JMethod $method, final JFieldVar $property, final JFieldVar $parameter) {
        final var property = attribute.getPropertyInfo();
        final var name = property.getName(true);
        final var $default = defaultValueFor(attribute);
        if ($parameter.type().isPrimitive()) {
            $method.javadoc().addParam($parameter).append(format(PARAM_THAT_IS_PRIMITIVE, name));
            $method.body().assign($this.ref($property), $parameter);
        } else if (isOptional(attribute) && $default.isEmpty()) {
            $method.javadoc().addParam($parameter).append(format(PARAM_THAT_IS_OPTIONAL, name));
            $method.body().assign($this.ref($property), $parameter);
        } else if (isRequired(attribute) && $default.isEmpty()) {
            $method.javadoc().addParam($parameter).append(format(PARAM_THAT_IS_REQUIRED, name));
            $method._throws(IllegalArgumentException.class);
            final var $condition = $method.body()._if($parameter.eq($null));
            $condition._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required field '" + $parameter.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), $parameter);
        } else {
            assertThat($default).isPresent();
            $method.javadoc().addParam($parameter).append(format(property.isCollection() ? PARAM_WITH_DEFAULT_MULTI_VALUE : PARAM_WITH_DEFAULT_SINGLE_VALUE, name));
            $method.body().assign($this.ref($property), cond($parameter.eq($null), $default.get(), $parameter));
        }
    }

    private final void generateValuesConstructorFactory(final JDefinedClass $factory, final ClassOutline clazz, final JMethod $constructor) {
        final var $blueprint = getMethod($factory, guessFactoryName(clazz));
        // 1/3: Create
        final var $construction = $factory.method($blueprint.mods().getValue(), $blueprint.type(), $blueprint.name());
        // TODO: Re-throw declared exceptions of constructor
        // 2/3: JavaDocument
        $construction.javadoc().addAll($blueprint.javadoc());
        // 3/3: Implement
        final var $instantiation = _new(clazz.getImplClass());
        for (final var $param : $constructor.listParams()) {
            // TODO: Generate JavaDoc similar to all-values constructor
            $construction.javadoc().addParam($param).append("See all-values constructor of ").append(clazz.getImplClass());
            $construction.param(FINAL, $param.type(), $param.name());
            $instantiation.arg($param);
        }
        $construction.body()._return($instantiation);
    }

    private final void removeDefaultConstructorFactory(final JDefinedClass $factory, final ClassOutline clazz) {
        // 1/2: Identify
        final var $original = getMethod($factory, guessFactoryName(clazz));
        // 2/2: Remove
        $factory.methods().remove($original);
    }

    private final void considerValuesBuilder(final ClassOutline clazz) {
        if (!this.generateValueBuilder) {
            LOG.trace(SKIP_BUILDER, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            // TODO: LOG.warn(SKIP_BUILDER, "default", fullName(clazz), BECAUSE_BUILDER_EXISTS);
            LOG.info("Generate all-values builder for [{}]", fullName(clazz));
            this.generateValuesBuilder(clazz);
        }
    }

    private JClass generateValuesBuilder(final ClassOutline clazz) {
        final var $clazz = clazz.getImplClass();
        try {
            final var isAbstract = $clazz.isAbstract();
            final var isFinal = ($clazz.mods().getValue() & FINAL) != 0;
            final var mods = PUBLIC | STATIC | (isAbstract ? ABSTRACT : NONE) | (isFinal ? FINAL : NONE);
            // 1/3: Create
            final var $builder = $clazz._class(mods, "Builder", ClassType.CLASS);
            if (!isAbstract) {
                assertThat(mods & ABSTRACT).isEqualTo(0);
                final var $factory = $clazz.method(mods, $builder, "builder");
                $factory.body()._return(_new($builder));
            }
            // 2/3: JavaDocument
            // 3/3: Implement
            if (clazz.getSuperClass() != null) {
                $builder._extends(this.generateValuesBuilder(clazz.getSuperClass()));
            }
            for (final var field : generatedFieldsOf(clazz).entrySet()) {
                final var attribute = field.getKey();
                final var $parameter = field.getValue();
                final var $property = $builder.field(PROTECTED, $parameter.type(), $parameter.name());
                final var $wither = $builder.method(PUBLIC | FINAL, $builder, guessBuilderName(attribute));
                $wither.param(FINAL, $parameter.type(), $parameter.name());
                this.accordingAssignment(attribute, $wither, $property, $parameter);
                $wither.body()._return($this);
            }
            final var $build = $builder.method(PUBLIC | (isAbstract ? ABSTRACT : NONE), $clazz, "build");
            if (!isAbstract) {
                final var $instantiation = _new($clazz);
                superAndGeneratedFieldsOf(clazz).values().forEach($field -> $instantiation.arg($this.ref($field)));
                $build.body()._return($instantiation);
            }
            return $builder;
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
                final var $blueprint = getter.getValue();
                if (isRequired(attribute)) {
                    LOG.debug(SKIP_OPTIONAL_GETTER, $blueprint.name(), fullName(clazz), BECAUSE_ATTRIBUTE_IS_REQUIRED);
                } else if (isOptionalMethod($blueprint)) {
                    LOG.warn(SKIP_OPTIONAL_GETTER, $blueprint.name(), fullName(clazz), BECAUSE_METHOD_EXISTS);
                } else {
                    LOG.info("Replace return type X of [{}#{}()] with an according OptionalDouble, OptionalInt, OptionalLong, or Optional<X> type", fullName(clazz), $blueprint.name());
                    this.generateOptionalGetters(clazz, getter);
                }
            }
        }
    }

    private final void generateOptionalGetters(final ClassOutline clazz, final Entry<FieldOutline, JMethod> original) {
        final var attribute = original.getKey();
        final var property = attribute.getPropertyInfo();
        final var $blueprint = original.getValue();
        final var type = $blueprint.type();
        // 1/3: Create
        final var getterType = accordingOptionalFor(type);
        final var $getter = clazz.getImplClass().method($blueprint.mods().getValue(), getterType, $blueprint.name());
        // 2/3: JavaDocument
        $getter.javadoc().addReturn().append(format(RETURN_OPTIONAL_VALUE, property.getName(true)));
        // 3/3: Implement
        final var $factory = getterType.erasure();
        final var $delegation = $this.invoke($blueprint);
        if (type.isPrimitive()) {
            $getter.body()._return($factory.staticInvoke("of").arg($delegation));
        } else {
            final var $value = $getter.body().decl(FINAL, type, "value", $delegation);
            $getter.body()._return(cond($value.eq($null), $factory.staticInvoke("empty"), $factory.staticInvoke("of").arg($value)));
        }
        // Subsequently (!) modify the original getter method
        $blueprint.mods().setPrivate();
        $blueprint.mods().setFinal(true);
        $blueprint.name("_" + $blueprint.name());
    }

    private final void considerEquals(final ClassOutline clazz) {
        if (!this.generateEquals) {
            LOG.trace(SKIP_METHOD, "#equals(Object)", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, "equals", Object.class) != null) {
            LOG.warn(SKIP_METHOD, "#equals(Object)", fullName(clazz), BECAUSE_METHOD_EXISTS);
        } else {
            LOG.info("Generate [#equals(Object)] method for [{}]", fullName(clazz));
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
        final var fields = generatedFieldsOf(clazz).values();
        if (!fields.isEmpty()) {
            final var $Objects = this.reference(Objects.class);
            final var $that = $equals.body().decl(FINAL, clazz.getImplClass(), "that", cast(clazz.getImplClass(), $other));
            for (final var $field : fields) {
                comparisons.add($Objects.staticInvoke("equals").arg($this.ref($field)).arg($that.ref($field)));
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
            LOG.info("Generate [#hashCode()] method for [{}]", fullName(clazz));
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
        for (final var $field : generatedFieldsOf(clazz).values()) {
            calculation.arg($this.ref($field));
        }
        $hashCode.body()._return(calculation.listArgs().length > 0 ? calculation : $this.invoke("getClass").invoke("hashCode"));
    }

    private final void considerToString(final ClassOutline clazz) {
        if (!this.generateToString) {
            LOG.trace(SKIP_METHOD, "#toString()", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, "toString") != null) {
            LOG.warn(SKIP_METHOD, "#toString()", fullName(clazz), BECAUSE_METHOD_EXISTS);
        } else {
            LOG.info("Generate [#toString()] method for [{}]", fullName(clazz));
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
        for (final var field : generatedFieldsOf(clazz).entrySet()) {
            final var attribute = field.getKey();
            final var property = attribute.getPropertyInfo();
            final var $parameter = field.getValue();
            parts.add(lit(property.getName(true) + ": ").plus($Objects.staticInvoke("toString").arg($this.ref($parameter))));
        }
        if (clazz.getSuperClass() != null) {
            parts.add(lit("Super: ").plus($super.invoke("toString")));
        }
        final var $joiner = _new(this.reference(StringJoiner.class)).arg(", ").arg(clazz.getImplClass().name() + "[").arg("]");
        // TODO: InsurantIdType#ROOT in toString()-Ausgabe aufnehmen
        $toString.body()._return(parts.stream().reduce($joiner, (join, next) -> join.invoke("add").arg(next)).invoke("toString"));
    }

}
