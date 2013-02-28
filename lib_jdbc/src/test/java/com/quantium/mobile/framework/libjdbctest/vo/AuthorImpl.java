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
    private static final long serialVersionUID = 9069129324679577603L;

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

