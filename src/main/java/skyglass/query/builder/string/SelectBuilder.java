package skyglass.query.builder.string;

import java.util.ArrayList;
import java.util.List;

public class SelectBuilder {

	private List<SelectField> selectFields = new ArrayList<SelectField>();

	private QueryStringBuilder parent;

	public SelectBuilder(QueryStringBuilder parent) {
		this.parent = parent;
	}

	public SelectBuilder addSelect(String alias, String expression) {
		SelectField selectField = new SelectField(alias, expression);
		this.selectFields.add(selectField);
		return this;
	}

	public QueryStringBuilder buildSelect() {
		return parent.buildSelect(selectFields);
	}

}
