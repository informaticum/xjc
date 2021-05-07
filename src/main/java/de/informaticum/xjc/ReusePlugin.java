package de.informaticum.xjc;

import static de.informaticum.xjc.util.Printify.fullName;
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

    public static final String OPTION_NAME = "informaticum-xjc-reuse";
    private static final CommandLineArgument REUSE_QNAMES = new CommandLineArgument("reuse-qnames", "Modify QName constants' accessibility to \"public\". Default: false");

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>(OPTION_NAME, "Widens and/or lessens the usage of the generated XJC-API elements.");
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
        $factory.fields().values().stream().filter(f -> $QName.isAssignableFrom(f.type().boxify())).forEach($qName -> {
            LOG.info("Set accessibility of QName [{}#{}] onto [public].", fullName($factory), $qName);
            $qName.javadoc().append("In order to allow reusage of this specific QName, <a href=\"https://github.com/informaticum/xjc\">it has gained 'public' access</a>.");
            $qName.mods().setPublic();
        });
    }

}
