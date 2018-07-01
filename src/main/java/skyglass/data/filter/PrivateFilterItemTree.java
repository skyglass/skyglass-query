package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.model.query.InternalUtil;
import skyglass.query.model.query.QueryFilter;


public class PrivateFilterItemTree extends PrivateFilterItem {
	
	private List<PrivateFilterItem> children = new ArrayList<PrivateFilterItem>();
	
	public PrivateFilterItemTree(FilterType filterType) {
		this(null, filterType);	
	}
	
	public PrivateFilterItemTree(FieldResolver fieldResolver, FilterType filterType) {
		super(null, fieldResolver, null, filterType);	
	}
	
	public void addChild(PrivateFilterItem filterItem) {
		this.children.add(filterItem);
	}

	public List<PrivateFilterItem> getChildren() {
		return children;
	}
	
    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        switch (filterType) {
        case And:
        case Or:
            String op = filterType == FilterType.And ? " and " : " or ";

            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            for (PrivateFilterItem filterItem: children) {
                if (first) {
                    first = false;
                } else {
                    sb.append(op);
                }
                sb.append(filterItem.toString());
            }
            if (first)
                return (filterType == FilterType.And ? "AND: " : "OR: ") + "**EMPTY LIST**";

            sb.append(")");
            return sb.toString();
        case Not:
            return "not " + value.toString();
        case QueryFilter.OP_SOME:
            if (!(value instanceof QueryFilter)) {
                return "SOME: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
            }
            return "some `" + property + "` {" + value.toString() + "}";
        case QueryFilter.OP_ALL:
            if (!(value instanceof QueryFilter)) {
                return "ALL: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
            }
            return "all `" + property + "` {" + value.toString() + "}";
        case QueryFilter.OP_NONE:
            if (!(value instanceof QueryFilter)) {
                return "NONE: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
            }
            return "none `" + property + "` {" + value.toString() + "}";
        default:
            return "**INVALID OPERATOR: (" + operator + ") - VALUE: " + InternalUtil.paramDisplayString(value) + " **";
        }
    }
}
