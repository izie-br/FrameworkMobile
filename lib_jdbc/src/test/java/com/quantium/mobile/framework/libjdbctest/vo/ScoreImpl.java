package com.quantium.mobile.framework.libjdbctest.vo;

import com.quantium.mobile.framework.libjdbctest.gen.AbstractScore;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class ScoreImpl extends AbstractScore {

    public static final long serialVersionUID = AbstractScore.publicSerialVersionUID;

    public ScoreImpl() {
        super();
    }

    public ScoreImpl(
            String _id, Author _Author, Document _Document,
            long _score) {
        setId(_id);
        setAuthor(_Author);
        setDocument(_Document);
        setScore(_score);
    }
}

