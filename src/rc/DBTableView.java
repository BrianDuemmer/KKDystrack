package rc;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Represents a {@link TableView} that is tied to a table in a database
 * @author Duemmer
 *
 * @param <T>
 */
public class DBTableView<T extends TableViewModel> extends ArrayList<T> 
{
	private static final long serialVersionUID = 8725759009938266298L;

	public DBTableView() {
	}

	public DBTableView(int initialCapacity) {
		super(initialCapacity);

	}

	public DBTableView(Collection<? extends T> c) {
		super(c);

	}

}
