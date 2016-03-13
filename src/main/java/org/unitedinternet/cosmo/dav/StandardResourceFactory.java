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
package org.unitedinternet.cosmo.dav;

import carldav.card.CardQueryProcessor;
import carldav.repository.CollectionDao;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dav.impl.*;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibEventItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.User;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;
import org.unitedinternet.cosmo.util.UriTemplate;

import javax.servlet.http.HttpServletRequest;

import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.CALENDAR;
import static org.unitedinternet.cosmo.dav.caldav.CaldavConstants.CONTACTS;

public class StandardResourceFactory implements DavResourceFactory, ExtendedDavConstants{

    private ContentService contentService;
    private ItemDao itemDao;
    private CollectionDao collectionDao;
    private CosmoSecurityManager securityManager;
    private CalendarQueryProcessor calendarQueryProcessor;
    private CardQueryProcessor cardQueryProcessor;
    private UserService userService;

    public StandardResourceFactory(ContentService contentService,
                                   ItemDao itemDao,
                                   CollectionDao collectionDao,
                                   CosmoSecurityManager securityManager,
                                   CalendarQueryProcessor calendarQueryProcessor,
                                   CardQueryProcessor cardQueryProcessor,
                                   UserService userService) {
        this.contentService = contentService;
        this.itemDao = itemDao;
        this.collectionDao = collectionDao;
        this.securityManager = securityManager;
        this.calendarQueryProcessor = calendarQueryProcessor;
        this.cardQueryProcessor = cardQueryProcessor;
        this.userService = userService;
    }

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exist and the request method
     * indicates that one is to be created, returns a resource backed by a 
     * newly-instantiated item that has not been persisted or assigned a UID.
     * Otherwise, if the resource does not exists, then a
     * {@link NotFoundException} is thrown.
     * </p>
     * <p>
     * The type of resource to create is chosen as such:
     * <ul>
     * <li><code>PUT</code>, <code>COPY</code>, <code>MOVE</code></li>:
     * {@link DavCard}</li>
     * </ul>
     */
    public WebDavResource resolve(DavResourceLocator locator, HttpServletRequest request) {
        WebDavResource resource = resolve(locator);
        if (resource != null) {
            return resource;
        }

        if (request.getMethod().equals("PUT")) {
            // will be replaced by the provider if a different resource
            // type is required
            WebDavResource parent = resolve(locator.getParentLocator());
            //TODO
            if(parent != null && CALENDAR.equals(parent.getName())) {
                return new DavCalendarResource(new HibEventItem(), locator, this);
            }
            //TODO
            if(parent != null && CONTACTS.equals(parent.getName())) {
                return new DavCard(locator, this);
            }
        }

        if(request.getMethod().equals("DELETE")) {
            return new DavCollectionBase(locator, this);
        }

        throw new NotFoundException();
    }

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link WebDavResource}.
     * </p>
     * <p>
     * If the identified resource does not exists, returns <code>null</code>.
     * </p>
     */
    public WebDavResource resolve(DavResourceLocator locator)
        throws CosmoDavException {
        String uri = locator.getPath();

        UriTemplate.Match match = TEMPLATE_USER.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator, match);
        }

        match = TEMPLATE_PRINCIPALS.match(uri);
        if (match != null) {
            return createUserPrincipalResource(locator);
        }

        return createUnknownResource(locator);
    }

    /**
     * <p>
     * Instantiates a <code>WebDavResource</code> representing the
     * <code>Item</code> located by the given <code>DavResourceLocator</code>.
     * </p>
     */
    public WebDavResource createResource(DavResourceLocator locator, HibItem hibItem)  throws CosmoDavException {
        Assert.notNull(hibItem, "item cannot be null");

        //TODO
        if(hibItem.getName().endsWith(".vcf")) {
            return new DavCard(hibItem, locator, this);
        }

        return new DavCalendarResource(hibItem, locator, this);
    }

    public WebDavResource createCollectionResource(DavResourceLocator locator, HibCollectionItem hibItem) {
        Assert.notNull(hibItem, "item cannot be null");

        //TODO
        if ("homeCollection".equals(hibItem.getDisplayName())) {
            return new DavHomeCollection(hibItem, locator, this);
        }
        //TODO
        if (CALENDAR.equals(hibItem.getName())) {
            return new DavCalendarCollection(hibItem, locator, this);
        }
        //TODO
        if (CONTACTS.equals(hibItem.getName())) {
            return new DavCardCollection(hibItem, locator, this, getCardQueryProcessor());
        }

        return new DavCollectionBase(hibItem, locator, this);
    }

    protected WebDavResource createUserPrincipalResource(DavResourceLocator locator, UriTemplate.Match match) throws CosmoDavException {
        User user = userService.getUser(match.get("username"));
        return user != null ? new DavUserPrincipal(user, locator, this) : null;
    }

    protected WebDavResource createUserPrincipalResource(DavResourceLocator locator) throws CosmoDavException {
        User user = securityManager.getSecurityContext().getUser();
        return user != null ? new DavUserPrincipal(user, locator, this) : null;
    }

    private WebDavResource createUnknownResource(DavResourceLocator locator) {
        final String itemUid = locator.itemUid();
        if(itemUid != null) {
            final HibItem userItem = itemDao.findByOwnerAndName(locator.username(), locator.itemUid());
            if(userItem == null) {
                return null;
            }
            return createResource(locator, userItem);
        }

        final String collection = locator.collection();
        if(collection != null) {
            final HibCollectionItem userCollection = collectionDao.findByOwnerAndName(locator.username(), collection);
            if(userCollection == null) {
                return null;
            }
            return createCollectionResource(locator, userCollection);
        }

        final HibCollectionItem homeCollection = collectionDao.findByOwnerAndName(locator.username(), locator.username());
        return createCollectionResource(locator, homeCollection);
    }

    public ContentService getContentService() {
        return contentService;
    }
    
    public CalendarQueryProcessor getCalendarQueryProcessor() {
        return calendarQueryProcessor;
    }

    public CardQueryProcessor getCardQueryProcessor() {
        return cardQueryProcessor;
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    @Override
    public ItemDao getItemDao() {
        return itemDao;
    }

    @Override
    public CollectionDao getCollectionDao() {
        return collectionDao;
    }
}
