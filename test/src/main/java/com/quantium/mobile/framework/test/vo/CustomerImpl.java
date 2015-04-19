package com.quantium.mobile.framework.test.vo;

import com.quantium.mobile.framework.query.QuerySet;
import com.quantium.mobile.framework.test.document.vo.Document;
import com.quantium.mobile.framework.test.gen.AbstractCustomer;

import java.util.Date;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class CustomerImpl extends AbstractCustomer {

    public static final long serialVersionUID = AbstractCustomer.publicSerialVersionUID;

    public CustomerImpl() {
        super();
    }

    public CustomerImpl(
            String _id, Date _createdAt, Date _inactivatedAt,
            Date _lastModified, String _name, QuerySet<Document> _CustomerDocuments) {
        setId(_id);
        setName(_name);
        setCustomerDocuments(_CustomerDocuments);
        setCreatedAt(_createdAt);
        setInactivatedAt(_inactivatedAt);
        setLastModified(_lastModified);
    }
}

