package com.quantium.mobile.framework.test.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.document.gen.DocumentEditable;
import com.quantium.mobile.framework.test.vo.Author;
import com.quantium.mobile.framework.test.gen.AuthorEditable;
import com.quantium.mobile.framework.test.gen.AbstractScore;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class ScoreImpl extends AbstractScore
{
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
