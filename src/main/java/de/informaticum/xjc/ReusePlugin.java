package de.informaticum.xjc;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.LinkedHashMap;
import javax.xml.namespace.QName;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.outline.Outline;
import org.slf4j.Logger;
import org.xml.sax.ErrorHandler;

public final class ReusePlugin
extends AbstractPlugin {

    private static final Logger LOG = getLogger(ReusePlugin.class);

    private static final String REUSE_QNAMES = "-reuse-qnames";

    private boolean reuseQNames = false;

    @Override
    public final String getOptionName() {
        return "ITBSG-xjc-reuse";
    }

    @Override
    public final String getOptionDescription() {
        return "Reuse generated XJC-API elements.";
    }

    @Override
    public final LinkedHashMap<String, String> getPluginOptions() {
        return new LinkedHashMap<>(ofEntries(entry(REUSE_QNAMES, "Modify QNames' accessibility to \"public\". Default: false")));
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
    protected final boolean runObjectFactory(final Outline outline, final Options options, final ErrorHandler errorHandler, final JDefinedClass objectFactory) {
        if (this.reuseQNames) {
            final var $QName = outline.getCodeModel().ref(QName.class);
            LOG.info("Changing the access modifier of the [{}] fields of [{}].", $QName.name(), objectFactory.fullName());
            objectFactory.fields().values().stream().filter(f -> f.type() instanceof JClass && $QName.isAssignableFrom((JClass) f.type())).forEach(this::publicifyField);
        }
        return true;
    }

    private final void publicifyField(final JFieldVar field) {
        LOG.debug("Changing the access modifier of field [{}] to [{}].", field.name(), "public");
        field.mods().setPublic();
    }

}
