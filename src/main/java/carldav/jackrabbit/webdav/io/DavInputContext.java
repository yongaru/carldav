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
package carldav.jackrabbit.webdav.io;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarDataException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.unitedinternet.cosmo.dav.caldav.UnsupportedCalendarDataException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

import static carldav.CarldavConstants.TEXT_CALENDAR;
import static carldav.CarldavConstants.TEXT_VCARD;

/**
 * An <code>InputContext</code> that supports the semantics of DAV extensions like CalDAV.
 *
 */
public class DavInputContext implements CaldavConstants {

    private final HttpServletRequest request;
    private final InputStream in;
    private Calendar calendar;

    public DavInputContext(HttpServletRequest request, InputStream in) {
        Assert.notNull(request, "request is null");
        this.request = request;
        this.in = in;
    }

    @Deprecated
    public Calendar getCalendar() throws CosmoDavException {
        return getCalendar(false);
    }

    public String getCalendarString() throws CosmoDavException {
        return getCalendar().toString();
    }

    /**
     * Parses the input stream into a calendar object.
     * 
     * @param allowCalendarWithMethod
     *            don't break on Calendars with METHOD property
     * @return Calendar parsed
     * @throws CosmoDavException
     *             - if something is wrong this exception is thrown.
     */
    public Calendar getCalendar(boolean allowCalendarWithMethod) throws CosmoDavException {
        if (calendar != null) {
            return calendar;
        }

        if (!hasStream()) {
            return null;
        }

        if (getContentType() == null) {
            throw new BadRequestException("No media type specified");
        }

        final MediaType mediaType = MediaType.parseMediaType(getContentType());
        if (!mediaType.isCompatibleWith(TEXT_CALENDAR) && !mediaType.isCompatibleWith(TEXT_VCARD)) {
            throw new UnsupportedCalendarDataException(mediaType.toString());
        }

        try {
            Calendar c = CalendarUtils.parseCalendar(getInputStream());
            c.validate(true);

            if (CalendarUtils.hasMultipleComponentTypes(c)) {
                throw new InvalidCalendarResourceException("Calendar object contains more than one type of component");
            }
            if (!allowCalendarWithMethod && c.getProperties().getProperty(Property.METHOD) != null) {
                throw new InvalidCalendarResourceException("Calendar object contains METHOD property");
            }

            calendar = c;
        } catch (IOException e) {
            throw new CosmoDavException(e);
        } catch (ParserException e) {
            throw new InvalidCalendarDataException("Failed to parse calendar object: " + e.getMessage());
        } catch (ValidationException e) {
            throw new InvalidCalendarDataException("Invalid calendar object: " + e.getMessage());
        }

        return calendar;
    }

    public boolean hasStream() {
        return in != null;
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getContentType() {
        return request.getHeader(HttpHeaders.CONTENT_TYPE);
    }
}
