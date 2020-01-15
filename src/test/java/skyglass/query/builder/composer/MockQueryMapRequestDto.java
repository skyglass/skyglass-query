package skyglass.query.builder.composer;

import java.util.List;

import skyglass.query.builder.QueryRequestDTO;

@SuppressWarnings("serial")
public class MockQueryMapRequestDto extends QueryRequestDTO {
	
	public static MockQueryMapRequestDto createWithPaging(String test) {
		MockQueryMapRequestDto result = new MockQueryMapRequestDto(test);
		return result;
	}

	public static MockQueryMapRequestDto create(String test) {
		return new MockQueryMapRequestDto(test);
	}

	public static MockQueryMapRequestDto create(String test1, String test2) {
		return new MockQueryMapRequestDto(test1, test2);
	}

	public static MockQueryMapRequestDto createWithList(String test, List<String> testList) {
		return new MockQueryMapRequestDto(test, testList);
	}

	public static MockQueryMapRequestDto create(List<String> testList) {
		return new MockQueryMapRequestDto(null, testList);
	}

	private MockQueryMapRequestDto(String test) {
		set("test", test);
	}

	private MockQueryMapRequestDto(String test1, String test2) {
		set("test1", test1);
		set("test2", test2);
	}

	private MockQueryMapRequestDto(String test, List<String> testList) {
		set("test", test);
		set("testList", testList);
	}

	public String getTest() {
		return get("test");
	}

	public void setTest(String test) {
		set("test", test);
	}

	@SuppressWarnings("unchecked")
	public List<String> getTestList() {
		return (List<String>)get("testList");
	}

	public void setTestList(List<String> testList) {
		set("testList", testList);
	}

	public String getTest1() {
		return get("test1");
	}

	public void setTest1(String test1) {
		set("test1", test1);
	}

	public String getTest2() {
		return get("test2");
	}

	public void setTest2(String test2) {
		set("test2", test2);
	}

}
