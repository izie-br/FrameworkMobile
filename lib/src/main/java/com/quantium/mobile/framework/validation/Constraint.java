package com.quantium.mobile.framework.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.framework.query.Table.Column;

public abstract class Constraint {

	private static PrimaryKey PRIMARY_KEY = new PrimaryKey();
	private static NotNull NOT_NULL = new NotNull();
	private static Unique UNIQUE = unique();

//	// apenas para facilitar
//	public static final Type PRIMARY_KEY = Type.PRIMARY_KEY;
//	public static final Type UNIQUE      = Type.UNIQUE;
//	public static final Type FOREIGN_KEY = Type.FOREIGN_KEY;
//	public static final Type NOT_NULL    = Type.NOT_NULL;
//	public static final Type DEFAULT     = Type.DEFAULT;
//	public static final Type MIN         = Type.MIN;
//	public static final Type MAX         = Type.MAX;
//	public static final Type REGEX       = Type.REGEX;
//
//	public static enum Type {
//		PRIMARY_KEY, UNIQUE, FOREIGN_KEY, NOT_NULL, DEFAULT, MIN, MAX, REGEX
//	}

	public static abstract class SimpleConstraint extends Constraint {

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != this.getClass())
				return false;
			return true;
		}
	}

	public static class PrimaryKey extends SimpleConstraint {
		private PrimaryKey(){}
	}

	public static PrimaryKey primaryKey () {
		return PRIMARY_KEY;
	}

	public static class NotNull extends SimpleConstraint {
		private NotNull(){}
	}

	public static NotNull notNull () {
		return NOT_NULL;
	}

	public abstract static class ColumnValuePairConstraint<T> extends SimpleConstraint {

		private T value;

		public ColumnValuePairConstraint(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			int valhash = this.value == null ? 0 :this.value.hashCode();
			return this.getClass().getSimpleName().hashCode() * valhash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != this.getClass())
				return false;
			ColumnValuePairConstraint<?> other = (ColumnValuePairConstraint<?>)obj;
			boolean valueEqualsOtherValue =
					(this.value == null)?
							(other.value == null):
							this.value.equals(obj);
			return valueEqualsOtherValue;
		}
	}

	public static class Default<T> extends ColumnValuePairConstraint<T> {
		private Default(T value) {
			super(value);
		}
	}

	public static <T> Default<T> defaultValue(T value) {
		return new Default<T>(value);
	}

	public static class Min<T> extends ColumnValuePairConstraint<T> {
		private Min(T value) {
			super(value);
		}
	}

	public static <T> Min<T> min (T value) {
		return new Min<T>(value);
	}

	public static class Max<T> extends ColumnValuePairConstraint<T> {
		private Max(T value) {
			super(value);
		}
	}

	public static <T> Max<T> max (T value) {
		return new Max<T>(value);
	}

	public static class Length<T> extends ColumnValuePairConstraint<T> {
		private Length(T value) {
			super(value);
		}
	}

	public static <T> Length<T> length (T value) {
		return new Length<T>(value);
	}

	public static class Regex extends ColumnValuePairConstraint<String> {
		private Regex(String value) {
			super(value);
		}
	}

	public static Regex regex(String pattern) {
		return new Regex(pattern);
	}

	public static class Unique extends Constraint {

		private Set<Table.Column<?>> columns;

		private Unique(Collection<Table.Column<?>> columns) {
			HashSet<Column<?>> set = new HashSet<Table.Column<?>>(columns);
			this.columns = Collections.unmodifiableSet(set);
		}

		public Collection<Table.Column<?>> getColumns() {
			return columns;
		}

		@Override
		public int hashCode() {
			return this.columns.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != this.getClass())
				return false;
			Unique other = (Unique) obj;
			return this.columns.equals(other.columns);
		}
	}

	public static Unique unique(Collection<Table.Column<?>> columns) {
		if (columns.size() == 0)
			return UNIQUE;
		return new Unique(columns);
	}

	public static Unique unique(Table.Column<?>...columns) {
		return new Unique(Arrays.asList(columns));
	}

	public static class ForeignKey extends Constraint {

		private Table.Column<?> reference;

		private ForeignKey(Column<?> reference) {
			this.reference = reference;
		}

		public Table.Column<?> getReference() {
			return reference;
		}

		@Override
		public int hashCode() {
			return this.reference.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != this.getClass())
				return false;
			ForeignKey other = (ForeignKey) obj;
			return this.reference.equals(other.reference);
		}

	}

	public static ForeignKey foreignKey(Table.Column<?> reference) {
		return new ForeignKey(reference);
	}

}
