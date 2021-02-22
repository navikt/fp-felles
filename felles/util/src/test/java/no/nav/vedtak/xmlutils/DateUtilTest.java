package no.nav.vedtak.xmlutils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.xml.datatype.DatatypeConstants;

import org.junit.jupiter.api.Test;

class DateUtilTest {

    @Test
    void test_convertToXMLGregorianCalendar_LocalDateTime() {

        var localDateTime = LocalDateTime.now();
        var xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDateTime);
        assertThat(xmlGregCal).isNotNull();
        assertThat(xmlGregCal.getHour()).isEqualTo(localDateTime.getHour());

        xmlGregCal = DateUtil.convertToXMLGregorianCalendar((LocalDateTime) null);
        assertThat(xmlGregCal).isNull();
    }

    @Test
    void test_convertToXMLGregorianCalendar_LocalDate() {

        var localDate = LocalDate.now();
        var xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDate);
        assertThat(xmlGregCal).isNotNull();
        assertThat(xmlGregCal.getDay()).isEqualTo(localDate.getDayOfMonth());

        xmlGregCal = DateUtil.convertToXMLGregorianCalendar((LocalDate) null);
        assertThat(xmlGregCal).isNull();
    }

    @Test
    void test_convertToXMLGregorianCalendarRemoveTimezone() {

        var localDate = LocalDate.now();
        var xmlGregCal = DateUtil.convertToXMLGregorianCalendarRemoveTimezone(localDate);
        assertThat(xmlGregCal).isNotNull();
        assertThat(xmlGregCal.getDay()).isEqualTo(localDate.getDayOfMonth());
        assertThat(xmlGregCal.getTimezone()).isEqualTo(DatatypeConstants.FIELD_UNDEFINED);

        assertThat(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(null)).isNull();
    }

    @Test
    void test_convertToLocalDateTime() {

        var localDateTime1 = LocalDateTime.now();
        var xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDateTime1);
        var localDateTime2 = DateUtil.convertToLocalDateTime(xmlGregCal);
        assertThat(localDateTime2).isNotNull();
        assertThat(localDateTime2.getHour()).isEqualTo(localDateTime1.getHour());

        var localDateTime3 = DateUtil.convertToLocalDateTime(null);
        assertThat(localDateTime3).isNull();
    }

    @Test
    void test_convertToLocalDate() {

        LocalDate localDate1 = LocalDate.now();
        var xmlGregCal = DateUtil.convertToXMLGregorianCalendar(localDate1);
        var localDate2 = DateUtil.convertToLocalDate(xmlGregCal);
        assertThat(localDate2).isNotNull();
        assertThat(localDate2.getDayOfMonth()).isEqualTo(localDate1.getDayOfMonth());

        assertThat(DateUtil.convertToLocalDate(null)).isNull();
    }
}
