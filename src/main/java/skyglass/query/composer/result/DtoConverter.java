package skyglass.query.composer.result;

import java.util.function.Supplier;

import skyglass.query.composer.QueryComposer;

public class DtoConverter<DTO> extends QueryResultConverter<Object[], DTO, DTO> {

	public DtoConverter(QueryComposer queryComposer, QueryResultProvider<Object[]> queryResultProvider, Supplier<DTO> dtoSupplier) {
		super(queryComposer, queryResultProvider, dtoSupplier, null, null);
	}

}
