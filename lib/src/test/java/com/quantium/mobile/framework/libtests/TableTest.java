package com.quantium.mobile.framework.libtests;

import org.junit.Test;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.validation.Constraint;

import static org.junit.Assert.*;

public class TableTest {

	@Test
	public void testCreateTable () {
		String name = "tablename";
		String col1Name = "columnone";
		String col2Name = "columntwo";
		Constraint col2Constraints [] = {new Constraint(Constraint.UNIQUE)};

		Table t1 = Table.create (name)
				.addColumn (String.class, col1Name)
				.addColumn (String.class, col2Name, col2Constraints)
				.get ();
		Table t2 = Table.create (name)
				.addColumn (String.class, col1Name)
				.addColumn (String.class, col2Name, col2Constraints)
				.get ();
		assertEquals (t1, t2);

		Constraint table3col2Constraints [] =
			{new Constraint(Constraint.NOT_NULL)};

		Table t3 = Table.create (name)
				.addColumn (String.class, col1Name)
				.addColumn (String.class, col2Name, table3col2Constraints)
				.get ();

		assertFalse (t1.equals (t3));

		Table t4 = Table.create (name)
				// Note o tipo alterado para integer
				.addColumn (Integer.class, col1Name)
				.addColumn (String.class, col2Name, table3col2Constraints)
				.get ();

		assertFalse (t1.equals (t4));
	}

	@Test
	public void testCreateTableWithConstraint () {
		String name = "tablename";
		String col1Name = "columnone";
		String col2Name = "columntwo";
		String col3Name = "columnthree";
		Constraint col2Constraints [] = {new Constraint(Constraint.UNIQUE)};

		Table t1 = Table.create (name)
				.addColumn (String.class, col1Name)
				.addColumn (String.class, col2Name, col2Constraints)
				.addColumn (String.class, col3Name, col2Constraints)
				.get ();

		Constraint table2Constraint =
				new Constraint(Constraint.UNIQUE, col1Name, col2Name);

		Table t2 = Table.create (name)
				.addColumn (String.class, col1Name)
				.addColumn (String.class, col2Name, col2Constraints)
				.addColumn (String.class, col3Name, col2Constraints)
				.addConstraint (table2Constraint)
				.get ();

		assertFalse (t1.equals (t2));


		Table t3 = Table.create (name)
				.addColumn (String.class, col1Name)
				.addColumn (String.class, col2Name, col2Constraints)
				.addColumn (String.class, col3Name, col2Constraints)
				.addConstraint (table2Constraint)
				.get ();

		assertEquals (t2, t3);

		// alterei o segundo de col2Name para col3Name
		Constraint table4Constraint =
				new Constraint(Constraint.UNIQUE, col1Name, col3Name);

		Table t4 = Table.create (name)
				.addColumn (String.class, col1Name)
				.addColumn (String.class, col2Name, col2Constraints)
				.addColumn (String.class, col3Name, col2Constraints)
				.addConstraint (table4Constraint)
				.get ();

		assertFalse (t2.equals (t4));

	}
}
