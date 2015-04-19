package com.quantium.mobile.framework.test.vo;

import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.gen.AbstractAuthor;
import com.quantium.mobile.framework.test.vo.Score;

import java.util.Date;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class AuthorImpl extends AbstractAuthor {

    public static final long serialVersionUID = AbstractAuthor.publicSerialVersionUID;

    public AuthorImpl() {
        super();
    }

    public AuthorImpl(
            String _id, Date _createdAt, Date _inactivatedAt,
            Date _lastModified, String _name, boolean _active,
            QuerySet<Document> _AuthorDocuments, QuerySet<Score> _AuthorScores) {
        setId(_id);
        setName(_name);
        setActive(_active);
        setAuthorDocuments(_AuthorDocuments);
        setAuthorScores(_AuthorScores);
        setCreatedAt(_createdAt);
        setInactivatedAt(_inactivatedAt);
        setLastModified(_lastModified);
    }
}

