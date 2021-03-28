package de.informaticum.xjc;

public class FoobarPlugin
extends AbstractPlugin {

    @Override
    public String getOptionName() {
        return "foobar";
    }

    @Override
    public String getOptionDescription() {
        return "Some foobar infomation.";
    }

}
