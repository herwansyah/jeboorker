package org.rr.commons.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.net.imagefetcher.IImageFetcher;
import org.rr.commons.net.imagefetcher.IImageFetcherEntry;
import org.rr.commons.net.imagefetcher.ImageFetcherFactory;
import org.rr.pm.image.IImageProvider;
import org.rr.pm.image.ImageProviderFactory;
import org.rr.pm.image.ImageUtils;

public class ImageDownloadDialog extends JDialog {
	
	private IResourceHandler selectedImage;
	
	private JTextField searchTextField;
	
	private JComboBox searchProviderComboBox;
	
	private JScrollPane scrollPane;
	
	private static Dimension cellSize = new Dimension(150, 250);
	
	/**
	 * Number of images to be loaded into the dialog.
	 */
	private int resultCount = 20;

	private Color selectedFgColor;

	private Color selectedBgColor;

	private Color bgColor;

	private Color fgColor;
	
	public ImageDownloadDialog(JFrame owner) {
		super(owner);
		init(owner);
	}
	
	public ImageDownloadDialog() {
		super();
		init(null);
	}

	protected void init(JFrame owner) {
		this.setSize(800, 400);
		if(owner != null) {
			this.setLocation(owner.getBounds().x + owner.getBounds().width/2 - this.getSize().width/2, owner.getBounds().y + 50);
		}
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		
		//workaround for a swing bug. The first time, the editor is used, the 
		//ui color instance draws the wrong color but have the right rgb values.
		Color color;
		color = SwingUtils.getSelectionForegroundColor();
		selectedFgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());		
		
		color = SwingUtils.getSelectionBackgroundColor();
		selectedBgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());		
		
		color = SwingUtils.getBackgroundColor();
		bgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		color = SwingUtils.getForegroundColor();
		fgColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(new TitledBorder(null, "Image search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_borderPanel = new GridBagConstraints();
		gbc_borderPanel.fill = GridBagConstraints.BOTH;
		gbc_borderPanel.gridx = 0;
		gbc_borderPanel.gridy = 0;
		getContentPane().add(borderPanel, gbc_borderPanel);
		GridBagLayout gbl_borderPanel = new GridBagLayout();
		gbl_borderPanel.columnWidths = new int[]{0, 340, 0, 0};
		gbl_borderPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_borderPanel.columnWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_borderPanel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		borderPanel.setLayout(gbl_borderPanel);
		
		searchProviderComboBox = new JComboBox();
		GridBagConstraints gbc_searchProviderComboBox = new GridBagConstraints();
		gbc_searchProviderComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_searchProviderComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchProviderComboBox.gridx = 0;
		gbc_searchProviderComboBox.gridy = 0;
		borderPanel.add(searchProviderComboBox, gbc_searchProviderComboBox);
		searchProviderComboBox.setModel(new DefaultComboBoxModel(ImageFetcherFactory.getFetcherNames().toArray()));
		
		searchTextField = new JTextField();
		GridBagConstraints gbc_searchTextField = new GridBagConstraints();
		gbc_searchTextField.fill = GridBagConstraints.BOTH;
		gbc_searchTextField.insets = new Insets(0, 0, 5, 5);
		gbc_searchTextField.gridx = 1;
		gbc_searchTextField.gridy = 0;
		borderPanel.add(searchTextField, gbc_searchTextField);
		searchTextField.setColumns(10);
		
		JButton searchButton = new JButton(new SearchAction());
		searchButton.setMargin(new Insets(0, 8, 0, 8));
		GridBagConstraints gbc_searchButton = new GridBagConstraints();
		gbc_searchButton.fill = GridBagConstraints.VERTICAL;
		gbc_searchButton.insets = new Insets(0, 0, 5, 0);
		gbc_searchButton.gridx = 2;
		gbc_searchButton.gridy = 0;
		borderPanel.add(searchButton, gbc_searchButton);
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		borderPanel.add(scrollPane, gbc_scrollPane);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		borderPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{287, 69, 0, 0};
		gbl_panel.rowHeights = new int[]{25, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton abortButton = new JButton(Bundle.getString("ImageDownloadDialog.Action.Cancel"));
		abortButton.setMargin(new Insets(2, 8, 2, 8));
		GridBagConstraints gbc_abortButton = new GridBagConstraints();
		gbc_abortButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_abortButton.insets = new Insets(0, 0, 0, 5);
		gbc_abortButton.gridx = 1;
		gbc_abortButton.gridy = 0;
		panel.add(abortButton, gbc_abortButton);
		abortButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedImage = null;
				setVisible(false);
				dispose();				
			}
		});
		
		JButton okButton = new JButton(Bundle.getString("ImageDownloadDialog.Action.OK"));
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.anchor = GridBagConstraints.EAST;
		gbc_okButton.gridx = 2;
		gbc_okButton.gridy = 0;
		panel.add(okButton, gbc_okButton);
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SearchResultPanel view = (SearchResultPanel) scrollPane.getViewport().getView();
				if(view != null) {
					int selectedColumn = view.getSelectedColumn();
					IImageFetcherEntry imageFetcher = (IImageFetcherEntry) view.getModel().getValueAt(0, selectedColumn);
					if(imageFetcher != null) {
						try {
							URL imageURL = imageFetcher.getImageURL();
							selectedImage = ResourceHandlerFactory.getResourceLoader(imageURL);
							setVisible(false);
							dispose();
						} catch (Exception e1) {
							LoggerFactory.getLogger(this).log(Level.WARNING, "Could not fetch image from " + imageFetcher.getImageURL());
						}
					}
				}
			}
		});
	}
	
	private class SearchAction extends AbstractAction {

		private SearchAction() {
			final URL resource = ImageDownloadDialog.class.getResource("resources/play_16.gif");
			putValue(Action.SMALL_ICON, new ImageIcon(resource));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			scrollPane.setViewportView(new SearchResultPanel(searchTextField.getText(), searchProviderComboBox.getSelectedItem().toString()));
		}
		
	}
	
	private class SearchResultPanel extends JTable {
		
		private HashMap<Integer, ImageIcon> imageCache = new HashMap<Integer, ImageIcon>(30);
		
		private TableCellRenderer renderer = new SearchResultTableRenderer();
		
		private SearchResultPanel(String searchPhrase, String searchProviderName) {
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.setRowHeight(cellSize.height);
			this.setTableHeader(null);
			this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.setShowGrid(false);
		    this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		    
			this.setColumnModel(new DefaultTableColumnModel(){

				@Override
				public TableColumn getColumn(int columnIndex) {
					TableColumn column = super.getColumn(columnIndex);
//					column.setWidth(cellSize.width);
					return column;
				}
				
			});
			
			IImageFetcher imageFetcher = ImageFetcherFactory.getImageFetcher(searchProviderName);
			imageFetcher.setSearchTerm(searchPhrase);
			
			this.setModel(new SearchResultTableModel(imageFetcher, getResultCount()));
			this.setDefaultRenderer(Object.class, renderer);
			
			int columnCount = this.getModel().getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				TableColumn column = this.getColumnModel().getColumn(i);
				column.setPreferredWidth(cellSize.width);
			}
		}
		
		private class SearchResultTableRenderer extends JPanel implements TableCellRenderer {
			
			private JLabel sizeLabel;
			
			private JLabel imageLabel;
			
			private SearchResultTableRenderer() {
				//init
				this.setLayout(new BorderLayout());
				this.setBorder(new EmptyBorder(0, 3, 0, 3));
				
				sizeLabel = new JLabel();
				sizeLabel.setOpaque(false);
				sizeLabel.setHorizontalAlignment(SwingConstants.CENTER);
				this.add(sizeLabel, BorderLayout.SOUTH);
				
				imageLabel = new JLabel();
				imageLabel.setOpaque(false);
				imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
				this.add(imageLabel, BorderLayout.CENTER);
			}

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				if(isSelected && hasFocus) {
					setBackground(selectedBgColor);
					sizeLabel.setForeground(selectedFgColor);
					imageLabel.setForeground(selectedFgColor);					
				} else {
					setBackground(bgColor);
					sizeLabel.setForeground(fgColor);
					imageLabel.setForeground(fgColor);
				}
				
				int imageHeight = ((IImageFetcherEntry)value).getImageHeight();
				int imageWidth = ((IImageFetcherEntry)value).getImageWidth();
				
				sizeLabel.setText(imageWidth + "x" + imageHeight);
				try {
					ImageIcon imageIcon = imageCache.get(Integer.valueOf(column));
					if(imageIcon == null) {
						byte[] thumbnailImageBytes = ((IImageFetcherEntry)value).getThumbnailImageBytes();
						IImageProvider imageProvider = ImageProviderFactory.getImageProvider(ResourceHandlerFactory.getResourceLoader(new ByteArrayInputStream(thumbnailImageBytes)));
						BufferedImage image = imageProvider.getImage();
						BufferedImage scaleToMatch = ImageUtils.scaleToMatch(image, cellSize, true);
						imageIcon = new ImageIcon(scaleToMatch);
						imageCache.put(Integer.valueOf(column), imageIcon);
					}
					imageLabel.setIcon(imageIcon);
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Images could not be retrieved.", e);
				}
				return this;
			}
			
		}
		
		/**
		 * Model loading and providing the images from the selected search provider.
		 */
		private class SearchResultTableModel extends AbstractTableModel {
			
			final List<IImageFetcherEntry> entries = new ArrayList<IImageFetcherEntry>();
			
			private SearchResultTableModel(IImageFetcher imageFetcher, int resultCount) {
				try { //init
					for(int i=1; entries.size() <= resultCount; i++) {
						List<IImageFetcherEntry> imageFetcherEntries = imageFetcher.getNextEntries();
						if(imageFetcherEntries.isEmpty()) {
							break;
						} else {
							entries.addAll(imageFetcherEntries);
						}
					}
				} catch (IOException e) {
					LoggerFactory.getLogger(this).log(Level.WARNING, "Images could not be retrieved.", e);
				}
			}

			@Override
			public int getRowCount() {
				return 1;
			}

			@Override
			public int getColumnCount() {
				return entries.size();
			}


			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return entries.get(columnIndex);
			}
		}
	}

	public String getSearchPhrase() {
		return this.searchTextField.getText();
	}

	public void setSearchPhrase(String searchPhrase) {
		this.searchTextField.setText(searchPhrase);
	}
	
	public IResourceHandler getSelectedImage() {
		return selectedImage;
	}

	public int getResultCount() {
		return resultCount;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}

	public static void main(String[] args) {
		ImageDownloadDialog imageDownloadDialog = new ImageDownloadDialog();
		imageDownloadDialog.setVisible(true);
		System.out.println(imageDownloadDialog.getSelectedImage());
		System.exit(0);
	}
}
