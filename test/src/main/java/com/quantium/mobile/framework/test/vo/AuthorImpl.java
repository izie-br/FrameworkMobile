package com.quantium.mobile.framework.test.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.test.vo.Score;
import com.quantium.mobile.framework.test.gen.ScoreEditable;
import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.document.gen.DocumentEditable;
import com.quantium.mobile.framework.test.gen.AbstractAuthor;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class AuthorImpl extends AbstractAuthor
{

    public static final long serialVersionUID = AbstractAuthor.publicSerialVersionUID;

    public AuthorImpl () {
        super();
    }

    public AuthorImpl (
            String _id, Date _createdAt, Date _lastModified,
            String _name, boolean _active, QuerySet<Document> _AuthorDocuments,
            QuerySet<Score> _AuthorScores) {
        super(
            _id, _createdAt, _lastModified,
            _name, _active, _AuthorDocuments,
            _AuthorScores);
    }
}

