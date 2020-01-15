package skyglass.query.builder.composer;

import java.util.List;

import skyglass.query.builder.QueryRequestDTO;

@SuppressWarnings("serial")
public class MockQueryRequestDto extends QueryRequestDTO {

	private String test;

	private List<String> testList;

	private String test1;

	private String test2;

	public static MockQueryRequestDto createWithPaging(String test) {
		MockQueryRequestDto result = new MockQueryRequestDto(test);
		return result;
	}

	public static MockQueryRequestDto create(String test) {
		return new MockQueryRequestDto(test);
	}

	public static MockQueryRequestDto create(String test1, String test2) {
		return new MockQueryRequestDto(test1, test2);
	}

	public static MockQueryRequestDto createWithList(String test, List<String> testList) {
		return new MockQueryRequestDto(test, testList);
	}

	public static MockQueryRequestDto create(List<String> testList) {
		return new MockQueryRequestDto(null, testList);
	}

	private MockQueryRequestDto(String test) {
		this.test = test;
	}

	private MockQueryRequestDto(String test1, String test2) {
		this.test1 = test1;
		this.test2 = test2;
	}

	private MockQueryRequestDto(String test, List<String> testList) {
		this.test = test;
		this.testList = testList;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}

	public List<String> getTestList() {
		return testList;
	}

	public void setTestList(List<String> testList) {
		this.testList = testList;
	}

	public String getTest1() {
		return test1;
	}

	public void setTest1(String test1) {
		this.test1 = test1;
	}

	public String getTest2() {
		return test2;
	}

	public void setTest2(String test2) {
		this.test2 = test2;
	}

}
