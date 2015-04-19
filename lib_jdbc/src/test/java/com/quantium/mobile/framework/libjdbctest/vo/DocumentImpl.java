package com.quantium.mobile.framework.libjdbctest.vo;

import com.quantium.mobile.framework.libjdbctest.gen.AbstractDocument;
import com.quantium.mobile.framework.query.QuerySet;

import java.util.Date;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class DocumentImpl extends AbstractDocument {

    public static final long serialVersionUID = AbstractDocument.publicSerialVersionUID;

    public DocumentImpl() {
        super();
    }

    public DocumentImpl(
            String _id, Date _createdAt, String _text,
            String _title, Author _Author, QuerySet<Score> _DocumentScores,
            QuerySet<Customer> _DocumentCustomers) {
        setId(_id);
        setCreatedAt(_createdAt);
        setText(_text);
        setDocumentCustomers(_DocumentCustomers);
        setAuthor(_Author);
        setDocumentScores(_DocumentScores);
        setTitle(_title);
    }
}

