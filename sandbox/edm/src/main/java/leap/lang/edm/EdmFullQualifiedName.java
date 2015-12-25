/**
 * file created at 2013-6-5
 */
package leap.lang.edm;

import leap.lang.Strings;

public class EdmFullQualifiedName {
	
	private final String namespace;
	private final String name;
	private final String fqName;
	
	public EdmFullQualifiedName(String fqName){
		this.fqName = fqName;
		
		int lastDotIndex = fqName.lastIndexOf(".");
		if(lastDotIndex > 0){
			this.namespace = fqName.substring(0,lastDotIndex);
			this.name      = fqName.substring(lastDotIndex+1);
		}else{
			this.namespace = "";
			this.name      = fqName;
		}
	}

	public EdmFullQualifiedName(String namespace,String name){
		this.namespace = namespace;
		this.name      = name;
		this.fqName    = Strings.isEmpty(namespace) ? name : namespace + "." + name;
	}
	
	public String getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}
	
	public String getFqName(){
		return fqName;
	}
}
