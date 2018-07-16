package no.nav.fo.veilarbperson.consumer.tps.mappers;


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class MapperTestUtils {

    public static XMLGregorianCalendar lagDato(final int ar, final int maned, final int dag) throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(ar, maned, dag, 1, 1, 1, 1, 1);
    }

}
