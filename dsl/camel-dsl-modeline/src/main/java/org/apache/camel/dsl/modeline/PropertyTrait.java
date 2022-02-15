/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dsl.modeline;

import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.CamelContextCustomizer;
import org.apache.camel.spi.PropertiesComponent;
import org.apache.camel.support.ResourceHelper;
import org.apache.camel.util.StringHelper;

public class PropertyTrait implements Trait {

    @Override
    public String getName() {
        return "property";
    }

    @Override
    public CamelContextCustomizer parseTrait(String trait) {
        String key;
        String value;
        if (trait.contains("=")) {
            key = StringHelper.before(trait, "=").trim();
            value = StringHelper.after(trait, "=").trim();
        } else {
            key = null;
            value = trait;
        }
        return new CamelContextCustomizer() {
            @Override
            public void configure(CamelContext camelContext) {
                PropertiesComponent pc = camelContext.getPropertiesComponent();
                if (ResourceHelper.hasScheme(value)) {
                    // it is a properties file so load resource
                    try (InputStream is = ResourceHelper.resolveResourceAsInputStream(camelContext, value)) {
                        Properties prop = new Properties();
                        prop.load(is);
                        for (String k : prop.stringPropertyNames()) {
                            String v = prop.getProperty(k);
                            pc.addInitialProperty(k, v);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                } else {
                    pc.addInitialProperty(key, value);
                }
            }
        };
    }

}
