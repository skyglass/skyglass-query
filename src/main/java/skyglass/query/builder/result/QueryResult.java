package skyglass.query.builder.result;

import java.io.Serializable;
import java.util.List;

public class QueryResult<T> implements Serializable {

	private static final long serialVersionUID = -2045986229298564328L;

	public QueryResult() {

	}

	protected QueryResult(QueryResult<T> original) {
		setResult(original.getResult());
		setTotalCount(original.getTotalCount());
	}

	protected List<T> result;

	protected int totalCount = -1;

	public List<T> getResult() {
		return result;
	}

	public void setResult(List<T> result) {
		this.result = result;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

}
