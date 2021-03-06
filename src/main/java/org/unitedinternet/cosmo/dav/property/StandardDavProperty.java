/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav.property;

import carldav.jackrabbit.webdav.property.DavPropertyName;
import carldav.jackrabbit.webdav.xml.DomUtils;
import carldav.jackrabbit.webdav.xml.XmlSerializable;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>
 * The standard implementation of {@link} WebDavProperty.
 * </p>
 * <p>
 * Note that the attribute <code>isProtected</code> does not actually refer
 * to whether or not the property's value is protected from modification or
 * deletion from the server. Rather, it denotes whether or not the property
 * is included in "allprop" <code>PROFIND</code> responses.
 * </p>
 */
public class StandardDavProperty implements WebDavProperty, XmlSerializable {

    private DavPropertyName name;
    private Object value;

    public StandardDavProperty(DavPropertyName name,
                               Object value) {
        this.name = name;
        this.value = value;
    }

    public DavPropertyName getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    /**
     * <p>
     * If the property value is an <code>Element</code>, the text and character
     * data content of the element and every child element are concatenated.
     * </p>
     * <p>
     * If the property is a <code>Set</code>, the set is sorted and joined.
     * </p>
     * <p>
     * If the property value is otherwise not null, {@link Object.toString()} is
     * called upon it, and the result is returned.
     * </p>
     */
    public String getValueText() {
        if (value == null) {
            return null;
        }
        if (value instanceof Element) {
            String text = DomUtils.getText((Element) value);
            if (text != null) {
                return text;
            }
        }
        if (value instanceof Set) {
            Set<Object> sorted = new LinkedHashSet<>((Set)value);
            return StringUtils.join(sorted, ", ");
        }
        return value.toString();
    }

    // XmlSerializable methods

    /**
     * <p>
     * If the property value is an <code>Element</code>, it is imported into
     * the provided <code>Document</code> and returned.
     * </p>
     * <p>
     * If the property value is an <code>XmlSerializable</code>, the element
     * returned by calling {@link XmlSerializable.toXml(Document)} on the
     * value is appended as a child of an element representing the property.
     * </p>
     * <p>
     * If the property value is otherwise not null, {@link Object.toString()} is
     * called upon it, and the result is set as text content of an element
     * representing the property.
     * </p>
     * <p>
     * In all cases, if the property has a language, it is used to set the
     * <code>xml:lang</code> attribute on the property element.
     * </p>
     */
    public Element toXml(Document document) {
        Element e;

        if (value != null && value instanceof Element) {
            e = (Element) document.importNode((Element) value, true);
        }
        else {
            e = getName().toXml(document);
            Object v = getValue();
            if (v != null) {
                if (v instanceof XmlSerializable) {
                    e.appendChild(((XmlSerializable)v).toXml(document));
                }
                else {
                    DomUtils.setText(e, v.toString());
                }
            }
        }
        return e;
    }

	@Override
	public boolean isInvisibleInAllprop() {
        return false;
    }
}
