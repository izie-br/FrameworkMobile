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

    public static final long serialVersionUID = AbstractScore.publicSerialVersionUID;

    public ScoreImpl () {
        super();
    }

    public ScoreImpl (
            String _id, Date _createdAt, Date _inactivatedAt,
            Date _lastModified, Author _Author, Document _Document,
            long _score) {
    	setId(_id);
    	setCreatedAt(_createdAt);
    	setInactivatedAt(_inactivatedAt);
    	setLastModified(_lastModified);
        setAuthor(_Author);
        setDocument(_Document);
        setScore(_score);
    }
}

