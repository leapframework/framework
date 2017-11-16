package leap.orm.mapping;

import leap.orm.enums.RemoteType;

public class RemoteSettings {

	private RemoteType remoteType;
	private String dataSource;
	private String pathPrefix;
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
	public String getPathPrefix() {
		return pathPrefix;
	}
	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

}
