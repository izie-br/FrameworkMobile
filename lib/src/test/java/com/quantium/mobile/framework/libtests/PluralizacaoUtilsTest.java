package com.quantium.mobile.framework.libtests;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.utils.PluralizacaoUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PluralizacaoUtilsTest {

    @Test
    public void testPlural() {
        assertEquals("Boys",
                PluralizacaoUtils.pluralizar("Boy"));
        assertEquals("boys",
                PluralizacaoUtils.pluralizar("boy"));
        assertEquals("skis",
                PluralizacaoUtils.pluralizar("ski"));
        assertEquals("witches",
                PluralizacaoUtils.pluralizar("witch"));
        assertEquals("boxes",
                PluralizacaoUtils.pluralizar("box"));
        assertEquals("kisses",
                PluralizacaoUtils.pluralizar("kiss"));
        assertEquals("Joneses",
                PluralizacaoUtils.pluralizar("Jones"));
        assertEquals("journeys",
                PluralizacaoUtils.pluralizar("journey"));
        assertEquals("cities",
                PluralizacaoUtils.pluralizar("city"));
        assertEquals("skies",
                PluralizacaoUtils.pluralizar("sky"));
        assertEquals("babies",
                PluralizacaoUtils.pluralizar("baby"));
        assertEquals("pencils",
                PluralizacaoUtils.pluralizar("pencil"));
        assertEquals("syncs",
                PluralizacaoUtils.pluralizar("sync"));
        assertEquals("syncsCities",
                PluralizacaoUtils.pluralizar("syncsCity"));
        assertEquals("tomatoes",
                PluralizacaoUtils.pluralizar("tomato"));
//        assertEquals("knife",
//                PluralizacaoUtils.pluralizar("knives"));
        assertEquals("addresses",
                PluralizacaoUtils.pluralizar("address"));
        assertEquals("userAddresses",
                PluralizacaoUtils.pluralizar("userAddress"));
    }


}
