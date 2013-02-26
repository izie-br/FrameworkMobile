package com.quantium.mobile.framework.libjdbctest.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.libjdbctest.vo.Score;
import com.quantium.mobile.framework.libjdbctest.gen.ScoreEditable;
import com.quantium.mobile.framework.libjdbctest.vo.Customer;
import com.quantium.mobile.framework.libjdbctest.gen.CustomerEditable;
import com.quantium.mobile.framework.libjdbctest.vo.Author;
import com.quantium.mobile.framework.libjdbctest.gen.AuthorEditable;
import com.quantium.mobile.framework.libjdbctest.gen.AbstractDocument;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class DocumentImpl extends AbstractDocument
{
    public DocumentImpl () {
        super();
    }

    public DocumentImpl (
            long _id, Date _createdAt, String _text,
            String _title, Author _Author, QuerySet<Score> _DocumentScores,
            QuerySet<Customer> _DocumentCustomers) {
        super(
            _id, _createdAt, _text,
            _title, _Author, _DocumentScores,
            _DocumentCustomers);
    }
}

