package com.quantium.mobile.framework.libjdbctest.vo;

import com.quantium.mobile.framework.libjdbctest.gen.AbstractCustomer;
import com.quantium.mobile.framework.query.QuerySet;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class CustomerImpl extends AbstractCustomer {

    public static final long serialVersionUID = AbstractCustomer.publicSerialVersionUID;

    public CustomerImpl() {
        super();
    }

    public CustomerImpl(
            String _id, String _name, QuerySet<Document> _DocumentDocuments) {
        setId(_id);
        setName(_name);
        setDocumentDocuments(_DocumentDocuments);
    }
}

