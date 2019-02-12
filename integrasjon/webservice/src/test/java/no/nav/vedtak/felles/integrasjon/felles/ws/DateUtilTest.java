package no.nav.vedtak.felles.integrasjon.felles.ws;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

public class DateUtilTest {

    @Test
    public void test_convertToXMLGregorianCalendar_LocalDateTime() throws DatatypeConfigurationException {

        LocalDateTime localDateTime = LocalDateTime.now();
        XMLGregorianCalendar xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDateTime);
        assertThat(xmlGregCal).isNotNull();
        assertThat(xmlGregCal.getHour()).isEqualTo(localDateTime.getHour());

        xmlGregCal = DateUtil.convertToXMLGregorianCalendar((LocalDateTime) null);
        assertThat(xmlGregCal).isNull();
    }

    @Test
    public void test_convertToXMLGregorianCalendar_LocalDate() throws DatatypeConfigurationException {

        LocalDate localDate = LocalDate.now();
        XMLGregorianCalendar xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDate);
        assertThat(xmlGregCal).isNotNull();
        assertThat(xmlGregCal.getDay()).isEqualTo(localDate.getDayOfMonth());

        xmlGregCal = DateUtil.convertToXMLGregorianCalendar((LocalDate) null);
        assertThat(xmlGregCal).isNull();
    }

    @Test
    public void test_convertToXMLGregorianCalendarRemoveTimezone() throws DatatypeConfigurationException {

        LocalDate localDate = LocalDate.now();
        XMLGregorianCalendar xmlGregCal = DateUtil.convertToXMLGregorianCalendarRemoveTimezone(localDate);
        assertThat(xmlGregCal).isNotNull();
        assertThat(xmlGregCal.getDay()).isEqualTo(localDate.getDayOfMonth());
        assertThat(xmlGregCal.getTimezone()).isEqualTo(DatatypeConstants.FIELD_UNDEFINED);

        xmlGregCal = DateUtil.convertToXMLGregorianCalendarRemoveTimezone(null);
        assertThat(xmlGregCal).isNull();
    }

    @Test
    public void test_convertToLocalDateTime() throws DatatypeConfigurationException {

        final LocalDateTime localDateTime1 = LocalDateTime.now();
        XMLGregorianCalendar xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDateTime1);
        final LocalDateTime localDateTime2 = DateUtil.convertToLocalDateTime(xmlGregCal);
        assertThat(localDateTime2).isNotNull();
        assertThat(localDateTime2.getHour()).isEqualTo(localDateTime1.getHour());

        final LocalDateTime localDateTime3 = DateUtil.convertToLocalDateTime(null);
        assertThat(localDateTime3).isNull();
    }

    @Test
    public void test_convertToLocalDate() throws DatatypeConfigurationException {

        final LocalDate localDate1 = LocalDate.now();
        XMLGregorianCalendar xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDate1);
        LocalDate localDate2 = DateUtil.convertToLocalDate(xmlGregCal);
        assertThat(localDate2).isNotNull();
        assertThat(localDate2.getDayOfMonth()).isEqualTo(localDate1.getDayOfMonth());

        LocalDate localDate3 = DateUtil.convertToLocalDate(null);
        assertThat(localDate3).isNull();
    }
}
