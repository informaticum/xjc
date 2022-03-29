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
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ADDER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ADDER_ARGUMENT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ADDER_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ADDER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ADDER_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ADDITIONAL_WITHER_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ALTERNATIVE_BUILDER;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ALTERNATIVE_CONSTRUCTORS;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.ALTERNATIVE_INSTANTIATION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BASIC_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_ADDER_ARGUMENT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_ADDER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_ADDER_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_BLUEPRINT_ARGUMENT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_BLUEPRINT_CONSTRUCTOR;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_BUILD_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_BUILD_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_BUILD_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_BUILD_RELAY_THROWS;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_BUILD_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_DEFAULT_CONSTRUCTOR;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_ILLEGAL_BLUEPRINT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_METHOD_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_METHOD_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_METHOD_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_REMOVER_ARGUMENT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_REMOVER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_REMOVER_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_WITHER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.BUILDER_WITHER_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.CLONE_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.CLONE_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.CONSTRUCTOR_BLUEPRINT_ARGUMENT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.CONSTRUCTOR_ILLEGAL_BLUEPRINT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.CONSTRUCTOR_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.CONSTRUCTOR_JAVADOC_END;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.CONSTRUCTOR_JAVADOC_SUPER_CLASS;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.COPY_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.DEFAULT_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.FACTORY_WITHER_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.HIDDEN_FACTORIES_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.IMPLEMENTS_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.PRIVATE_FACTORY_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.PROTECTED_CONSTRUCTOR_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.PROTECTED_DEFAULT_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.REMOVER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.REMOVER_ARGUMENT;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.REMOVER_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.REMOVER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.REMOVER_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.REMOVE_FACTORIES_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.TOBUILDER_ILLEGAL_INSTANCE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.TOBUILDER_METHOD_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.TOBUILDER_METHOD_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.TOBUILDER_METHOD_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.TOBUILDER_METHOD_RETURN;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.VALUES_CONSTRUCTOR_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.WITHER_ABSTRACT_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.WITHER_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.WITHER_JAVADOC;
import static de.informaticum.xjc.plugins.i18n.ConstructionPluginMessages.WITHER_RETURN;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.$super;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.cloneExpressionFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.copyFactoryFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.getConstructor;
import static de.informaticum.xjc.util.CodeModelAnalysis.getMethod;
import static de.informaticum.xjc.util.CodeModelAnalysis.javadocNameOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.typeParameterOf;
import static de.informaticum.xjc.util.CodeRetrofit.COPY_JAVADOC;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static de.informaticum.xjc.util.CodeRetrofit.relayParamDoc;
import static de.informaticum.xjc.util.CodeRetrofit.relayThrows;
import static de.informaticum.xjc.util.OutlineAnalysis.filter;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructor;
import static de.informaticum.xjc.util.OutlineAnalysis.getConstructors;
import static de.informaticum.xjc.util.OutlineAnalysis.getEmbeddedClass;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.guessAdderName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessBuilderName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessFactoryName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessRemoverName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessWithAdditionalName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessWitherName;
import static de.informaticum.xjc.util.OutlineAnalysis.guessWithoutSpecificName;
import static de.informaticum.xjc.util.OutlineAnalysis.javadocNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.superAndGeneratedPropertiesOf;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.api.CommandLineArgument;
import de.informaticum.xjc.util.CodeModelAnalysis;
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
    private static final String SKIP_NESTED_BUILDER = "Skip creation of nested builder class for [{}] because {}.";
    private static final String BECAUSE_NESTED_BUILDER_ALREADY_EXISTS = "such builder (coincidentally?) already exists";
    private static final String MODIFY_FACTORY = "Set {} of factory method [{}#{}(...)] onto [{}].";
    private static final String REMOVE_FACTORY = "Remove factory method [{}#{}(...)].";
    private static final String defoult = "default";
    private static final String required_values = "required-values";
    private static final String all_values = "all-values";
    private static final String copy = "copy";
    private static final String blueprint = "blueprint";

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

    /**
     * To be used as parameter for {@link CodeModelAnalysis#cloneExpressionFor(JType, JExpression, boolean)}: causes unmodifiable collection expressions.
     */
    private static final boolean WITH_UNMODIFIABLE_COLLECTION = true;

    /**
     * To be used as parameter for {@link CodeModelAnalysis#cloneExpressionFor(JType, JExpression, boolean)}: allows subsequent collection modification.
     */
    private static final boolean WITH_MODIFIABLE_COLLECTION = false;

    /**
     * To be used as parameter for {@link #accordingAssignment(Entry, JMethod, JExpression, Optional, JExpression)} and
     * {@link #accordingAssignmentAndJavadoc(Entry, JMethod, JExpression, Optional, JExpression)}: prevents default values.
     */
    private static final Optional<JExpression> WITHOUT_DEFAULT_VALUE = Optional.empty();

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
        return new ArrayList<>(super.getPluginArguments()){{this.addAll(args);}};
    }

    @Override
    public final boolean prepareRun() {
        // Factory wither-methods require embedded Builder.
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
        // TODO: What about final classes in general? Unfortunately, in a multi-XJC-run maven build
        //       some XJC runs may generate final classes but other runs require non-final ;-(
        //   Collection<? extends ClassOutline> classes = new ArrayList<>(this.outline().getClasses());
        //   classes.forEach(c -> { if (!c.implClass.isAbstract()) { c.implClass.mods().setFinal(true); }});
        //   classes.forEach(c -> { if (c.getSuperClass() != null) { c.getSuperClass().implClass.mods().setFinal(false); }});
        return true;
    }

    private static final Predicate<FieldOutline> DEFAULT_CONSTRUCTOR_PARAMETER_FILTER = any -> false;
    private static final Predicate<FieldOutline> VALUES_CONSTRUCTOR_PARAMETER_FILTER = any -> true;
    private static final Predicate<FieldOutline> MINIMUM_CONSTRUCTOR_PARAMETER_FILTER = OutlineAnalysis::isRequired;

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        GENERATE_DEFAULT_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, defoult, DEFAULT_CONSTRUCTOR_PARAMETER_FILTER), clazz);
        final var $valCtor = GENERATE_VALUES_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, all_values, VALUES_CONSTRUCTOR_PARAMETER_FILTER), clazz);
        final var $reqCtor = GENERATE_BASIC_CONSTRUCTOR.doOnActivation(c -> this.generateConstructor(c, required_values, MINIMUM_CONSTRUCTOR_PARAMETER_FILTER), clazz);
        GENERATE_COPY_CONSTRUCTOR.doOnActivation(this::generateCopyConstructor, clazz);
        GENERATE_CLONE.doOnActivation(this::generateClone, clazz);
        final var $builder = GENERATE_BUILDER.doOnActivation(c -> $valCtor.map($vc -> this.generateBuilder(c, $vc)).orElse(null), clazz);
        GENERATE_FACTORY_WITHER.doOnActivation(c -> $builder.map($b -> this.generateWithers(c, $b)).orElse(null), clazz);
        PROTECTED_DEFAULT_CONSTRUCTOR.doOnActivation(c -> this.hideDefaultConstructor(c, $valCtor, $reqCtor), clazz);
        HIDE_DEFAULT_FACTORIES.doOnActivation(this::hideDefaultFactory, clazz);
        REMOVE_DEFAULT_FACTORIES.doOnActivation(this::removeDefaultFactory, clazz);
        return true;
    }

    private final JMethod generateConstructor(final ClassOutline clazz, final String label, final Predicate<? super FieldOutline> passedAsParameter) {
        // 0/2: Preliminary
        final var $preliminaryLookup = getConstructor(clazz, filter(superAndGeneratedPropertiesOf(clazz), passedAsParameter));
        if ($preliminaryLookup.isPresent()) {
            LOG.info(SKIP_CONSTRUCTOR, label, fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return $preliminaryLookup.get();
        }
        // 1/2: Create
        LOG.info(GENERATE_CONSTRUCTOR, label, fullNameOf(clazz));
        final var $ImplClass = clazz.implClass;
        final var superClass = Optional.ofNullable(clazz.getSuperClass());
        final var $constructor = $ImplClass.constructor(PUBLIC);
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
            final var $superConstructor = getConstructor(sc.implClass, fieldsViaSuperConstructor.values()).get();
            relayThrows($superConstructor, $constructor, COPY_JAVADOC); // @throws is not inherited for constructor Javadoc, so it must be relayed
            for (final var property : fieldsViaSuperConstructor.entrySet()) {
                final var $property = property.getValue();
                final var $parameter = $constructor.param(FINAL, $property.type(), $property.name());
                $super.arg($parameter);
                relayParamDoc($superConstructor, $constructor, $parameter); // @param is not inherited for constructor Javadoc, so it must be relayed
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
        return $constructor;
    }

    private final JMethod generateCopyConstructor(final ClassOutline clazz) {
        // 0/2: Preliminary
        final var $ImplClass = clazz.implClass;
        final var $preliminaryLookup = getConstructor(clazz, $ImplClass);
        if ($preliminaryLookup.isPresent()) {
            LOG.warn(SKIP_CONSTRUCTOR, copy, fullNameOf(clazz), BECAUSE_CONSTRUCTOR_ALREADY_EXISTS);
            return $preliminaryLookup.get();
        }
        // 1/2: Create
        LOG.info(GENERATE_CONSTRUCTOR, copy, fullNameOf(clazz));
        final var superClass = Optional.ofNullable(clazz.getSuperClass());
        final var $constructor = $ImplClass.constructor(PUBLIC);
        javadocSection($constructor).append(CONSTRUCTOR_JAVADOC_BEGIN.text());
        superClass.ifPresent(sc -> $constructor.javadoc().append(CONSTRUCTOR_JAVADOC_SUPER_CLASS.format(copy)));
        $constructor.javadoc().append(CONSTRUCTOR_JAVADOC_END.text());
        // 2/2: Implement (with according Javadoc)
        final var $blueprint = $constructor.param(FINAL, $ImplClass, "blueprint");
        javadocSection($constructor.javadoc().addParam($blueprint)).append(CONSTRUCTOR_BLUEPRINT_ARGUMENT.text());
        // (A) Firstly, call super constructor:
        superClass.ifPresent(sc -> $constructor.body().invoke("super").arg($blueprint));
        $constructor._throws(IllegalArgumentException.class);
        $constructor.body()._if($blueprint.eq($null))._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required argument 'blueprint' must not be null!")));
        javadocSection($constructor.javadoc().addThrows(IllegalArgumentException.class)).append(CONSTRUCTOR_ILLEGAL_BLUEPRINT.text());
        // (B) Secondly, initialise all declared fields:
        final var fields = generatedPropertiesOf(clazz).entrySet();
        if (!fields.isEmpty()) {
            $constructor.body().directStatement("// below fields are assigned with their according blueprint value");
            for (final var property : fields) {
                final var $property = property.getValue();
                final var $nonNull = effectiveExpressionForNonNull($property.type(), $blueprint.ref($property));
                accordingAssignment(property, $constructor, $blueprint.ref($property), WITHOUT_DEFAULT_VALUE, $nonNull);
            }
        }
        return $constructor;
    }

    /**
     * @param $valCtor
     *            marker {@link Optional} holding the value constructor with the expectation to be not modified inadvertently
     * @param $reqCtor
     *            marker {@link Optional} holding the basic constructor with the expectation to be not modified inadvertently
     */
    private final Optional<JMethod> hideDefaultConstructor(final ClassOutline clazz, final Optional<? extends JMethod> $valCtor, final Optional<? extends JMethod> $reqCtor) {
        // 0/1: Preliminary
        final var $defaultConstructor = getConstructor(clazz);
        final var anyOtherConstructorExists = !getConstructors(clazz, c -> c.params().size() > 0).isEmpty();
        final var $EmbeddedBuilder = getEmbeddedClass(clazz, guessBuilderName(clazz));
        if ($defaultConstructor.isEmpty()) {
            LOG.warn(SKIP_HIDING_OF_MISSING, defoult, fullNameOf(clazz));
            return Optional.empty();
        } else if ($valCtor.isPresent() && $valCtor.get().params().isEmpty()) {
            LOG.info(SKIP_HIDING_OF_SIMILAR, defoult, fullNameOf(clazz), all_values);
            return Optional.empty();
        } else if ($reqCtor.isPresent() && $reqCtor.get().params().isEmpty()) {
            LOG.info(SKIP_HIDING_OF_SIMILAR, defoult, fullNameOf(clazz), required_values);
            return Optional.empty();
        } else if (!anyOtherConstructorExists && $EmbeddedBuilder.isEmpty()) {
            LOG.info(ABORT_HIDING_OF_MISSING, defoult, fullNameOf(clazz), BECAUSE_NO_ALTERNATIVE_EXISTS);
            return Optional.empty();
        }
        // 1/1: Modify
        assertThat($defaultConstructor).isPresent();
        LOG.info(HIDE_CONSTRUCTOR, defoult, fullNameOf(clazz));
        final var $constructor = $defaultConstructor.get();
        if (anyOtherConstructorExists && $EmbeddedBuilder.isPresent()) {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text()).append(ALTERNATIVE_INSTANTIATION.format(javadocNameOf($EmbeddedBuilder.get())));
        } else if (anyOtherConstructorExists) {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text()).append(ALTERNATIVE_CONSTRUCTORS.text());
        } else if ($EmbeddedBuilder.isPresent()) {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text()).append(ALTERNATIVE_BUILDER.format(javadocNameOf($EmbeddedBuilder.get())));
        } else {
            javadocSection($constructor).append(PROTECTED_CONSTRUCTOR_IMPLNOTE.text());
        }
        $constructor.mods().setProtected();
        return $defaultConstructor;
    }

    private final void addInterface(final ClassOutline clazz, final Class<?> interfaceClass) {
        // 0/1: Preliminary
        final var $ImplClass = clazz.implClass;
        assertThat(interfaceClass).isInterface();
        if (this.reference(interfaceClass).isAssignableFrom($ImplClass)) {
            LOG.warn(SKIP_INTERFACE, interfaceClass, fullNameOf(clazz));
            return;
        }
        // 1/1: Modify
        LOG.info(ADD_INTERFACE, interfaceClass, fullNameOf(clazz));
        javadocSection($ImplClass).append(IMPLEMENTS_IMPLNOTE.format(javadocNameOf(clazz)));
        $ImplClass._implements(interfaceClass);
    }

    private final JMethod generateClone(final ClassOutline clazz) {
        // 0/2: Preliminary
        final var $preliminaryLookup = getMethod(clazz, clone);
        if ($preliminaryLookup.isPresent()) {
            LOG.warn(SKIP_METHOD, CLONE_SIGNATURE, fullNameOf(clazz), BECAUSE_METHOD_ALREADY_EXISTS);
            return $preliminaryLookup.get();
        }
        // 1/2: Create
        LOG.info(GENERATE_METHOD, fullNameOf(clazz), CLONE_SIGNATURE);
        final var $ImplClass = clazz.implClass;
        assertThat(this.reference(Cloneable.class).isAssignableFrom($ImplClass)).isTrue();
        final var $clone = $ImplClass.method(PUBLIC, $ImplClass, clone);
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
        final var $replica = $body.decl(FINAL, $ImplClass, "replica", cast($ImplClass, $super.invoke(clone)));
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            final var $cloneExpression = cloneExpressionFor($property.type(), $this.ref($property), UNMODIFIABLE_COLLECTIONS.isActivated());
            if ($cloneExpression.isEmpty()) {
                $body.assign($replica.ref($property), $this.ref($property));
            } else {
                $body.assign($replica.ref($property), cond($this.ref($property).eq($null), $null, $cloneExpression.get()));
            }
        }
        $body._return($replica);
        return $clone;
    }

    private final JDefinedClass generateBuilder(final ClassOutline clazz, final JMethod $valCtor) {
        // 0/5: Preliminary
        final var builderClassName = guessBuilderName(clazz);
        final var $preliminaryLookup = getEmbeddedClass(clazz, builderClassName);
        if ($preliminaryLookup.isPresent()) {
            LOG.info(SKIP_NESTED_BUILDER, fullNameOf(clazz), BECAUSE_NESTED_BUILDER_ALREADY_EXISTS);
            return $preliminaryLookup.get();
        }
        // 1/5: Create Builder, Constructors, and Factory methods
        final var superClazz = clazz.getSuperClass();
        final var superBuilder = Optional.ofNullable(superClazz).flatMap(s -> getEmbeddedClass(s, guessBuilderName(s)));
        final var $ImplClass = clazz.implClass;
        final var modifiers = $ImplClass.mods().getValue() & ~STATIC; // exclude "static" modifier (happens for nested types)
        // (A.1) Generate embedded Builder [XyzClass.Builder] ...
        LOG.info(GENERATE_NESTED_BUILDER, fullNameOf(clazz));
        final var $Builder = this.outline().getClassFactory().createClass($ImplClass, modifiers, builderClassName, clazz.target.getLocator());
        javadocSection($Builder).append(BUILDER_JAVADOC.format(javadocNameOf(clazz)));
        // ... (A.2) including the default constructor [XyzClass.Builder#Builder()] ...
        LOG.debug(GENERATE_CONSTRUCTOR, defoult, $Builder.fullName());
        final var $builderDefaultConstructor = $Builder.constructor(PROTECTED /* keep protected, programmer should call Xyz#builder() instead */);
        javadocSection($builderDefaultConstructor).append(BUILDER_DEFAULT_CONSTRUCTOR.text());
        // ... (A.3) including the blueprint constructor [XyzClass.Builder#Builder(XyzClass)] ...
        LOG.debug(GENERATE_CONSTRUCTOR, blueprint, $Builder.fullName());
        final var $builderBlueprintConstructor = $Builder.constructor(PROTECTED/* keep protected, programmer must call Xyz#toBuilder() instead */);
        javadocSection($builderBlueprintConstructor).append(BUILDER_BLUEPRINT_CONSTRUCTOR.text());
        final var $blueprintParam = $builderBlueprintConstructor.param(FINAL, $ImplClass, "blueprint");
        javadocSection($builderBlueprintConstructor.javadoc().addParam($blueprintParam)).append(BUILDER_BLUEPRINT_ARGUMENT.text());
        // ... (A.4) including the #build() method [XyzClass.Builder#build()] ...
        LOG.debug(GENERATE_METHOD, $Builder.fullName(), BUILD_SIGNATURE);
        final var $build = $Builder.method(modifiers, $ImplClass, build);
        javadocSection($build).append(BUILDER_BUILD_JAVADOC.format(javadocNameOf(clazz)));
        javadocSection($build.javadoc().addReturn()).append(BUILDER_BUILD_RETURN.format(javadocNameOf(clazz)));
        // (B.1) Generate #toBuilder() method [XyzClass#toBuilder()]
        LOG.info(GENERATE_METHOD, fullNameOf(clazz), TOBUILDER_METHOD_SIGNATURE);
        final var $toBuilder = $ImplClass.method(modifiers, $Builder, toBuilder);
        javadocSection($toBuilder).append(TOBUILDER_METHOD_JAVADOC.format(javadocNameOf($Builder)));
        javadocSection($toBuilder.javadoc().addReturn()).append(TOBUILDER_METHOD_RETURN.format(javadocNameOf($Builder)));
        // 2/5: Follow class hierarchy
        if (superClazz != null) {
            // (A.1++) Embedded Builder has super class
            $Builder._extends(superBuilder.get());
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
            javadocSection($Builder).append(BUILDER_ABSTRACT_IMPLNOTE.text());
            // (A.4++) Document embedded Builder's #build() method
            javadocSection($build).append(BUILDER_BUILD_ABSTRACT_IMPLNOTE.text());
            // (B.1++) Document ImplClass's #toBuilder() method
            javadocSection($toBuilder).append(TOBUILDER_METHOD_ABSTRACT_IMPLNOTE.text());
        } else {
            // (A.1++) Document embedded Builder
            javadocSection($Builder).append(BUILDER_IMPLNOTE.text());
            // (A.4++) Implement embedded Builder's #build() method
            javadocSection($build).append(BUILDER_BUILD_IMPLNOTE.format(javadocNameOf(clazz), javadocNameOf($valCtor)));
            $build.body()._return(superAndGeneratedPropertiesOf(clazz).values().stream().map($p -> $this.ref($p)).reduce(_new($ImplClass), JInvocation::arg, JInvocation::arg));
            relayThrows($valCtor, $build, BUILDER_BUILD_RELAY_THROWS.text());
            // (B.1++) ImplClass's #toBuilder() method returns new Builder instance
            javadocSection($toBuilder).append(TOBUILDER_METHOD_IMPLNOTE.format(javadocNameOf($Builder), javadocNameOf($builderBlueprintConstructor)));
            $toBuilder.body()._return(_new($Builder).arg($this));
            // Do not simply relay thrown IllegalArgumentException because the 'null' instance will not happen.
            // However, some fields may be invalid and this (unexpected) scenario must be identified.
            if (!filterIllegalArgumentExceptionCandidates(superAndGeneratedPropertiesOf(clazz), false /* because builderBlueprintConstructor does not use default values */).isEmpty()) {
                $toBuilder._throws(IllegalArgumentException.class);
                javadocSection($toBuilder.javadoc().addThrows(IllegalArgumentException.class)).append(TOBUILDER_ILLEGAL_INSTANCE.format(javadocNameOf($Builder)));
            }
            // (B.2) Generate/Implement #builder() method [XyzClass#builder()]
            LOG.info(GENERATE_METHOD, fullNameOf(clazz), BUILDER_METHOD_SIGNATURE);
            final var $builder = $ImplClass.method(modifiers | STATIC, $Builder, builder);
            javadocSection($builder).append(BUILDER_METHOD_JAVADOC.format(javadocNameOf($Builder)));
            javadocSection($builder).append(BUILDER_METHOD_IMPLNOTE.format(javadocNameOf($Builder), javadocNameOf($builderDefaultConstructor)));
            javadocSection($builder.javadoc().addReturn()).append(BUILDER_METHOD_RETURN.format(javadocNameOf($Builder)));
            $builder.body()._return(_new($Builder));
        }
        // (A.3++) Implement blueprint constructor's parameter validation
        $builderBlueprintConstructor.body()._if($blueprintParam.eq($null))._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required argument 'blueprint' must not be null!")));
        $builderBlueprintConstructor._throws(IllegalArgumentException.class);
        javadocSection($builderBlueprintConstructor.javadoc().addThrows(IllegalArgumentException.class)).append(BUILDER_ILLEGAL_BLUEPRINT.text());
        // 4/5: Handle declared fields
        for (final var property : generatedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            // (A.1++) Generate field into embedded Builder (must be 'protected' to be accessible by sub-builders)
            $Builder.field(PROTECTED, $property.type(), $property.name(), defaultExpressionFor(attribute).orElse($null));
            // (A.3++) Declare blueprint constructor's field assignment (no defaults, collections must be modifiable)
            accordingAssignment(property, $builderBlueprintConstructor, $blueprintParam.ref($property), WITHOUT_DEFAULT_VALUE, cloneExpressionFor(property.getValue().type(), $blueprintParam.ref($property), WITH_MODIFIABLE_COLLECTION).orElse($blueprintParam.ref($property)));
            // (C.1) Generate Builder's "wither"-method for declared property [XyzClass.Builder#withAbc(AbcType)]
            final var $wither = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessWitherName(attribute));
            javadocSection($wither).append(BUILDER_WITHER_JAVADOC.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
            javadocSection($wither.javadoc().addReturn()).append(BUILDER_WITHER_RETURN.text());
            final var $witherParam = $wither.param(FINAL, $property.type(), $property.name());
            // assignment without defaults, collections must be modifiable
            accordingAssignmentAndJavadoc(property, $wither, $witherParam, WITHOUT_DEFAULT_VALUE, cloneExpressionFor(property.getValue().type(), $witherParam, WITH_MODIFIABLE_COLLECTION).orElse($witherParam));
            $wither.body()._return($this);
            if (GENERATE_ADDITIONAL_WITHER.isActivated() && attribute.getPropertyInfo().isCollection()) {
                // (D.1) Generate Builder's "adder"-method for declared Collection<T> property [XyzClass.Builder#addAbc(T)]
                final var $adder = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessAdderName(attribute));
                javadocSection($adder).append(BUILDER_ADDER_JAVADOC.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                javadocSection($adder.javadoc().addReturn()).append(BUILDER_ADDER_RETURN.text());
                // TODO: Any parameter check? Null-check? Maximum number of elements? Even throw IllegalArgumentException?
                final var $adderParam = $adder.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                javadocSection($adder.javadoc().addParam($adderParam)).append(BUILDER_ADDER_ARGUMENT.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                $adder.body()._if($this.ref($property).eq($null))._then().assign($this.ref($property), copyFactoryFor($property.type()));
                $adder.body().add($this.ref($property).invoke("add").arg($adderParam));
                $adder.body()._return($this);
                // (D.2) Generate Builder's "remover"-method for declared Collection<T> property [XyzClass.Builder#removeAbc(T)]
                final var $remover = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessRemoverName(attribute));
                javadocSection($remover).append(BUILDER_REMOVER_JAVADOC.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                javadocSection($remover.javadoc().addReturn()).append(BUILDER_REMOVER_RETURN.text());
                // TODO: Any collection check? Minimum number of elements? Even throw IllegalArgumentException?
                final var $removerParam = $remover.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                javadocSection($remover.javadoc().addParam($removerParam)).append(BUILDER_REMOVER_ARGUMENT.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                $remover.body()._if(not($this.ref($property).eq($null)))._then().add($this.ref($property).invoke("remove").arg($removerParam));
                $remover.body()._return($this);
            }
        }
        // 5/5: Handle inherited fields
        for (final var property : superAndGeneratedPropertiesOf(superClazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var $SuperBuilder =superBuilder.get();
            // (C.1++) Override Builder's "wither"-method for each inherited property [XyzClass.Builder#withAbc(AbcType)]
            final var $wither = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessWitherName(attribute));
            $wither.annotate(Override.class);
            final var $witherParam = $wither.param(FINAL, $property.type(), $property.name());
            $wither.body().invoke($super, $wither).arg($witherParam);
            $wither.body()._return($this);
            relayThrows(getMethod($SuperBuilder, guessWitherName(attribute), $property.type()).get(), $wither);
            if (GENERATE_ADDITIONAL_WITHER.isActivated() && attribute.getPropertyInfo().isCollection()) {
                // (D.1++) Override Builder's "adder"-method for each inherited Collection<T> property [XyzClass.Builder#addAbc(T)]
                final var $adder = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessAdderName(attribute));
                $adder.annotate(Override.class);
                final var $adderParam = $adder.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                $adder.body().invoke($super, $adder).arg($adderParam);
                $adder.body()._return($this);
                // TODO: Relay @throws? Currently no exception is thrown. 
                // (E.2) Override Builder's "remover"-method for each inherited Collection<T> property [XyzClass.Builder#removeAbc(T)]
                final var $remover = $Builder.method(modifiers & ~ABSTRACT, $Builder, guessRemoverName(attribute));
                $remover.annotate(Override.class);
                final var $removerParam = $remover.param(FINAL, typeParameterOf($property.type().boxify()), $property.name());
                $remover.body().invoke($super, $remover).arg($removerParam);
                $remover.body()._return($this);
                // TODO: Relay @throws? Currently no exception is thrown.
            }
        }
        return $Builder;
    }

    private final List<JMethod> generateWithers(final ClassOutline clazz, final JDefinedClass $Builder) {
        final var $ImplClass = clazz.implClass;
        final var modifiers = $ImplClass.mods().getValue() & ~STATIC; // exclude "static" modifier (happens for nested types)
        final var $implClassToBuilder = getMethod($ImplClass, toBuilder).get();
        final var inheritedProperties = superAndGeneratedPropertiesOf(clazz.getSuperClass());
        final var $generated = new ArrayList<JMethod>();
        for (final var property : superAndGeneratedPropertiesOf(clazz).entrySet()) {
            final var attribute = property.getKey();
            final var $property = property.getValue();
            final var $builderBuild = getMethod($Builder, build).get();
            // A.0/2: Preliminary
            final var $builderWither = getMethod($Builder, guessWitherName(attribute), $property.type()).get();
            final var $builderWitherParam = $builderWither.params().get(0);
            // A.1/2: Create "wither" factory method
            final var $wither = $ImplClass.method(modifiers, $ImplClass, guessWitherName(attribute));
            final var $witherParam = $wither.param(FINAL, $builderWitherParam.type(), $builderWitherParam.name());
            if (inheritedProperties.containsKey(attribute)) {
                $wither.annotate(Override.class);
            } else {
                javadocSection($wither).append(WITHER_JAVADOC.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                // javadocSection($wither.javadoc().addParam($parameter)).append(WITHER_ARGUMENT.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                javadocSection($wither.javadoc().addReturn()).append(WITHER_RETURN.text());
                relayParamDoc($builderWither, $wither, $builderWitherParam);
            }
            // A.2/2: Implement "wither" factory method
            if ($wither.mods().isAbstract()) {
                javadocSection($wither).append(WITHER_ABSTRACT_IMPLNOTE.text());
            } else {
                final var code = String.format("this.%s().%s(%s).%s()", $implClassToBuilder.name(), $builderWither.name(), $witherParam.name(), $builderBuild.name());
                javadocSection($wither).append(WITHER_IMPLNOTE.format(javadocNameOf($Builder), code));
                $wither.body()._return($this.invoke($implClassToBuilder).invoke($builderWither).arg($witherParam).invoke($builderBuild));
                if (inheritedProperties.containsKey(attribute)) {
                    relayThrows($builderWither, $wither);
                    // TODO: Relay @throws of $builderBuild? Currently no need, exception of $builderWither is sufficient enough.
                } else {
                    relayThrows($builderWither, $wither, COPY_JAVADOC);
                    // TODO: Relay @throws of $builderBuild? Currently no need, exception of $builderWither is sufficient enough.
                }
            }
            $generated.add($wither);
            if (GENERATE_ADDITIONAL_WITHER.isActivated() && attribute.getPropertyInfo().isCollection()) {
                // B.0/2: Preliminary
                final var $builderAdder = getMethod($Builder, guessAdderName(attribute), typeParameterOf($property.type().boxify())).get();
                final var $builderAdderParam = $builderAdder.params().get(0);
                // B.1/2: Create "adder" factory method
                final var $adder = $ImplClass.method(modifiers, $ImplClass, guessWithAdditionalName(attribute));
                final var $adderParam = $adder.param(FINAL, $builderAdderParam.type(), $builderAdderParam.name());
                if (inheritedProperties.containsKey(attribute)) {
                    $adder.annotate(Override.class);
                } else {
                    javadocSection($adder).append(ADDER_JAVADOC.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                    javadocSection($adder.javadoc().addParam($adderParam)).append(ADDER_ARGUMENT.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                    javadocSection($adder.javadoc().addReturn()).append(ADDER_RETURN.text());
                }
                // B.2/2: Implement "adder" factory method
                if ($adder.mods().isAbstract()) {
                    javadocSection($adder).append(ADDER_ABSTRACT_IMPLNOTE.text());
                } else {
                    final var code = String.format("this.%s().%s(%s).%s()", $implClassToBuilder.name(), $builderAdder.name(), $adderParam.name(), $builderBuild.name());
                    javadocSection($adder).append(ADDER_IMPLNOTE.format(javadocNameOf($Builder), code));
                    $adder.body()._return($this.invoke($implClassToBuilder).invoke($builderAdder).arg($adderParam).invoke($builderBuild));
                    // TODO: Relay @throws? Currently $builderAdder does not throw exception. 
                }
                $generated.add($adder);
                // C.0/2: Preliminary
                final var $builderRemover = getMethod($Builder, guessRemoverName(attribute), typeParameterOf($property.type().boxify())).get();
                final var $builderRemoverParam = $builderRemover.params().get(0);
                // C.1/2: Create "remover" factory method
                final var $remover = $ImplClass.method(modifiers, $ImplClass, guessWithoutSpecificName(attribute));
                final var $removerParam = $remover.param(FINAL, $builderRemoverParam.type(), $builderRemoverParam.name());
                if (inheritedProperties.containsKey(attribute)) {
                    $remover.annotate(Override.class);
                } else {
                    javadocSection($remover).append(REMOVER_JAVADOC.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                    javadocSection($remover.javadoc().addParam($removerParam)).append(REMOVER_ARGUMENT.format(javadocNameOf(attribute.parent().implClass), javadocNameOf($property)));
                    javadocSection($remover.javadoc().addReturn()).append(REMOVER_RETURN.text());
                }
                // C.2/2: Implement "remover" factory method
                if ($remover.mods().isAbstract()) {
                    javadocSection($remover).append(REMOVER_ABSTRACT_IMPLNOTE.text());
                } else {
                    final var code = String.format("this.%s().%s(%s).%s()", $implClassToBuilder.name(), $builderRemover.name(), $removerParam.name(), $builderBuild.name());
                    javadocSection($remover).append(REMOVER_IMPLNOTE.format(javadocNameOf($Builder), code));
                    $remover.body()._return($this.invoke($implClassToBuilder).invoke($builderRemover).arg($removerParam).invoke($builderBuild));
                    // TODO: Relay @throws? Currently $builderRemover does not throw exception. 
                }
                $generated.add($remover);
            }
        }
        return $generated;
    }

    private final JMethod hideDefaultFactory(final ClassOutline clazz) {
        final var $ObjectFactory = clazz._package().objectFactory();
        final var $EmbeddedBuilder = getEmbeddedClass(clazz, guessBuilderName(clazz));
        final var $preliminaryLookup = getMethod($ObjectFactory, guessFactoryName(clazz));
        if ($preliminaryLookup.isPresent()) {
            final var anyOtherConstructorExists = !getConstructors(clazz, c -> c.params().size() > 0).isEmpty();
            final var $factory = $preliminaryLookup.get();
            LOG.info(MODIFY_FACTORY, "accessibility", $ObjectFactory.fullName(), $factory.name(), "private");
            $factory.mods().setPrivate();
            $factory.annotate(SuppressWarnings.class).param("value", "unused");
            javadocSection($factory).append(PRIVATE_FACTORY_IMPLNOTE.text());
            if (anyOtherConstructorExists && $EmbeddedBuilder.isPresent()) {
                javadocSection($factory).append(ALTERNATIVE_INSTANTIATION.format(javadocNameOf($EmbeddedBuilder.get())));
            } else if (anyOtherConstructorExists) {
                javadocSection($factory).append(ALTERNATIVE_CONSTRUCTORS.text());
            } else if ($EmbeddedBuilder.isPresent()) {
                javadocSection($factory).append(ALTERNATIVE_BUILDER.format(javadocNameOf($EmbeddedBuilder.get())));
            } else {
                // nothing to say
            }
        }
        return $preliminaryLookup.orElse(null);
    }

    private final JMethod removeDefaultFactory(final ClassOutline clazz) {
        final var $ObjectFactory = clazz._package().objectFactory();
        final var $factoryLookup = getMethod($ObjectFactory, guessFactoryName(clazz));
        if ($factoryLookup.isPresent()) {
            final var $factory = $factoryLookup.get();
            LOG.info(REMOVE_FACTORY, $ObjectFactory.fullName(), $factory.name());
            $ObjectFactory.methods().remove($factory);
            assertThat(getMethod($ObjectFactory, guessFactoryName(clazz))).isNull();
        }
        return $factoryLookup.orElse(null);
    }

}
