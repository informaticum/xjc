package de.informaticum.xjc.plugins;

import static com.sun.codemodel.JOp.cond;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.GETTER_JAVADOC_END;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.HINT_DEFAULTED_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.HINT_DEFAULTED_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.HINT_DEFENSIVE_COPY_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.HINT_EMPTY_COLLECTION_CONTAINER;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.HINT_LIVE_REFERENCE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.HINT_NULLABLE_VALUE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.HINT_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_DEFAULTED_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_DEFAULTED_VALUE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_DEFENSIVE_COPY_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_DEFENSIVE_COPY_COLLECTION_CONTAINER;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_EMPTY_CONTAINER;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_LIVE_REFERENCE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_LIVE_REFERENCE_CONTAINER;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_NULLABLE_VALUE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_OPTIONAL_VALUE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_REQUIRED_VALUE;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_UNMODIFIABLE_COLLECTION;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.NOTE_UNMODIFIABLE_COLLECTION_CONTAINER;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTIONAL_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTIONAL_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.OPTIONAL_VALUE_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.STRAIGHT_COLLECTION_OR_EMPTY_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.STRAIGHT_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.STRAIGHT_DEFAULTED_VALUE_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.STRAIGHT_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.STRAIGHT_VALUE_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_OR_EMPTY_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.UNMODIFIABLE_COLLECTION_RETURN;
import static de.informaticum.xjc.plugins.i18n.PropertyPluginMessages.UNMODIFIABLE_GETTER_JAVADOC_BEGIN;
import static de.informaticum.xjc.util.CodeModelAnalysis.$null;
import static de.informaticum.xjc.util.CodeModelAnalysis.javadocNameOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.javadocSimpleNameOf;
import static de.informaticum.xjc.util.CodeModelAnalysis.render;
import static de.informaticum.xjc.util.CodeRetrofit.eraseBody;
import static de.informaticum.xjc.util.CodeRetrofit.eraseJavadoc;
import static de.informaticum.xjc.util.CodeRetrofit.javadocBreak;
import static de.informaticum.xjc.util.OutlineAnalysis.isRequired;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import java.util.function.Function;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import de.informaticum.xjc.api.ResourceBundleEntry;
import de.informaticum.xjc.plugins.i18n.PropertyPluginMessages;
import de.informaticum.xjc.util.CodeModelAnalysis;

/*package*/ enum GetterRefactoring {

    PRIMITIVE_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_VALUE_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            return($.$nonNull);
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($)};
        }
    },

    DEFAULTED_UNMODIFIABLE_COLLECTION_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, UNMODIFIABLE_COLLECTION_OR_EMPTY_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            assertThat($.$default).isPresent();
            assertThat($.$returnType).matches(CodeModelAnalysis::isCollectionType);
            return(cond($.$prop.eq($null), $.$default.get(), $.$view()));
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_DEFAULTED_UNMODIFIABLE_COLLECTION, HINT_DEFAULTED_UNMODIFIABLE_COLLECTION, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION};
        }
    },

    DEFAULTED_MODIFIABLE_COLLECTION_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_COLLECTION_OR_EMPTY_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            assertThat($.$default).isPresent();
            return(cond($.$prop.eq($null), $.$default.get(), $.$nonNull));
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_DEFAULTED_COLLECTION, HINT_DEFAULTED_COLLECTION, NOTE_REFERENCE.apply($), HINT_REFERENCE.apply($)};
        }
    },

    OPTIONAL_UNMODIFIABLE_COLLECTION_PROPERTY(OPTIONAL_UNMODIFIABLE_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, OPTIONAL_UNMODIFIABLE_COLLECTION_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            assertThat($.$returnType).matches(CodeModelAnalysis::isCollectionType);
            return(cond($.$prop.eq($null), $.$optionalEmpty, $.$optionalOf.arg($.$view())));
        }
        @Override /*package*/ final Optional<JType> returnType(final GetterBricks $) {
            return Optional.of($.$OptionalType);
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_UNMODIFIABLE_COLLECTION_CONTAINER, HINT_UNMODIFIABLE_COLLECTION};
        }
        @Override /*package*/ void supersedeJavadoc(final GetterBricks $) {
            this.supersedeJavadoc($, javadocSimpleNameOf($.$OptionalType));
        }
    },

    OPTIONAL_MODIFIABLE_COLLECTION_PROPERTY(OPTIONAL_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, OPTIONAL_COLLECTION_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            return(cond($.$prop.eq($null), $.$optionalEmpty, $.$optionalOf.arg($.$nonNull)));
        }
        @Override /*package*/ final Optional<JType> returnType(final GetterBricks $) {
            return Optional.of($.$OptionalType);
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_EMPTY_CONTAINER, HINT_EMPTY_COLLECTION_CONTAINER, NOTE_REFERENCE_CONTAINER.apply($), HINT_REFERENCE.apply($)};
        }
        @Override /*package*/ void supersedeJavadoc(final GetterBricks $) {
            this.supersedeJavadoc($, javadocSimpleNameOf($.$OptionalType));
        }
    },

    UNMODIFIABLE_COLLECTION_PROPERTY(UNMODIFIABLE_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, UNMODIFIABLE_COLLECTION_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            assertThat($.$returnType).matches(CodeModelAnalysis::isCollectionType);
            return(cond($.$prop.eq($null), $null, $.$view()));
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_UNMODIFIABLE_COLLECTION, HINT_UNMODIFIABLE_COLLECTION};
        }
    },

    MODIFIABLE_COLLECTION_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_COLLECTION_RETURN) {
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE, NOTE_REFERENCE.apply($), HINT_REFERENCE.apply($)};
        }
    },

    DEFAULTED_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_DEFAULTED_VALUE_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            assertThat($.$default).isPresent();
            return(cond($.$prop.eq($null), $.$default.get(), $.$nonNull));
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_DEFAULTED_VALUE};
        }
        @Override /*package*/ void supersedeJavadoc(final GetterBricks $) {
            assertThat($.$default).isPresent();
            this.supersedeJavadoc($, render($.$default.get()));
        }
    },

    OPTIONAL_PROPERTY(OPTIONAL_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, OPTIONAL_VALUE_RETURN) {
        @Override /*package*/ final JExpression returnExpression(final GetterBricks $) {
            return(cond($.$prop.eq($null), $.$optionalEmpty, $.$optionalOf.arg($.$nonNull)));
        }
        @Override /*package*/ final Optional<JType> returnType(final GetterBricks $) {
            return Optional.of($.$OptionalType);
        }
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_EMPTY_CONTAINER};
        }
        @Override /*package*/ void supersedeJavadoc(final GetterBricks $) {
            this.supersedeJavadoc($, javadocSimpleNameOf($.$OptionalType));
        }
    },

    STRAIGHT_PROPERTY(STRAIGHT_GETTER_JAVADOC_BEGIN, GETTER_JAVADOC_END, STRAIGHT_VALUE_RETURN) {
        @Override /*package*/ final ResourceBundleEntry[] notes(final GetterBricks $) {
            return new ResourceBundleEntry[]{DECLARATION_TYPE.apply($), NOTE_NULLABLE_VALUE, HINT_NULLABLE_VALUE};
        }
    };

    private static final Function<GetterBricks, PropertyPluginMessages> DECLARATION_TYPE         = b -> isRequired(b.field) ? NOTE_REQUIRED_VALUE : NOTE_OPTIONAL_VALUE;
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

    /*package*/ JExpression returnExpression(final GetterBricks bricks) {
        if (render(bricks.$prop).equals(render(bricks.$nonNull))) {
            return bricks.$prop;
        } else {
            return cond(bricks.$prop.eq($null), $null, bricks.$nonNull);
        }
    }

    /*package*/ Optional<JType> returnType(final GetterBricks bricks) {
        return Optional.empty();
    }

    /*package*/ abstract ResourceBundleEntry[] notes(final GetterBricks bricks);

    /*package*/ void supersedeJavadoc(final GetterBricks bricks) {
        this.supersedeJavadoc(bricks, javadocSimpleNameOf(bricks.$returnType));
    }

    /*package*/ final void supersedeJavadoc(final GetterBricks bricks, final String noteArg) {
        final var $javadoc = bricks.$getter.javadoc();
        eraseJavadoc($javadoc).append(this.introJavadoc.format(javadocNameOf(bricks.field.parent().getImplClass()), javadocNameOf(bricks.$field)));
        for (final var note : this.notes(bricks)) {
            javadocBreak($javadoc).append(note.format(noteArg));
        }
        javadocBreak($javadoc).append(this.outroJavadoc.text());
        final var $return = bricks.$getter.javadoc().addReturn();
        eraseJavadoc($return).append(this.returnJavadoc.format(javadocNameOf(bricks.field.parent().getImplClass()), javadocNameOf(bricks.$field), noteArg));
    }

    /*package*/ final void supersedeGetter(final GetterBricks bricks) {
        eraseBody(bricks.$getter)._return(this.returnExpression(bricks));
        this.returnType(bricks).ifPresent(t -> bricks.$getter.type(t));
        this.supersedeJavadoc(bricks);
    }

}