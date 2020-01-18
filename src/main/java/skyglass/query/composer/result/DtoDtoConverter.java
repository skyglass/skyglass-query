package skyglass.query.composer.result;

import java.util.function.Function;
import java.util.function.Supplier;

import skyglass.query.composer.QueryComposer;

public class DtoDtoConverter<DTO1, DTO2> extends QueryResultConverter<Object[], DTO1, DTO2> {

	public DtoDtoConverter(QueryComposer queryComposer, QueryResultProvider<Object[]> queryResultProvider, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter) {
		super(queryComposer, queryResultProvider, dto1Supplier, null, dto1Dto2Converter);
	}

}
