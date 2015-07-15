package com.quantium.mobile.framework.libtests;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.utils.PluralizacaoUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PluralizacaoUtilsTest {

    public static final String BOY_PLURAL = "boys";
    public static final String JORNEY_PLURAL = "jorneys";
    public static final String CITY_PLURAL = "cities";
    public static final String BABY_PLURAL = "babies";
    public static final String SKY_PLURAL = "skies";
    public static final String BOY_SINGULAR = "boy";
    public static final String JORNEY_SINGULAR = "jorney";
    public static final String CITY_SINGULAR = "city";
    public static final String BABY_SINGULAR = "baby";
    public static final String SKY_SINGULAR = "sky";
    private static final String PENCIL_PLURAL = "pencils";
    private static final String PENCIL_SINGULAR = "pencil";
    private static final String DOCUMENTS_PLURAL = "documents";
    private static final String DOCUMENTS_SINGULAR = "documents";



    @Test
    public void testEndingWithY() {
        assertEquals(BOY_PLURAL,
                PluralizacaoUtils.pluralizar(BOY_SINGULAR));
        assertEquals(JORNEY_PLURAL,
                PluralizacaoUtils.pluralizar(JORNEY_SINGULAR));
        assertEquals(CITY_PLURAL,
                PluralizacaoUtils.pluralizar(CITY_SINGULAR));
        assertEquals(BABY_PLURAL,
                PluralizacaoUtils.pluralizar(BABY_SINGULAR));
        assertEquals(SKY_PLURAL,
                PluralizacaoUtils.pluralizar(SKY_SINGULAR));
    }


    @Test
    public void testOthers() {
        assertEquals(PENCIL_PLURAL,
                PluralizacaoUtils.pluralizar(PENCIL_SINGULAR));
        assertEquals(DOCUMENTS_PLURAL,
                PluralizacaoUtils.pluralizar(DOCUMENTS_SINGULAR));
    }


}
