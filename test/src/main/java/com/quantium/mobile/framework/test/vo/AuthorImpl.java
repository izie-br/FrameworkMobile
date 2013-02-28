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
    private static final long serialVersionUID = -6993679503898507472L;

    public AuthorImpl () {
        super();
    }

    public AuthorImpl (
            long _id, Date _createdAt, String _name,
            boolean _active, QuerySet<Document> _AuthorDocuments, QuerySet<Score> _AuthorScores) {
        super(
            _id, _createdAt, _name,
            _active, _AuthorDocuments, _AuthorScores);
    }
}

