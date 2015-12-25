/**
 * file created at 2013-6-9
 */
package leap.lang.edm;

public class EdmUnresolvedTypeRef extends EdmTypeRef {

	public EdmUnresolvedTypeRef(String name, String fullQualifiedName) {
	    super(name, fullQualifiedName);
    }

	@Override
    public EdmTypeKind getRefTypeKind() {
	    return EdmTypeKind.Unresolved;
    }
}
