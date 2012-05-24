package org.rr.jeborker.db;

import java.util.AbstractList;
import java.util.List;

import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

class ODocumentMapper<T> extends AbstractList<T> {

	private List<?> documents;
	
	private OObjectDatabaseTx db;
	
	public ODocumentMapper(List<?> documents, OObjectDatabaseTx db) {
		this.documents = documents;
		this.db = db;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get(int index) {
		Object object = documents.get(index);
		if(object instanceof ORecordInternal) {
			return (T) db.getUserObjectByRecord((ORecordInternal<T>) object, null);
		} else {
			return (T)object;
		}
	}

	@Override
	public boolean isEmpty() {
		return documents.isEmpty();
	}

	@Override
	public int size() {
		return documents.size();
	}
}
