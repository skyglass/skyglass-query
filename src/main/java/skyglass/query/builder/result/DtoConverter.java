package skyglass.query.builder.result;

import java.util.function.Supplier;

import skyglass.query.builder.string.QueryStringBuilder;

class DtoConverter<DTO> extends QueryResultConverter<Object[], DTO, DTO> {

	DtoConverter(QueryStringBuilder queryStringBuilder, QueryResultProvider<Object[]> queryResultProvider, Supplier<DTO> dtoSupplier) {
		super(queryStringBuilder, queryResultProvider, dtoSupplier, null, null);
	}

}
