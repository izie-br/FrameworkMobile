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
		Constraint col2Constraints [] = {Constraint.unique()};

		Table t1 = new Table(name);
		t1.addColumn (String.class, col1Name);
		t1.addColumn (String.class, col2Name, col2Constraints);

		Table t2 = new Table(name);
		t2.addColumn (String.class, col1Name);
		t2.addColumn (String.class, col2Name, col2Constraints);
		assertEquals (t1, t2);

		Constraint table3col2Constraints [] =
			{Constraint.notNull()};

		Table t3 = new Table (name);
		t3.addColumn (String.class, col1Name);
		t3.addColumn (String.class, col2Name, table3col2Constraints);

		assertFalse (t1.equals (t3));

		Table t4 = new Table (name);
				// Note o tipo alterado para integer
		t4.addColumn (Integer.class, col1Name);
		t4.addColumn (String.class, col2Name, table3col2Constraints);

		assertFalse (t1.equals (t4));
	}

	@Test
	public void testCreateTableWithConstraint () {
		String name = "tablename";
		String col1Name = "columnone";
		String col2Name = "columntwo";
		String col3Name = "columnthree";
		Constraint col2Constraints [] = {Constraint.unique()};

		Table t1 = new Table (name);
		Table.Column<?> col1 = t1.addColumn (String.class, col1Name);
		Table.Column<?> col2 = t1.addColumn (String.class, col2Name, col2Constraints);
		t1.addColumn (String.class, col3Name, col2Constraints);

		Constraint table2Constraint =
				Constraint.unique(col1, col2);

		Table t2 = new Table (name);
		t2.addColumn (String.class, col1Name);
		t2.addColumn (String.class, col2Name, col2Constraints);
		t2.addColumn (String.class, col3Name, col2Constraints);
		t2.addConstraint (table2Constraint);

		assertFalse (t1.equals (t2));


		Table t3 = new Table (name);
		col1 = t3.addColumn (String.class, col1Name);
		t3.addColumn (String.class, col2Name, col2Constraints);
		Table.Column<?> col3 = t3.addColumn (String.class, col3Name, col2Constraints);
		t3.addConstraint (table2Constraint);

		assertEquals (t2, t3);

		// alterei o segundo de col2Name para col3Name
		Constraint table4Constraint =
				Constraint.unique(col1, col3);

		Table t4 = new Table (name);
		t4.addColumn (String.class, col1Name);
		t4.addColumn (String.class, col2Name, col2Constraints);
		t4.addColumn (String.class, col3Name, col2Constraints);
		t4.addConstraint (table4Constraint);

		assertFalse (t2.equals (t4));

	}
}
