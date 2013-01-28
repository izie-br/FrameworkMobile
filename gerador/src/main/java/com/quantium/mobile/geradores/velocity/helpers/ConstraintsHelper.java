package com.quantium.mobile.geradores.velocity.helpers;

import java.util.Collection;

import com.quantium.mobile.framework.validation.Constraint;

public class ConstraintsHelper {

	public String get(Object obj) {
		Constraint constraints [] = new Constraint[0];
		if (obj instanceof Constraint[]) {
			constraints = (Constraint[])obj;
		} else if (obj instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<Constraint> col = (Collection<Constraint>) obj;
			constraints = new Constraint[col.size ()];
			col.toArray (constraints);
		}

		final String indent = "\n                ";
		StringBuilder sb = new StringBuilder ();
		sb.append ("new Constraint[] {");
		for (Constraint constraint : constraints) {
			sb.append (indent);
			sb.append ("new Constraint(Constraint.");
			sb.append (constraint.getType ());
			Object args [] = constraint.getArgs ();
			if (args != null && args.length> 0){
				for (Object arg : args) {
					sb.append (", ");
					boolean isString = arg instanceof String;
					if (isString)
						sb.append ('"');
					sb.append (arg.toString ());
					if (isString)
						sb.append ('"');
				}
			}
			sb.append ("),");
		}
		sb.append ("}");
		return sb.toString ();
	}

}
