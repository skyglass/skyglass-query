package skyglass.query.composer;

import java.util.Objects;

public class QueryPartString {

	public static final String WHERE = "WHERE ";

	public static final String AND = "AND ";

	public static final String OR = "OR ";

	private String firstDelimiter;

	private String delimiter;

	private String part;

	private boolean wherePart;

	public static QueryPartString getWhereOrQueryPart(String part) {
		return new QueryPartString(WHERE, OR, part, true);
	}

	public static QueryPartString getWhereAndQueryPart(String part) {
		return new QueryPartString(WHERE, AND, part, true);
	}

	public QueryPartString(String firstDelimiter, String delimiter, String part, boolean wherePart) {
		this.firstDelimiter = firstDelimiter;
		this.delimiter = delimiter;
		this.part = part;
		this.wherePart = wherePart;
	}

	public String getFirstDelimiter() {
		return firstDelimiter;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public String getPart() {
		return part;
	}

	public boolean isWherePart() {
		return wherePart;
	}

	@Override
	public int hashCode() {
		return 31 + Objects.hash(part);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof QueryPartString)) {
			return false;
		}
		QueryPartString other = (QueryPartString) obj;
		return part.equals(other.part);
	}

	@Override
	public String toString() {
		return part;
	}

}
