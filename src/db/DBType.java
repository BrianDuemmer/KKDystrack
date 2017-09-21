package db;

/**
 * Stores a valid datatype for the database
 * @author Duemmer
 *
 */
public enum DBType 
{
	INTEGER("INTEGER"), 
	REAL("REAL"), 
	TEXT("TEXT"), 
	BLOB("BLOB"),
	DATETIME("DATETIME"),
	VARCHAR("VARCHAR");
	private final String value;

	private DBType(final String value) { this.value = value; }
	public String getValue() {return value; }
	
	
	/**
	 * returns true if the type should be associated with a length / size attribute
	 */
	public static boolean hasLenAttrib(DBType t)
	{
		boolean hla = t == DBType.VARCHAR;
		return hla;
	}
	
	@Override
	public String toString() { return getValue(); }
}
