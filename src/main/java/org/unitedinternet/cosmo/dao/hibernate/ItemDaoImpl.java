/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.hibernate;

import carldav.service.generator.IdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.UnresolvableObjectException;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.security.core.token.TokenService;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dao.DuplicateItemNameException;
import org.unitedinternet.cosmo.dao.ItemDao;
import org.unitedinternet.cosmo.dao.ItemNotFoundException;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.UidInUseException;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.BaseModelObject;
import org.unitedinternet.cosmo.model.hibernate.HibEventStamp;
import org.unitedinternet.cosmo.model.hibernate.HibHomeCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibICalendarItem;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolationException;


/**
 * Implementation of ItemDao using Hibernate persistent objects.
 */
public abstract class ItemDaoImpl extends AbstractDaoImpl implements ItemDao {

    private static final Log LOG = LogFactory.getLog(ItemDaoImpl.class);

    private IdGenerator idGenerator = null;
    private TokenService ticketKeyGenerator = null;
    private ItemPathTranslator itemPathTranslator = null;
    private ItemFilterProcessor itemFilterProcessor = null;

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByPath(java.lang.String)
     */
    public Item findItemByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemByPath(path);
            return dbItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByPath(java.lang.String, java.lang.String)
     */
    public Item findItemByPath(String path, String parentUid) {
        try {
            Item parent = findItemByUid(parentUid);
            if (parent == null) {
                return null;
            }
            Item item = itemPathTranslator.findItemByPath(path, (CollectionItem) parent);
            return item;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }


    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemParentByPath(java.lang.String)
     */
    public Item findItemParentByPath(String path) {
        try {
            Item dbItem = itemPathTranslator.findItemParent(path);
            return dbItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#findItemByUid(java.lang.String)
     */
    public Item findItemByUid(String uid) {
        try {
            // prevent auto flushing when looking up item by uid
            getSession().setFlushMode(FlushMode.MANUAL);
            return (Item) getSession().byNaturalId(HibItem.class).using("uid", uid).load();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItem(org.unitedinternet.cosmo.model.Item)
     */
    public void removeItem(Item item) {
        try {

            if (item == null) {
                throw new IllegalArgumentException("item cannot be null");
            }

            if (item instanceof HibHomeCollectionItem) {
                throw new IllegalArgumentException("cannot remove root item");
            }

            removeItemInternal(item);
            getSession().flush();

        } catch (ObjectNotFoundException onfe) {
            throw new ItemNotFoundException("item not found");
        } catch (ObjectDeletedException ode) {
            throw new ItemNotFoundException("item not found");
        } catch (UnresolvableObjectException uoe) {
            throw new ItemNotFoundException("item not found");
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public HibHomeCollectionItem getRootItem(User user, boolean forceReload) {
        if(forceReload){
            getSession().clear();
        }
        return getRootItem(user);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.ItemDao#getRootItem(org.unitedinternet.cosmo.model.User)
     */
    public HibHomeCollectionItem getRootItem(User user) {
        try {
            return findRootItem(user.getId());
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#createRootItem(org.unitedinternet.cosmo.model.User)
     */
    public HibHomeCollectionItem createRootItem(User user) {
        try {

            if (user == null) {
                throw new IllegalArgumentException("invalid user");
            }

            if (findRootItem(user.getId()) != null) {
                throw new CosmoException("user already has root item", new CosmoException());
            }

            HibHomeCollectionItem newItem = new HibHomeCollectionItem();

            newItem.setOwner(user);
            newItem.setName(user.getEmail());
            //do not set this, it might be sensitive or different than name
            //newItem.setDisplayName(newItem.getName()); 
            setBaseItemProps(newItem);
            getSession().save(newItem);
            getSession().flush();
            return newItem;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        } catch (ConstraintViolationException cve) {
            logConstraintViolationException(cve);
            throw cve;
        }
    }

    public void addItemToCollection(Item item, CollectionItem collection) {
        try {
            addItemToCollectionInternal(item, collection);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void removeItemFromCollection(Item item, CollectionItem collection) {
        try {
            removeItemFromCollectionInternal(item, collection);
            getSession().flush();
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItemByPath(java.lang.String)
     */
    public void removeItemByPath(String path) {
        try {
            Item item = itemPathTranslator.findItemByPath(path);
            if (item == null) {
                throw new ItemNotFoundException("item at " + path
                        + " not found");
            }
            removeItem(item);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#removeItemByUid(java.lang.String)
     */
    public void removeItemByUid(String uid) {
        try {
            Item item = findItemByUid(uid);
            if (item == null) {
                throw new ItemNotFoundException("item with uid " + uid
                        + " not found");
            }
            removeItem(item);
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ItemDao#initializeItem(org.unitedinternet.cosmo.model.Item)
     */
    public void initializeItem(Item item) {
        try {
            LOG.info("initialize Item : "+item.getUid());
            // initialize all the proxied-associations, to prevent
            // lazy-loading of this data
            Hibernate.initialize(item.getAttributes());
            Hibernate.initialize(item.getStamps());
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    
    /**
     * find the set of collection items as children of the given collection item.
     * 
     * @param collectionItem parent collection item
     * @return set of children collection items or empty list of parent collection has no children
     */
    public Set<CollectionItem> findCollectionItems(CollectionItem collectionItem){
        try {
            HashSet<CollectionItem> children = new HashSet<CollectionItem>();
            Query hibQuery = getSession().getNamedQuery("collections.children.by.parent")
                    .setParameter("parent", collectionItem);

            List<?> results = hibQuery.list();
            for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
                CollectionItem content = (CollectionItem) it.next();
                children.add(content);
            }
            return children;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public Set<Item> findCollectionFileItems(CollectionItem collectionItem){
        try {
            HashSet<Item> children = new HashSet<Item>();
            Query hibQuery = getSession().getNamedQuery("collections.files.by.parent")
                    .setParameter("parent", collectionItem);

            List<?> results = hibQuery.list();
            for (Iterator<?> it = results.iterator(); it.hasNext(); ) {
                Item content = (Item) it.next();
                children.add(content);
            }
            return children;
        } catch (HibernateException e) {
            getSession().clear();
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /**
     * Set the unique ID generator for new items
     *
     * @param idGenerator
     */
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Set the unique key generator for new tickets
     *
     * @param ticketKeyGenerator
     */
    public void setTicketKeyGenerator(TokenService ticketKeyGenerator) {
        this.ticketKeyGenerator = ticketKeyGenerator;
    }

    /**
     * Set the path translator. The path translator is responsible for
     * translating a path to an item in the database.
     *
     * @param itemPathTranslator
     */
    public void setItemPathTranslator(ItemPathTranslator itemPathTranslator) {
        this.itemPathTranslator = itemPathTranslator;
    }


    public void setItemFilterProcessor(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }


    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.Dao#destroy()
     */
    public abstract void destroy();

    /*
     * (non-Javadoc)
     *
     * @see org.unitedinternet.cosmo.dao.Dao#init()
     */
    public void init() {
        if (idGenerator == null) {
            throw new IllegalStateException("idGenerator is required");
        }

        if (ticketKeyGenerator == null) {
            throw new IllegalStateException("ticketKeyGenerator is required");
        }

        if (itemPathTranslator == null) {
            throw new IllegalStateException("itemPathTranslator is required");
        }

        if (itemFilterProcessor == null) {
            throw new IllegalStateException("itemFilterProcessor is required");
        }

    }

    /**
     * Verifies that name is unique in collection, meaning no item exists
     * in collection with the same item name.
     *
     * @param item       item name to check
     * @param collection collection to check against
     * @throws org.unitedinternet.cosmo.dao.DuplicateItemNameException if item with same name exists
     *                                    in collection
     */
    protected void verifyItemNameUnique(Item item, CollectionItem collection) {
        Query hibQuery = getSession().getNamedQuery("itemId.by.parentId.name");
        hibQuery.setParameter("name", item.getName()).setParameter("parentid",
                ((HibItem) collection).getId());
        List<Long> results = hibQuery.list();
        if (results.size() > 0) {
            throw new DuplicateItemNameException(item, "item name " + item.getName() +
                    " already exists in collection " + collection.getUid());
        }
    }

    // Set server generated item properties
    protected void setBaseItemProps(Item item) {
        if (item.getUid() == null) {
            item.setUid(idGenerator.nextStringIdentifier());
        }
        if (item.getName() == null) {
            item.setName(item.getUid());
        }
        if (item instanceof HibICalendarItem) {
            HibICalendarItem ical = (HibICalendarItem) item;
            if (ical.getIcalUid() == null) {
                ical.setIcalUid(item.getUid());
                EventStamp es = HibEventStamp.getStamp(ical);
                if (es != null) {
                    es.setIcalUid(ical.getIcalUid());
                }
            }
        }
    }

    protected HibHomeCollectionItem findRootItem(Long dbUserId) {
        Query hibQuery = getSession().getNamedQuery(
                "homeCollection.by.ownerId").setParameter("ownerid",
                dbUserId);
        hibQuery.setCacheable(true);
        hibQuery.setFlushMode(FlushMode.MANUAL);

        return (HibHomeCollectionItem) hibQuery.uniqueResult();
    }

    protected void checkForDuplicateUid(Item item) {
        // verify uid not in use
        if (item.getUid() != null) {

            // Lookup item by uid
            Query hibQuery = getSession().getNamedQuery("itemid.by.uid")
                    .setParameter("uid", item.getUid());
            hibQuery.setFlushMode(FlushMode.MANUAL);

            Long itemId = (Long) hibQuery.uniqueResult();

            // if uid is in use throw exception
            if (itemId != null) {
                throw new UidInUseException(item.getUid(), "uid " + item.getUid()
                        + " already in use");
            }
        }
    }

    protected void removeItemFromCollectionInternal(Item item, CollectionItem collection) {

        getSession().update(collection);
        getSession().update(item);

        // do nothing if item doesn't belong to collection
        if (!item.getParents().contains(collection)) {
            return;
        }

        ((HibItem) item).removeParent(collection);

        // If the item belongs to no collection, then it should
        // be purged.
        if (item.getParents().size() == 0) {
            removeItemInternal(item);
        }
    }

    protected void addItemToCollectionInternal(Item item,
                                               CollectionItem collection) {
        verifyItemNameUnique(item, collection);
        getSession().update(item);
        getSession().update(collection);
        ((HibItem) item).addParent(collection);
    }

    protected void removeItemInternal(Item item) {
        getSession().delete(item);
    }

    protected BaseModelObject getBaseModelObject(Object obj) {
        return (BaseModelObject) obj;
    }
}
