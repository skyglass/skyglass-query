package skyglass.data.common.model;

import java.io.Serializable;

//test
public interface IdObject extends Serializable {
	public String getUuid();

	public void setUuid(String uuid);
}
