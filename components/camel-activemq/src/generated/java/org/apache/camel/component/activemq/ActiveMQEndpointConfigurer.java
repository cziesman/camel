/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.activemq;

import javax.annotation.processing.Generated;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ExtendedPropertyConfigurerGetter;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.spi.ConfigurerStrategy;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.util.CaseInsensitiveMap;
import org.apache.camel.component.jms.JmsEndpointConfigurer;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.EndpointSchemaGeneratorMojo")
@SuppressWarnings("unchecked")
public class ActiveMQEndpointConfigurer extends JmsEndpointConfigurer implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        ActiveMQEndpoint target = (ActiveMQEndpoint) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "destinationoptions":
        case "destinationOptions": target.setDestinationOptions(property(camelContext, java.util.Map.class, value)); return true;
        default: return super.configure(camelContext, obj, name, value, ignoreCase);
        }
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "destinationoptions":
        case "destinationOptions": return java.util.Map.class;
        default: return super.getOptionType(name, ignoreCase);
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        ActiveMQEndpoint target = (ActiveMQEndpoint) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "destinationoptions":
        case "destinationOptions": return target.getDestinationOptions();
        default: return super.getOptionValue(obj, name, ignoreCase);
        }
    }

    @Override
    public Object getCollectionValueType(Object target, String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "destinationoptions":
        case "destinationOptions": return java.lang.String.class;
        default: return super.getCollectionValueType(target, name, ignoreCase);
        }
    }
}

