package org.rr.jeborker.gui.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.rr.commons.utils.DateConversionUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.metadata.MetadataProperty;

import com.l2fprod.common.propertysheet.DefaultProperty;

public class EbookSheetProperty extends DefaultProperty {

	private static final long serialVersionUID = 1L;
	
	private final List<EbookPropertyItem> items;
	
	protected MetadataProperty metadataProperty;
	
	protected int propertyIndex;
	
	/**
	 * Flags that tells that the value for this property has changed 
	 */
	private boolean changed = false;
	
	public EbookSheetProperty(final MetadataProperty metadataProperty, final List<EbookPropertyItem> items, final int propertyIndex) {
		this.metadataProperty = metadataProperty;
		this.propertyIndex = propertyIndex;
		this.items = items;
		this.setEditable(metadataProperty.isEditable());
		this.setDeletable(metadataProperty.isDeletable());
	}
	
	/**
	 * Get the {@link EbookPropertyItem} for this property.
	 * @return The desired {@link EbookPropertyItem}.
	 */
	public List<EbookPropertyItem> getEbookPropertyItems() {
		return this.items;
	}

	void firePropertyChanged(Object oldValue, Object newValue) {
		firePropertyChange(oldValue, newValue);
	}
	
	/**
	 * Tells if the property value has changed.
	 * @return <code>true</code> if the property value has changed and <code>false</code> otherwise.
	 */
	public boolean isChanged() {
		return changed;
	}

	void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public void setValue(Object value) {
		if(value!=null && !value.equals(getValue())) {
			metadataProperty.setValue(value, this.propertyIndex);
			this.setChanged(true);	
		}
		super.setValue(value);
	}

	@Override
	public String getName() {
		String name = metadataProperty.getName();
		return name;
	}
	
	@Override
	public String getShortDescription() {
		final StringBuilder value = new StringBuilder();
		final boolean isDate = ReflectionUtils.equals(metadataProperty.getPropertyClass(), Date.class);
		final int lpropertyIndex = this.propertyIndex >= 0 ? this.propertyIndex : 0;
		if (isDate) {
			if (value.length() > 0) {
				value.append("<br/>");
			}
			Object propertyValue = metadataProperty.getValues().get(lpropertyIndex);
			if (propertyValue instanceof Date) {
				value.append(DateFormat.getDateInstance(SimpleDateFormat.LONG).format((Date) propertyValue));
			} else {
				Date date = DateConversionUtils.toDate(StringUtils.toString(metadataProperty.getValues().get(lpropertyIndex)));
				if (date != null) {
					value.append(DateFormat.getDateInstance(SimpleDateFormat.LONG).format(date));
				}
			}
		} else {
			if (value.length() > 0) {
				value.append("<br/>");
			}
			value.append(String.valueOf(metadataProperty.getValues().get(lpropertyIndex)));
		}
		
		return value.toString();
	}

	@Override
	public String getDisplayName() {
		final String name = metadataProperty.getName();

		String localizedName = MainController.getController().getLocalizedString(name);
		if(!isMultiSelection() && metadataProperty.getValues().size() > 1) {
			localizedName = (this.propertyIndex + 1) + ") " + localizedName; 
		}
		
		return localizedName.toString();
	}

	public String getDisplayDescriptionName() {
		final String name = metadataProperty.getName();
		
		String localizedName = getDisplayName();
		localizedName += "</b><i> &lt;" + name+"&gt;</i><b>";
		return localizedName;
	}

	@Override
	public Object getValue() {
		if(propertyIndex >= 0) {
			return metadataProperty.getValues().get(propertyIndex);
		} else {
			return metadataProperty.getValues();
		}
	}


	@Override
	public Class<?> getType() {
		return metadataProperty.getPropertyClass();
	}

	/**
	 * Gets the encapsulated {@link MetadataProperty} which holds the current value state.
	 * @return The encapsulated {@link MetadataProperty} or <code>null</code> if no one existing.
	 */
	public List<MetadataProperty> getMetadataProperties() {
		return Arrays.asList(this.metadataProperty);
	}
	
	/**
	 * Tells if there is currently more than one entry selected. 
	 * @return <code>true</code> if more than one value is selected and false otherwise. 
	 */
	private boolean isMultiSelection() {
		return MainController.getController().getSelectedEbookPropertyItemRows().length > 1;
	}
}
