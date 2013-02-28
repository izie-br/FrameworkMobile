package com.quantium.mobile.framework.test.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.document.gen.DocumentEditable;
import com.quantium.mobile.framework.test.gen.AbstractCustomer;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class CustomerImpl extends AbstractCustomer
{
    private static final long serialVersionUID = -3674103493879914624L;

    public CustomerImpl () {
        super();
    }

    public CustomerImpl (
            long _id, String _name, QuerySet<Document> _DocumentDocuments) {
        super(
            _id, _name, _DocumentDocuments);
    }
}

