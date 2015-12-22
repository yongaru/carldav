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

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VTimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.LockedException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.unitedinternet.cosmo.dav.caldav.TimeZoneExtractor;
import org.unitedinternet.cosmo.dav.caldav.UidConflictException;
import org.unitedinternet.cosmo.dav.caldav.property.GetCTag;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCalendarComponentSet;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCalendarData;
import org.unitedinternet.cosmo.dav.caldav.property.SupportedCollationSet;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.icalendar.ICalendarConstants;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionLockedException;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.IcalUidInUseException;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Extends <code>DavCollection</code> to adapt the Cosmo
 * <code>CalendarCollectionItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>CALDAV:calendar-description</code></li>
 * <li><code>CALDAV:calendar-timezone</code></li>
 * <li><code>CALDAV:calendar-supported-calendar-component-set</code>
 * (protected)</li>
 * <li><code>CALDAV:supported-calendar-data</code> (protected)</li>
 * <li><code>CALDAV:max-resource-size</code> (protected)</li>
 * <li><code>CS:getctag</code> (protected)</li>
 * <li><code>XC:calendar-color</code></li>
 * <li><code>XC:calendar-visible</code></li>
 * </ul>
 *
 * @see DavCollection
 * @see CalendarCollectionItem
 */
public class DavCalendarCollection extends DavCollectionBase
    implements CaldavConstants, ICalendarConstants {
    private static final Log LOG =  LogFactory.getLog(DavCalendarCollection.class);
    private static final Set<String> DEAD_PROPERTY_FILTER = new HashSet<>();

    static {
        registerLiveProperty(CALENDARTIMEZONE);
        registerLiveProperty(SUPPORTEDCALENDARCOMPONENTSET);
        registerLiveProperty(SUPPORTEDCALENDARDATA);
        registerLiveProperty(GET_CTAG);
        
        DEAD_PROPERTY_FILTER.add(CalendarCollectionStamp.class.getName());
    }

    /** */
    public DavCalendarCollection(CollectionItem collection,
                                 DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 EntityFactory entityFactory)
        throws CosmoDavException {
        super(collection, locator, factory, entityFactory);
    }

    /** */
    public DavCalendarCollection(DavResourceLocator locator,
                                 DavResourceFactory factory,
                                 EntityFactory entityFactory)
        throws CosmoDavException {
        this(entityFactory.createCollection(), locator, factory, entityFactory);
        getItem().addStamp(entityFactory.createCalendarCollectionStamp((CollectionItem) getItem()));
    }

    // Jackrabbit WebDavResource

    /** */
    public String getSupportedMethods() {
        // calendar collections not allowed inside calendar collections
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, PUT, DELETE, REPORT";
    }

    public boolean isCalendarCollection() {
        return true;
    }

    /**
     * @return The default timezone for this calendar collection, if
     * one has been set.
     */
    public VTimeZone getTimeZone() {
        Calendar obj = getCalendarCollectionStamp().getTimezoneCalendar();
        if (obj == null) {
            return null;
        }
        return (VTimeZone)
            obj.getComponents().getComponent(Component.VTIMEZONE);
    }

    protected Set<QName> getResourceTypes() {
        Set<QName> rt = super.getResourceTypes();
        rt.add(RESOURCE_TYPE_CALENDAR);
        return rt;
    }
    
    public CalendarCollectionStamp getCalendarCollectionStamp() {
        return StampUtils.getCalendarCollectionStamp(getItem());
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null) {
            return;
        }

        // add CS:getctag property, which is the collection's entitytag
        // if it exists
        Item item = getItem();
        if(item!=null && item.getEntityTag()!=null) {
            properties.add(new GetCTag(item.getEntityTag()));
        }
        
        properties.add(new SupportedCalendarComponentSet());
        properties.add(new SupportedCollationSet());
        properties.add(new SupportedCalendarData());
    }

    /** 
     * The CALDAV:supported-calendar-component-set property is
      used to specify restrictions on the calendar component types that
      calendar object resources may contain in a calendar collection.
      Any attempt by the client to store calendar object resources with
      component types not listed in this property, if it exists, MUST
      result in an error, with the CALDAV:supported-calendar-component
      precondition (Section 5.3.2.1) being violated.  Since this
      property is protected, it cannot be changed by clients using a
      PROPPATCH request.
     * */
    protected void setLiveProperty(WebDavProperty property, boolean create)
        throws CosmoDavException {
        super.setLiveProperty(property, create);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null) {
            return;
        }

        DavPropertyName name = property.getName();
        if (property.getValue() == null) {
            throw new UnprocessableEntityException("Property " + name + " requires a value");
        }
        
        if(!(create && name.equals(SUPPORTEDCALENDARCOMPONENTSET)) &&
            (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
                name.equals(SUPPORTEDCALENDARDATA) ||
                name.equals(GET_CTAG))) {
                throw new ProtectedPropertyModificationException(name);
        }

        if (name.equals(CALENDARTIMEZONE)) {
            cc.setTimezoneCalendar(TimeZoneExtractor.extract(property));
        }
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name)
        throws CosmoDavException {
        super.removeLiveProperty(name);

        CalendarCollectionStamp cc = getCalendarCollectionStamp();
        if (cc == null) {
            return;
        }

        if (name.equals(SUPPORTEDCALENDARCOMPONENTSET) ||
            name.equals(SUPPORTEDCALENDARDATA) ||
            name.equals(GET_CTAG)) {
            throw new ProtectedPropertyModificationException(name);
        }

        if (name.equals(CALENDARTIMEZONE)) {
            cc.setTimezoneCalendar(null);
            return;
        }
    }

    /** */
    protected Set<String> getDeadPropertyFilter() {
        Set<String> copy = new HashSet<>();
        copy.addAll(super.getDeadPropertyFilter());
        copy.addAll(DEAD_PROPERTY_FILTER);
        return copy;
    }

    /** */
    protected void saveContent(DavItemContent member)
        throws CosmoDavException {
        if (! (member instanceof DavCalendarResource)) {
            throw new IllegalArgumentException("member not DavCalendarResource");
        }

        if (member instanceof DavEvent) {
            saveEvent(member);
        } else {
            try {
                super.saveContent(member);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            }
        }
    }

    private void saveEvent(DavItemContent member)
        throws CosmoDavException {
        
        ContentItem content = (ContentItem) member.getItem();
        EventStamp event = StampUtils.getEventStamp(content);
        EntityConverter converter = new EntityConverter(getEntityFactory());
        Set<ContentItem> toUpdate = new LinkedHashSet<>();
        
        try {
            // convert icalendar representation to cosmo data model
            toUpdate.addAll(converter.convertEventCalendar(
                    (NoteItem) content, event.getEventCalendar()));
        } catch (ModelValidationException e) {
            throw new InvalidCalendarResourceException(e.getMessage());
        }
        
        if (event.getCreationDate()!=null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("updating event " + member.getResourcePath());
            }

            try {
                getContentService().updateContentItems(content.getParents(),
                        toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            } 
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating event " + member.getResourcePath());
            }

            try {
                getContentService().createContentItems(
                        (CollectionItem) getItem(), toUpdate);
            } catch (IcalUidInUseException e) {
                throw new UidConflictException(e);
            } catch (CollectionLockedException e) {
                throw new LockedException();
            }
        }

        member.setItem(content);
    }
}
