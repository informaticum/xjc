package de.informaticum.xjc.plugins;

import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugins.BoilerplatePlugin.BECAUSE_METHOD_ALREADY_EXISTS;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.COLLECTION_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.COLLECTION_SETTER_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.COLLECTION_SETTER_JAVADOC;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.FINAL_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.FINAL_FIELD_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.FINAL_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.FINAL_GETTER_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.FINAL_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.FINAL_SETTER_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.GETTER_JAVADOC_END;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.HINT_DEFAULTED_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.HINT_DEFAULTED_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.HINT_DEFENSIVE_COPY_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.HINT_EMPTY_COLLECTION_CONTAINER;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.HINT_LIVE_REFERENCE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.HINT_NULLABLE_VALUE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.HINT_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_DEFAULTED_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_DEFAULTED_VALUE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_DEFENSIVE_COPY_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_DEFENSIVE_COPY_COLLECTION_CONTAINER;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_EMPTY_CONTAINER;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_LIVE_REFERENCE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_LIVE_REFERENCE_CONTAINER;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_NULLABLE_VALUE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_OPTIONAL_VALUE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_REQUIRED_VALUE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.NOTE_UNMODIFIABLE_COLLECTION_CONTAINER;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTIONAL_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTIONAL_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTIONAL_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTIONAL_ORDEFAULT_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTIONAL_VALUE_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.ORBUILTIN_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.ORBUILTIN_JAVADOC;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.ORBUILTIN_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.ORDEFAULT_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.ORDEFAULT_JAVADOC;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.ORDEFAULT_PARAM;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.ORDEFAULT_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.PRIVATE_FIELDS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.PRIVATE_FIELD_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.REFACTORED_GETTER_IMPLNOTE_INTRO;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.REFACTORED_GETTER_IMPLNOTE_OUTRO;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.REMOVED_SETTERS_IMPLNOTE;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.REMOVE_SETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.STRAIGHT_COLLECTION_OR_EMPTY_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.STRAIGHT_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.STRAIGHT_DEFAULTED_VALUE_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.STRAIGHT_GETTERS_DESCRIPTION;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.STRAIGHT_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.STRAIGHT_VALUE_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_OR_EMPTY_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.PropertyPluginMessages.UNMODIFIABLE_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.$this;
import static de.informaticum.xjc.util.CodeModelAnalysis.deoptionalisedTypeFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.isCollectionMethod;
import static de.informaticum.xjc.util.CodeModelAnalysis.isOptionalMethod;
import static de.informaticum.xjc.util.CodeModelAnalysis.javadocNameOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.javadocSimpleNameOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.optionalTypeFor;
import static de.informaticum.xjc.util.CodeModelAnalysis.render;
import static de.informaticum.xjc.util.CodeModelAnalysis.unmodifiableViewFactoryFor;
import static de.informaticum.xjc.util.CodeRetrofit.eraseBody;
import static de.informaticum.xjc.util.CodeRetrofit.eraseJavadoc;
import static de.informaticum.xjc.util.CodeRetrofit.javadocBreak;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static de.informaticum.xjc.util.OutlineAnalysis.filter;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedGettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedSettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.guessSetterName;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.OutlineAnalysis.javadocNameOf;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import de.informaticum.xjc.api.CommandLineArgument;
import de.informaticum.xjc.api.ResourceBundleEntry;
import de.informaticum.xjc.util.CodeModelAnalysis;
import de.informaticum.xjc.util.OutlineAnalysis;
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
    private static final String MODIFY_METHOD = "Set {} of method [{}#{}] onto [{}].";
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
        REMOVE_SETTERS.deactivates(FINAL_SETTERS);
        REMOVE_SETTERS.deactivates(COLLECTION_SETTERS);
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
        COLLECTION_SETTERS.doOnActivation(this::addCollectionSetter, clazz);
        FINAL_SETTERS.doOnActivation(this::setSettersFinal, clazz); // finalise after collection-setter creation
        return true;
    }

    private final List<JFieldVar> setFieldsPrivate(final ClassOutline clazz) {
        final var $fields = generatedPropertiesOf(clazz).values().stream();
        final var $modified = $fields.peek($f -> {
            LOG.info(MODIFY_PROPERTY, "accessibility", fullNameOf(clazz), $f.name(), "private");
            javadocSection($f).append(PRIVATE_FIELD_IMPLNOTE.text());
            $f.mods().setPrivate();
        });
        return $modified.collect(toList());
    }

    private final List<JFieldVar> setFieldsFinal(final ClassOutline clazz) {
        final var $fields = generatedPropertiesOf(clazz).values().stream();
        final var $modified = $fields.peek($f -> {
            LOG.info(MODIFY_PROPERTY, "mutability", fullNameOf(clazz), $f.name(), "final");
            javadocSection($f).append(FINAL_FIELD_IMPLNOTE.text());
            $f.mods().setFinal(true);
        });
        return $modified.collect(toList());
    }

    private final List<JMethod> setGettersFinal(final ClassOutline clazz) {
        final var $getters = generatedGettersOf(clazz).values().stream();
        final var $modified = $getters.peek($g -> {
            LOG.info(MODIFY_METHOD, "mutability", fullNameOf(clazz), $g, "final");
            javadocSection($g).append(FINAL_GETTER_IMPLNOTE.text());
            $g.mods().setFinal(true);
        });
        return $modified.collect(toList());
    }

    private static final class GetterBricks {
        private final Entry<? extends FieldOutline, ? extends JMethod> getter;
        private final JFieldVar $property;
        private final FieldOutline attribute;
        private final CPropertyInfo attributeInfo;
        private final JMethod $getter;
        private final JType $returnType;
        private final JClass $OptionalType;
        private final JFieldRef $prop;
        private final Optional<JExpression> $default;
        private final JExpression $nonNull;
        private final JInvocation $optionalEmpty;
        private final JInvocation $optionalOf;
        private final JInvocation $view() {
            // no precalculation, on-demand only ($returnType might be non-collection type and that causes an IllegalArgumentException)
            return unmodifiableViewFactoryFor(this.$returnType).arg(this.$prop);
        }
        public GetterBricks(final Entry<? extends FieldOutline, ? extends JMethod> getter, final JFieldVar $property) {
            this.getter = getter;
            this.$property = $property;
            this.attribute = this.getter.getKey();
            this.attributeInfo = this.attribute.getPropertyInfo();
            this.$getter = this.getter.getValue();
            this.$returnType = this.$getter.type();
            this.$OptionalType = optionalTypeFor(this.$returnType);
            this.$prop = $this.ref(this.$property);
            this.$default = defaultExpressionFor(this.attribute);
            this.$nonNull = effectiveExpressionForNonNull(this.$property.type(), this.$prop);
            this.$optionalEmpty = this.$OptionalType.erasure().staticInvoke("empty");
            this.$optionalOf = this.$OptionalType.erasure().staticInvoke("of");
        }
    }

    private static enum GetterRefactoring {

        PRIMITIVE_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_VALUE_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return($.$nonNull);
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($)};
            }
        },

        DEFAULTED_UNMODIFIABLE_COLLECTION_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, UNMODIFIABLE_COLLECTION_OR_EMPTY_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return(cond($.$prop.eq($null), $.$default.get(), $.$view()));
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION, HINT_DEFAULTED_UNMODIFIABLE_COLLECTION, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION};
            }
        },

        DEFAULTED_MODIFIABLE_COLLECTION_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_COLLECTION_OR_EMPTY_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return(cond($.$prop.eq($null), $.$default.get(), $.$nonNull));
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_DEFAULTED_COLLECTION, HINT_DEFAULTED_COLLECTION, NOTE_REFERENCE.apply($), HINT_REFERENCE.apply($)};
            }
        },

        OPTIONAL_UNMODIFIABLE_COLLECTION_PROPERTY(OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, OPTIONAL_UNMODIFIABLE_COLLECTION_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return(cond($.$prop.eq($null), $.$optionalEmpty, $.$optionalOf.arg($.$view())));
            }
            @Override protected final Optional<JType> returnType(final GetterBricks $) {
                return Optional.of($.$OptionalType);
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_UNMODIFIABLE_COLLECTION_CONTAINER, HINT_UNMODIFIABLE_COLLECTION};
            }
            @Override protected void supersedeJavadoc(final GetterBricks $) {
                this.supersedeJavadoc($, javadocSimpleNameOf($.$OptionalType));
            }
        },

        OPTIONAL_MODIFIABLE_COLLECTION_PROPERTY(OPTIONAL_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, OPTIONAL_COLLECTION_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return(cond($.$prop.eq($null), $.$optionalEmpty, $.$optionalOf.arg($.$nonNull)));
            }
            @Override protected final Optional<JType> returnType(final GetterBricks $) {
                return Optional.of($.$OptionalType);
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_REFERENCE_CONTAINER.apply($), HINT_REFERENCE.apply($)};
            }
            @Override protected void supersedeJavadoc(final GetterBricks $) {
                this.supersedeJavadoc($, javadocSimpleNameOf($.$OptionalType));
            }
        },

        UNMODIFIABLE_COLLECTION_PROPERTY(UNMODIFIABLE_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, UNMODIFIABLE_COLLECTION_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return(cond($.$prop.eq($null), $null, $.$view()));
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION};
            }
        },

        MODIFIABLE_COLLECTION_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_COLLECTION_RETURN) {
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_REFERENCE.apply($), HINT_REFERENCE.apply($)};
            }
        },

        DEFAULTED_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_DEFAULTED_VALUE_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return(cond($.$prop.eq($null), $.$default.get(), $.$nonNull));
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_DEFAULTED_VALUE};
            }
            @Override protected void supersedeJavadoc(final GetterBricks $) {
                this.supersedeJavadoc($, render($.$default.get()));
            }
        },

        OPTIONAL_PROPERTY(OPTIONAL_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, OPTIONAL_VALUE_RETURN) {
            @Override protected final JExpression returnExpression(final GetterBricks $) {
                return(cond($.$prop.eq($null), $.$optionalEmpty, $.$optionalOf.arg($.$nonNull)));
            }
            @Override protected final Optional<JType> returnType(final GetterBricks $) {
                return Optional.of($.$OptionalType);
            }
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_EMPTY_CONTAINER};
            }
            @Override protected void supersedeJavadoc(final GetterBricks $) {
                this.supersedeJavadoc($, javadocSimpleNameOf($.$OptionalType));
            }
        },

        STRAIGHT_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_VALUE_RETURN) {
            @Override protected final ResourceBundleEntry[] notes(final GetterBricks $) {
                return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE};
            }
        };

        private static final Function<GetterBricks, PropertyPluginMessages> DECLARATION_TYPE         = b -> isRequired(b.attribute) ? NOTE_REQUIRED_VALUE : NOTE_OPTIONAL_VALUE;
        private static final Function<GetterBricks, PropertyPluginMessages> NOTE_REFERENCE           = b -> (b.$prop == b.$nonNull) ? NOTE_LIVE_REFERENCE : NOTE_DEFENSIVE_COPY_COLLECTION;
        private static final Function<GetterBricks, PropertyPluginMessages> HINT_REFERENCE           = b -> (b.$prop == b.$nonNull) ? HINT_LIVE_REFERENCE : HINT_DEFENSIVE_COPY_COLLECTION;
        private static final Function<GetterBricks, PropertyPluginMessages> NOTE_REFERENCE_CONTAINER = b -> (b.$prop == b.$nonNull) ? NOTE_LIVE_REFERENCE_CONTAINER : NOTE_DEFENSIVE_COPY_COLLECTION_CONTAINER;

        private final ResourceBundleEntry introJavadoc;
        private final ResourceBundleEntry outroJavadoc;
        private final ResourceBundleEntry returnJavadoc;

        private GetterRefactoring(final ResourceBundleEntry introJavadoc, final ResourceBundleEntry outroJavadoc, final ResourceBundleEntry returnJavadoc) {
            this.introJavadoc = introJavadoc;
            this.outroJavadoc = outroJavadoc;
            this.returnJavadoc = returnJavadoc;
        }

        protected JExpression returnExpression(final GetterBricks bricks) {
            if (render(bricks.$prop).equals(render(bricks.$nonNull))) {
                return bricks.$prop;
            } else {
                return cond(bricks.$prop.eq($null), $null, bricks.$nonNull);
            }
        }

        protected Optional<JType> returnType(final GetterBricks bricks) {
            return Optional.empty();
        }

        protected abstract ResourceBundleEntry[] notes(final GetterBricks bricks);

        protected void supersedeJavadoc(final GetterBricks bricks) {
            this.supersedeJavadoc(bricks, javadocSimpleNameOf(bricks.$returnType));
        }

        protected final void supersedeJavadoc(final GetterBricks bricks, final String noteArg) {
            final var $javadoc = bricks.getter.getValue().javadoc();
            eraseJavadoc($javadoc).append(this.introJavadoc.format(javadocNameOf(bricks.attribute.parent().getImplClass()), javadocNameOf(bricks.$property)));
            for (final var note : this.notes(bricks)) {
                javadocBreak($javadoc).append(note.format(noteArg));
            }
            javadocBreak($javadoc).append(this.outroJavadoc.text());
            final var $return = bricks.getter.getValue().javadoc().addReturn();
            eraseJavadoc($return).append(this.returnJavadoc.format(javadocNameOf(bricks.attribute.parent().getImplClass()), javadocNameOf(bricks.$property), noteArg));
        }

        public final void supersedeGetter(final GetterBricks bricks) {
            eraseBody(bricks.$getter)._return(this.returnExpression(bricks));
            this.returnType(bricks).ifPresent(t -> bricks.$getter.type(t));
            this.supersedeJavadoc(bricks);
        }

    }

    private final ClassOutline refactorGetter(final ClassOutline clazz) {
        final var properties = generatedPropertiesOf(clazz);
        for (final var getter : generatedGettersOf(clazz).entrySet()) {
            assertThat(properties).containsKey(getter.getKey());
            final var $bricks = new GetterBricks(getter, properties.get(getter.getKey()));
            final var originJavadoc = new ArrayList<>($bricks.$getter.javadoc());

            if ($bricks.$property.type().isPrimitive()) {
                assertThat($bricks.$getter).matches(not(CodeModelAnalysis::isCollectionMethod));
                assertThat(isOptionalMethod($bricks.$getter)).isFalse();
                assertThat($bricks.$returnType.isPrimitive()).isTrue();
                assertThat($bricks.$returnType.isReference()).isFalse();
                LOG.info(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $bricks.$getter.name());
                GetterRefactoring.PRIMITIVE_PROPERTY.supersedeGetter($bricks);
            } else if ($bricks.attributeInfo.isCollection()) {
                assertThat($bricks.$getter).matches(CodeModelAnalysis::isCollectionMethod);
                assertThat(isOptionalMethod($bricks.$getter)).isFalse();
                assertThat($bricks.$returnType.isPrimitive()).isFalse();
                assertThat($bricks.$returnType.isReference()).isTrue();
                if ($bricks.$default.isPresent() && UNMODIFIABLE_COLLECTIONS.isActivated()) {
                    LOG.info(REFACTOR_AS_UNMODIFIABLE_AND_DEFAULTED, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.DEFAULTED_UNMODIFIABLE_COLLECTION_PROPERTY.supersedeGetter($bricks);
                } else if ($bricks.$default.isPresent()) {
                    assertThat(UNMODIFIABLE_COLLECTIONS.isActivated()).isFalse();
                    LOG.info(REFACTOR_AS_DEFAULTED, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.DEFAULTED_MODIFIABLE_COLLECTION_PROPERTY.supersedeGetter($bricks);
                } else if (OPTIONAL_GETTERS.isActivated() && isOptional($bricks.attribute) && UNMODIFIABLE_COLLECTIONS.isActivated()) {
                    LOG.info(REFACTOR_AS_UNMODIFIABLE_AND_OPTIONAL, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.OPTIONAL_UNMODIFIABLE_COLLECTION_PROPERTY.supersedeGetter($bricks);
                } else if (OPTIONAL_GETTERS.isActivated() && isOptional($bricks.attribute)) {
                    assertThat(UNMODIFIABLE_COLLECTIONS.isActivated()).isFalse();
                    LOG.info(REFACTOR_AS_OPTIONAL, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.OPTIONAL_MODIFIABLE_COLLECTION_PROPERTY.supersedeGetter($bricks);
                } else if (UNMODIFIABLE_COLLECTIONS.isActivated()) {
                    LOG.info(REFACTOR_AS_UNMODIFIABLE, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.UNMODIFIABLE_COLLECTION_PROPERTY.supersedeGetter($bricks);
                } else {
                    assertThat(UNMODIFIABLE_COLLECTIONS.isActivated()).isFalse();
                    LOG.info(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.MODIFIABLE_COLLECTION_PROPERTY.supersedeGetter($bricks);
                }
            // } else if ($.$returnType.isArray()) { // TODO: handle array type similar to collections (defensive copies, non-modifiable, etc.)
            } else {
                assertThat($bricks.$getter).matches(not(CodeModelAnalysis::isCollectionMethod));
                assertThat(isOptionalMethod($bricks.$getter)).isFalse();
                // assertThat($.$returnType.isPrimitive()).isFalse(); // TODO: return type may be primitive, even if property is not
                // assertThat($.$returnType.isReference()).isTrue();  // TODO: return type may be primitive, even if property is not
                if ($bricks.$default.isPresent()) {
                    LOG.info(REFACTOR_AS_DEFAULTED, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.DEFAULTED_PROPERTY.supersedeGetter($bricks);
                } else if (OPTIONAL_GETTERS.isActivated() && isOptional($bricks.attribute)) {
                    assertThat(isOptionalMethod($bricks.$getter)).withFailMessage("This case is not considered yet ;-(").isFalse(/* TODO: Handle getters that already return Optional */);
                    LOG.info(REFACTOR_AS_OPTIONAL, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.OPTIONAL_PROPERTY.supersedeGetter($bricks);
                } else {
                    LOG.info(REFACTOR_JUST_STRAIGHT, fullNameOf(clazz), $bricks.$getter.name());
                    GetterRefactoring.STRAIGHT_PROPERTY.supersedeGetter($bricks);
                }
            }

            javadocSection($bricks.$getter).append(REFACTORED_GETTER_IMPLNOTE_INTRO.text());
            $bricks.$getter.javadoc().addAll(originJavadoc);
            $bricks.$getter.javadoc().append(REFACTORED_GETTER_IMPLNOTE_OUTRO.text());
        }
        return clazz;
    }

    private final List<JMethod> generateOrDefaultGetters(final ClassOutline clazz) {
        final var getters = generatedGettersOf(clazz);
        final var properties = generatedPropertiesOf(clazz);
        getters.values().removeIf(g -> !isOptionalMethod(g));
        final var $generated = new ArrayList<JMethod>();
        for (final var getter : getters.entrySet()) {
            final var attribute = getter.getKey();
            assertThat(properties).containsKey(attribute);
            final var $property = properties.get(attribute);
            final var $getOrDefault = this.generateOrDefaultGetter(clazz, getter, $property);
            final var $getOrBuiltin = this.generateOrBuiltinGetter(clazz, getter, $property, $getOrDefault);
            $generated.add($getOrDefault);
            $getOrBuiltin.ifPresent($g -> $generated.add($g));
        }
        return $generated;
    }

    private final JMethod generateOrDefaultGetter(final ClassOutline clazz, final Entry<? extends FieldOutline, ? extends JMethod> getter, final JFieldVar $property) {
        final var $ImplClass = clazz.implClass;
        final var $getter = getter.getValue();
        final var methodMods = $getter.mods().getValue();
        final var $methodType = deoptionalisedTypeFor($getter.type().boxify()).orElse($property.type());
        final var methodName = $getter.name() + "OrDefault";
        // 0/2: Preliminary
        final var $preliminaryLookup = getMethod(clazz, methodName, $methodType);
        if ($preliminaryLookup.isPresent()) {
            LOG.warn(SKIP_ORDEFAULT, fullNameOf(clazz), methodName, $methodType, $property.name(), BECAUSE_METHOD_ALREADY_EXISTS);
            return $preliminaryLookup.get();
        }
        // 1/2: Create
        LOG.info(GENERATE_ORDEFAULT, fullNameOf(clazz), methodName, $methodType, $property.name());
        final var $getOrDefault = $ImplClass.method(methodMods, $methodType, methodName);
        final var $defaultValue = $getOrDefault.param(FINAL, $methodType, "defaultValue");
        javadocSection($getOrDefault).append(ORDEFAULT_JAVADOC.format(javadocNameOf(clazz), javadocNameOf($getter), $defaultValue.name()));
        javadocSection($getOrDefault).append(ORDEFAULT_IMPLNOTE.text());
        javadocSection($getOrDefault.javadoc().addParam($defaultValue)).append(ORDEFAULT_PARAM.format(javadocNameOf(clazz), javadocNameOf($getter)));
        javadocSection($getOrDefault.javadoc().addReturn()).append(ORDEFAULT_RETURN.format(javadocNameOf(clazz), javadocNameOf($getter), $defaultValue.name()));
        // 2/2: Implement
        $getOrDefault.body()._return($this.invoke($getter).invoke("orElse").arg($defaultValue));
        return $getOrDefault;
    }

    private final Optional<JMethod> generateOrBuiltinGetter(final ClassOutline clazz, final Entry<? extends FieldOutline, ? extends JMethod> getter, final JFieldVar $property, final JMethod $getOrDefault) {
        final var $ImplClass = clazz.implClass;
        final var attribute = getter.getKey();
        final var $getter = getter.getValue();
        final var methodMods = $getOrDefault.mods().getValue();
        final var $methodType = $getOrDefault.type();
        final var methodName = isCollectionMethod($getOrDefault) ? $getter.name() + "OrEmpty" : $getOrDefault.name();
        // 0/2: Preliminary
        final var $preliminaryLookup = getMethod(clazz, methodName);
        if ($preliminaryLookup.isPresent()) {
            LOG.warn(SKIP_ORDEFAULT, fullNameOf(clazz), methodName, "", $property.name(), BECAUSE_METHOD_ALREADY_EXISTS);
            return $preliminaryLookup;
        }
        final var $builtinValue = OutlineAnalysis.defaultExpressionFor(attribute, true, UNMODIFIABLE_COLLECTIONS.isActivated());
        if ($builtinValue.isEmpty()) {
            LOG.info(SKIP_ORDEFAULT, fullNameOf(clazz), methodName, "", $property.name(), BECAUSE_NO_DEFAULT_EXISTS);
            return Optional.empty();
        }
        // 1/2: Create
        LOG.info(GENERATE_ORDEFAULT, fullNameOf(clazz), methodName, "", $property.name());
        final var $getOrBuiltin = $ImplClass.method(methodMods, $methodType, methodName);
        javadocSection($getOrBuiltin).append(ORBUILTIN_JAVADOC.format(javadocNameOf(clazz), javadocNameOf($getter), render($builtinValue.get())));
        javadocSection($getOrBuiltin).append(ORBUILTIN_IMPLNOTE.text());
        javadocSection($getOrBuiltin.javadoc().addReturn()).append(ORBUILTIN_RETURN.format(javadocNameOf(clazz), javadocNameOf($getter), render($builtinValue.get())));
        // 2/2: Implement
        $getOrBuiltin.body()._return($this.invoke($getOrDefault).arg($builtinValue.get()));
        return Optional.of($getOrBuiltin);
    }

    private final List<JMethod> addCollectionSetter(final ClassOutline clazz) {
        final var $generated = new ArrayList<JMethod>();
        for (final var property : filter(generatedPropertiesOf(clazz), k -> k.getPropertyInfo().isCollection()).entrySet()) {
            final var attribute = property.getKey();
            assertThat(attribute.getPropertyInfo().isCollection()).isTrue();
            assertThat(attribute.getPropertyInfo().defaultValue).isNull();
            final var $property = property.getValue();
            assertThat($property.type().isPrimitive()).isFalse();
            final var setterName = guessSetterName(attribute);
            // 0/2: Preliminary
            final var $preliminaryLookup = getMethod(clazz, setterName, $property.type());
            if ($preliminaryLookup.isPresent()) {
                LOG.warn(SKIP_SETTER, fullNameOf(clazz), setterName, $property.type(), $property.name(), BECAUSE_METHOD_ALREADY_EXISTS);
                $generated.add($preliminaryLookup.get());
                continue;
            }
            // 1/2: Create
            LOG.info(GENERATE_SETTER, fullNameOf(clazz), setterName, $property.type(), $property.name());
            final var $ImplClass = clazz.implClass;
            final var $setter = $ImplClass.method(PUBLIC, this.codeModel().VOID, setterName);
            javadocSection($setter).append(COLLECTION_SETTER_JAVADOC.format(javadocNameOf($property)));
            javadocSection($setter).append(COLLECTION_SETTER_IMPLNOTE.text());
            // 2/2: Implement
            final var $param = $setter.param(FINAL, $property.type(), $property.name());
            accordingAssignmentAndJavadoc(property, $setter, $param);
            $generated.add($setter);
        }
        return $generated;
    }

    private final List<JMethod> setSettersFinal(final ClassOutline clazz) {
        final var $setters = generatedSettersOf(clazz).values().stream();
        final var $modified = $setters.peek($s -> {
            LOG.info(MODIFY_METHOD, "mutability", fullNameOf(clazz), $s, "final");
            javadocSection($s).append(FINAL_SETTER_IMPLNOTE.text());
            $s.mods().setFinal(true);
        });
        return $modified.collect(toList());
    }

    private final List<JMethod> removeSetter(final ClassOutline clazz) {
        final var $ImplClass = clazz.implClass;
        final var $setters = generatedSettersOf(clazz).values().stream();
        final var $removed = $setters.peek($s -> {
            LOG.info(REMOVE_SETTER, fullNameOf(clazz), $s.name());
            $ImplClass.methods().remove($s);
        }).collect(toList());
        if (!$removed.isEmpty()) {
            javadocSection($ImplClass).append(REMOVED_SETTERS_IMPLNOTE.text());
        }
        return $removed;
    }

}
