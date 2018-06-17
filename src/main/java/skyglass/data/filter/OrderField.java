package skyglass.data.filter;

public class OrderField {
	
	private FieldResolver fieldResolver;
	
	private OrderType orderType;
	
	public OrderField(FieldResolver fieldResolver, OrderType orderType) {
		this.fieldResolver = fieldResolver;
		this.orderType = orderType;
	}
	
	public FieldResolver getOrderField() {
   		return fieldResolver;			
	}
	
	public OrderType getOrderType() {
		return orderType;
	}
	
	public boolean isDescending() {
		return orderType == OrderType.DESC;
	}
	
	public boolean isMultiple() {
		return fieldResolver.isMultiple();
	}
	
	public boolean isSingle() {
		return fieldResolver.isSingle();
	}		

}	
