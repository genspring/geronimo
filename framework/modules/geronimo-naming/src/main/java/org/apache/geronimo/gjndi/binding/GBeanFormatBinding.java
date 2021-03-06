/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.gjndi.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Name;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gjndi.KernelContextGBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class GBeanFormatBinding extends KernelContextGBean {
    protected static final Logger log = LoggerFactory.getLogger(GBeanFormatBinding.class);
    private static final Pattern PATTERN = Pattern.compile("(\\{)(\\w+)(})");

    protected final String format;
    protected final Pattern namePattern;

    public GBeanFormatBinding(String format, String namePattern, String nameInNamespace, AbstractNameQuery abstractNameQuery, Kernel kernel) throws NamingException {
        super(nameInNamespace, abstractNameQuery, kernel);
        this.format = format;
        if (namePattern != null && namePattern.length() > 0) {
            this.namePattern = Pattern.compile(namePattern);
        } else {
            this.namePattern = null;
        }
    }

    @Override
    protected Name createBindingName(AbstractName abstractName, Object value) throws NamingException {
        String name = abstractName.getNameProperty("name");
        if (namePattern != null) {
            Matcher matcher = namePattern.matcher(name);
            if (!matcher.matches()) {
                return null;
            }
        }
        Map<String, String> map = new HashMap<String, String>(abstractName.getName());
        Artifact artifact = abstractName.getArtifact();
        map.put("groupId", artifact.getGroupId());
        map.put("artifactId", artifact.getArtifactId());
        map.put("version", artifact.getVersion().toString());
        map.put("type", artifact.getType());
        String fullName = format(format, map);
        return getNameParser().parse(fullName);
    }

    static String format(String input, Map<String, String> map) {
        Matcher matcher = PATTERN.matcher(input);
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(2);
            String value = map.get(key);
            matcher.appendReplacement(buf, value);
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(GBeanFormatBinding.class, KernelContextGBean.GBEAN_INFO, "Context");
        builder.addAttribute("format", String.class, true);
        builder.addAttribute("namePattern", String.class, true);
        builder.setConstructor(new String[]{"format", "namePattern", "nameInNamespace", "abstractNameQuery", "kernel"});
        GBEAN_INFO = builder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
