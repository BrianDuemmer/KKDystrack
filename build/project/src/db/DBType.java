package db;

/**
 * Stores a valid datatype for the database
 * @author Duemmer
 *
 */
public enum DBType 
{
	INTEGER("INTEGER"), REAL("REAL"), TEXT("TEXT"), BLOB("BLOB");
	private final String value;

	private DBType(final String value) { this.value = value; }
	public String getValue() {return value; }
	
	@Override
	public String toString() { return getValue(); }
}
