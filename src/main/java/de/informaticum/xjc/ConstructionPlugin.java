package de.informaticum.xjc;

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
import static de.informaticum.xjc.JavaDoc.COPY_CONSTRUCTOR_JAVADOC_FIELDS;
import static de.informaticum.xjc.JavaDoc.COPY_CONSTRUCTOR_JAVADOC_INTRO;
import static de.informaticum.xjc.JavaDoc.COPY_CONSTRUCTOR_JAVADOC_SUPER;
import static de.informaticum.xjc.JavaDoc.DEFAULT_CONSTRUCTOR_JAVADOC_FIELDS;
import static de.informaticum.xjc.JavaDoc.DEFAULT_CONSTRUCTOR_JAVADOC_INTRO;
import static de.informaticum.xjc.JavaDoc.DEFAULT_CONSTRUCTOR_JAVADOC_SUPER;
import static de.informaticum.xjc.JavaDoc.DEFAULT_FIELD_ASSIGNMENT;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_OPTIONAL;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_PRIMITIVE;
import static de.informaticum.xjc.JavaDoc.PARAM_THAT_IS_REQUIRED;
import static de.informaticum.xjc.JavaDoc.PARAM_WITH_DEFAULT_MULTI_VALUE;
import static de.informaticum.xjc.JavaDoc.PARAM_WITH_DEFAULT_SINGLE_VALUE;
import static de.informaticum.xjc.JavaDoc.THROWS_IAE_BY_NULL;
import static de.informaticum.xjc.JavaDoc.VALUES_CONSTRUCTOR_JAVADOC_FIELDS;
import static de.informaticum.xjc.JavaDoc.VALUES_CONSTRUCTOR_JAVADOC_INTRO;
import static de.informaticum.xjc.JavaDoc.VALUES_CONSTRUCTOR_JAVADOC_SUPER;
import static de.informaticum.xjc.plugin.TargetCode.$null;
import static de.informaticum.xjc.plugin.TargetCode.$super;
import static de.informaticum.xjc.plugin.TargetCode.$this;
import static de.informaticum.xjc.util.CollectionAnalysis.accordingCopyFactoryFor;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
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
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
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
import com.sun.tools.xjc.outline.Outline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class ConstructionPlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(ConstructionPlugin.class);

    public static final String ADD_INTERFACE = "Add [{}] interface extension for [{}].";
    public static final String SKIP_INTERFACE = "Skip [{}] interface extension for [{}] because {}.";
    public static final String BECAUSE_ALREADY_IMPLEMENTED = "interface is already implemented";

    public static final String GENERATE_CONSTRUCTOR = "Generate {} constructor for [{}].";
    public static final String SKIP_CONSTRUCTOR = "Skip creation of {} constructor for [{}] because {}.";
    public static final String BECAUSE_CONSTRUCTOR_ALREADY_EXISTS = "such constructor already exists";
    public static final String BECAUSE_EFFECTIVELY_SIMILAR = "it is effectively similar to default-constructor";

    private static final String HIDE_DEFAULT_CONSTRUCTOR = "Hide default constructor [{}#{}()].";
    private static final String SKIP_HIDE_DEFAULT_CONSTRUCTOR = "Skip hiding of default constructor for [{}] because {}.";

    public static final String GENERATE_FACTORY = "Adopt {} constructor for [{}] in according package's ObjectFactory.";
    public static final String SKIP_FACTORY = "Skip adoption of {} object-factory method for [{}] because {}.";
    public static final String BECAUSE_ABSTRACT_CLASS = "this class is abstract";
    public static final String BECAUSE_UNDEFINED_DEFAULT_PENDANT = "according package's ObjectFactory does not contain a pendant default object-factory method for this class";
    public static final String BECAUSE_UNDEFINED_OBJECT_FACTORY = "there is no according package's ObjectFactory";
    public static final String BECAUSE_FACTORY_ALREADY_EXISTS = "such object-factory method already exists";

    public static final String GENERATE_BUILDER = "Generate builder for [{}].";
    public static final String SKIP_BUILDER = "Skip creation of builder for [{}] because {}.";

    private static final String HIDE_DEFAULT_FACTORY = "Hide default factory [{}#{}()].";
    private static final String SKIP_HIDE_DEFAULT_FACTORY = "Skip removal of default factory method for [{}] because {}.";

    private static final String REMOVE_DEFAULT_FACTORY = "Remove default factory [{}#{}()].";
    private static final String SKIP_REMOVE_DEFAULT_FACTORY = "Skip removal of default factory method for [{}] because {}.";

    private static final String clone = "clone";
    private static final String CLONE_SIGNATURE = format("#%s()", clone);

    private static final String OPTION_NAME = "ITBSG-xjc-construction";
    private static final String GENERATE_DEFAULTCONSTRUCTOR_NAME = "construction-default-constructor";
    private static final CommandLineArgument GENERATE_DEFAULTCONSTRUCTOR = new CommandLineArgument(GENERATE_DEFAULTCONSTRUCTOR_NAME, "Generate default constructor.");
    private static final String HIDE_DEFAULTCONSTRUCTOR_NAME = "construction-hide-default-constructor";
    private static final CommandLineArgument HIDE_DEFAULTCONSTRUCTOR = new CommandLineArgument(HIDE_DEFAULTCONSTRUCTOR_NAME, "Hides default constructors if such constructor exists. Default: false");
    // TODO: Minimum-value constructor (only required fields without default)
    // TODO: Reduced-value constructor (only required fields)
    private static final String GENERATE_VALUESCONSTRUCTOR_NAME = "construction-values-constructor";
    private static final CommandLineArgument GENERATE_VALUESCONSTRUCTOR = new CommandLineArgument(GENERATE_VALUESCONSTRUCTOR_NAME, format("Generate all-values constructor (automatically enables option '%s').", GENERATE_DEFAULTCONSTRUCTOR_NAME));
    private static final String GENERATE_COPYCONSTRUCTOR_NAME = "construction-copy-constructor";
    private static final CommandLineArgument GENERATE_COPYCONSTRUCTOR = new CommandLineArgument(GENERATE_COPYCONSTRUCTOR_NAME, format("Generate copy constructor (automatically enables option '%s').", GENERATE_DEFAULTCONSTRUCTOR_NAME));
    private static final String GENERATE_VALUESBUILDER_NAME = "construction-builder";
    private static final CommandLineArgument GENERATE_VALUESBUILDER = new CommandLineArgument(GENERATE_VALUESBUILDER_NAME, "Generate builder.");
    private static final String GENERATE_CLONE_NAME = "construction-clone";
    private static final CommandLineArgument GENERATE_CLONE = new CommandLineArgument(GENERATE_CLONE_NAME, format("Generate [%s] method.", CLONE_SIGNATURE));
    private static final String GENERATE_DEFENSIVECOPIES_NAME = "construction-defensive-copies";
    private static final CommandLineArgument GENERATE_DEFENSIVECOPIES = new CommandLineArgument(GENERATE_DEFENSIVECOPIES_NAME, "Generated code will create defensive copies of the submitted collection/array/cloneable arguments. (Note: No deep copies!)");
    private static final String HIDE_DEFAULT_FACTORIES_NAME = "construction-hide-default-factories";
    private static final CommandLineArgument HIDE_DEFAULT_FACTORIES = new CommandLineArgument(HIDE_DEFAULT_FACTORIES_NAME, "Hides default factory methods of object factories. Default: false");
    private static final String REMOVE_DEFAULT_FACTORIES_NAME = "construction-remove-default-factories";
    private static final CommandLineArgument REMOVE_DEFAULT_FACTORIES = new CommandLineArgument(REMOVE_DEFAULT_FACTORIES_NAME, "Removes default factory methods of object factories. Default: false");

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, "Generates construction code, i.e., constructors, builders, clones.");
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(GENERATE_DEFAULTCONSTRUCTOR, HIDE_DEFAULTCONSTRUCTOR,
                      GENERATE_VALUESCONSTRUCTOR,
                      GENERATE_COPYCONSTRUCTOR,
                      GENERATE_VALUESBUILDER,
                      GENERATE_CLONE,
                      GENERATE_DEFENSIVECOPIES,
                      HIDE_DEFAULT_FACTORIES, REMOVE_DEFAULT_FACTORIES);
    }

    @Override
    public final boolean prepareRun(final Outline outline, final Options options, final ErrorHandler errorHandler)
    throws SAXException {
        GENERATE_VALUESCONSTRUCTOR.alsoActivate(GENERATE_DEFAULTCONSTRUCTOR);
        GENERATE_COPYCONSTRUCTOR.alsoActivate(GENERATE_DEFAULTCONSTRUCTOR);
        outline.getClasses().forEach(this::considerCloneable);
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        this.considerDefaultConstructor(clazz);
        this.considerValuesConstructor(clazz);
        this.considerCopyConstructor(clazz);
        this.considerValuesBuilder(clazz);
        this.considerClone(clazz);
        // Default-Constructor-Hiding must be called after Builder creation! (Otherwise JavaDoc misses reference on it.) 
        this.considerHideDefaultConstructor(clazz);
        this.considerHideDefaultFactory(clazz);
        this.considerRemoveDefaultFactory(clazz);
        return true;
    }

    private final void considerDefaultConstructor(final ClassOutline clazz) {
        if (!GENERATE_DEFAULTCONSTRUCTOR.isActivated()) {
            LOG.trace(SKIP_CONSTRUCTOR, "default", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getConstructor(clazz) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "default", fullName(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
        } else {
            LOG.info(GENERATE_CONSTRUCTOR, "default", fullName(clazz));
            assertThat(getConstructor(clazz)).as("check undefined %s constructor", "default").isNull();
            this.generateDefaultConstructor(clazz);
            assertThat(getConstructor(clazz)).as("check generated %s constructor", "default").isNotNull();
            // TODO: Verify Object-Factory's zero-arguments construction method exists (or create it?)
        }
    }

    private final void generateDefaultConstructor(final ClassOutline clazz) {
        // 1/2: Create
        final var $Type = clazz.implClass;
        final var $constructor = $Type.constructor(PUBLIC);
        // 2/2: Implement (with JavaDoc)
        $constructor.javadoc().append(format(DEFAULT_CONSTRUCTOR_JAVADOC_INTRO));
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

    private final void considerHideDefaultConstructor(final ClassOutline clazz) {
        if (!HIDE_DEFAULTCONSTRUCTOR.isActivated()) {
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

    private final void considerValuesConstructor(final ClassOutline clazz) {
        if (!GENERATE_VALUESCONSTRUCTOR.isActivated()) {
            LOG.trace(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (superAndGeneratedPropertiesOf(clazz).isEmpty() && GENERATE_DEFAULTCONSTRUCTOR.isActivated()) {
            LOG.info(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_EFFECTIVELY_SIMILAR);
        } else if (getConstructor(clazz, superAndGeneratedPropertiesOf(clazz)) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "all-values", fullName(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
        } else {
            LOG.info(GENERATE_CONSTRUCTOR, "all-values", fullName(clazz));
            assertThat(getConstructor(clazz, superAndGeneratedPropertiesOf(clazz))).as("check undefined %s constructor", "all-values").isNull();
            final var $constructor = this.generateValuesConstructor(clazz);
            assertThat(getConstructor(clazz, superAndGeneratedPropertiesOf(clazz))).as("check generated %s constructor", "all-values").isNotNull();
            // this.considerValuesObjectFactory(clazz, $constructor);
        }
    }

    private final JMethod generateValuesConstructor(final ClassOutline clazz) {
        // 1/2: Create
        final var $Type = clazz.implClass;
        final var $constructor = $Type.constructor(PUBLIC);
        // 2/2: Implement (with JavaDoc)
        $constructor.javadoc().append(format(VALUES_CONSTRUCTOR_JAVADOC_INTRO));
        $constructor.javadoc(/* TODO: @throws nur, wenn wirklich möglich (Super-Konstruktor beachten) */).addThrows(IllegalArgumentException.class).append(format(THROWS_IAE_BY_NULL));
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
            appendParameterJavaDoc($constructor.javadoc(), attribute, $parameter);
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

    private final void accordingAssignment(final FieldOutline attribute, final JMethod $method, final JFieldVar $property, final JExpression $expression) {
        final var $default = defaultValueFor(attribute);
        if ($property.type().isPrimitive()) {
            $method.body().assign($this.ref($property), $expression);
        } else if (isOptional(attribute) && $default.isEmpty()) {
            final var $copyExpression = this.potentialDefensiveCopy(attribute, $property, $expression);
            if ($copyExpression == $expression) {
                $method.body().assign($this.ref($property), $expression);
            } else {
                $method.body().assign($this.ref($property), cond($expression.eq($null), $null, $copyExpression));
            }
        } else if (isRequired(attribute) && $default.isEmpty()) {
            $method._throws(IllegalArgumentException.class);
            final var $condition = $method.body()._if($expression.eq($null));
            $condition._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
            $condition._else().assign($this.ref($property), this.potentialDefensiveCopy(attribute, $property, $expression));
        } else {
            assertThat($default).isPresent();
            $method.body().assign($this.ref($property), cond($expression.eq($null), $default.get(), this.potentialDefensiveCopy(attribute, $property, $expression)));
        }
    }

    private final JExpression potentialDefensiveCopy(final FieldOutline attribute, final JFieldVar $property, final JExpression $expression) {
        if (GENERATE_DEFENSIVECOPIES.isActivated()) {
            // TODO: use copy-constructor if exits
            if (attribute.getPropertyInfo().isCollection()) {
                // TODO: Cloning the collection's elements
                return accordingCopyFactoryFor($property.type()).arg($expression);
            } else if ($property.type().isArray()) {
                return cast($property.type(), $expression.invoke("clone"));
            } else if (this.reference(Cloneable.class).isAssignableFrom($property.type().boxify())) {
                // TODO (?): Skip cats if "clone()" already returns required type
                return cast($property.type(), $expression.invoke("clone"));
            } else {
                LOG.debug("Skip defensive copy for [{}] because [{}] is neither Collection, Array, nor Cloneable.", $property.name(), $property.type().boxify().erasure());
            }
        }
        return $expression;
    }

    private final void considerValuesObjectFactory(final ClassOutline clazz, final JMethod $constructor) {
        assertThat(GENERATE_VALUESCONSTRUCTOR.isActivated()).isTrue();
        if (clazz._package().objectFactory() == null) {
            LOG.error(SKIP_FACTORY, "all-values", fullName(clazz), BECAUSE_UNDEFINED_OBJECT_FACTORY);
        } else if (clazz.implClass.isAbstract()) {
            LOG.info(SKIP_FACTORY, "all-values", fullName(clazz), BECAUSE_ABSTRACT_CLASS);
        } else if (getMethod(clazz._package().objectFactory(), guessFactoryName(clazz)) == null) {
            LOG.error(SKIP_FACTORY, "all-values", fullName(clazz), BECAUSE_UNDEFINED_DEFAULT_PENDANT);
        } else {
            LOG.info(GENERATE_FACTORY, "all-values", fullName(clazz));
            assertThat(getMethod(clazz._package().objectFactory(), guessFactoryName(clazz), $constructor.listParamTypes())).as("check undefined %s factory method", "all-values").isNull();
            this.generateValuesObjectFactory(clazz._package().objectFactory(), clazz, $constructor);
            assertThat(getMethod(clazz._package().objectFactory(), guessFactoryName(clazz), $constructor.listParamTypes())).as("check generated %s factory method", "all-values").isNotNull();
        }
    }

    private final void generateValuesObjectFactory(final JDefinedClass $objectFactory, final ClassOutline clazz, final JMethod $constructor) {
        // 1/2: Create
        final var $defaultFactory = getMethod($objectFactory, guessFactoryName(clazz));
        final var $valuesFactory = $objectFactory.method($defaultFactory.mods().getValue(), $defaultFactory.type(), $defaultFactory.name());
        // 2/2: Implement (with JavaDoc)
        $valuesFactory.javadoc().addAll($defaultFactory.javadoc());
        // TODO: Re-throw declared exceptions of constructor
        // TODO: @return-JavaDoc?!
        final var $Type = clazz.implClass;
        final var $instantiation = _new($Type);
        for (final var $constructorParameter : $constructor.listParams()) {
            final var $factoryParameter = $valuesFactory.param(FINAL, $constructorParameter.type(), $constructorParameter.name());
            $valuesFactory.javadoc(/* TODO: Generate JavaDoc similar to all-values constructor */).addParam($factoryParameter).append("see all-values constructor of ").append($Type);
            $instantiation.arg($factoryParameter);
        }
        $valuesFactory.body()._return($instantiation);
    }

    private final void considerCopyConstructor(final ClassOutline clazz) {
        if (!GENERATE_COPYCONSTRUCTOR.isActivated()) {
            LOG.trace(SKIP_CONSTRUCTOR, "copy", fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getConstructor(clazz, clazz.implClass) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "copy", fullName(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
        } else {
            LOG.info(GENERATE_CONSTRUCTOR, "copy", fullName(clazz));
            assertThat(getConstructor(clazz, clazz.implClass)).as("check undefined %s constructor", "copy").isNull();
            final var $constructor = this.generateCopyConstructor(clazz);
            assertThat(getConstructor(clazz, clazz.implClass)).as("check generated %s constructor", "copy").isNotNull();
            // this.considerCopyObjectFactory(clazz, $constructor);
        }
    }

    private final JMethod generateCopyConstructor(final ClassOutline clazz) {
        // 1/2: Create
        final var $Type = clazz.implClass;
        final var $constructor = $Type.constructor(PUBLIC);
        // 2/2: Implement (with JavaDoc)
        final var $blueprint = $constructor.param(FINAL, $Type, "blueprint");
        // TODO: Null-Check of $blueprint
        $constructor.javadoc().addParam($blueprint).append("the blueprint instance");
        $constructor.javadoc().append(format(COPY_CONSTRUCTOR_JAVADOC_INTRO));
        if (clazz.getSuperClass() != null) {
            $constructor.javadoc().append(format(COPY_CONSTRUCTOR_JAVADOC_SUPER));
            $constructor.body().invoke("super").arg($blueprint);
        }
        $constructor.javadoc().append(format(COPY_CONSTRUCTOR_JAVADOC_FIELDS));
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            $constructor.body().assign($this.ref($property), this.potentialDefensiveCopy(attribute, $property, $blueprint.ref($property)));
        }
        return $constructor;
    }

    private final void considerCopyObjectFactory(final ClassOutline clazz, final JMethod $constructor) {
        assertThat(GENERATE_COPYCONSTRUCTOR.isActivated()).isTrue();
        if (clazz._package().objectFactory() == null) {
            LOG.error(SKIP_FACTORY, "copy", fullName(clazz), BECAUSE_UNDEFINED_OBJECT_FACTORY);
        } else if (clazz.implClass.isAbstract()) {
            LOG.info(SKIP_FACTORY, "copy", fullName(clazz), BECAUSE_ABSTRACT_CLASS);
        } else if (getMethod(clazz._package().objectFactory(), guessFactoryName(clazz)) == null) {
            LOG.error(SKIP_FACTORY, "copy", fullName(clazz), BECAUSE_UNDEFINED_DEFAULT_PENDANT);
        } else {
            LOG.info(GENERATE_FACTORY, "copy", fullName(clazz));
            assertThat(getMethod(clazz._package().objectFactory(), guessFactoryName(clazz), clazz.implClass)).as("check undefined %s factory method", "copy").isNull();
            this.generateCopyObjectFactory(clazz._package().objectFactory(), clazz, $constructor);
            assertThat(getMethod(clazz._package().objectFactory(), guessFactoryName(clazz), clazz.implClass)).as("check generated %s factory method", "copy").isNotNull();
        }
    }

    private final void generateCopyObjectFactory(final JDefinedClass $objectFactory, final ClassOutline clazz, final JMethod $constructor) {
        // 1/2: Create
        final var $defaultFactory = getMethod($objectFactory, guessFactoryName(clazz));
        final var copyFactory = $objectFactory.method($defaultFactory.mods().getValue(), $defaultFactory.type(), $defaultFactory.name());
        // 2/2: Implement (with JavaDoc)
        copyFactory.javadoc().addAll($defaultFactory.javadoc());
        final var $Type = clazz.implClass;
        final var $blueprint = copyFactory.param(FINAL, $Type, "blueprint");
        // TODO: Null-Check of $blueprint
        copyFactory.javadoc(/* TODO: Generate JavaDoc similar to all-values constructor */).addParam($blueprint).append("the blueprint instance, see copy constructor of ").append($Type);
        copyFactory.body()._return(_new($Type).arg($blueprint));
    }

    private final void considerCloneable(final ClassOutline clazz) {
        if (!GENERATE_CLONE.isActivated()) {
            LOG.trace(SKIP_INTERFACE, Cloneable.class, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (this.reference(Cloneable.class).isAssignableFrom(clazz.implClass)) {
            LOG.warn(SKIP_INTERFACE, Cloneable.class, fullName(clazz), BECAUSE_ALREADY_IMPLEMENTED);
        } else {
            LOG.info(ADD_INTERFACE, Cloneable.class, fullName(clazz));
            assertThat(this.reference(Cloneable.class).isAssignableFrom(clazz.implClass)).isFalse();
            this.addCloneable(clazz);
            assertThat(this.reference(Cloneable.class).isAssignableFrom(clazz.implClass)).isTrue();
        }
    }

    private final void addCloneable(final ClassOutline clazz) {
        // 1/1: Implement
        final var $Type = clazz.implClass;
        $Type._implements(Cloneable.class);
    }

    private final void considerClone(final ClassOutline clazz) {
        if (!GENERATE_CLONE.isActivated()) {
            LOG.trace(SKIP_METHOD, CLONE_SIGNATURE, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else if (getMethod(clazz, clone) != null) {
            LOG.warn(SKIP_METHOD, CLONE_SIGNATURE, fullName(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
        } else {
            LOG.info(GENERATE_METHOD, CLONE_SIGNATURE, fullName(clazz));
            assertThat(this.reference(Cloneable.class).isAssignableFrom(clazz.implClass)).isTrue();
            assertThat(getMethod(clazz, clone)).as("check undefined method %s", CLONE_SIGNATURE).isNull();
            this.addClone(clazz);
            assertThat(this.reference(Cloneable.class).isAssignableFrom(clazz.implClass)).isTrue();
            assertThat(getMethod(clazz, clone)).as("check generated method %s", CLONE_SIGNATURE).isNotNull();
        }
    }

    private final void addClone(final ClassOutline clazz) {
        // 1/3: Create
        final var $Type = clazz.implClass;
        final var $clone = $Type.method(PUBLIC, $Type, clone);
        // 2/3: Annotate
        $clone.annotate(Override.class);
        // 3/3: Implement (with JavaDoc)
        final JBlock $body;
        if (clazz.getSuperClass() == null) {
            final var $try = $clone.body()._try();
            $body = $try.body();
            final var $catch = $try._catch(this.reference(CloneNotSupportedException.class));
            final var $bug = $catch.param("bug");
            $bug.mods().setFinal(true);
            $catch.body()._throw(_new(this.reference(RuntimeException.class)).arg("WTF! Super's #clone() failed unexpectedly.").arg($bug));
        } else {
            $body = $clone.body();
        }
        final var $reproduction = $body.decl(FINAL, $Type, "reproduction", cast($Type, $super.invoke("clone")));
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            $body.assign($reproduction.ref($property), this.potentialDefensiveCopy(attribute, $property, $this.ref($property)));
        }
        $body._return($reproduction);
    }

    private final void considerValuesBuilder(final ClassOutline clazz) {
        if (!GENERATE_VALUESBUILDER.isActivated()) {
            LOG.trace(SKIP_BUILDER, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            // TODO: LOG.warn(SKIP_BUILDER, "default", fullName(clazz), BECAUSE_BUILDER_EXISTS);
            LOG.info(GENERATE_BUILDER, fullName(clazz));
            this.generateValuesBuilder(clazz);
        }
    }

    private final JClass generateValuesBuilder(final ClassOutline clazz) {
        final var $clazz = clazz.implClass;
        try {
            final var isAbstract = $clazz.isAbstract();
            final var isFinal = ($clazz.mods().getValue() & FINAL) != 0;
            final var builderModifiers = PUBLIC | STATIC | (isAbstract ? ABSTRACT : NONE) | (isFinal ? FINAL : NONE);
            // 1/2: Create
            final var $Builder = $clazz._class(builderModifiers, "Builder", ClassType.CLASS);
            // 2/2: Implement (with JavaDoc)
            $Builder.javadoc().append("Builder for (enclosing) class ").append($clazz).append(".");
            // (a) "toBuilder()" in XSDClass
            final var $toBuilder = $clazz.method(builderModifiers & ~STATIC, $Builder, "toBuilder");
            $toBuilder.javadoc().addReturn().append("a new ").append($Builder).append(" instance with all properties initialised with the current values of {@code this} instance");
            // (b) "Builder()" in Builder
            final var $defaultConstructor = $Builder.constructor(PROTECTED);
            $defaultConstructor.javadoc().append("Default constructor.");
            // (c) "Builder(XSDClass blueprint)" in Builder
            final var $blueprintConstructor = $Builder.constructor(PROTECTED);
            $blueprintConstructor.javadoc().append("Blueprint constructor.");
            final var $blueprint = $blueprintConstructor.param(FINAL, $clazz, "blueprint");
            $blueprintConstructor.javadoc().addParam($blueprint).append("the blueprint ").append($clazz).append(" instance to get all initial values from");
            // (d) "build()" in Builder
            final var $build = $Builder.method(builderModifiers & ~STATIC, $clazz, "build");
            $build.javadoc().addReturn().append("a new instance of ").append($clazz);
            if (clazz.getSuperClass() != null) {
                $Builder._extends(this.generateValuesBuilder(clazz.getSuperClass()));
                // (a) "toBuilder()" in XSDClass
                $toBuilder.annotate(Override.class);
                // (b) "Builder()" in Builder
                $defaultConstructor.body().invoke("super");
                // (c) "Builder(XSDClass blueprint)" in Builder
                $blueprintConstructor.body().invoke("super").arg($blueprint);
                // (d) "build()" in Builder
                $build.annotate(Override.class);
            }
            if (!isAbstract) {
                final var $builder = $clazz.method(builderModifiers, $Builder, "builder");
                $builder.body()._return(_new($Builder));
                $builder.javadoc().addReturn().append("a new instance of the ").append($Builder).append(" class, corresponding to this ").append($clazz).append(" clazz");
                // (a) "toBuilder()" in XSDClass
                $toBuilder.body()._return(_new($Builder).arg($this));
                // (d) "build()" in Builder
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
                $build.body()._return($instantiation);
            }
            for (final var blueprintProperty : superAndGeneratedPropertiesOf(clazz.getSuperClass()).entrySet()) {
                final var attribute = blueprintProperty.getKey();
                final var $blueprintProperty = blueprintProperty.getValue();
                final var $wither = $Builder.method(PUBLIC | (isFinal ? FINAL : NONE), $Builder, guessWitherName(attribute));
                $wither.annotate(Override.class);
                final var $parameter = $wither.param(FINAL, $blueprintProperty.type(), $blueprintProperty.name());
                $wither.body().invoke($super, $wither).arg($parameter);
                $wither.body()._return($this);
                // TODO: immediate "withXYZ(xyz)"-Methoden: { return this.toBuilder.with(xyz).build(); }
            }
            for (final var blueprintProperty : generatedPropertiesOf(clazz).entrySet()) {
                final var attribute = blueprintProperty.getKey();
                final var $blueprintProperty = blueprintProperty.getValue();
                final var $builderProperty = $Builder.field(PROTECTED, $blueprintProperty.type(), $blueprintProperty.name(), defaultValueFor(attribute).orElse($null));
                this.accordingAssignment(attribute, $blueprintConstructor, $builderProperty, $blueprint.ref($blueprintProperty));
                final var $wither = $Builder.method(PUBLIC | (isFinal ? FINAL : NONE), $Builder, guessWitherName(attribute));
                final var $parameter = $wither.param(FINAL, $builderProperty.type(), $builderProperty.name());
                appendParameterJavaDoc($wither.javadoc(), attribute, $parameter);
                this.accordingAssignment(attribute, $wither, $builderProperty, $parameter);
                $wither.body()._return($this);
                $wither.javadoc().addReturn().append("{@code this} builder instance for fluent API style");
            }
            return $Builder;
        } catch (final JClassAlreadyExistsException alreadyExists) {
            return stream($clazz.listClasses()).filter(nested -> "Builder".equals(nested.name())).findFirst()
            .orElseThrow(() -> new RuntimeException("Nested class 'Builder' already exists but cannot be found!", alreadyExists));
        }
    }

    private final void considerHideDefaultFactory(final ClassOutline clazz) {
        if (!HIDE_DEFAULT_FACTORIES.isActivated()) {
            LOG.trace(SKIP_HIDE_DEFAULT_FACTORY, fullName(clazz), BECAUSE_OPTION_IS_DISABLED);
        } else {
            final var $objectFactory = clazz._package().objectFactory();
            final var $factory = getMethod($objectFactory, guessFactoryName(clazz));
            if ($factory == null) {
                //
            } else {
                LOG.info(HIDE_DEFAULT_FACTORY, fullName($objectFactory), $factory.name());
                this.hideDefaultFactory($objectFactory, $factory, clazz);
                // TODO: assert private modifier
            }
        }
    }

    private final void hideDefaultFactory(final JDefinedClass $objectFactory, final JMethod $factory, final ClassOutline clazz) {
        $factory.mods().setPrivate();
        $factory.annotate(SuppressWarnings.class).param("value", "unused");
        $factory.javadoc().append(format("%n%nThis factory method has been intentionally set on {@code protected} visibility to be not used anymore."))
                          .append(format("%nInstead in order to create instances of this class, use the all-values constructor"));
        final var $Builder = stream(clazz.implClass.listClasses()).filter(nested -> "Builder".equals(nested.name())).findFirst();
        if ($Builder.isPresent()) {
            $factory.javadoc().append(" or utilise the nested ").append($Builder.get());
        }
        $factory.javadoc().append(".");
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

}
