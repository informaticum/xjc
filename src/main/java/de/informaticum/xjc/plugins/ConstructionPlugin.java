package de.informaticum.xjc.plugins;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.ABSTRACT;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PROTECTED;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;
import static com.sun.codemodel.JOp.cond;
import static com.sun.codemodel.JOp.not;
import static de.informaticum.xjc.plugins.BoilerplatePlugin.BECAUSE_METHOD_ALREADY_EXISTS;
import static de.informaticum.xjc.plugins.BoilerplatePlugin.GENERATE_METHOD;
import static de.informaticum.xjc.plugins.BoilerplatePlugin.SKIP_METHOD;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ADDER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ADDER_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ADDER_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ADDER_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ADDITIONAL_WITHER_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ALTERNATIVE_BUILDER;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ALTERNATIVE_CONSTRUCTORS;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ALTERNATIVE_INSTANTIATION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BASIC_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BLUEPRINT_ARGUMENT;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_ADDER_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_ADDER_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_BLUEPRINT_CONSTRUCTOR;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_BLUEPRINT_PARAM;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_BUILD_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_BUILD_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_BUILD_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_BUILD_RELAY_THROWS;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_BUILD_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_DEFAULT_CONSTRUCTOR;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_METHOD_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_METHOD_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_METHOD_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_REMOVER_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_REMOVER_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_WITHER_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.BUILDER_WITHER_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.CLONE_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.CLONE_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.CONSTRUCTOR_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.CONSTRUCTOR_JAVADOC_END;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.CONSTRUCTOR_JAVADOC_SUPER_CLASS;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.COPY_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.DEFAULT_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.FACTORY_WITHER_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.HIDDEN_FACTORIES_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.ILLEGAL_BLUEPRINT;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.IMPLEMENTS_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.PRIVATE_FACTORY_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.PROTECTED_CONSTRUCTOR_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.PROTECTED_DEFAULT_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.REMOVER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.REMOVER_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.REMOVER_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.REMOVER_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.REMOVE_FACTORIES_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.TOBUILDER_METHOD_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.TOBUILDER_METHOD_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.TOBUILDER_METHOD_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.TOBUILDER_METHOD_RETURN;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.VALUES_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.WITHER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.WITHER_IMPLNOTE;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.WITHER_JAVADOC;
import static de.informaticum.xjc.plugins.ConstructionPluginMessages.WITHER_RETURN;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.$super;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.cloneExpressionFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.copyFactoryFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.getConstructor;
import static de.informaticum.xjc.util.CodeModelAnalysis.getMethod;
import static de.informaticum.xjc.util.CodeModelAnalysis.typeParameterOf;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static de.informaticum.xjc.util.CodeRetrofit.relayThrows;
import static de.informaticum.xjc.util.OutlineAnalysis.filter;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructor;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.guessAdderName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessFactoryName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessRemoverName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessWithAdditionalName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessWitherName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessWithoutSpecificName;
import static de.informaticum.xjc.util.OutlineAnalysis.superAndGeneratedPropertiesOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.api.AssignmentPlugin;
import de.informaticum.xjc.api.CommandLineArgument;
import de.informaticum.xjc.util.OutlineAnalysis;
import org.slf4j.Logger;

public class ConstructionPlugin
extends AssignmentPlugin {

    private static final Logger LOG = getLogger(ConstructionPlugin.class);
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
    private static final String GENERATE_NESTED_BUILDER = "Generate nested builder class for [{}].";
    private static final String MODIFY_FACTORY = "Set {} of factory method [{}#{}(...)] onto [{}].";
    private static final String REMOVE_FACTORY = "Remove factory method [{}#{}(...)].";

    private static final String MISSING_ALLVALUES_CONSTRUCTOR = "Some of the code generated for the enclosing Type (i.e., the 'Type#Type(...)' constructor) is missing!";
    private static final String MISSING_TOBUILDER_FACTORY = "Some of the code generated for the embedded Builder (i.e., the '#toBuilder()' method) is missing!";
    private static final String MISSING_BUILDER_WITH_METHOD = "Some of the code generated for the embedded Builder (i.e., the 'Builder#withXyz(T)' method) is missing!";
    private static final String MISSING_BUILDER_BUILD_METHOD = "Some of the code generated for the embedded Builder (i.e., the 'Builder#build()' method) is missing!";

    private static final String clone = "clone";
    private static final String CLONE_SIGNATURE = format("#%s()", clone);
    private static final String builder = "builder";
    private static final String BUILDER_METHOD_SIGNATURE = format("#%s()", builder);
    private static final String toBuilder = "toBuilder";
    private static final String TOBUILDER_METHOD_SIGNATURE = format("#%s()", toBuilder);
    private static final String build = "build";
    private static final String BUILD_SIGNATURE = format("#%s()", build);

    private static final String OPTION_NAME = "informaticum-xjc-construction";
    private static final CommandLineArgument GENERATE_DEFAULT_CONSTRUCTOR  = new CommandLineArgument("construction-default-constructor",      DEFAULT_CONSTRUCTOR_DESCRIPTION.text());
    private static final CommandLineArgument GENERATE_VALUES_CONSTRUCTOR   = new CommandLineArgument("construction-values-constructor",       VALUES_CONSTRUCTOR_DESCRIPTION.format(GENERATE_DEFAULT_CONSTRUCTOR));
    private static final CommandLineArgument GENERATE_BASIC_CONSTRUCTOR    = new CommandLineArgument("construction-basic-constructor",        BASIC_CONSTRUCTOR_DESCRIPTION.format(GENERATE_DEFAULT_CONSTRUCTOR));
    private static final CommandLineArgument GENERATE_COPY_CONSTRUCTOR     = new CommandLineArgument("construction-copy-constructor",         COPY_CONSTRUCTOR_DESCRIPTION.format(GENERATE_DEFAULT_CONSTRUCTOR));
    private static final CommandLineArgument PROTECTED_DEFAULT_CONSTRUCTOR = new CommandLineArgument("construction-hide-default-constructor", PROTECTED_DEFAULT_CONSTRUCTOR_DESCRIPTION.format(GENERATE_DEFAULT_CONSTRUCTOR));
    private static final CommandLineArgument GENERATE_CLONE                = new CommandLineArgument("construction-clone",                    CLONE_DESCRIPTION.format(CLONE_SIGNATURE));
    private static final CommandLineArgument GENERATE_BUILDER              = new CommandLineArgument("construction-builder",                  BUILDER_DESCRIPTION.format(GENERATE_VALUES_CONSTRUCTOR));
    private static final CommandLineArgument GENERATE_FACTORY_WITHER       = new CommandLineArgument("construction-factory-withers",          FACTORY_WITHER_DESCRIPTION.format(GENERATE_BUILDER));
    private static final CommandLineArgument GENERATE_ADDITIONAL_WITHER    = new CommandLineArgument("construction-additional-withers",       ADDITIONAL_WITHER_DESCRIPTION.format(GENERATE_BUILDER, GENERATE_FACTORY_WITHER));
    private static final CommandLineArgument HIDE_DEFAULT_FACTORIES        = new CommandLineArgument("construction-hide-default-factories",   HIDDEN_FACTORIES_DESCRIPTION.text());
    private static final CommandLineArgument REMOVE_DEFAULT_FACTORIES      = new CommandLineArgument("construction-remove-default-factories", REMOVE_FACTORIES_DESCRIPTION.format(HIDE_DEFAULT_FACTORIES));

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        final var args = asList(GENERATE_DEFAULT_CONSTRUCTOR, PROTECTED_DEFAULT_CONSTRUCTOR,           // default constructor options
                                GENERATE_VALUES_CONSTRUCTOR, GENERATE_BASIC_CONSTRUCTOR,               // value constructor options
                                GENERATE_COPY_CONSTRUCTOR,                                             // copy constructor options
                                GENERATE_CLONE,                                                        // clone options
                                GENERATE_BUILDER, GENERATE_FACTORY_WITHER, GENERATE_ADDITIONAL_WITHER, // builder/factory method options
                                HIDE_DEFAULT_FACTORIES, REMOVE_DEFAULT_FACTORIES);                     // ObjectFactory options
        return concat(super.getPluginArguments().stream(), args.stream()).collect(toList());
    }

    @Override
    public final boolean prepareRun() {
        // Factory wither methods require embedded Builder.
        GENERATE_FACTORY_WITHER.activates(GENERATE_BUILDER);
        // Builders refer to the according all-value constructor, so {@link #GENERATE_VALUES_CONSTRUCTOR} must be
        // activated.
        GENERATE_BUILDER.activates(GENERATE_VALUES_CONSTRUCTOR);
        // Further, any explicit constructor negates the implicit default constructor. Thus,
        // {@link #GENERATE_DEFAULT_CONSTRUCTOR} must be activated when creating all-values constructor, the
        // required-values (a.k.a. basic) constructor, or the copy constructor. Similar, the default constructor must
        // be created if that constructor shall be limited to 'protected' access.
        GENERATE_VALUES_CONSTRUCTOR.activates(GENERATE_DEFAULT_CONSTRUCTOR);
        GENERATE_BASIC_CONSTRUCTOR.activates(GENERATE_DEFAULT_CONSTRUCTOR);
        GENERATE_COPY_CONSTRUCTOR.activates(GENERATE_DEFAULT_CONSTRUCTOR);
        PROTECTED_DEFAULT_CONSTRUCTOR.activates(GENERATE_DEFAULT_CONSTRUCTOR);
        // To generate #clone() methods correctly, all potential cloneable classes must implement Cloneable interface.
        GENERATE_CLONE.doOnActivation(() -> this.outline().getClasses().forEach(c -> this.addInterface(c, Cloneable.class)));
        // What about final classes in general? Unfortunately, in a multi-XJC-run maven build some XJC runs may generate final
        // classes but other runs require non-final ;-(
        //   Collection<? extends ClassOutline> classes = new ArrayList<>(this.outline().getClasses());
        //   classes.forEach(c -> { if (!c.implClass.isAbstract()) { c.implClass.mods().setFinal(true); }});
        //   classes.forEach(c -> { if (c.getSuperClass() != null) { c.getSuperClass().implClass.mods().setFinal(false); }});
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        GENERATE_DEFAULT_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, "default", any -> false), clazz);
        GENERATE_VALUES_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, "all-values", any -> true), clazz);
        GENERATE_BASIC_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, "required-values", OutlineAnalysis::isRequired), clazz);
        GENERATE_COPY_CONSTRUCTOR.doOnActivation(this::generateCopyConstructor, clazz);
        PROTECTED_DEFAULT_CONSTRUCTOR.doOnActivation(this::hideDefaultConstructor, clazz);
        GENERATE_CLONE.doOnActivation(this::generateClone, clazz);
        GENERATE_BUILDER.doOnActivation(this::generateBuilder, clazz);
        GENERATE_FACTORY_WITHER.doOnActivation(this::generateWithers, clazz);
        HIDE_DEFAULT_FACTORIES.doOnActivation(this::hideDefaultFactory, clazz);
        REMOVE_DEFAULT_FACTORIES.doOnActivation(this::removeDefaultFactory, clazz);
        return true;
    }

    private final void generateConstructor(final ClassOutline clazz, final String label, final Predicate<? super FieldOutline> passedAsParameter) {
        // 0/2: Preliminary
        if (getConstructor(clazz, filter(superAndGeneratedPropertiesOf(clazz), passedAsParameter)).isPresent()) {
            LOG.warn(SKIP_CONSTRUCTOR, label, fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return;
        }
        // 1/2: Create
        LOG.info(GENERATE_CONSTRUCTOR, label, fullNameOf(clazz));
        final var $Class = clazz.implClass;
        final var superClass = Optional.ofNullable(clazz.getSuperClass());
        final var $constructor = $Class.constructor(PUBLIC);
        javadocSection($constructor).append(CONSTRUCTOR_JAVADOC_BEGIN.text());
        superClass.ifPresent(sc -> $constructor.javadoc().append(CONSTRUCTOR_JAVADOC_SUPER_CLASS.format(label)));
        $constructor.javadoc().append(CONSTRUCTOR_JAVADOC_END.text());
        // 2/2: Implement
        // (A) Firstly, call super constructor (and pass through according parameters):
        superClass.ifPresent(sc -> {
            final var fieldsViaSuperConstructor = filter(superAndGeneratedPropertiesOf(sc), passedAsParameter);
            if (!fieldsViaSuperConstructor.isEmpty()) {
                $constructor.body().directStatement("// below fields are assigned via super constructor");
            }
            final var $super = $constructor.body().invoke("super");
            for (final var property : fieldsViaSuperConstructor.entrySet()) {
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                accordingSuperAssignmentAndJavadoc(property, $constructor, $super, $parameter);
            }
        });
        // (B) Secondly, assign all declared fields with its according parameter:
        final var fieldsWithParameter = filter(generatedPropertiesOf(clazz), passedAsParameter).entrySet();
        if (!fieldsWithParameter.isEmpty()) {
            $constructor.body().directStatement("// below fields are assigned with their according parameter");
            for (final var property : fieldsWithParameter) {
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                accordingAssignmentAndJavadoc(property, $constructor, $parameter);
            }
        }
        // (C) Thirdly, initialise all declared fields without an according parameter:
        final var fieldsWithInitialisation = filter(generatedPropertiesOf(clazz), not(passedAsParameter));
        if (!fieldsWithInitialisation.isEmpty()) {
            $constructor.body().directStatement("// below fields are assigned with their according initial value");
            accordingInitialisationAndJavadoc(fieldsWithInitialisation, $constructor);
        }
    }

    private final void generateCopyConstructor(final ClassOutline clazz) {
        // 0/2: Preliminary
        if (getConstructor(clazz, clazz.implClass).isPresent()) {
            LOG.warn(SKIP_CONSTRUCTOR, "copy", fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return;
        }
        // 1/2: Create
        LOG.info(GENERATE_CONSTRUCTOR, "copy", fullNameOf(clazz));
        final var $Class = clazz.implClass;
        final var superClass = Optional.ofNullable(clazz.getSuperClass());
        final var $constructor = $Class.constructor(PUBLIC);
        javadocSection($constructor).append(CONSTRUCTOR_JAVADOC_BEGIN.text());
        superClass.ifPresent(sc -> $constructor.javadoc().append(CONSTRUCTOR_JAVADOC_SUPER_CLASS.format("copy")));
        $constructor.javadoc().append(CONSTRUCTOR_JAVADOC_END.text());
        // 2/2: Implement (with according Javadoc)
        final var $blueprint = $constructor.param(FINAL, $Class, "blueprint");
        javadocSection($constructor.javadoc().addParam($blueprint)).append(BLUEPRINT_ARGUMENT.text());
        // (A) Firstly, call super constructor:
        superClass.ifPresent(sc -> $constructor.body().invoke("super").arg($blueprint));
        $constructor.body()._if($blueprint.eq($null))._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required argument 'blueprint' must not be null!")));
        javadocSection($constructor.javadoc().addThrows(IllegalArgumentException.class)).append(ILLEGAL_BLUEPRINT.text());
        // (B) Secondly, initialise all declared fields:
        final var fields = generatedPropertiesOf(clazz).entrySet();
        if (!fields.isEmpty()) {
            $constructor.body().directStatement("// below fields are assigned with their according blueprint value");
            for (final var property : fields) {
                final var $property = property.getValue();
                final Optional<JExpression> $noDefault = empty(/* no default value (to be in sync with blueprint instance) */);
                final var $nonNull = effectiveExpressionForNonNull($property.type(), $blueprint.ref($property));
                accordingAssignment(property, $constructor, $blueprint.ref($property), $noDefault, $nonNull);
            }
        }
    }

    private final void hideDefaultConstructor(final ClassOutline clazz) {
        // 0/1: Preliminary
        final var $ImplClass = clazz.implClass;
        final var $constructorLookup = getConstructor(clazz);
        final var anyOtherConstructorExists = stream(spliteratorUnknownSize($ImplClass.constructors(), 0), false).anyMatch(c -> c.params().size() > 0);
        final var builderExists = GENERATE_BUILDER.getAsBoolean();
        if ($constructorLookup.isEmpty()) {
            LOG.warn(SKIP_HIDING_OF_MISSING, "default", fullNameOf(clazz));
            return;
        } else if (GENERATE_VALUES_CONSTRUCTOR.getAsBoolean() && superAndGeneratedPropertiesOf(clazz).isEmpty()) {
            LOG.warn(SKIP_HIDING_OF_SIMILAR, "default", fullNameOf(clazz), "all-values");
            return;
        } else if (GENERATE_BASIC_CONSTRUCTOR.getAsBoolean() && filter(superAndGeneratedPropertiesOf(clazz), OutlineAnalysis::isRequired).isEmpty()) {
            LOG.warn(SKIP_HIDING_OF_SIMILAR, "default", fullNameOf(clazz), "required-values");
            return;
        } else if (!anyOtherConstructorExists && !builderExists) {
            LOG.error(ABORT_HIDING_OF_MISSING, "default", fullNameOf(clazz), BECAUSE_NO_ALTERNATIVE_EXISTS);
            return;
        }
        // 1/1: Modify
        assertThat($constructorLookup).isPresent();
        LOG.info(HIDE_CONSTRUCTOR, "default", fullNameOf(clazz));
        final var $constructor = $constructorLookup.get();
        if (anyOtherConstructorExists && builderExists) {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text()).append(ALTERNATIVE_INSTANTIATION.format($ImplClass.name(), BUILDER_NAME.apply($ImplClass)));
        } else if (anyOtherConstructorExists) {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text()).append(ALTERNATIVE_CONSTRUCTORS.text());
        } else if (builderExists) {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text()).append(ALTERNATIVE_BUILDER.format($ImplClass.name(), BUILDER_NAME.apply($ImplClass)));
        } else {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text());
        }
        $constructor.mods().setProtected();
    }

    private final void addInterface(final ClassOutline clazz, final Class<?> interfaceClass) {
        // 0/1: Preliminary
        final var $Class = clazz.implClass;
        assertThat(interfaceClass).isInterface();
        if (this.reference(interfaceClass).isAssignableFrom($Class)) {
            LOG.warn(SKIP_INTERFACE, interfaceClass, fullNameOf(clazz));
            return;
        }
        // 1/1: Modify
        LOG.info(ADD_INTERFACE, interfaceClass, fullNameOf(clazz));
        javadocSection($Class).append(IMPLEMENTS_IMPLNOTE.text());
        $Class._implements(interfaceClass);
    }

    private final void generateClone(final ClassOutline clazz) {
        // 0/2: Preliminary
        if (getMethod(clazz, clone).isPresent()) {
            LOG.warn(SKIP_METHOD, CLONE_SIGNATURE, fullNameOf(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
            return;
        }
        // 1/2: Create
        LOG.info(GENERATE_METHOD, fullNameOf(clazz), CLONE_SIGNATURE);
        final var $Class = clazz.implClass;
        assertThat(this.reference(Cloneable.class).isAssignableFrom($Class)).isTrue();
        final var $clone = $Class.method(PUBLIC, $Class, clone);
        $clone.annotate(Override.class);
        javadocSection($clone).append(CLONE_IMPLNOTE.text()); // No further method/@param Javadoc; will be inherited instead
        // 2/2: Implement
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
        final var $replica = $body.decl(FINAL, $Class, "replica", cast($Class, $super.invoke(clone)));
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            final var $cloneExpression = cloneExpressionFor($property.type(), $this.ref($property), UNMODIFIABLE_COLLECTIONS.getAsBoolean());
            $body.assign($replica.ref($property), cond($this.ref($property).eq($null), $null, $cloneExpression.orElse($this.ref($property))));
        }
        $body._return($replica);
    }

    /**
     * Different names are essential, because {@code nested type XYZ cannot hide an enclosing type XYZ}.
     */
    private static final Function<JDefinedClass, String> BUILDER_NAME = $c -> ("Builder".equals($c.name()) ? $c.name() : "") + "Builder";

    private final JDefinedClass generateBuilder(final ClassOutline clazz) {
        final var $ImplClass = clazz.implClass;
        final var builderClassName = BUILDER_NAME.apply($ImplClass);
        // 0/5: Preliminary
        final var alreadyGeneratedBuilder = stream(spliteratorUnknownSize($ImplClass.classes(), 0), false).filter(nested -> builderClassName.equals(nested.name())).findFirst();
        if (alreadyGeneratedBuilder.isPresent()) {
            // sub-classes query for super-class' Builder
            return alreadyGeneratedBuilder.get();
        }
        // 1/5: Create Builder, Constructors, and Factory methods
        final var modifiers = $ImplClass.mods().getValue() & ~STATIC; // exclude "static" modifier (happens for nested types)
        // (A.1) Generate embedded Builder [XyzClass.Builder] ...
        LOG.info(GENERATE_NESTED_BUILDER, fullNameOf(clazz));
        final var $Builder = this.outline().getClassFactory().createClass($ImplClass, modifiers, builderClassName, clazz.target.getLocator());
        javadocSection($Builder).append(BUILDER_JAVADOC.format($ImplClass.name()));
        javadocSection($Builder).append(BUILDER_IMPLNOTE.text());
        // ... (A.2) including the default constructor [XyzClass.Builder#Builder()] ...
        final var $builderDefaultConstructor = $Builder.constructor(PROTECTED /* keep protected, programmer should call Xyz#builder() instead */);
        javadocSection($builderDefaultConstructor).append(BUILDER_DEFAULT_CONSTRUCTOR.text());
        // ... (A.3) including the blueprint constructor [XyzClass.Builder#Builder(XyzClass)] ...
        final var $builderBlueprintConstructor = $Builder.constructor(PROTECTED/* keep protected, programmer must call Xyz#toBuilder() instead */);
        final var $blueprintParam = $builderBlueprintConstructor.param(FINAL, $ImplClass, "blueprint");
        javadocSection($builderBlueprintConstructor).append(BUILDER_BLUEPRINT_CONSTRUCTOR.format($ImplClass.name(), $blueprintParam.name()));
        javadocSection($builderBlueprintConstructor.javadoc().addParam($blueprintParam)).append(BUILDER_BLUEPRINT_PARAM.format($ImplClass.name()));
        // ... (A.4) including the #build() method [XyzClass.Builder#build()] ...
        LOG.info(GENERATE_METHOD, $Builder.fullName(), BUILD_SIGNATURE);
        final var $build = $Builder.method(modifiers, $ImplClass, build);
        javadocSection($build).append(BUILDER_BUILD_JAVADOC.format($ImplClass.name()));
        javadocSection($build.javadoc().addReturn()).append(BUILDER_BUILD_RETURN.format($ImplClass.name()));
        // (B.1) Generate #toBuilder() method [XyzClass#toBuilder()]
        LOG.info(GENERATE_METHOD, fullNameOf(clazz), TOBUILDER_METHOD_SIGNATURE);
        final var $toBuilder = $ImplClass.method(modifiers, $Builder, toBuilder);
        javadocSection($toBuilder).append(TOBUILDER_METHOD_JAVADOC.format($ImplClass.name(), $Builder.name()));
        javadocSection($toBuilder.javadoc().addReturn()).append(TOBUILDER_METHOD_RETURN.format($ImplClass.name(), $Builder.name()));
        // 2/5: Follow class hierarchy
        if (clazz.getSuperClass() != null) {
            // (A.1++) Embedded Builder has super class
            $Builder._extends(this.generateBuilder(clazz.getSuperClass()));
            // (A.2++) Embedded Builder's default constructor calls super constructor
            $builderDefaultConstructor.body().invoke("super");
            // (A.3++) Embedded Builder's blueprint constructor calls super constructor
            $builderBlueprintConstructor.body().invoke("super").arg($blueprintParam);
            // (A.4++) Embedded Builder's #build() method overrides super method
            $build.annotate(Override.class);
            // (B.1++) ImplClass's #toBuilder() method overrides super method
            $toBuilder.annotate(Override.class);
        }
        // 3/5: Either document or implement relevant builder methods
        if ($Builder.isAbstract()) {
            // (A.1++) Document embedded Builder
            javadocSection($Builder).append(BUILDER_ABSTRACT_IMPLNOTE.format($ImplClass.name()));
            // (A.4++) Document embedded Builder's #build() method
            javadocSection($build).append(BUILDER_BUILD_ABSTRACT_IMPLNOTE.format($ImplClass.name()));
            // (B.1++) Document ImplClass's #toBuilder() method
            javadocSection($toBuilder).append(TOBUILDER_METHOD_ABSTRACT_IMPLNOTE.text());
        } else {
            // (A.4++) Implement embedded Builder's #build() method
            javadocSection($build).append(BUILDER_BUILD_IMPLNOTE.format($ImplClass.name()));
            final var $properties = superAndGeneratedPropertiesOf(clazz).values();
            final var $allValuesConstructor = getConstructor($ImplClass, $properties.stream().map(JFieldVar::type).toArray(JType[]::new)).orElseThrow(() -> new IllegalStateException(MISSING_ALLVALUES_CONSTRUCTOR));
            final var $instantiation = $properties.stream().map($p -> $this.ref($p)).reduce(_new($ImplClass), JInvocation::arg, JInvocation::arg);
            $build.body()._return($instantiation);
            relayThrows($allValuesConstructor, $build, BUILDER_BUILD_RELAY_THROWS.text());
            // (B.1++) ImplClass's #toBuilder() method returns new Builder instance
            javadocSection($toBuilder).append(TOBUILDER_METHOD_IMPLNOTE.format($ImplClass.name(), $Builder.name()));
            $toBuilder.body()._return(_new($Builder).arg($this));
            // (C.1) Generate/Implement #builder() method [XyzClass#builder()]
            LOG.info(GENERATE_METHOD, fullNameOf(clazz), BUILDER_METHOD_SIGNATURE);
            final var $builder = $ImplClass.method(modifiers | STATIC, $Builder, builder);
            javadocSection($builder).append(BUILDER_METHOD_JAVADOC.format($ImplClass.name(), $Builder.name()));
            javadocSection($builder).append(BUILDER_METHOD_IMPLNOTE.format($ImplClass.name(), $Builder.name()));
            javadocSection($builder.javadoc().addReturn()).append(BUILDER_METHOD_RETURN.format($ImplClass.name(), $Builder.name()));
            $builder.body()._return(_new($Builder));
        }
        // 4/5: Handle declared fields
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            // (A.1++) Generate field into embedded Builder (must be 'protected' to be accessible by sub-builders)
            final var $builderProperty = $Builder.field(PROTECTED, $property.type(), $property.name(), defaultExpressionFor(attribute).orElse($null));
            // (A.2++) TODO
            // TODO: default value for all fields of this default builder? or just leave empty and everything is null'ed?
            // (A.3++) TODO Doc
            // TODO: default value for null fields?
            accordingAssignment(property, $builderBlueprintConstructor, $blueprintParam.ref($property), Optional.empty(), cloneExpressionFor(property.getValue().type(), $blueprintParam.ref($property), false).orElse($blueprintParam.ref($property)));
            // (D.1) Generate Builder's "wither"-method for declared property [XyzClass.Builder#withAbc(AbcType)]
            final var $wither = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessWitherName(attribute));
            javadocSection($wither).append(BUILDER_WITHER_JAVADOC.format($property.name()));
            javadocSection($wither.javadoc().addReturn()).append(BUILDER_WITHER_RETURN.text());
            final var $parameter = $wither.param(FINAL, $builderProperty.type(), $builderProperty.name());
            final var NO_DEFAULT_VALUE = Optional.<JExpression>empty(); // no default value to prevent origin-vs-builder-divergence
            final var NO_IMMUTABLE_VIEW = false; // no immutable view to allow subsequent modification
            accordingAssignment(property, $wither, $parameter, NO_DEFAULT_VALUE, cloneExpressionFor(property.getValue().type(), $parameter, NO_IMMUTABLE_VIEW).orElse($parameter));
            accordingAssignmentJavadoc(property, $wither);
            $wither.body()._return($this);
            if (GENERATE_ADDITIONAL_WITHER.getAsBoolean() && attribute.getPropertyInfo().isCollection()) {
                // (E.1) Generate Builder's "adder"-method for declared Collection<T> property [XyzClass.Builder#addAbc(T)]
                final var $adder = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessAdderName(attribute));
                javadocSection($adder).append(BUILDER_ADDER_JAVADOC.format($property.name()));
                javadocSection($adder.javadoc().addReturn()).append(BUILDER_ADDER_RETURN.text());
                final var $adderParam = $adder.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                $adder.body()._if($this.ref($property).eq($null))._then().assign($this.ref($property), copyFactoryFor($property.type()));
                $adder.body().add($this.ref($property).invoke("add").arg($adderParam));
                $adder.body()._return($this);
                // (E.2) Generate Builder's "remover"-method for declared Collection<T> property [XyzClass.Builder#removeAbc(T)]
                final var $remover = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessRemoverName(attribute));
                javadocSection($remover).append(BUILDER_REMOVER_JAVADOC.format($property.name()));
                javadocSection($remover.javadoc().addReturn()).append(BUILDER_REMOVER_RETURN.text());
                final var $removerParam = $remover.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                $remover.body()._if(not($this.ref($property).eq($null)))._then().add($this.ref($property).invoke("remove").arg($removerParam));
                $remover.body()._return($this);
            }
        }
        // 5/5: Handle inherited fields
        for (final var property : superAndGeneratedPropertiesOf(clazz.getSuperClass()).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            // (D.2) Override Builder's "wither"-method for each inherited property [XyzClass.Builder#withAbc(AbcType)]
            final var $wither = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessWitherName(attribute));
            $wither.annotate(Override.class); // also inherits method's and @param's Javadoc
            final var $parameter = $wither.param(FINAL, $property.type(), $property.name());
            $wither.body().invoke($super, $wither).arg($parameter);
            $wither.body()._return($this);
            if (GENERATE_ADDITIONAL_WITHER.getAsBoolean() && attribute.getPropertyInfo().isCollection()) {
                // (E.1) Override Builder's "adder"-method for each inherited Collection<T> property [XyzClass.Builder#addAbc(T)]
                final var $adder = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessAdderName(attribute));
                $adder.annotate(Override.class); // also inherits method's and @param's Javadoc
                final var $adderParam = $adder.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                $adder.body().invoke($super, $adder).arg($adderParam);
                $adder.body()._return($this);
                // (E.2) Override Builder's "remover"-method for each inherited Collection<T> property [XyzClass.Builder#removeAbc(T)]
                final var $remover = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessRemoverName(attribute));
                $remover.annotate(Override.class); // also inherits method's and @param's Javadoc
                final var $removerParam = $remover.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                $remover.body().invoke($super, $remover).arg($removerParam);
                $remover.body()._return($this);
            }
        }
        return $Builder;
    }

    private final void generateWithers(final ClassOutline clazz) {
        final var $ImplClass = clazz.implClass;
        final var modifiers = $ImplClass.mods().getValue() & ~STATIC; // exclude "static" modifier (happens for nested types)
        final var $Builder = this.generateBuilder(clazz); // will immediately return because Builder already exists
        final var $implClassToBuilder = getMethod($ImplClass, toBuilder).orElseThrow(() -> new IllegalStateException(MISSING_TOBUILDER_FACTORY));
        final var inheritedProperties = superAndGeneratedPropertiesOf(clazz.getSuperClass());
        for (final var property : superAndGeneratedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            // (A) generate/implement "wither" factory method
            final var $wither = $ImplClass.method(modifiers, $ImplClass, guessWitherName(attribute));
            if (inheritedProperties.containsKey(attribute)) {
                $wither.annotate(Override.class);
            }
            javadocSection($wither).append(WITHER_JAVADOC.format($property.name()));
            javadocSection($wither.javadoc().addReturn()).append(WITHER_RETURN.format($property.name()));
            final var $parameter = $wither.param(FINAL, $property.type(), $property.name());
            accordingAssignmentJavadoc(property, $wither);
            if ($wither.mods().isAbstract()) {
                javadocSection($wither).append(WITHER_ABSTRACT_IMPLNOTE.text());
            } else {
                final var $builderWither = getMethod($Builder, guessWitherName(attribute), $property.type()).orElseThrow(() -> new IllegalStateException(MISSING_BUILDER_WITH_METHOD));
                final var $builderBuild = getMethod($Builder, build).orElseThrow(() -> new IllegalStateException(MISSING_BUILDER_BUILD_METHOD));
                javadocSection($wither).append(WITHER_IMPLNOTE.format(String.format("this.%s().%s(%s).%s()", $implClassToBuilder.name(), $builderWither.name(), $parameter.name(), $builderBuild.name())));
                $wither.body()._return($this.invoke($implClassToBuilder).invoke($builderWither).arg($parameter).invoke($builderBuild));
            }
            if (GENERATE_ADDITIONAL_WITHER.getAsBoolean() && attribute.getPropertyInfo().isCollection()) {
                // (B) generate/implement "adder" factory method
                final var $adder = $ImplClass.method(modifiers, $ImplClass, guessWithAdditionalName(attribute));
                if (inheritedProperties.containsKey(attribute)) {
                    $adder.annotate(Override.class);
                }
                javadocSection($adder).append(ADDER_JAVADOC.format($property.name()));
                javadocSection($adder.javadoc().addReturn()).append(ADDER_RETURN.format($property.name()));
                final var $addParameter = $adder.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                // TODO: parameter Javadoc (must be different to the collection field Javadoc! nullable?)
                if ($adder.mods().isAbstract()) {
                    javadocSection($adder).append(ADDER_ABSTRACT_IMPLNOTE.text());
                } else {
                    final var $builderAdder = getMethod($Builder, guessAdderName(attribute), typeParameterOf($property.type().boxify())).orElseThrow(() -> new IllegalStateException(MISSING_BUILDER_WITH_METHOD));
                    final var $builderBuild = getMethod($Builder, build).orElseThrow(() -> new IllegalStateException(MISSING_BUILDER_BUILD_METHOD));
                    javadocSection($adder).append(ADDER_IMPLNOTE.format(String.format("this.%s().%s(%s).%s()", $implClassToBuilder.name(), $builderAdder.name(), $parameter.name(), $builderBuild.name())));
                    $adder.body()._return($this.invoke($implClassToBuilder).invoke($builderAdder).arg($addParameter).invoke($builderBuild));
                }
                // (C) generate/implement "remover" factory method
                final var $remover = $ImplClass.method(modifiers, $ImplClass, guessWithoutSpecificName(attribute));
                if (inheritedProperties.containsKey(attribute)) {
                    $remover.annotate(Override.class);
                }
                javadocSection($remover).append(REMOVER_JAVADOC.format($property.name()));
                javadocSection($remover.javadoc().addReturn()).append(REMOVER_RETURN.format($property.name()));
                final var $removeParameter = $remover.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                // TODO: parameter Javadoc (must be different to the collection field Javadoc! nullable?)
                if ($remover.mods().isAbstract()) {
                    javadocSection($remover).append(REMOVER_ABSTRACT_IMPLNOTE.text());
                } else {
                    final var $builderRemover = getMethod($Builder, guessRemoverName(attribute), typeParameterOf($property.type().boxify())).orElseThrow(() -> new IllegalStateException(MISSING_BUILDER_WITH_METHOD));
                    final var $builderBuild = getMethod($Builder, build).orElseThrow(() -> new IllegalStateException(MISSING_BUILDER_BUILD_METHOD));
                    javadocSection($remover).append(REMOVER_IMPLNOTE.format(String.format("this.%s().%s(%s).%s()", $implClassToBuilder.name(), $builderRemover.name(), $parameter.name(), $builderBuild.name())));
                    $remover.body()._return($this.invoke($implClassToBuilder).invoke($builderRemover).arg($removeParameter).invoke($builderBuild));
                }
            }
        }
    }

    private final void hideDefaultFactory(final ClassOutline clazz) {
        final var $ImplClass = clazz.implClass;
        final var $ObjectFactory = clazz._package().objectFactory();
        final var $factoryLookup = getMethod($ObjectFactory, guessFactoryName(clazz));
        if ($factoryLookup.isPresent()) {
            final var anyOtherConstructorExists = stream(spliteratorUnknownSize($ImplClass.constructors(), 0), false).anyMatch(c -> c.params().size() > 0);
            final var builderExists = GENERATE_BUILDER.getAsBoolean();
            final var $factory = $factoryLookup.get();
            LOG.info(MODIFY_FACTORY, "accessibility", $ObjectFactory.fullName(), $factory.name(), "private");
            $factory.mods().setPrivate();
            $factory.annotate(SuppressWarnings.class).param("value", "unused");
            javadocSection($factory).append(PRIVATE_FACTORY_IMPLNOTE.text());
            if (anyOtherConstructorExists && builderExists) {
                javadocSection($factory).append(ALTERNATIVE_INSTANTIATION.format($ImplClass.name(), BUILDER_NAME.apply($ImplClass)));
            } else if (anyOtherConstructorExists) {
                javadocSection($factory).append(ALTERNATIVE_CONSTRUCTORS.text());
            } else if (builderExists) {
                javadocSection($factory).append(ALTERNATIVE_BUILDER.format($ImplClass.name(), BUILDER_NAME.apply($ImplClass)));
            } else {
                // nothing to say
            }
        }
    }

    private final void removeDefaultFactory(final ClassOutline clazz) {
        final var $ObjectFactory = clazz._package().objectFactory();
        final var $factoryLookup = getMethod($ObjectFactory, guessFactoryName(clazz));
        if ($factoryLookup.isPresent()) {
            final var $factory = $factoryLookup.get();
            LOG.info(REMOVE_FACTORY, $ObjectFactory.fullName(), $factory.name());
            $ObjectFactory.methods().remove($factory);
            assertThat(getMethod($ObjectFactory, guessFactoryName(clazz))).isNull();
        }
    }

}
