package com.quantium.mobile.geradores.velocity.helpers;

import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.javabean.Property;
import org.apache.commons.lang.NotImplementedException;

import java.util.Map;

public class ConstraintsHelper {

    private static final String INDENT = "\n                ";

    private Map<Property, OneToManyAssociationHelper> associationForField;

    public ConstraintsHelper(Map<Property, OneToManyAssociationHelper> associationForField) {
        this.associationForField = associationForField;
    }

    private static String getConstraintName(Constraint constraint) {
        if (constraint instanceof Constraint.PrimaryKey)
            return "primaryKey";
        if (constraint instanceof Constraint.NotNull)
            return "notNull";
        if (constraint instanceof Constraint.Unique)
            return "unique";
        if (constraint instanceof Constraint.Default)
            return "defaultValue";
        if (constraint instanceof Constraint.ForeignKey)
            return "foreignKey";
        if (constraint instanceof Constraint.Min)
            return "min";
        if (constraint instanceof Constraint.Max)
            return "max";
        if (constraint instanceof Constraint.Length)
            return "length";
        throw new RuntimeException();
    }

    private static Object[] getConstraintArgs(Constraint constraint) {
        if (constraint instanceof Constraint.PrimaryKey ||
                constraint instanceof Constraint.NotNull) {
            return new Object[0];
        }
        if (constraint instanceof Constraint.Default ||
                constraint instanceof Constraint.Min ||
                constraint instanceof Constraint.Max ||
                constraint instanceof Constraint.Length) {
            Constraint.ColumnValuePairConstraint<?> colVal =
                    (Constraint.ColumnValuePairConstraint<?>) constraint;
            return new Object[]{colVal.getValue()};
        }
        if (constraint instanceof Constraint.Unique) {
            Constraint.Unique unique = (Constraint.Unique) constraint;
            if (unique.getColumns().size() != 0) {
                throw new NotImplementedException();
            }
            return new Object[0];
        }
        throw new NotImplementedException();
    }

    public String get(Object obj) {
        Constraint constraints[] = new Constraint[0];
        boolean isFk = false;
        if (obj instanceof Property) {
            if (associationForField.get(obj) != null) {
                isFk = true;
            }
            constraints = ((Property) obj).getConstraints();
        }
        /*if (obj instanceof Constraint[]) {
			constraints = (Constraint[])obj;
		} else if (obj instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<Constraint> col = (Collection<Constraint>) obj;
			constraints = new Constraint[col.size ()];
			col.toArray (constraints);
		}*/

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < constraints.length; i++) {
            Constraint constraint = constraints[i];
            if (isFk && (
                    constraint instanceof Constraint.Min ||
                            constraint instanceof Constraint.Max ||
                            constraint instanceof Constraint.Length)) {
                continue;
            }
            sb.append(INDENT);
            sb.append("Constraint.");
            sb.append(getConstraintName(constraint));
            sb.append('(');

            Object args[] = getConstraintArgs(constraint);
            if (args != null && args.length > 0) {
                boolean first = true;
                for (Object arg : args) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(", ");
                    }
                    boolean isString = arg instanceof String;
                    if (isString)
                        sb.append('"');
                    sb.append(arg.toString());
                    if (isString)
                        sb.append('"');
                }
            }

            sb.append(")");
            if (i < (constraints.length - 1)) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

}
