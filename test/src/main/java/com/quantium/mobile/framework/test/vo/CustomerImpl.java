package com.quantium.mobile.framework.test.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.document.gen.DocumentEditable;
import com.quantium.mobile.framework.test.gen.AbstractCustomer;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class CustomerImpl extends AbstractCustomer
{

    public static final long serialVersionUID = AbstractCustomer.publicSerialVersionUID;

    public CustomerImpl () {
        super();
    }

    public CustomerImpl (
            String _id, Date _createdAt, Date _inactivatedAt,
            Date _lastModified, String _name, QuerySet<Document> _CustomerDocuments) {
        super(
            _id, _createdAt, _inactivatedAt,
            _lastModified, _name, _CustomerDocuments);
    }
}

