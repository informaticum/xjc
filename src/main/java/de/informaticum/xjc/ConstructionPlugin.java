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
import static de.informaticum.xjc.BoilerplatePlugin.BECAUSE_METHOD_ALREADY_EXISTS;
import static de.informaticum.xjc.BoilerplatePlugin.GENERATE_METHOD;
import static de.informaticum.xjc.BoilerplatePlugin.SKIP_METHOD;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$super;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.getMethod;
import static de.informaticum.xjc.util.CollectionAnalysis.copyFactoryFor;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
import static de.informaticum.xjc.util.OutlineAnalysis.filter;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructor;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.OutlineAnalysis.superAndGeneratedPropertiesOf;
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
import java.util.Optional;
import java.util.function.Predicate;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import de.informaticum.xjc.util.OutlineAnalysis;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

public class ConstructionPlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(ConstructionPlugin.class);

    private static final String clone = "clone";
    private static final String CLONE_SIGNATURE = format("#%s()", clone);

    private static final String OPTION_NAME = "informaticum-xjc-construction";
    private static final String OPTION_DESC = "Generates construction code, i.e., constructors, builders, clones.";
    private static final CommandLineArgument GENERATE_DEFAULTCONSTRUCTOR = new CommandLineArgument("construction-default-constructor",      "Generate default constructor. Default: false");
    private static final CommandLineArgument HIDE_DEFAULTCONSTRUCTOR     = new CommandLineArgument("construction-hide-default-constructor", "Hides default constructors if such constructor exists. Default: false");
    private static final CommandLineArgument GENERATE_VALUESCONSTRUCTOR  = new CommandLineArgument("construction-values-constructor",       "Generate all-values constructor (automatically enables option '-construction-default-constructor'). Default: false");
    private static final CommandLineArgument GENERATE_BASICCONSTRUCTOR   = new CommandLineArgument("construction-basic-constructor",        "Generate basic-values constructor, based on all required fields (automatically enables option '-construction-default-constructor'). Default: false");
    private static final CommandLineArgument GENERATE_COLLECTIONINIT     = new CommandLineArgument("construction-initialise-collections",   "Each time a collection initialisation statement is generated, the value will not be 'null' but the empty collection instance. Default: false");
    private static final CommandLineArgument GENERATE_COPYCONSTRUCTOR    = new CommandLineArgument("construction-copy-constructor",         "Generate copy constructor (automatically enables option '-construction-default-constructor'). Default: false");
    private static final CommandLineArgument GENERATE_BUILDER            = new CommandLineArgument("construction-builder",                  "Generate builder. Default: false");
    private static final CommandLineArgument GENERATE_CLONE              = new CommandLineArgument("construction-clone",             format("Generate [%s] method. Default: false", CLONE_SIGNATURE));
    private static final CommandLineArgument GENERATE_DEFENSIVECOPIES    = new CommandLineArgument("construction-defensive-copies",         "Generated code will create defensive copies of the submitted collection/array/cloneable arguments. (Note: No deep copies!) Default: false");
    private static final CommandLineArgument HIDE_DEFAULT_FACTORIES      = new CommandLineArgument("construction-hide-default-factories",   "Hides default factory methods of object factories. Default: false");
    private static final CommandLineArgument REMOVE_DEFAULT_FACTORIES    = new CommandLineArgument("construction-remove-default-factories", "Removes default factory methods of object factories. Default: false");

    private static final String GENERATE_CONSTRUCTOR = "Generate {} constructor for [{}].";
    private static final String SKIP_CONSTRUCTOR = "Skip creation of {} constructor for [{}] because {}.";
    private static final String BECAUSE_CONSTRUCTOR_ALREADY_EXISTS = "such constructor already exists";

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESC);
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(GENERATE_DEFAULTCONSTRUCTOR, HIDE_DEFAULTCONSTRUCTOR, GENERATE_VALUESCONSTRUCTOR, GENERATE_BASICCONSTRUCTOR, GENERATE_COLLECTIONINIT, GENERATE_COPYCONSTRUCTOR, GENERATE_BUILDER, GENERATE_CLONE, GENERATE_DEFENSIVECOPIES, HIDE_DEFAULT_FACTORIES, REMOVE_DEFAULT_FACTORIES);
    }

    @Override
    public final boolean prepareRun()
    throws SAXException {
        GENERATE_VALUESCONSTRUCTOR.activates(GENERATE_DEFAULTCONSTRUCTOR);
        GENERATE_BASICCONSTRUCTOR.activates(GENERATE_DEFAULTCONSTRUCTOR);
        GENERATE_COPYCONSTRUCTOR.activates(GENERATE_DEFAULTCONSTRUCTOR);
        GENERATE_CLONE.doOnActivation(() -> this.outline().getClasses().forEach(this::addCloneable));
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        GENERATE_DEFAULTCONSTRUCTOR.doOnActivation(this::generateDefaultConstructor, clazz);
        GENERATE_VALUESCONSTRUCTOR.doOnActivation(this::generateValuesConstructor, clazz);
        GENERATE_BASICCONSTRUCTOR.doOnActivation(this::generateBasicConstructor, clazz);
        GENERATE_COPYCONSTRUCTOR.doOnActivation(this::generateCopyConstructor, clazz);
        GENERATE_BUILDER.doOnActivation(this::generateBuilder, clazz);
        GENERATE_CLONE.doOnActivation(this::addClone, clazz);
        // GENERATE_DEFENSIVECOPIES is used indirectly
        // Default-Constructor-Hiding must be called after Builder creation! (Otherwise JavaDoc misses reference on it.)
        HIDE_DEFAULTCONSTRUCTOR.doOnActivation(this::hideDefaultConstructor, clazz);
        HIDE_DEFAULT_FACTORIES.doOnActivation(this::hideDefaultFactory, clazz);
        REMOVE_DEFAULT_FACTORIES.doOnActivation(this::removeDefaultFactory, clazz);
        return true;
    }

    private final void generateDefaultConstructor(final ClassOutline clazz) {
        // 1/3: Prepare
        if (getConstructor(clazz) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "default", fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_CONSTRUCTOR, "default", fullNameOf(clazz));
        // 2/3: Create
        final var $Class = clazz.implClass;
        final var $constructor = $Class.constructor(PUBLIC);
        // 3/3: Implement (with JavaDoc)
        $constructor.javadoc().append(format("<a href=\"https://github.com/informaticum/xjc\">Creates a new instance of this class.</a>%nIn detail, "));
        if (clazz.getSuperClass() != null) {
            $constructor.javadoc().append("the default constructor of the super class is called, and then ");
            $constructor.body().invoke("super");
        }
        $constructor.javadoc().append("all fields are initialised in succession.");
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var $value = defaultValueFor(attribute, GENERATE_COLLECTIONINIT, /*TODO*/ GENERATE_COLLECTIONINIT).orElse($null);
            $constructor.javadoc().append(format("%n%nThe field {@link #%s} will be initialised with: {@code %s}", $property.name(), render($value)));
            $constructor.body().assign($this.ref($property), $value);
        }
    }

    private final void hideDefaultConstructor(final ClassOutline clazz) {
        // 1/2: Prepare
        if (getConstructor(clazz) == null) {
            LOG.warn("Skip hiding of default constructor for [{}] because such constructor does not exist.", fullNameOf(clazz));
            return;
        } else if (GENERATE_VALUESCONSTRUCTOR.isActivated() && superAndGeneratedPropertiesOf(clazz).isEmpty()) {
            LOG.warn("Skip hiding of default constructor for [{}] because it is similar to the all-values constructor.", fullNameOf(clazz));
            return;
        } else if (GENERATE_BASICCONSTRUCTOR.isActivated() && filter(superAndGeneratedPropertiesOf(clazz), OutlineAnalysis::isRequired).isEmpty()) {
            LOG.warn("Skip hiding of default constructor for [{}] because it is similar to the basic-values constructor.", fullNameOf(clazz));
            return;
        }
        LOG.info("Hide default constructor [{}#{}()].", fullNameOf(clazz), fullNameOf(clazz));
        // 2/2: Modify
        final var $constructor = getConstructor(clazz);
        $constructor.mods().setProtected();
        $constructor.javadoc().append(format("%n%nThis constructor has been <a href=\"https://github.com/informaticum/xjc\">intentionally set on {@code protected} visibility</a> to be not used anymore."))
                              .append(format("%nInstead in order to create instances of this class, use any of the other constructors"));
        final var $Builder = stream(clazz.implClass.listClasses()).filter(nested -> "Builder".equals(nested.name())).findFirst();
        if ($Builder.isPresent()) {
            $constructor.javadoc().append(" or utilise the nested ").append($Builder.get());
        }
        $constructor.javadoc().append(format(".%n"))
                              .append(format("%nSince JAX-B's reflective instantiation bases on a default constructor, it has not been removed."))
                              .append(format("%n(As an aside, it cannot be set to {@code private} because the similarly kept sub-classes' default constructors must have access to this constructor.)"));
    }

    private final void generateValuesConstructor(final ClassOutline clazz) {
        this.generateValuesConstructor(clazz, "all-values", any -> true);
    }

    private final void generateBasicConstructor(final ClassOutline clazz) {
        this.generateValuesConstructor(clazz, "basic-values", OutlineAnalysis::isRequired);
    }

    private final void generateValuesConstructor(final ClassOutline clazz, final String label, final Predicate<? super FieldOutline> predicate) {
        // 1/3: Prepare
        if (filter(superAndGeneratedPropertiesOf(clazz), predicate).isEmpty() && (getConstructor(clazz) != null)) {
            LOG.info(SKIP_CONSTRUCTOR, label, fullNameOf(clazz), "it is effectively similar to default-constructor");
            return;
        }
        if (getConstructor(clazz, filter(superAndGeneratedPropertiesOf(clazz), predicate)) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, label, fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_CONSTRUCTOR, label, fullNameOf(clazz));
        // 2/3: Create
        final var $Class = clazz.implClass;
        final var $constructor = $Class.constructor(PUBLIC);
        // 3/3: Implement (with JavaDoc)
        $constructor.javadoc().append(format("<a href=\"https://github.com/informaticum/xjc\">Creates a new instance of this class.</a>%nIn detail, "));
        $constructor.javadoc(/* TODO: @throws nur, wenn wirklich möglich (Super-Konstruktor beachten) */).addThrows(IllegalArgumentException.class).append("iff any given value is {@code null} illegally");
        if (clazz.getSuperClass() != null) {
            $constructor.javadoc().append(format("the %s constructor of the super class is called, and then ", label));
            final var $super = $constructor.body().invoke("super");
            for (final var property : filter(superAndGeneratedPropertiesOf(clazz.getSuperClass()), predicate).entrySet()) {
                final var attribute = property.getKey();
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                appendParameterJavaDoc($constructor.javadoc(), attribute, $parameter);
                $super.arg($parameter);
            }
        }
        $constructor.javadoc().append("all fields are assigned in succession.")
                              .append(format("%nIf any given value is invalid, either the according default value will be assigned (if such value exists) or an according exception will be thrown."));
        final var specifiedFields = filter(generatedPropertiesOf(clazz), predicate).entrySet();
        if (!specifiedFields.isEmpty()) {
            $constructor.body().directStatement("// below fields are assigned with their according parameter");
            for (final var property : specifiedFields) {
                final var attribute = property.getKey();
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                appendParameterJavaDoc($constructor.javadoc(), attribute, $parameter);
                this.accordingAssignment(attribute, $constructor, $property, $parameter);
            }
        }
        final var additionalFields = filter(generatedPropertiesOf(clazz), predicate.negate()).entrySet();
        if (!additionalFields.isEmpty()) {
            $constructor.body().directStatement("// below fields are assigned with their according initial value");
            for (final var property : additionalFields) {
                final var attribute = property.getKey();
                final var $property = property.getValue();
                final var $parameter = $null;
                this.accordingAssignment(attribute, $constructor, $property, $parameter);
            }
        }
    }

    private static final void appendParameterJavaDoc(final JDocComment javadoc, final FieldOutline attribute, final JVar $parameter) {
        final var $default = defaultValueFor(attribute, GENERATE_COLLECTIONINIT, /*TODO*/ GENERATE_COLLECTIONINIT);
        appendParameterJavaDoc(javadoc, attribute, $parameter, $default);
    }

    static final void appendParameterJavaDoc(final JDocComment javadoc, final FieldOutline attribute, final JVar $parameter, final Optional<JExpression> $default) {
        final var info = attribute.getPropertyInfo();
        final var name = info.getName(true);
        if ($parameter.type().isPrimitive()) {
            javadoc.addParam($parameter).append(format("value for the attribute '%s'", name));
        } else if (isOptional(attribute) && $default.isEmpty()) {
            javadoc.addParam($parameter).append(format("value for the attribute '%s' (can be {@code null} because attribute is optional)", name));
        } else if (isRequired(attribute) && $default.isEmpty()) {
            javadoc.addParam($parameter).append(format("value for the attribute '%s' (cannot be {@code null} because attribute is required)", name));
        } else {
            assertThat($default).isPresent();
            javadoc.addParam($parameter).append(format(info.isCollection() ? "value for the attribute '%s' (can be {@code null} because an empty, modifiable collection will be used instead)" : "value for the attribute '%s' (can be {@code null} because an according default value will be used instead)", name));
        }
    }

    private final void accordingAssignment(final FieldOutline attribute, final JMethod $method, final JFieldVar $property, final JExpression $expression) {
        final var $default = defaultValueFor(attribute, GENERATE_COLLECTIONINIT, /*TODO*/ GENERATE_COLLECTIONINIT);
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
            if ($expression.equals($null)) {
                $method.body().assign($this.ref($property), $default.get());
            } else {
                $method.body().assign($this.ref($property), cond($expression.eq($null), $default.get(), this.potentialDefensiveCopy(attribute, $property, $expression)));
            }
        }
    }

    private final JExpression potentialDefensiveCopy(final FieldOutline attribute, final JFieldVar $property, final JExpression $expression) {
        if (GENERATE_DEFENSIVECOPIES.isActivated()) {
            // TODO: use copy-constructor if exits
            if (attribute.getPropertyInfo().isCollection()) {
                // TODO: Cloning the collection's elements (a.k.a. deep clone)
                return copyFactoryFor($property.type()).arg($expression);
            } else if ($property.type().isArray()) {
                return cast($property.type(), $expression.invoke("clone"));
            } else if (this.reference(Cloneable.class).isAssignableFrom($property.type().boxify())) {
                // TODO (?): Skip cast if "clone()" already returns required type
                return cast($property.type(), $expression.invoke("clone"));
            } else {
                LOG.debug("Skip defensive copy for [{}] because [{}] is neither Collection, Array, nor Cloneable.", $property.name(), $property.type().boxify().erasure());
            }
        }
        return $expression;
    }

    private final void generateCopyConstructor(final ClassOutline clazz) {
        // 1/3: Prepare
        if (getConstructor(clazz, clazz.implClass) != null) {
            LOG.warn(SKIP_CONSTRUCTOR, "copy", fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_CONSTRUCTOR, "copy", fullNameOf(clazz));
        // 2/3: Create
        final var $Class = clazz.implClass;
        final var $constructor = $Class.constructor(PUBLIC);
        // 3/3: Implement (with JavaDoc)
        final var $blueprint = $constructor.param(FINAL, $Class, "blueprint");
        // TODO: Null-Check of $blueprint
        $constructor.javadoc().addParam($blueprint).append("the blueprint instance");
        $constructor.javadoc().append(format("<a href=\"https://github.com/informaticum/xjc\">Creates a new instance of this class.</a>%nIn detail, "));
        if (clazz.getSuperClass() != null) {
            $constructor.javadoc().append("the copy-constructor of the super class is called, and then ");
            $constructor.body().invoke("super").arg($blueprint);
        }
        $constructor.javadoc().append("all fields are assigned in succession.");
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            $constructor.body().assign($this.ref($property), this.potentialDefensiveCopy(attribute, $property, $blueprint.ref($property)));
        }
    }

    private final void addCloneable(final ClassOutline clazz) {
        // 1/2: Prepare
        if (this.reference(Cloneable.class).isAssignableFrom(clazz.implClass)) {
            LOG.warn("Skip [{}] interface extension for [{}] because interface is already implemented.", Cloneable.class, fullNameOf(clazz));
            return;
        }
        LOG.info("Add [{}] interface extension for [{}].", Cloneable.class, fullNameOf(clazz));
        // 2/2: Implement
        final var $Class = clazz.implClass;
        $Class._implements(Cloneable.class);
    }

    private final void addClone(final ClassOutline clazz) {
        // 1/4: Prepare
        if (getMethod(clazz, clone) != null) {
            LOG.warn(SKIP_METHOD, CLONE_SIGNATURE, fullNameOf(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        assertThat(this.reference(Cloneable.class).isAssignableFrom(clazz.implClass)).isTrue();
        LOG.info(GENERATE_METHOD, CLONE_SIGNATURE, fullNameOf(clazz));
        // 2/4: Create
        final var $Class = clazz.implClass;
        final var $clone = $Class.method(PUBLIC, $Class, clone);
        // 3/4: Annotate
        $clone.annotate(Override.class);
        // 4/4: Implement (with JavaDoc)
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
        final var $reproduction = $body.decl(FINAL, $Class, "reproduction", cast($Class, $super.invoke("clone")));
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            $body.assign($reproduction.ref($property), this.potentialDefensiveCopy(attribute, $property, $this.ref($property)));
        }
        $body._return($reproduction);
    }

    private final JClass generateBuilder(final ClassOutline clazz) {
        // TODO: Skip if Builder already exists
        LOG.info("Generate builder for [{}].", fullNameOf(clazz));
        final var $Class = clazz.implClass;
        try {
            final var isAbstract = $Class.isAbstract();
            final var isFinal = ($Class.mods().getValue() & FINAL) != 0;
            final var builderModifiers = PUBLIC | STATIC | (isAbstract ? ABSTRACT : NONE) | (isFinal ? FINAL : NONE);
            // 1/2: Create
            final var $Builder = $Class._class(builderModifiers, "Builder", ClassType.CLASS);
            // 2/2: Implement (with JavaDoc)
            $Builder.javadoc().append("<a href=\"https://github.com/informaticum/xjc\">Builder</a> for (enclosing) class ").append($Class).append(".");
            // (a) "toBuilder()" in XSDClass
            final var $toBuilder = $Class.method(builderModifiers & ~STATIC, $Builder, "toBuilder");
            $toBuilder.javadoc().addReturn().append("a new ").append($Builder).append(" instance with all properties initialised with the current values of {@code this} instance");
            // (b) "Builder()" in Builder
            final var $defaultConstructor = $Builder.constructor(PROTECTED);
            $defaultConstructor.javadoc().append("Default constructor.");
            // (c) "Builder(XSDClass blueprint)" in Builder
            final var $blueprintConstructor = $Builder.constructor(PROTECTED);
            $blueprintConstructor.javadoc().append("Blueprint constructor.");
            final var $blueprint = $blueprintConstructor.param(FINAL, $Class, "blueprint");
            $blueprintConstructor.javadoc().addParam($blueprint).append("the blueprint ").append($Class).append(" instance to get all initial values from");
            // (d) "build()" in Builder
            final var $build = $Builder.method(builderModifiers & ~STATIC, $Class, "build");
            $build.javadoc().addReturn().append("a new instance of ").append($Class);
            if (clazz.getSuperClass() != null) {
                $Builder._extends(this.generateBuilder(clazz.getSuperClass()));
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
                final var $builder = $Class.method(builderModifiers, $Builder, "builder");
                $builder.body()._return(_new($Builder));
                $builder.javadoc().addReturn().append("a new instance of the ").append($Builder).append(" class, corresponding to this ").append($Class).append(" clazz");
                // (a) "toBuilder()" in XSDClass
                $toBuilder.body()._return(_new($Builder).arg($this));
                // (d) "build()" in Builder
                final var $instantiation = _new($Class);
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
                final var $builderProperty = $Builder.field(PROTECTED, $blueprintProperty.type(), $blueprintProperty.name(), defaultValueFor(attribute, GENERATE_COLLECTIONINIT, /*TODO*/ GENERATE_COLLECTIONINIT).orElse($null));
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
            return stream($Class.listClasses()).filter(nested -> "Builder".equals(nested.name()))
                                               .findFirst()
                                               .orElseThrow(() -> new RuntimeException("Nested class 'Builder' already exists but cannot be found!", alreadyExists));
        }
    }

    private final void hideDefaultFactory(final ClassOutline clazz) {
        final var $ObjectFactory = clazz._package().objectFactory();
        final var $factory = getMethod($ObjectFactory, guessFactoryName(clazz));
        if ($factory == null) {
            //
        } else {
            LOG.info("Hide default factory [{}#{}()].", $ObjectFactory.fullName(), $factory.name());
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
    }

    private final void removeDefaultFactory(final ClassOutline clazz) {
        final var $objectFactory = clazz._package().objectFactory();
        final var $factory = getMethod($objectFactory, guessFactoryName(clazz));
        if ($factory == null) {
            //
        } else {
            LOG.info("Remove default factory [{}#{}()].", $objectFactory.fullName(), $factory.name());
            $objectFactory.methods().remove($factory);
            assertThat(getMethod($objectFactory, guessFactoryName(clazz))).isNull();
        }
    }

}
