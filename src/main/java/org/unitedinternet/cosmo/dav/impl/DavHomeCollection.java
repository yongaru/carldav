/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.impl;

import carldav.service.generator.IdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Extends <code>DavCollection</code> to adapt the Cosmo
 * <code>HomeCollectionItem</code> to the DAV resource model.
 *
 * @see DavCollection
 * @see HibHomeCollectionItem
 */
public class DavHomeCollection extends DavCollectionBase {
    private static final Log LOG =
            LogFactory.getLog(DavHomeCollection.class);

    /** */
    public DavHomeCollection(HibHomeCollectionItem collection,
                             DavResourceLocator locator,
                             DavResourceFactory factory,
                             IdGenerator idGenerator)
            throws CosmoDavException {
        super(collection, locator, factory, idGenerator);
    }

    // WebDavResource

    /** */
    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND";
    }

    // DavCollection

    public boolean isHomeCollection() {
        return true;
    }

    @Override
    public DavResourceIterator getMembers() {
        List<org.apache.jackrabbit.webdav.DavResource> members = new ArrayList<>();
        try {
            for (HibItem memberHibItem : ((HibCollectionItem) getItem()).getChildren()) {
                WebDavResource resource = memberToResource(memberHibItem);
                if (resource != null) {
                    members.add(resource);
                }
            }

            if (LOG.isTraceEnabled()) {
                //Fix Log Forging - fortify
                //Writing unvalidated user input to log files can allow an attacker to forge log entries or
                //inject malicious content into the logs.
                LOG.trace("Members of Home Collection: " + members.toString());
            }
            return new DavResourceIteratorImpl(members);
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
    }
    
    @Override
    public DavResourceIterator getCollectionMembers() {
        List<org.apache.jackrabbit.webdav.DavResource> members = new ArrayList<org.apache.jackrabbit.webdav.DavResource>();
        try {
            Set<HibCollectionItem> hibCollectionItems = getContentService().findCollectionItems((HibCollectionItem) getItem());
            for (HibItem memberHibItem : hibCollectionItems) {
                WebDavResource resource = memberToResource(memberHibItem);
                if (resource != null) {
                    members.add(resource);
                }
            }
        } catch (CosmoDavException e) {
            throw new CosmoException(e);
        }
        return new DavResourceIteratorImpl(members);
    }
    
}
