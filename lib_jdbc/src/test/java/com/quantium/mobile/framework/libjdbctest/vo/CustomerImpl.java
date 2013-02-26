package com.quantium.mobile.framework.libjdbctest.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.libjdbctest.vo.Document;
import com.quantium.mobile.framework.libjdbctest.gen.DocumentEditable;
import com.quantium.mobile.framework.libjdbctest.gen.AbstractCustomer;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class CustomerImpl extends AbstractCustomer
{
    public CustomerImpl () {
        super();
    }

    public CustomerImpl (
            long _id, String _name, QuerySet<Document> _DocumentDocuments) {
        super(
            _id, _name, _DocumentDocuments);
    }
}

