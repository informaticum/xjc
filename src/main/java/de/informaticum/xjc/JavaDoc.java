package de.informaticum.xjc;

public enum JavaDoc {
    ;

    public static final String DEFAULT_CONSTRUCTOR_JAVADOC = "Creates a new instance of this class.%nIn detail, the default constructor of the super class is called, and then all fields are initialised in succession.";

    public static final String VALUES_CONSTRUCTOR_JAVADOC = "Creates a new instance of this class.%nIn detail, the all-values-constructor of the super class is called, and then all fields are assigned in succession.%nIf any given value is invalid, either the according default value will be assigned (if such value exists) or an according exception will be thrown.";

    public static final String DEFAULT_FIELD_ASSIGNMENT = "%n%nThe field {@link #%s} will be initialised with: {@code %s}";

    public static final String PARAM_WITH_DEFAULT_MULTI_VALUE = "value for the attribute '%s' (can be {@code null} because an empty, modifiable list will be used instead)";

    public static final String PARAM_WITH_DEFAULT_SINGLE_VALUE = "value for the attribute '%s' (can be {@code null} because an according default value will be used instead)";

    public static final String PARAM_THAT_IS_OPTIONAL = "value for the attribute '%s' (can be {@code null} because attribute is optional)";

    public static final String PARAM_THAT_IS_PRIMITIVE = "value for the attribute '%s'";

    public static final String PARAM_THAT_IS_REQUIRED = "value for the attribute '%s' (cannot be {@code null} because attribute is required)";

    public static final String RETURN_OPTIONAL_VALUE = "the value of the optional attribute '%s'";

    public static final String THROWS_IAE_BY_NULL = "iff any given value is {@code null} illegally";

}
