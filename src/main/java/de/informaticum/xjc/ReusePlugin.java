package de.informaticum.xjc;

import static de.informaticum.xjc.JavaDoc.PUBLIC_QNAME;
import static de.informaticum.xjc.util.Printify.fullName;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.Options;
import de.informaticum.xjc.plugin.BasePlugin;
import org.slf4j.Logger;

public final class ReusePlugin
extends BasePlugin {

    private static final Logger LOG = getLogger(ReusePlugin.class);

    private static final String REUSE_QNAMES = "-reuse-qnames";
    private static final String REUSE_QNAMES_DESC = "Modify QNames' accessibility to \"public\". Default: false";
    private boolean reuseQNames = false;

    @Override
    public final Entry<String, String> getOption() {
        return new SimpleImmutableEntry<>("ITBSG-xjc-reuse", "Widens and/or lessens the usage of the generated XJC-API elements.");
    }

    @Override
    public final LinkedHashMap<String, String> getPluginOptions() {
        return new LinkedHashMap<>(ofEntries(entry(REUSE_QNAMES, REUSE_QNAMES_DESC)));
    }

    @Override
    public final int parseArgument(final Options options, final String[] arguments, final int index) {
        switch (arguments[index]) {
            case REUSE_QNAMES:
                this.reuseQNames = true;
                return 1;
            default:
                return 0;
        }
    }

    @Override
    protected final boolean runObjectFactory(final JDefinedClass $factory) {
        if (this.reuseQNames) {
            final var $QName = this.reference(QName.class);
            LOG.info("Changing the access modifier of the [{}] fields of [{}].", $QName.name(), fullName($factory));
            $factory.fields().values().stream().filter(f -> $QName.isAssignableFrom(f.type().boxify())).forEach(ReusePlugin::publicifyField);
        }
        return true;
    }

    private static final void publicifyField(final JFieldVar $field) {
        LOG.debug("Changing the access modifier of field [{}] to [{}].", $field.name(), "public");
        $field.javadoc().append(PUBLIC_QNAME);
        $field.mods().setPublic();
    }

}
