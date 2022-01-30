package de.informaticum.xjc;

import static de.informaticum.xjc.resources.ReusePluginMessages.OPTION_DESCRIPTION;
import static de.informaticum.xjc.resources.ReusePluginMessages.PUBLIC_QNAMES_JAVADOC;
import static de.informaticum.xjc.resources.ReusePluginMessages.REUSE_QNAMES_DESCRIPTION;
import static de.informaticum.xjc.util.CodeRetrofit.javadocAppendSection;
import static de.informaticum.xjc.util.OutlineAnalysis.fullNameOf;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import com.sun.codemodel.JDefinedClass;
import de.informaticum.xjc.plugin.BasePlugin;
import de.informaticum.xjc.plugin.CommandLineArgument;
import org.slf4j.Logger;

public final class ReusePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(ReusePlugin.class);
    private static final String PUBLIC_QNAME = "Modify accessibility of QName [{}#{}] to [public].";

    private static final String OPTION_NAME = "informaticum-xjc-reuse";
    private static final CommandLineArgument REUSE_QNAMES = new CommandLineArgument("reuse-qnames", REUSE_QNAMES_DESCRIPTION);

    @Override
    public final Entry<String, CharSequence> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, OPTION_DESCRIPTION);
    }

    @Override
    public final List<CommandLineArgument> getPluginArguments() {
        return asList(REUSE_QNAMES);
    }

    @Override
    protected final boolean runObjectFactory(final JDefinedClass $factory) {
        REUSE_QNAMES.doOnActivation(this::publicifyQNames, $factory);
        return true;
    }

    private final void publicifyQNames(final JDefinedClass $factory) {
        final var $QName = this.reference(QName.class);
        $factory.fields().values().stream()
                .filter($field -> $QName.isAssignableFrom($field.type().boxify()))
                .forEach($qName -> {
                    LOG.info(PUBLIC_QNAME, fullNameOf($factory), $qName.name());
                    javadocAppendSection($qName, PUBLIC_QNAMES_JAVADOC);
                    $qName.mods().setPublic();
                });
    }

}
