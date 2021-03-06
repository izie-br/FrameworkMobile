package com.quantium.mobile.framework.libjdbctest.vo;

import com.quantium.mobile.framework.libjdbctest.gen.AbstractAuthor;
import com.quantium.mobile.framework.query.QuerySet;

import java.util.Date;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class AuthorImpl extends AbstractAuthor {

    public static final long serialVersionUID = AbstractAuthor.publicSerialVersionUID;

    public AuthorImpl() {
        super();
    }

    public AuthorImpl(
            String _id, Date _createdAt, String _name,
            boolean _active, QuerySet<Document> _AuthorDocuments, QuerySet<Score> _AuthorScores) {
        setId(_id);
        setCreatedAt(_createdAt);
        setName(_name);
        setActive(_active);
        setAuthorDocuments(_AuthorDocuments);
        setAuthorScores(_AuthorScores);
    }
}

