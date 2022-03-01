package de.informaticum.xjc;

import static de.informaticum.xjc.resources.ReusePluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.resources.ReusePluginMessages.PUBLIC_QNAMES_IMPLNOTE;
import static de.informaticum.xjc.resources.ReusePluginMessages.REUSE_QNAMES_DESCRIPTION;
import static de.informaticum.xjc.util.CodeRetrofit.javadocSection;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import com.sun.codemodel.JDefinedClass;
import de.informaticum.xjc.api.BasePlugin;
import de.informaticum.xjc.api.CommandLineArgument;
import org.slf4j.Logger;

public final class ReusePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(ReusePlugin.class);
    private static final String PUBLIC_QNAME = "Modify accessibility of QName [{}#{}] to [public].";

    private static final String OPTION_NAME = "informaticum-xjc-reuse";
    private static final CommandLineArgument REUSE_QNAMES = new CommandLineArgument("reuse-qnames", REUSE_QNAMES_DESCRIPTION.text());

    @Override
    public final Entry<String, String> getOptionEntry() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION.text());
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(REUSE_QNAMES);
    }

    @Override
    protected final boolean runObjectFactory(final JDefinedClass $Factory) {
        REUSE_QNAMES.doOnActivation(this::publicifyQNames, $Factory);
        return true;
    }

    private final void publicifyQNames(final JDefinedClass $Factory) {
        final var $QName = this.reference(QName.class);
        $Factory.fields().values().stream()
                .filter($field -> $QName.isAssignableFrom($field.type().boxify()))
                .forEach($qNameField -> {
                    LOG.info(PUBLIC_QNAME, $Factory.fullName(), $qNameField.name());
                    javadocSection($qNameField).append(PUBLIC_QNAMES_IMPLNOTE.text());
                    $qNameField.mods().setPublic();
                });
    }

}
