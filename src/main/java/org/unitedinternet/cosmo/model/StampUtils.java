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
package org.unitedinternet.cosmo.model;

import org.unitedinternet.cosmo.model.hibernate.HibCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibEventExceptionStamp;
import org.unitedinternet.cosmo.model.hibernate.HibItem;

/**
 * Contains static helper methods for dealing with Stamps.
 */
public class StampUtils {

    /**
     * Return EventStamp from Item
     * @param hibItem
     * @return EventStamp from Item
     */
    public static EventStamp getEventStamp(HibItem hibItem) {
        return (EventStamp) hibItem.getStamp(EventStamp.class);
    }
    
    /**
     * Return EventExceptionStamp from Item
     * @param hibItem
     * @return EventExceptionStamp from Item
     */
    public static HibEventExceptionStamp getEventExceptionStamp(HibItem hibItem) {
        return (HibEventExceptionStamp) hibItem.getStamp(HibEventExceptionStamp.class);
    }
    
    /**
     * Return CalendarCollectionStamp from Item
     * @param hibItem
     * @return CalendarCollectionStamp from Item
     */
    public static HibCalendarCollectionStamp getCalendarCollectionStamp(HibItem hibItem) {
        return (HibCalendarCollectionStamp) hibItem.getStamp(HibCalendarCollectionStamp.class);
    }
}
