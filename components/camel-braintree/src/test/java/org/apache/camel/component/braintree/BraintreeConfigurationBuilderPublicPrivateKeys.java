package org.apache.camel.component.braintree;

import org.apache.camel.component.braintree.internal.BraintreeLogHandler;
import org.apache.camel.util.IntrospectionSupport;
import org.apache.camel.util.ObjectHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BraintreeConfigurationBuilderPublicPrivateKeys implements BraintreeConfigurationBuilder {

    private static final String TEST_OPTIONS_PROPERTIES = "/test-options.properties";

    @Override
    public BraintreeConfiguration build() throws Exception {

        final Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream(TEST_OPTIONS_PROPERTIES));
        } catch (Exception e) {
            throw new IOException(String.format("%s could not be loaded: %s", TEST_OPTIONS_PROPERTIES, e.getMessage()), e);
        }

        Map<String, Object> options = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase("accessToken")) {
                continue;
            }
            options.put(entry.getKey().toString(), entry.getValue());
        }

        addOptionIfMissing(options, "environment", "CAMEL_BRAINTREE_ENVIRONMENT");
        addOptionIfMissing(options, "merchantId", "CAMEL_BRAINTREE_MERCHANT_ID");
        addOptionIfMissing(options, "publicKey", "CAMEL_BRAINTREE_PUBLIC_KEY");
        addOptionIfMissing(options, "privateKey", "CAMEL_BRAINTREE_PRIVATE_KEY");

        final BraintreeConfiguration configuration = new BraintreeConfiguration();
        configuration.setHttpLogLevel(BraintreeLogHandler.DEFAULT_LOGGER_VERSION);
        configuration.setHttpLogName(BraintreeLogHandler.DEFAULT_LOGGER_NAME);
        IntrospectionSupport.setProperties(configuration, options);

        return configuration;
    }

    protected void addOptionIfMissing(Map<String, Object> options, String name, String envName) {
        if (!options.containsKey(name)) {
            String value = System.getenv(envName);
            if (ObjectHelper.isNotEmpty(value)) {
                options.put(name, value);
            }
        }
    }
}
