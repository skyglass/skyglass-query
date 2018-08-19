package skyglass.query.model.criteria;

import java.util.List;

import skyglass.data.filter.OrderField;
import skyglass.data.filter.PrivateCompositeFilterItem;

public interface IQueryProcessor {
	
	public String generateCountQueryString();
	
	public String generateQueryString();
	
	public void applyOrder(List<OrderField> orderFields);
	
	public void applyFilter(PrivateCompositeFilterItem filterItem);

}
