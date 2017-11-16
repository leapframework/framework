package leap.orm.mapping;

import leap.orm.enums.RemoteType;

public class RemoteSettings {

	private RemoteType remoteType;
	private String dataSource;
	private String relativePath;
	private String endpoint;
	public RemoteType getRemoteType() {
		return remoteType;
	}
	public void setRemoteType(RemoteType remoteType) {
		this.remoteType = remoteType;
	}
	public String getDataSource() {
		return dataSource;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}
