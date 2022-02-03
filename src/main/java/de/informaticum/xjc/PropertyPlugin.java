package de.informaticum.xjc;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.ConstructionPlugin.appendParameterJavaDoc;
import static de.informaticum.xjc.plugin.TargetSugar.$null;
import static de.informaticum.xjc.plugin.TargetSugar.$this;
import static de.informaticum.xjc.util.CodeRetrofit.eraseBody;
import static de.informaticum.xjc.util.CollectionAnalysis.defaultInstanceOf;
import static de.informaticum.xjc.util.CollectionAnalysis.emptyImmutableInstanceOf;
import static de.informaticum.xjc.util.CollectionAnalysis.isCollectionMethod;
import static de.informaticum.xjc.util.CollectionAnalysis.unmodifiableViewFactoryFor;
import static de.informaticum.xjc.util.DefaultAnalysis.defaultValueFor;
import static de.informaticum.xjc.util.OptionalAnalysis.isOptionalMethod;
import static de.informaticum.xjc.util.OptionalAnalysis.isPrimitiveOptional;
import static de.informaticum.xjc.util.OptionalAnalysis.optionalTypeFor;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedGettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedPropertiesOf;
import static de.informaticum.xjc.util.OutlineAnalysis.generatedSettersOf;
import static de.informaticum.xjc.util.OutlineAnalysis.getMethod;
import static de.informaticum.xjc.util.OutlineAnalysis.isOptional;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static de.informaticum.xjc.util.Printify.implode;
import static de.informaticum.xjc.util.Printify.render;
import static de.informaticum.xjc.util.XjcPropertyGuesser.guessSetterName;
import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.ClassOutline;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import de.informaticum.xjc.util.CollectionAnalysis;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

public class PropertyPlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(PropertyPlugin.class);

    private static final String OPTION_NAME = "informaticum-xjc-properties";
    private static final String OPTION_DESC = "Refactors the code of type fields (i.e., origin field, according getter or setter).";
    private static final CommandLineArgument PRIVATE_FIELDS                = new CommandLineArgument("properties-private-fields",         "Modifies the visibility of the generated fields onto 'private'. Default: false");
    private static final CommandLineArgument FINAL_FIELDS                  = new CommandLineArgument("properties-final-fields",           "Modifies the generated fields onto 'final'. Default: false");
    private static final CommandLineArgument GENERATE_STRAIGHT_GETTERS     = new CommandLineArgument("properties-straight-getters",       "Refactor collection fields' getter methods with immediate return statement, i.e., without previous implicit field assigment in case of an actual 'null' value. Default: false");
    private static final CommandLineArgument GENERATE_UNMODIFIABLE_GETTERS = new CommandLineArgument("properties-unmodifiable-getters",   "Replace return value for collection fields' getter methods with an unmodifiable view. Default: false");
    private static final CommandLineArgument GENERATE_OPTIONAL_GETTERS     = new CommandLineArgument("properties-optional-getters",       "Replace return type [T] of optional fields' getter methods with [OptionalDouble]/[OptionalInt]/[OptionalLong]/[Optional<T>]. Default: false");
    private static final CommandLineArgument GENERATE_COLLECTION_SETTERS   = new CommandLineArgument("properties-collection-setters",     "Generates setter methods for collection fields. Default: false");
    private static final CommandLineArgument GENERATE_COLLECTIONINIT       = new CommandLineArgument("properties-initialise-collections", "Each time a collection initialisation statement is generated, the value will not be 'null' but the empty collection instance. Default: false");
    private static final CommandLineArgument REMOVE_SETTERS                = new CommandLineArgument("properties-remove-setters",         "Removes the property setters. Default: false");

    private static final String STRAIGHT_VALUE_JAVADOC_MESSAGE                   = implode("This method returns                         the value of the                     attribute {@link #%1$s}; plus notably%n<ul>%n<li>this method neither checks for {@code null} value nor it returns an alternative value,</li>%n                        <li>hence, your business code either must handle {@code null} values appropriately or must ensure valid (non-{@code null}-attributed) instances at all time (even if unmarshalled from unknown, untrustworthy XML sources).</li>%n                                         </ul>");
    private static final String STRAIGHT_DEFAULTED_VALUE_JAVADOC_MESSAGE         = implode("This method returns                         the value of the                     attribute {@link #%1$s}; plus notably%n<ul>%n<li>if the current value is {@code null}, the                 default value {@code %2$s} will be returned instead.</li>%n                                                                                                                                                                                                                                                                           </ul>");
    private static final String STRAIGHT_COLLECTION_JAVADOC_MESSAGE              = implode("This method returns                         the value of the          collection attribute {@link #%1$s}; plus notably%n<ul>%n<li>if the current value is {@code null}, an                          empty {@link %2$s} will be returned instead,</li>%n                                                                                                                                            <li>in particular, the empty {@link %2$s} is not a live {@link %2$s} anymore and cannot be utilised to add/remove items!</li>%n</ul>");
    private static final String UNMODIFIABLE_COLLECTION_JAVADOC_MESSAGE          = implode("This method returns an unmodifiable view of the value of the          collection attribute {@link #%1$s}; plus notably%n<ul>%n<li>if the current value is {@code null}, an             unmodifiable empty {@link %2$s} will be returned instead,</li>%n                                                                                                                                            <li>in particular, either unmodifiable view                                      cannot be utilised to add/remove items!</li>%n</ul>");
    private static final String OPTIONAL_VALUE_JAVADOC_MESSAGE                   = implode("This method returns                         the value of the optional            attribute {@link #%1$s}; plus notably%n<ul>%n<li>if the current value is {@code null}, an {@linkplain %2$s#empty() empty        %2$s} will be returned instead.</li>%n                                                                                                                                                                                                                                                                           </ul>");
    private static final String OPTIONAL_COLLECTION_JAVADOC_MESSAGE              = implode("This method returns                         the value of the optional collection attribute {@link #%1$s}; plus notably%n<ul>%n<li>if the current value is {@code null}, an {@linkplain %2$s#empty() empty        %2$s} will be returned instead,</li>%n<li>if the current value is not {@code null}, a non-empty {@link %2$s} (holding that value) will be returned,</li>%n                        <li>in particular, the empty {@link %2$s}                                        cannot be utilised to add/remove items!</li>%n</ul>");
    private static final String OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_MESSAGE = implode("This method returns an unmodifiable view of the value of the optional collection attribute {@link #%1$s}; plus notably%n<ul>%n<li>if the current value is {@code null}, an {@linkplain %2$s#empty() empty        %2$s} will be returned instead,</li>%n<li>if the current value is not {@code null}, a non-empty {@link %2$s} (holding an unmodifiable view of that value) will be returned,</li>%n<li>in particular, the    unmodifiable view                                      cannot be utilised to add/remove items!</li>%n</ul>");

    private static final String STRAIGHT_VALUE_JAVADOC_SUMMARY                   = implode("                                            the value of the                     attribute {@link #%1$s}                                                                                                                                                    ");
    private static final String STRAIGHT_DEFAULTED_VALUE_JAVADOC_SUMMARY         = implode("                                            the value of the                     attribute {@link #%1$s}%n(or                                                                                             {@code %2$s} if the current value is {@code null})");
    private static final String STRAIGHT_COLLECTION_JAVADOC_SUMMARY              = implode("                                            the value of the          collection attribute {@link #%1$s}%n(or                                                                                       empty {@link %2$s} if the current value is {@code null})");
    private static final String UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY          = implode("                    an unmodifiable view of the value of the          collection attribute {@link #%1$s}%n(or                                                                          unmodifiable empty {@link %2$s} if the current value is {@code null})");
    private static final String OPTIONAL_VALUE_JAVADOC_SUMMARY                   = implode("                                            the value of the optional            attribute {@link #%1$s}%n(or                                                              {@linkplain %2$s#empty() empty        %2$s} if the current value is {@code null})");
    private static final String OPTIONAL_COLLECTION_JAVADOC_SUMMARY              = implode("                                            the value of the optional collection attribute {@link #%1$s}%n(or                                                              {@linkplain %2$s#empty() empty        %2$s} if the current value is {@code null})");
    private static final String OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY = implode("                    an unmodifiable view of the value of the optional collection attribute {@link #%1$s}%n(or                                                              {@linkplain %2$s#empty() empty        %2$s} if the current value is {@code null})");

    private static final String JAVADOC_PREFIX = "%1$s%n%n<p><em>Please note</em>: This method <a href=\"https://github.com/informaticum/xjc\">has been refactored by the informaticum's XJC plugins</a> during the JAXB/XJC code generating process.%n%n<p>For your information, the former description was:%n<blockquote>%n";
    private static final String JAVADOC_SUFFIX = "%n</blockquote>";

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESC);
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return Arrays.asList(PRIVATE_FIELDS, FINAL_FIELDS, GENERATE_STRAIGHT_GETTERS, GENERATE_UNMODIFIABLE_GETTERS, GENERATE_OPTIONAL_GETTERS, GENERATE_COLLECTION_SETTERS, GENERATE_COLLECTIONINIT, REMOVE_SETTERS);
    }

    @Override
    public void onActivated(final Options options)
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
        // TODO: FINAL_FIELDS.alsoActivate(GENERATE_STRAIGHT_GETTERS or GENERATE_UNMODIFIABLE_GETTERS or GENERATE_OPTIONAL_GETTERS ? );
        // TODO: Consider GENERATE_UNMODIFIABLE_GETTERS.alsoActivate(FINAL_FIELDS); ?
        // TODO: (1) GENERATE_COLLECTION_SETTERS disables REMOVE_SETTERS or (2) abort execution or (3) let it happen (generate and remove immediately)?
        return true;
    }

    @Override
    protected final boolean runClass(final ClassOutline clazz) {
        PRIVATE_FIELDS.doOnActivation(this::setFieldsPrivate, clazz);
        FINAL_FIELDS.doOnActivation(this::setFieldsFinal, clazz);
        GENERATE_STRAIGHT_GETTERS.or(GENERATE_UNMODIFIABLE_GETTERS).or(GENERATE_OPTIONAL_GETTERS).doOnActivation(this::refactorGetter, clazz);
        GENERATE_COLLECTION_SETTERS.doOnActivation(this::addCollectionSetter, clazz);
        REMOVE_SETTERS.doOnActivation(this::removeSetter, clazz);
        return true;
    }

    private final void setFieldsPrivate(final ClassOutline clazz) {
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            LOG.info("Set accessibility of property [{}#{}] onto [private].", fullNameOf(clazz), $property.name());
            $property.mods().setPrivate();
        }
    }

    private final void setFieldsFinal(final ClassOutline clazz) {
        for (final var $property : generatedPropertiesOf(clazz).values()) {
            LOG.info("Set mutability of property [{}#{}] onto [final].", fullNameOf(clazz), $property.name());
            $property.mods().setFinal(true);
        }
    }

    private final void refactorGetter(final ClassOutline clazz) {
        final var properties = generatedPropertiesOf(clazz);
        for (final var getter : generatedGettersOf(clazz).entrySet()) {
            final var attribute = getter.getKey();
            final var attributeInfo = attribute.getPropertyInfo();
            assertThat(properties).containsKey(attribute);
            final var $property = properties.get(attribute);
            final var $getter = getter.getValue();
            final var $ReturnType = $getter.type();
            final var $OptionalType = optionalTypeFor($ReturnType);

            final String javadocMsg;
            final String javadocRet;
            final JExpression $statement;
            if (attributeInfo.isCollection()) {
                assertThat(attributeInfo.defaultValue).isNull();
                assertThat($getter).matches(CollectionAnalysis::isCollectionMethod);
                assertThat(isOptionalMethod($getter)).isFalse();
                assertThat($ReturnType.isPrimitive()).isFalse();
                assertThat($ReturnType.isReference()).isTrue();
                if (GENERATE_OPTIONAL_GETTERS.isActivated() && isOptional(attribute) && GENERATE_UNMODIFIABLE_GETTERS.isActivated()) {
                    LOG.debug("Refactor [{}#{}()]: Optional<X> container and unmodifiable view", fullNameOf(clazz), $getter.name());
                    javadocMsg = format(OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_MESSAGE, $property.name(), $OptionalType.erasure().name());
                    javadocRet = format(OPTIONAL_UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY, $property.name(), $OptionalType.erasure().name());
                    $statement = $OptionalType.erasure().staticInvoke("ofNullable").arg(cond($this.ref($property).eq($null), $null, unmodifiableViewFactoryFor($ReturnType).arg($this.ref($property))));
                    $getter.type($OptionalType);
                } else if (GENERATE_OPTIONAL_GETTERS.isActivated() && isOptional(attribute) && GENERATE_UNMODIFIABLE_GETTERS.isNotActivated()) {
                    LOG.debug("Refactor [{}#{}()]: Optional<X> container", fullNameOf(clazz), $getter.name());
                    javadocMsg = format(OPTIONAL_COLLECTION_JAVADOC_MESSAGE, $property.name(), $OptionalType.erasure().name());
                    javadocRet = format(OPTIONAL_COLLECTION_JAVADOC_SUMMARY, $property.name(), $OptionalType.erasure().name());
                    $statement = $OptionalType.erasure().staticInvoke("ofNullable").arg($this.ref($property));
                    $getter.type($OptionalType);
                } else if (GENERATE_UNMODIFIABLE_GETTERS.isActivated()) {
                    LOG.debug("Refactor [{}#{}()]: unmodifiable view", fullNameOf(clazz), $getter.name());
                    javadocMsg = format(UNMODIFIABLE_COLLECTION_JAVADOC_MESSAGE, $property.name(), $ReturnType.erasure().name());
                    javadocRet = format(UNMODIFIABLE_COLLECTION_JAVADOC_SUMMARY, $property.name(), $ReturnType.erasure().name());
                    $statement = cond($this.ref($property).eq($null), emptyImmutableInstanceOf($ReturnType), unmodifiableViewFactoryFor($ReturnType).arg($this.ref($property)));
                } else if (GENERATE_STRAIGHT_GETTERS.isActivated()) {
                    LOG.debug("Refactor [{}#{}()]: empty collection if 'null'", fullNameOf(clazz), $getter.name());
                    javadocMsg = format(STRAIGHT_COLLECTION_JAVADOC_MESSAGE, $property.name(), $ReturnType.erasure().name());
                    javadocRet = format(STRAIGHT_COLLECTION_JAVADOC_SUMMARY, $property.name(), $ReturnType.erasure().name());
                    $statement = cond($this.ref($property).eq($null), defaultInstanceOf($ReturnType), $this.ref($property));
                } else {
                    // TODO: Handle that edge case? (no options is activated at all, or GENERATE_OPTIONAL_GETTERS but required attribute)
                    javadocMsg = null;
                    javadocRet = null;
                    $statement = null;
                }
            } else {
                assertThat($getter).matches(not(CollectionAnalysis::isCollectionMethod));
                final var $default = defaultValueFor(attribute, isCollectionMethod($getter));
                if (GENERATE_STRAIGHT_GETTERS.isActivated() && $property.type().isReference() && $default.isPresent()) {
                    LOG.debug("Refactor [{}#{}()]: default value if 'null'", fullNameOf(clazz), $getter.name());
                    javadocMsg = format(STRAIGHT_DEFAULTED_VALUE_JAVADOC_MESSAGE, $property.name(), render($default.get()));
                    javadocRet = format(STRAIGHT_DEFAULTED_VALUE_JAVADOC_SUMMARY, $property.name(), render($default.get()));
                    $statement = cond($this.ref($property).eq($null), $default.get(), $this.ref($property));
                } else if (GENERATE_OPTIONAL_GETTERS.isActivated() && isOptional(attribute) && !isOptionalMethod($getter)) {
                    LOG.debug("Refactor [{}#{}()]: OptionalDouble, OptionalInt, OptionalLong, or Optional<X> container", fullNameOf(clazz), $getter.name());
                    javadocMsg = format(OPTIONAL_VALUE_JAVADOC_MESSAGE, $property.name(), $OptionalType.erasure().name());
                    javadocRet = format(OPTIONAL_VALUE_JAVADOC_SUMMARY, $property.name(), $OptionalType.erasure().name());
                    if ($property.type().isPrimitive()) {
                        $statement = $OptionalType.erasure().staticInvoke("of").arg($this.ref($property));
                    } else if (isPrimitiveOptional($OptionalType)) {
                        $statement = cond($this.ref($property).eq($null), $OptionalType.erasure().staticInvoke("empty"), $OptionalType.erasure().staticInvoke("of").arg($this.ref($property)));
                    } else {
                        $statement = $OptionalType.erasure().staticInvoke("ofNullable").arg($this.ref($property));
                    }
                    $getter.type($OptionalType);
                } else if (GENERATE_STRAIGHT_GETTERS.isActivated()) {
                    LOG.debug("Refactor [{}#{}()]: immediate value return", fullNameOf(clazz), $getter.name());
                    javadocMsg = format(STRAIGHT_VALUE_JAVADOC_MESSAGE, $property.name()); 
                    javadocRet = format(STRAIGHT_VALUE_JAVADOC_SUMMARY, $property.name());
                    $statement = $this.ref($property);
                } else {
                    // TODO: Any further case to deal with?
                    javadocMsg = null;
                    javadocRet = null;
                    $statement = null;
                }
            }
            if (javadocMsg != null) {
                $getter.javadoc().add(0, format(JAVADOC_PREFIX, javadocMsg));
                $getter.javadoc().append(format(JAVADOC_SUFFIX));
            }
            if (javadocRet != null) {
                $getter.javadoc().addReturn().clear();
                $getter.javadoc().addReturn().append(javadocRet);
            }
            if ($statement != null) {
                eraseBody($getter);
                $getter.body()._return($statement);
            }
        }
    }

    private final void addCollectionSetter(final ClassOutline clazz) {
        final var properties = generatedPropertiesOf(clazz);
        for (final var property : properties.entrySet()) {
            final var attribute = property.getKey();
            final var attributeInfo = attribute.getPropertyInfo();
            if (attributeInfo.isCollection()) {
                // TODO: All "$class = clazz.implClass" --> "$Class = clazz.implClass"
                final var $class = clazz.implClass;
                final var $property = properties.get(attribute);
                final var setterName = guessSetterName(attribute);
                if (getMethod(clazz, setterName, $property.type()) == null) {
                    LOG.info("Generate setter method [{}#{}({})] for collection property [{}].", clazz.implClass.fullName(), setterName, $property.type(), $property.name());
                    final var $setter = $class.method(PUBLIC | FINAL, clazz.implClass.owner().VOID, setterName);
                    final var $parameter = $setter.param(FINAL, $property.type(), $property.name());
                    final var $default = defaultValueFor(attribute, GENERATE_COLLECTIONINIT);
                    if ($parameter.type().isPrimitive()) {
                        $setter.body().assign($this.ref($property), $parameter);
                    } else if (isOptional(attribute) && $default.isEmpty()) {
                        $setter.body().assign($this.ref($property), $parameter);
                    } else if (isRequired(attribute) && $default.isEmpty()) {
                        $setter._throws(IllegalArgumentException.class);
                        final var $condition = $setter.body()._if($parameter.eq($null));
                        $condition._then()._throw(_new(this.reference(IllegalArgumentException.class)).arg(lit("Required field '" + $property.name() + "' cannot be assigned to null!")));
                        $condition._else().assign($this.ref($property), $parameter);
                        $setter.javadoc().addThrows(IllegalArgumentException.class).append("iff the given value is {@code null} illegally");
                    } else {
                        assertThat($default).isPresent();
                        $setter.body().assign($this.ref($property), cond($parameter.eq($null), $default.get(), $parameter));
                    }
                    $setter.javadoc().append(format("<a href=\"https://github.com/informaticum/xjc\">Sets the value of the attribute {@link #%1$s}</a>.", $property.name()));
                    appendParameterJavaDoc($setter.javadoc(), attribute, $parameter, $default);
                } else {
                    LOG.error("Unexpectedly. there is already a setter method [{}#{}({})] for collection property [{}].", clazz.implClass.fullName(), setterName, $property.type(), $property.name());
                }
            }
        }
    }

    private final void removeSetter(final ClassOutline clazz) {
        for (final var $setter : generatedSettersOf(clazz).values()) {
            LOG.info("Remove property setter [{}#{}(...)].", fullNameOf(clazz), $setter.name());
            final var $class = clazz.implClass;
            $class.methods().remove($setter);
        }
    }

}
