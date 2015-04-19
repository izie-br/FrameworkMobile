package com.quantium.mobile.framework.validation;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.query.Table.Column;

public class ValidationError {

    private final Table.Column<?> column;
    private final Constraint constraint;

    public ValidationError(Column<?> column, Constraint constraint) {
        super();
        this.column = column;
        this.constraint = constraint;
    }

    public Table.Column<?> getColumn() {
        return column;
    }

    public Constraint getConstraint() {
        return constraint;
    }

}
