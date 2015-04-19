package com.quantium.mobile.framework.test.gerador;

import android.test.ActivityInstrumentationTestCase2;
import com.quantium.mobile.framework.test.TestActivity;
import com.quantium.mobile.framework.test.utils.Utils;
import com.quantium.mobile.framework.test.vo.Author;
import com.quantium.mobile.framework.test.vo.AuthorImpl;
import com.quantium.mobile.framework.utils.DateUtil;

import java.util.Date;

public class VOTests extends ActivityInstrumentationTestCase2<TestActivity> {

    public VOTests() {
        super("com.quantium.mobile.framework.test", TestActivity.class);
    }

    public void testHashCode() {
        Author author1 = Utils.randomAuthor();
        Author author2 = new AuthorImpl(
                author1.getId(), author1.getCreatedAt(), null, null,
                author1.getName(), author1.isActive(), null, null);
        assertEquals(author1, author2);
        assertEquals(author1.hashCode(), author2.hashCode());
    }

    public void testEquals() {
        Author author1 = Utils.randomAuthor();
        Author author2 = new AuthorImpl(
                author1.getId(), author1.getCreatedAt(), null, null,
                author1.getName(), author1.isActive(), null, null);

        assertTrue(author1.equals(author2));
        assertTrue(author1.equals(author1));

        assertFalse(author1.equals(null));
        assertFalse(author1.equals(new Date()));

        assertFalse(author1.equals(new AuthorImpl()));

        assertFalse(author1.equals(new AuthorImpl(
                author1.getId() + 1,    /* changed */
                author1.getCreatedAt(), null, null,
                author1.getName(), author1.isActive(), null, null
        )));

        Date newCreatedAt = DateUtil.adicionaDias(author1.getCreatedAt(), 3);
        assertFalse(author1.equals(new AuthorImpl(
                author1.getId(),
                newCreatedAt,    /* changed */null, null,
                author1.getName(), author1.isActive(), null, null
        )));

        assertFalse(author1.equals(new AuthorImpl(
                author1.getId(), author1.getCreatedAt(), null, null,
                author1.getName() + " Diff",    /* changed */
                author1.isActive(), null, null
        )));

        assertFalse(author1.equals(new AuthorImpl(
                author1.getId(), author1.getCreatedAt(), null, null,
                author1.getName(),
                !(author1.isActive()),    /* changed */
                null, null
        )));

    }

}
