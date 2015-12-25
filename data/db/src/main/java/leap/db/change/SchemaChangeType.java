package leap.db.change;

public enum SchemaChangeType {
	
	/**
	 * Adds/Creates an object.
	 */
	ADD,
	
	/**
	 * Removes/Drops an object.
	 */
	REMOVE,

	/**
	 * Updates/Changes the definition
	 */
	UPDATE;
}