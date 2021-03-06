package de.informaticum.xjc.plugins;

import static de.informaticum.xjc.plugins.i18n.ReusePluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.plugins.i18n.ReusePluginMessages.PUBLIC_QNAMES_COMMENT;
import static de.informaticum.xjc.plugins.i18n.ReusePluginMessages.PUBLIC_QNAMES_IMPLNOTE;
import static de.informaticum.xjc.plugins.i18n.ReusePluginMessages.REUSE_QNAMES_DESCRIPTION;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import com.sun.codemodel.JDefinedClass;
import de.informaticum.xjc.api.CommandLineArgument;
import de.informaticum.xjc.api.XjcOption;
import org.slf4j.Logger;

public final class ReusePlugin
extends AdoptAnnotationsPlugin {

    private static final Logger LOG = getLogger(ReusePlugin.class);
    private static final String PUBLIC_QNAME = "Modify accessibility of QName [{}#{}] to [public].";

    private static final String OPTION_NAME = "informaticum-xjc-reuse";
    private static final CommandLineArgument REUSE_QNAMES = new CommandLineArgument("reuse-qnames", REUSE_QNAMES_DESCRIPTION.text());

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<XjcOption> getPluginArguments() {
        return asList(REUSE_QNAMES);
    }

    @Override
    protected final boolean runObjectFactory(final JDefinedClass $Factory) {
        REUSE_QNAMES.doOnActivation(this::setQNamesPublic, $Factory);
        return true;
    }

    private final void setQNamesPublic(final JDefinedClass $Factory) {
        final var $QName = this.reference(QName.class);
        final var $fields = $Factory.fields().values();
        final var $qNameFields = $fields.stream().filter($f -> $QName.isAssignableFrom($f.type().boxify()));
        $qNameFields.forEach($q -> {
            LOG.info(PUBLIC_QNAME, $Factory.fullName(), $q.name());
            this.appendGeneratedAnnotation($Factory, $q, PUBLIC_QNAMES_COMMENT.format(ReusePlugin.class.getName()));
            javadocSection($q).append(PUBLIC_QNAMES_IMPLNOTE.text());
            $q.mods().setPublic();
        });
    }

}
