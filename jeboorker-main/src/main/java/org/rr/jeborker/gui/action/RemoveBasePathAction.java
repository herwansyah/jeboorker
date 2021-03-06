package org.rr.jeborker.gui.action;

import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuBarController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class RemoveBasePathAction extends AbstractAction {

	private static final long serialVersionUID = 6607018752541779846L;
	
	private static final String REMOVE_ALL = "removeAll";
	
	private String path;

	RemoveBasePathAction(String text) {
		this.path = text;
		if(REMOVE_ALL.equals(text)) {
			putValue(Action.NAME, Bundle.getString("RemoveBasePathAction.removeAll.name"));
		} else if(text == null) {
			putValue(Action.NAME, Bundle.getString("ReadEbookFolderAction.name"));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("remove_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("remove_22.png"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		if(REMOVE_ALL.equals(path)) {
			final List<String> basePaths = preferenceStore.getBasePath();
			for (String basePath : basePaths) {
				removeBasePathEntries(basePath, true);
				preferenceStore.removeBasePath(basePath);
				MainMenuBarController.getController().removeBasePathMenuEntry(basePath);
			}
		} else {
			final String name = (String) getValue(Action.NAME);
			removeBasePathEntries(name, true);
			preferenceStore.removeBasePath(name);
			MainMenuBarController.getController().removeBasePathMenuEntry(name);
		}
	}

	static void removeBasePathEntries(final String path, boolean refreshTable) {
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		
		try {
			progressMonitor.monitorProgressStart(Bundle.getString("RemoveBasePathAction.message"));
			try {
				ArrayList<EbookPropertyItem> itemsToRemove = getItemsByBasePath(path);
				removeAllEbookPropertyItems(itemsToRemove);
			} catch(Exception ex) {
				LoggerFactory.logWarning(RemoveBasePathAction.class, "Error while removing ebooks from catalog", ex);
			}
		} finally {
			progressMonitor.monitorProgressStop();
			if(refreshTable) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						MainController.getController().getEbookTableHandler().refreshTable();
						MainController.getController().getMainTreeHandler().refreshBasePathTree();
					}
				});
			}
		}
	}
	
	/**
	 * Get all items from the database with a fitting base path.
	 * @param basePath The base path string
	 * @return A list over all items with a fitting base path. Never returns null.
	 */
	static ArrayList<EbookPropertyItem> getItemsByBasePath(final String basePath) {
		final String normalizedPath = StringUtil.replace(basePath, File.separator, EMPTY);
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final ArrayList<EbookPropertyItem> toRemove = new ArrayList<>();
		final Iterable<EbookPropertyItem> items = defaultDBManager.getItems(EbookPropertyItem.class);
		for (EbookPropertyItem item : items) {
			if(StringUtil.replace(item.getBasePath(), File.separator, EMPTY).equals(normalizedPath)) {
				toRemove.add(item);
			}
		}
		return toRemove;
	}
	
	/**
	 * Deletes the given items from the database and the view.
	 * @param items The items to be deleted.
	 */
	static void removeAllEbookPropertyItems(final List<EbookPropertyItem> items) {
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		final MainController controller = MainController.getController();
		
		progressMonitor.setMessage(Bundle.getString("RemoveBasePathAction.deletingMany"));
		
		for(EbookPropertyItem item : items) {
			DefaultDBManager.getInstance().deleteObject(item);
		}
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				controller.getEbookTableHandler().refreshTable();
			}
		});
	}
	
}
