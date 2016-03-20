package carldav.repository;

import org.unitedinternet.cosmo.model.filter.ItemFilter;
import carldav.entity.HibItem;

import java.util.Set;

/**
 * @author Kamill Sokol
 */
interface ItemRepositoryCustom {

    Set<HibItem> findCalendarItems(ItemFilter itemFilter);
}