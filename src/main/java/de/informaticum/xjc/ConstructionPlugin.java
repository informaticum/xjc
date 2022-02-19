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
import static de.informaticum.xjc.resources.AssignmentPluginMessages.ILLEGAL_VALUE;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.ALTERNATIVES_BEGIN;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.ALTERNATIVES_BUILDER;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.ALTERNATIVES_CONSTRUCTOR;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.ALTERNATIVES_END;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.ALTERNATIVES_FACTORY;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.ASSIGN_ALL_FIELDS;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.BASIC_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.BLUEPRINT_ARGUMENT;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.CONSTRUCTOR_INTRO;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.COPY_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.DEFAULT_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.GENERATE_CLONE_DESCRIPTION;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.HIDDEN_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.INJECT_SUPER_CONSTRUCTOR;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.INVALID_ARGUMENT_HANDLING;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.PROTECTED_CONSTRUCTOR_JAVADOC;
import static de.informaticum.xjc.resources.ConstructionPluginMessages.VALUES_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.util.CodeModelAnalysis.getMethod;
import static de.informaticum.xjc.util.CodeRetrofit.javadocAppendSection;
import static de.informaticum.xjc.util.OutlineAnalysis.filter;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructor;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.guessFactoryName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessWitherName;
import static de.informaticum.xjc.util.OutlineAnalysis.superAndGeneratedPropertiesOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.plugin.AssignmentPlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import de.informaticum.xjc.util.OutlineAnalysis;
import org.slf4j.Logger;

public class ConstructionPlugin
extends AssignmentPlugin {

    private static final Logger LOG = getLogger(ConstructionPlugin.class);

    private static final String clone = "clone";
    private static final String CLONE_SIGNATURE = format("#%s()", clone);

    private static final String OPTION_NAME = "informaticum-xjc-construction";

    private static final CommandLineArgument GENERATE_DEFAULT_CONSTRUCTOR = new CommandLineArgument("construction-default-constructor",      DEFAULT_CONSTRUCTOR_DESCRIPTION.text());
    private static final CommandLineArgument GENERATE_VALUES_CONSTRUCTOR  = new CommandLineArgument("construction-values-constructor",       VALUES_CONSTRUCTOR_DESCRIPTION.format(GENERATE_DEFAULT_CONSTRUCTOR));
    private static final CommandLineArgument GENERATE_BASIC_CONSTRUCTOR   = new CommandLineArgument("construction-basic-constructor",        BASIC_CONSTRUCTOR_DESCRIPTION.format(GENERATE_DEFAULT_CONSTRUCTOR));
    private static final CommandLineArgument GENERATE_COPY_CONSTRUCTOR    = new CommandLineArgument("construction-copy-constructor",         COPY_CONSTRUCTOR_DESCRIPTION.format(GENERATE_DEFAULT_CONSTRUCTOR, DEFENSIVE_COPIES));
    private static final CommandLineArgument HIDE_DEFAULT_CONSTRUCTOR     = new CommandLineArgument("construction-hide-default-constructor", HIDDEN_CONSTRUCTOR_DESCRIPTION.text());
    private static final CommandLineArgument GENERATE_CLONE               = new CommandLineArgument("construction-clone",                    GENERATE_CLONE_DESCRIPTION.format(CLONE_SIGNATURE));

    private static final CommandLineArgument GENERATE_BUILDER            = new CommandLineArgument("construction-builder",                  "Generate builder. Default: false");
    private static final CommandLineArgument HIDE_DEFAULT_FACTORIES      = new CommandLineArgument("construction-hide-default-factories",   "Hides default factory methods of object factories. Default: false");
    private static final CommandLineArgument REMOVE_DEFAULT_FACTORIES    = new CommandLineArgument("construction-remove-default-factories", "Removes default factory methods of object factories. Default: false");

    private static final String GENERATE_CONSTRUCTOR = "Generate {} constructor for [{}].";
    private static final String SKIP_CONSTRUCTOR = "Skip creation of {} constructor for [{}] because {}.";
    private static final String BECAUSE_CONSTRUCTOR_ALREADY_EXISTS = "such constructor (coincidentally?) already exists";
    private static final String HIDE_CONSTRUCTOR = "Hide {} constructor of [{}].";
    private static final String SKIP_HIDING_OF_MISSING = "Skip hiding of {} constructor for [{}] because such constructor does not exist.";
    private static final String SKIP_HIDING_OF_SIMILAR = "Skip hiding of {} constructor for [{}] because it is similar to the {} constructor.";
    private static final String ABORT_HIDING_OF_MISSING = "Abort hiding of {} constructor for [{}] because {}.";
    private static final String BECAUSE_NO_ALTERNATIVE_EXISTS = "there is no alternative to create an instance";
    private static final String ADD_INTERFACE = "Add [{}] interface extension for [{}].";
    private static final String SKIP_INTERFACE = "Skip [{}] interface extension for [{}] because interface is already implemented.";

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(NOTNULL_COLLECTIONS, DEFENSIVE_COPIES, UNMODIFIABLE_COLLECTIONS,
                      GENERATE_DEFAULT_CONSTRUCTOR, GENERATE_VALUES_CONSTRUCTOR, GENERATE_BASIC_CONSTRUCTOR, GENERATE_COPY_CONSTRUCTOR,
                      HIDE_DEFAULT_CONSTRUCTOR,
                      GENERATE_CLONE, 

                      GENERATE_BUILDER, HIDE_DEFAULT_FACTORIES, REMOVE_DEFAULT_FACTORIES);
    }

    @Override
    public final boolean prepareRun() {
        // Any explicit constructor removes the implicit default constructor.
        // Thus, {@link #GENERATE_DEFAULT_CONSTRUCTOR} must be activated too when creating all-values constructor.
        GENERATE_VALUES_CONSTRUCTOR.activates(GENERATE_DEFAULT_CONSTRUCTOR);
        // Similar, {@link #GENERATE_DEFAULT_CONSTRUCTOR} must be activated too when creating required-values constructor.
        GENERATE_BASIC_CONSTRUCTOR.activates(GENERATE_DEFAULT_CONSTRUCTOR);
        // Similar, {@link #GENERATE_DEFAULT_CONSTRUCTOR} must be activated too when creating copy constructor.
        GENERATE_COPY_CONSTRUCTOR.activates(GENERATE_DEFAULT_CONSTRUCTOR);
        GENERATE_CLONE.doOnActivation(() -> this.outline().getClasses().forEach(c -> this.addInterface(c, Cloneable.class)));
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        GENERATE_DEFAULT_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, "default", any -> false), clazz);
        GENERATE_VALUES_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, "all-values", any -> true), clazz);
        GENERATE_BASIC_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, "required-values", OutlineAnalysis::isRequired), clazz);
        GENERATE_COPY_CONSTRUCTOR.doOnActivation(this::generateCopyConstructor, clazz);
        HIDE_DEFAULT_CONSTRUCTOR.doOnActivation(this::hideDefaultConstructor, clazz);
        GENERATE_CLONE.doOnActivation(this::generateClone, clazz);

        GENERATE_BUILDER.doOnActivation(this::generateBuilder, clazz);
        HIDE_DEFAULT_FACTORIES.doOnActivation(this::hideDefaultFactory, clazz);
        REMOVE_DEFAULT_FACTORIES.doOnActivation(this::removeDefaultFactory, clazz);
        return true;
    }

    private final void generateConstructor(final ClassOutline clazz, final String label, final Predicate<? super FieldOutline> passedAsParameter) {
        // 1/3: Prepare
        if (getConstructor(clazz, filter(superAndGeneratedPropertiesOf(clazz), passedAsParameter)).isPresent()) {
            LOG.warn(SKIP_CONSTRUCTOR, label, fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_CONSTRUCTOR, label, fullNameOf(clazz));
        // 2/3: Create
        final var $Class = clazz.implClass;
        final var $constructor = $Class.constructor(PUBLIC);
        // 3/3: Implement (with JavaDoc)
        javadocAppendSection($constructor, CONSTRUCTOR_INTRO);
        if (clazz.getSuperClass() != null) {
            // Firstly, call super constructor (and pass through according parameters) ...
            $constructor.javadoc().add(INJECT_SUPER_CONSTRUCTOR.format(label));
            final var $super = $constructor.body().invoke("super");
            for (final var property : filter(superAndGeneratedPropertiesOf(clazz.getSuperClass()), passedAsParameter).entrySet()) {
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                accordingSuperAssignment(property, $constructor, $super, $parameter);
            }
        }
        final var fieldsWithParameter = filter(generatedPropertiesOf(clazz), passedAsParameter).entrySet();
        $constructor.javadoc().add(ASSIGN_ALL_FIELDS.text());
        if (!fieldsWithParameter.isEmpty()) {
            // ... Secondly, assign all declared fields with its according parameter ...
            javadocAppendSection($constructor, INVALID_ARGUMENT_HANDLING);
            $constructor.body().directStatement("// below fields are assigned with their according parameter");
            for (final var property : fieldsWithParameter) {
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                accordingAssignment(property, $constructor, $parameter);
            }
        }
        final var fieldsWithInitialisation = filter(generatedPropertiesOf(clazz), not(passedAsParameter)).entrySet();
        if (!fieldsWithInitialisation.isEmpty()) {
            // ... Thirdly, initialise all declared fields without an according parameter.
            $constructor.body().directStatement("// below fields are assigned with their according initial value");
            for (final var property : fieldsWithInitialisation) {
                accordingInitialisation(property, $constructor);
            }
        }
    }

    private final void generateCopyConstructor(final ClassOutline clazz) {
        // 1/3: Prepare
        if (getConstructor(clazz, clazz.implClass).isPresent()) {
            LOG.warn(SKIP_CONSTRUCTOR, "copy", fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return;
        }
        LOG.info(GENERATE_CONSTRUCTOR, "copy", fullNameOf(clazz));
        // 2/3: Create
        final var $Class = clazz.implClass;
        final var $constructor = $Class.constructor(PUBLIC);
        // 3/3: Implement (with JavaDoc)
        final var $blueprint = $constructor.param(FINAL, $Class, "blueprint");
        $constructor.javadoc().addParam($blueprint).append(BLUEPRINT_ARGUMENT.text());
        javadocAppendSection($constructor, CONSTRUCTOR_INTRO);
        if (clazz.getSuperClass() != null) {
            $constructor.javadoc().add(INJECT_SUPER_CONSTRUCTOR.format("copy"));
            $constructor.body().invoke("super").arg($blueprint);
        }
        $constructor.body()._if($blueprint.eq($null))._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required argument 'blueprint' must not be null!")));
        javadocAppendSection($constructor.javadoc().addThrows(IllegalArgumentException.class), ILLEGAL_VALUE);
        final var fields = generatedPropertiesOf(clazz).entrySet();
        $constructor.javadoc().add(ASSIGN_ALL_FIELDS.text());
        if (!fields.isEmpty()) {
            $constructor.body().directStatement("// below fields are assigned with their according blueprint value");
            for (final var property : fields) {
                final var $property = property.getValue();
                $constructor.body().assign($this.ref($property), cond($blueprint.ref($property).eq($null), $null, effectiveExpressionForNonNull($property.type(), $blueprint.ref($property))));
            }
        }
    }

    private final void hideDefaultConstructor(final ClassOutline clazz) {
        // 1/2: Prepare
        final var $constructorLookup = getConstructor(clazz);
        if (HIDE_DEFAULT_FACTORIES.or(REMOVE_DEFAULT_FACTORIES).getAsBoolean() && !GENERATE_VALUES_CONSTRUCTOR.and(GENERATE_BASIC_CONSTRUCTOR).and(GENERATE_BUILDER).getAsBoolean()) {
            LOG.error(ABORT_HIDING_OF_MISSING, "default", fullNameOf(clazz), BECAUSE_NO_ALTERNATIVE_EXISTS);
            return;
        } else if ($constructorLookup.isEmpty()) {
            LOG.warn(SKIP_HIDING_OF_MISSING, "default", fullNameOf(clazz));
            return;
        } else if (GENERATE_VALUES_CONSTRUCTOR.getAsBoolean() && superAndGeneratedPropertiesOf(clazz).isEmpty()) {
            LOG.warn(SKIP_HIDING_OF_SIMILAR, "default", fullNameOf(clazz), "all-values");
            return;
        } else if (GENERATE_BASIC_CONSTRUCTOR.getAsBoolean() && filter(superAndGeneratedPropertiesOf(clazz), OutlineAnalysis::isRequired).isEmpty()) {
            LOG.warn(SKIP_HIDING_OF_SIMILAR, "default", fullNameOf(clazz), "required-values");
            return;
        }
        LOG.info(HIDE_CONSTRUCTOR, "default", fullNameOf(clazz));
        // 2/2: Modify
        final var $constructor = $constructorLookup.get();
        javadocAppendSection($constructor, PROTECTED_CONSTRUCTOR_JAVADOC);
        javadocAppendSection($constructor, ALTERNATIVES_BEGIN);
        if (GENERATE_VALUES_CONSTRUCTOR.or(GENERATE_BASIC_CONSTRUCTOR).getAsBoolean()) { $constructor.javadoc().append(ALTERNATIVES_CONSTRUCTOR.text()); }
        if (GENERATE_BUILDER.getAsBoolean()                                          ) { $constructor.javadoc().append(ALTERNATIVES_BUILDER.format("Builder"));     }
        if (!HIDE_DEFAULT_FACTORIES.or(REMOVE_DEFAULT_FACTORIES).getAsBoolean()      ) { $constructor.javadoc().append(ALTERNATIVES_FACTORY.text());     }
        $constructor.javadoc().append(ALTERNATIVES_END.text());
        $constructor.mods().setProtected();
    }

    private final void addInterface(final ClassOutline clazz, final Class<?> interfaceClass) {
        // 1/2: Prepare
        assertThat(interfaceClass).isInterface();
        if (this.reference(interfaceClass).isAssignableFrom(clazz.implClass)) {
            LOG.warn(SKIP_INTERFACE, interfaceClass, fullNameOf(clazz));
            return;
        }
        LOG.info(ADD_INTERFACE, interfaceClass, fullNameOf(clazz));
        // 2/2: Implement
        final var $Class = clazz.implClass;
        $Class._implements(interfaceClass);
    }

    private final void generateClone(final ClassOutline clazz) {
        // 1/4: Prepare
        if (getMethod(clazz, clone).isPresent()) {
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
            // case (a): there is no JAXB-bound super class -- incorporate the CloneNotSupportedException
            final var $try = $clone.body()._try();
            $body = $try.body();
            final var $catch = $try._catch(this.reference(CloneNotSupportedException.class));
            final var $bug = $catch.param("bug");
            $bug.mods().setFinal(true);
            $catch.body()._throw(_new(this.reference(RuntimeException.class)).arg("Seriously?!? Super's #clone() failed, although this class implements the Cloneable interface.").arg($bug));
        } else {
            // case (b): there is a JAXB-bound super class -- no need to handle a CloneNotSupportedException
            $body = $clone.body();
        }
        final var $instance = $body.decl(FINAL, $Class, "cloneInstance", cast($Class, $super.invoke(clone)));
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var $property = property.getValue();
            $body.assign($instance.ref($property), cond($this.ref($property).eq($null), $null, effectiveExpressionForNonNull($property.type(), $this.ref($property))));
        }
        $body._return($instance);
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
            assertThat($Class.name()).withFailMessage("The nested type '{}' cannot hide an enclosing type {}!", "Builder", $Class.fullName()).isNotEqualTo("Builder");
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
                final var $builderProperty = $Builder.field(PROTECTED, $blueprintProperty.type(), $blueprintProperty.name(), defaultExpressionFor(attribute).orElse($null));
                accordingAssignment(blueprintProperty, $blueprintConstructor, $blueprint.ref($blueprintProperty));
                final var $wither = $Builder.method(PUBLIC | (isFinal ? FINAL : NONE), $Builder, guessWitherName(attribute));
                final var $parameter = $wither.param(FINAL, $builderProperty.type(), $builderProperty.name());
                accordingAssignment(blueprintProperty, $wither, $parameter);
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
        final var $factoryLookup = getMethod($ObjectFactory, guessFactoryName(clazz));
        if ($factoryLookup.isPresent()) {
            final var $factory = $factoryLookup.get();
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
        final var $ObjectFactory = clazz._package().objectFactory();
        final var $factoryLookup = getMethod($ObjectFactory, guessFactoryName(clazz));
        if ($factoryLookup.isPresent()) {
            final var $factory = $factoryLookup.get();
            LOG.info("Remove default factory [{}#{}()].", $ObjectFactory.fullName(), $factory.name());
            $ObjectFactory.methods().remove($factory);
            assertThat(getMethod($ObjectFactory, guessFactoryName(clazz))).isNull();
        }
    }

}
