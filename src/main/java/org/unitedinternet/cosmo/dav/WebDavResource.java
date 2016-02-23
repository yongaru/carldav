/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.unitedinternet.cosmo.CosmoException;

import java.io.IOException;
import java.util.List;

/**
 * An interface providing resource functionality required by WebDAV
 * extensions implemented by Cosmo.
 */
public interface WebDavResource
    extends org.apache.jackrabbit.webdav.DavResource
{

    /**
     * String constant representing the WebDAV 1 compliance
     * class as well as the Cosmo extended classes.
     */
    // see bug 5137 for why we don't include class 2
    String COMPLIANCE_CLASS = "1, 3, addressbook, calendar-access";

    /**
     * Returns a comma separated list of all compliance classes the given
     * resource is fulfilling.
     *
     * @return compliance classes
     */
    String getComplianceClass();

    /**
     * Returns a comma separated list of all METHODS supported by the given
     * resource.
     *
     * @return METHODS supported by this resource.
     */
    String getSupportedMethods();

    /**
     * Returns true if this webdav resource represents an existing repository item.
     *
     * @return true, if the resource represents an existing repository item.
     */
    boolean exists();

    /**
     * Returns true if this webdav resource has the resourcetype 'collection'.
     *
     * @return true if the resource represents a collection resource.
     */
    boolean isCollection();

    /**
     * Returns the display name of this resource.
     *
     * @return display name.
     */
     String getDisplayName();

    /**
     * Returns the path of the hierarchy element defined by this <code>DavResource</code>.
     * This method is a shortcut for <code>DavResource.getLocator().getResourcePath()</code>.
     *
     * @return path of the element defined by this <code>DavResource</code>.
     */
    String getResourcePath();

    /**
     * Return the time of the last modification or -1 if the modification time
     * could not be retrieved.
     *
     * @return time of last modification or -1.
     */
    long getModificationTime();

    /**
     * @return Returns the parent collection for this resource.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    DavCollection getParent() throws CosmoDavException;

    /**
     * Removes the specified member from this resource.
     *
     * @throws DavException
     */
    void removeMember(DavResource member) throws DavException;

    /**
     * Returns the absolute href of this resource as returned in the
     * multistatus response body.
     *
     * @return href
     */
    String getHref();

    /**
     * Returns all webdav properties present on this resource that will be
     * return upon a {@link DavConstants#PROPFIND_ALL_PROP} request. The
     * implementation may in addition expose other (protected or calculated)
     * properties which should be marked accordingly (see also
     * {@link org.apache.jackrabbit.webdav.property.DavProperty#isInvisibleInAllprop()}.
     *
     * @return a {@link DavPropertySet} containing at least all properties
     * of this resource that are exposed in 'allprop' PROPFIND request.
     */
    DavPropertySet getProperties();

    /**
     * Return the webdav property with the specified name.
     *
     * @param name name of the webdav property
     * @return the {@link DavProperty} with the given name or <code>null</code>
     * if the property does not exist.
     */
    DavProperty<?> getProperty(DavPropertyName name);

    /**
     * Returns an array of all {@link DavPropertyName property names} available
     * on this resource.
     *
     * @return an array of property names.
     */
    DavPropertyName[] getPropertyNames();

    void writeTo(OutputContext out)
        throws CosmoDavException, IOException;

    /**
     * @return Return the report that matches the given report info if it is
     * supported by this resource.
     * @param info The given report info.
     * @throws CosmoDavException - if something is wrong this exception is thrown.
     */
    Report getReport(ReportInfo info)
        throws CosmoDavException;

    DavResourceLocator getResourceLocator();
    
    String getETag();

    /**
     * Returns an iterator over all internal members.
     *
     * @return a {@link List<WebDavResource>} over all internal members.
     */
    //TODO rename me as soon as jackrabbit has been replaced with custom code
    List<WebDavResource> getMembers2();

    //TODO remove me as soon as jackrabbit has been replaced with custom code
    @Deprecated
    default DavResourceIterator getMembers() {
        List ll = getMembers2();
        return new DavResourceIteratorImpl((List<DavResource>)ll);
    }

    //TODO remove me as soon as jackrabbit has been replaced with custom code
    @Deprecated
    default DavResource getCollection() {
        throw new UnsupportedOperationException();
    }
}
