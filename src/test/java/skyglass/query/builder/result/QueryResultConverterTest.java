package skyglass.query.builder.result;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.DateUtil;
import skyglass.query.builder.string.QueryComposer;

public class QueryResultConverterTest {

	@Test
	public void testSimpleConverter() {
		List<MockEntity> list = TestQueryManager.getEntityTestList(QueryComposer.nativ("sm").select("*").skipUuid().from("SpaceMission sm"), getEntityListSupplier(), MockEntity.class);
		Assert.assertEquals("test1", list.get(0).getTest());
		Assert.assertEquals("test2", list.get(1).getTest());
		Assert.assertEquals("test3", list.get(2).getTest());
	}

	@Test
	public void testSimpleNativeConverter() { 
		List<MockDTO> list = TestQueryManager.getNativeTestList(QueryComposer.nativ("sm").select("test").skipUuid().from("SpaceMission sm"), getDTOSupplier(), getNativeListSupplier());
		Assert.assertEquals("test1", list.get(0).getTest());
		Assert.assertEquals("test2", list.get(1).getTest());
		Assert.assertEquals("test3", list.get(2).getTest());
	}

	@Test
	public void testDateNativeConverter() {
		List<MockDTO> list = TestQueryManager.getNativeTestList(QueryComposer.nativ("sm").select("testDate").skipUuid().from("SpaceMission sm"), getDTOSupplier(), getNativeDateListSupplier());
		Assert.assertEquals("2018-01-01", DateUtil.format(list.get(0).getTestDate()));
		Assert.assertEquals("2018-03-01", DateUtil.format(list.get(1).getTestDate()));
		Assert.assertEquals("2018-05-01", DateUtil.format(list.get(2).getTestDate()));
	}

	@Test
	public void testEnumNativeConverter() {
		List<MockDTO> list = TestQueryManager.getNativeTestList(QueryComposer.nativ("sm").select("testEnum").skipUuid().from("SpaceMission sm"), getDTOSupplier(), getNativeEnumListSupplier());
		Assert.assertEquals(MockEnum.Test1, list.get(0).getTestEnum());
		Assert.assertEquals(MockEnum.Test2, list.get(1).getTestEnum());
		Assert.assertEquals(MockEnum.Test3, list.get(2).getTestEnum());
	}

	@Test
	public void testEntityDtoConverter() {
		List<MockDTO> list = TestQueryManager.getEntityDtoTestList(QueryComposer.jpa("sm").select("*").skipUuid().from("SpaceMission sm"), getEntityDtoConverter(), getEntityListSupplier(),
				MockEntity.class);
		Assert.assertEquals("test1", list.get(0).getTest());
		Assert.assertEquals("test2", list.get(1).getTest());
		Assert.assertEquals("test3", list.get(2).getTest());
	}

	@Test
	public void testDtoDtoConverter() {
		List<MockDTO2> list = TestQueryManager.getDtoDtoTestList(QueryComposer.nativ("sm").select("test").skipUuid().from("SpaceMission sm"), getDTOSupplier(), getDto1Dto2Converter(),
				getNativeListSupplier());
		Assert.assertEquals("test1", list.get(0).getTest());
		Assert.assertEquals("test2", list.get(1).getTest());
		Assert.assertEquals("test3", list.get(2).getTest());
	}

	private Function<MockEntity, MockDTO> getEntityDtoConverter() {
		return entity -> {
			MockDTO mockDto = new MockDTO();
			mockDto.setTest(entity.getTest());
			return mockDto;
		};
	}

	private Function<MockDTO, MockDTO2> getDto1Dto2Converter() {
		return dto1 -> {
			MockDTO2 mockDto2 = new MockDTO2();
			mockDto2.setTest(dto1.getTest());
			return mockDto2;
		};
	}

	private Supplier<List<MockEntity>> getEntityListSupplier() {
		return () -> Stream.of(new MockEntity("test1"), new MockEntity("test2"), new MockEntity("test3")).collect(Collectors.toList());
	}

	private Supplier<List<Object[]>> getNativeListSupplier() {
		return () -> Stream.of(new Object[] { "test1" }, new Object[] { "test2" }, new Object[] { "test3" }).collect(Collectors.toList());
	}

	private Supplier<List<Object[]>> getNativeDateListSupplier() {
		return () -> Stream.of(new Object[] { DateUtil.parse("2018-01-01") }, new Object[] { DateUtil.parse("2018-03-01") }, new Object[] { DateUtil.parse("2018-05-01") })
				.collect(Collectors.toList());
	}

	private Supplier<List<Object[]>> getNativeEnumListSupplier() {
		return () -> Stream.of(new Object[] { MockEnum.Test1 }, new Object[] { MockEnum.Test2 }, new Object[] {MockEnum.Test3 })
				.collect(Collectors.toList());
	}

	private Supplier<MockDTO> getDTOSupplier() {
		return () -> new MockDTO();
	}

}
