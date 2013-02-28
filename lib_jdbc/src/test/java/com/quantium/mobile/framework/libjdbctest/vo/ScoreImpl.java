package com.quantium.mobile.framework.libjdbctest.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.libjdbctest.vo.Document;
import com.quantium.mobile.framework.libjdbctest.gen.DocumentEditable;
import com.quantium.mobile.framework.libjdbctest.vo.Author;
import com.quantium.mobile.framework.libjdbctest.gen.AuthorEditable;
import com.quantium.mobile.framework.libjdbctest.gen.AbstractScore;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class ScoreImpl extends AbstractScore
{
    private static final long serialVersionUID = -7438227287147311338L;

    public ScoreImpl () {
        super();
    }

    public ScoreImpl (
            long _id, Author _Author, Document _Document,
            long _score) {
        super(
            _id, _Author, _Document,
            _score);
    }
}

