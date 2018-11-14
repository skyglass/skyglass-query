package skyglass.query.builder.result;

import java.util.function.Function;
import java.util.function.Supplier;

import skyglass.query.builder.string.QueryStringBuilder;

class DtoDtoConverter<DTO1, DTO2> extends QueryResultConverter<Object[], DTO1, DTO2> {

	DtoDtoConverter(QueryStringBuilder queryStringBuilder, QueryResultProvider<Object[]> queryResultProvider, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter) {
		super(queryStringBuilder, queryResultProvider, dto1Supplier, null, dto1Dto2Converter);
	}

}
