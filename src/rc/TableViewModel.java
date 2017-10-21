package rc;

import javafx.scene.control.TableView;

/**
 * Represents an object that can be applied as a model to a tableview
 * @author Duemmer
 * 
 */
@FunctionalInterface public interface TableViewModel {
	public <T> TableView<T> applyModel(TableView<T> t);
}
