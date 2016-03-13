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
package org.unitedinternet.cosmo.dao.hibernate.query;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitedinternet.cosmo.IntegrationTestSupport;
import org.unitedinternet.cosmo.dao.query.hibernate.StandardItemFilterProcessor;
import org.unitedinternet.cosmo.model.filter.*;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

import java.util.Calendar;
import java.util.Date;

public class StandardItemFilterProcessorTest extends IntegrationTestSupport {

    @Autowired
    private SessionFactory sessionFactory;

    private StandardItemFilterProcessor queryBuilder;

    private TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();

    @Before
    public void before() {
        queryBuilder = new StandardItemFilterProcessor(sessionFactory);
    }

    /**
     * Tests uid query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testUidQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setUid(Restrictions.eq("abc"));
        Query query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.uid=:param0", query.getQueryString());
    }

    @Test
    public void testModifiedSinceQuery(){
        NoteItemFilter filter = new NoteItemFilter();
        Calendar c = Calendar.getInstance();
        Date end = c.getTime();
        c.add(Calendar.YEAR, -1);
        filter.setModifiedSince(Restrictions.between(c.getTime(), end));
        Query query = queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.modifiedDate between :param0 and :param1", query.getQueryString());
    }

    /**
     * Tests display name query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDisplayNameQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setDisplayName(Restrictions.eq("test"));
        Query query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName=:param0", query.getQueryString());

        filter.setDisplayName(Restrictions.neq("test"));
        query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName!=:param0", query.getQueryString());

        filter.setDisplayName(Restrictions.like("test"));
        query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName like :param0", query.getQueryString());

        filter.setDisplayName(Restrictions.nlike("test"));
        query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName not like :param0", query.getQueryString());

        filter.setDisplayName(Restrictions.isNull());
        query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where i.displayName is null", query.getQueryString());

        filter.setDisplayName(Restrictions.ilike("test"));
        query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where lower(i.displayName) like :param0", query.getQueryString());

        filter.setDisplayName(Restrictions.nilike("test"));
        query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i where lower(i.displayName) not like :param0", query.getQueryString());

    }

    /**
     * Tests parent query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setParent(1L);
        Query query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i join i.collection pd where "
                + "pd.id=:parent", query.getQueryString());
    }

    /**
     * Tests display name and parent query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testDisplayNameAndParentQuery() throws Exception {
        ItemFilter filter = new ItemFilter();
        filter.setParent(0L);
        filter.setDisplayName(Restrictions.eq("test"));
        Query query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i join i.collection pd where "
                + "pd.id=:parent and i.displayName=:param1", query.getQueryString());
    }

    /**
     * Tests basic stamp query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testBasicStampQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        StampFilter stampFilter = new StampFilter();
        stampFilter.setStampClass(HibItem.class);
        filter.getStampFilters().add(stampFilter);
        Query query = queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i", query.getQueryString());
    }

    /**
     * Tests event stamp query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        filter.setParent(0L);
        filter.setDisplayName(Restrictions.eq("test"));
        filter.setIcalUid(Restrictions.eq("icaluid"));
        //filter.setBody("body");
        filter.getStampFilters().add(eventFilter);
        Query query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i join i.collection pd "
                + "where pd.id=:parent and "
                + "i.displayName=:param1 and i.type=:type and i.uid=:param3", query.getQueryString());

        eventFilter.setIsRecurring(true);
        query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i join i.collection pd "
                + "where pd.id=:parent and i.displayName=:param1 and "
                + "i.type=:type and (i.recurring=:recurring) "
                + "and i.uid=:param4", query.getQueryString());
    }

    /**
     * Tests event stamp time range query.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventStampTimeRangeQuery() throws Exception {
        NoteItemFilter filter = new NoteItemFilter();
        EventStampFilter eventFilter = new EventStampFilter();
        Period period = new Period(new DateTime("20070101T100000Z"), new DateTime("20070201T100000Z"));
        eventFilter.setPeriod(period);
        eventFilter.setTimezone(registry.getTimeZone("America/Chicago"));

        filter.setParent(0L);
        filter.getStampFilters().add(eventFilter);
        Query query =  queryBuilder.buildQuery(sessionFactory.getCurrentSession(), filter);
        Assert.assertEquals("select i from HibItem i join i.collection pd " +
                "where pd.id=:parent and i.type=:type and ( (i.startDate < :endDate) and i.endDate > :startDate) " +
                "or (i.startDate=i.endDate and (i.startDate=:startDate or i.startDate=:endDate)))"
                ,query.getQueryString());
    }
}
