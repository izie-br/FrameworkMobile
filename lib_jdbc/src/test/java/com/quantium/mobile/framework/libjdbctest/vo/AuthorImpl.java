package com.quantium.mobile.framework.libjdbctest.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.libjdbctest.vo.Score;
import com.quantium.mobile.framework.libjdbctest.gen.ScoreEditable;
import com.quantium.mobile.framework.libjdbctest.vo.Document;
import com.quantium.mobile.framework.libjdbctest.gen.DocumentEditable;
import com.quantium.mobile.framework.libjdbctest.gen.AbstractAuthor;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class AuthorImpl extends AbstractAuthor
{

    public static final long serialVersionUID = AbstractAuthor.publicSerialVersionUID;

    public AuthorImpl () {
        super();
    }

    public AuthorImpl (
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

