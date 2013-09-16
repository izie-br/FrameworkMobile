package com.quantium.mobile.framework.test.document.vo;

import java.util.Date;
import com.quantium.mobile.framework.query.QuerySet;

import com.quantium.mobile.framework.test.vo.Author;
import com.quantium.mobile.framework.test.gen.AuthorEditable;
import com.quantium.mobile.framework.test.vo.Score;
import com.quantium.mobile.framework.test.gen.ScoreEditable;
import com.quantium.mobile.framework.test.vo.Customer;
import com.quantium.mobile.framework.test.gen.CustomerEditable;
import com.quantium.mobile.framework.test.document.gen.AbstractDocument;

@SuppressWarnings("unused") /* remova este SuppressWarnings ao editar este arquivo */
public class DocumentImpl extends AbstractDocument
{

    public static final long serialVersionUID = AbstractDocument.publicSerialVersionUID;

    public DocumentImpl () {
        super();
    }

    public DocumentImpl (
            String _id, Date _createdAt, Date _inactivatedAt,
            Date _lastModified, Author _Author, String _text,
            String _title, QuerySet<Score> _DocumentScores, QuerySet<Customer> _CustomerCustomers) {
        setId(_id);
        setAuthor(_Author);
        setText(_text);
        setTitle(_title);
        setDocumentScores(_DocumentScores);
        setCustomerCustomers(_CustomerCustomers);
        setCreatedAt(_createdAt);
    	setInactivatedAt(_inactivatedAt);
    	setLastModified(_lastModified);
    }
}

