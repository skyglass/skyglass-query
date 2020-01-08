package skyglass.query.builder.result;

import java.util.Date;

public class MockDTO {
	
	private String uuid;

	private String test;

	private Date testDate;

	private MockEnum testEnum;

	public MockDTO() {

	}

	public MockDTO(String test) {
		this.test = test;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}

	public Date getTestDate() {
		return testDate;
	}

	public void setTestDate(Date testDate) {
		this.testDate = testDate;
	}

	public MockEnum getTestEnum() {
		return testEnum;
	}

	public void setTestEnum(MockEnum testEnum) {
		this.testEnum = testEnum;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
