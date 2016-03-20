package org.unitedinternet.cosmo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.dao.query.ItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
public class ItemDaoImpl implements ItemDaoCustom {

    private final ItemFilterProcessor itemFilterProcessor;

    @Autowired
    public ItemDaoImpl(ItemFilterProcessor itemFilterProcessor) {
        this.itemFilterProcessor = itemFilterProcessor;
    }

    @Override
    public Set<HibItem> findCalendarItems(ItemFilter itemFilter) {
        return itemFilterProcessor.processFilter(itemFilter);
    }
}