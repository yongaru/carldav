package carldav;

import carldav.jackrabbit.webdav.property.DavPropertyName;
import org.springframework.http.MediaType;

import javax.xml.namespace.QName;
import java.nio.charset.Charset;

import static carldav.jackrabbit.webdav.property.DavPropertyName.create;

/**
 * @author Kamill Sokol
 */
public final class CarldavConstants {

    public static final MediaType TEXT_CALENDAR = new MediaType("text", "calendar", Charset.forName("UTF-8"));

    public static final String TEXT_CALENDAR_VALUE = TEXT_CALENDAR.toString();

    public static final MediaType TEXT_HTML = new MediaType("text", "html", Charset.forName("UTF-8"));

    public static final String TEXT_HTML_VALUE = TEXT_HTML.toString();

    public static final MediaType TEXT_VCARD = new MediaType("text", "vcard");

    public static String PRE_CALDAV = "D";
    public static String NS_CALDAV = "DAV:";

    public static String PRE_CARD = "CARD";
    public static String NS_CARDDAV = "urn:ietf:params:xml:ns:carddav";

    public static String PRE_CS = "CS";
    public static String NS_CS = "http://calendarserver.org/ns/";

    public static String PRE_C = "C";
    public static String NS_C = "urn:ietf:params:xml:ns:caldav";

    public static QName EMPTY = new QName("");

    public static final String XML_STATUS = "status";

    public static final String ELEMENT_CALDAV_CALENDAR = "calendar";

    public static final DavPropertyName DISPLAY_NAME = create(caldav("displayname"));
    public static final DavPropertyName GET_CONTENT_LENGTH = create(caldav("getcontentlength"));
    public static final DavPropertyName GET_CONTENT_TYPE = create(caldav("getcontenttype"));
    public static final DavPropertyName GET_ETAG = create(caldav("getetag"));
    public static final DavPropertyName GET_LAST_MODIFIED = create(caldav("getlastmodified"));
    public static final DavPropertyName RESOURCE_TYPE = create(caldav("resourcetype"));
    public static final DavPropertyName IS_COLLECTION = create(caldav("iscollection"));
    public static final DavPropertyName PRINCIPAL_URL = create(caldav("principal-URL"));
    public static final DavPropertyName SUPPORTED_REPORT_SET = create(caldav("supported-report-set"));

    public static final DavPropertyName SUPPORTED_CALENDAR_DATA = create(c("supported-calendar-data"));
    public static final DavPropertyName SUPPORTED_COLLATION_SET = create(c("supported-collation-set"));
    public static final DavPropertyName SUPPORTED_CALENDAR_COMPONENT_SET = create(c("supported-calendar-component-set"));
    public static final DavPropertyName CALENDAR_DATA = create(c("calendar-data"));
    public static final DavPropertyName CALENDAR_HOME_SET = create(c("calendar-home-set"));

    public static final DavPropertyName ADDRESSBOOK_HOME_SET = create(carddav("addressbook-home-set"));
    public static final DavPropertyName ADDRESS_DATA = create(carddav("address-data"));
    public static final DavPropertyName SUPPORTED_ADDRESS_DATA = create(carddav("supported-address-data"));

    public static final DavPropertyName GET_CTAG = create(cs("getctag"));

    public static final QName RESOURCE_TYPE_CALENDAR = c(ELEMENT_CALDAV_CALENDAR); // CosmoQName(NS_CALDAV, ELEMENT_CALDAV_CALENDAR, PRE_CALDAV);

    public static QName caldav(String localName) {
        return new QName(NS_CALDAV, localName, PRE_CALDAV);
    }

    public static QName carddav(String localName) {
        return new QName(NS_CARDDAV, localName, PRE_CARD);
    }

    public static QName cs(String localName) {
        return new QName(NS_CS, localName, PRE_CS);
    }

    public static QName c(String localName) {
        return new QName(NS_C, localName, PRE_C);
    }
}
