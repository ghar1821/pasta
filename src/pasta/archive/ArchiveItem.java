package pasta.archive;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import pasta.util.ProjectProperties;

public class ArchiveItem<T> {
	protected static Logger logger = Logger.getLogger(ArchiveItem.class);
	
	private Archivable<T> data;
	private long instanceId;
	
	public ArchiveItem(Archivable<T> data) {
		this.data = data;
		this.instanceId = getThisInstanceID();
	}
	
	private ArchiveItem(Archivable<T> data, long instanceId) {
		this.data = data;
		this.instanceId = instanceId;
	}
	
	public Serializable getData() {
		return data;
	}

	private long getThisInstanceID() {
		return ProjectProperties.getInstance().getInstanceId();
	}
	
	@Deprecated
	public void clearDataId() {
		if(data != null) {
			try {
				Method setId = data.getClass().getMethod("setId", Long.class);
				setId.invoke(data, (Long)null);
			} catch (NoSuchMethodException e) {
				// Do nothing
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.error("Could not set ID to null", e);
			}
		}
	}
	
	public boolean isFromThisInstance() {
		return this.instanceId == getThisInstanceID();
	}
	
	public ObjectOutputStream archiveTo(ObjectOutputStream out) {
		try {
			out.writeObject(data);
			out.writeLong(instanceId);
		} catch (IOException e) {
			logger.error("Error writing archive item to stream", e);
		}
		return out;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArchiveItem<?> readFrom(ObjectInputStream in) {
		try {
			Archivable<?> data = (Archivable<?>) in.readObject();
			long instanceId = in.readLong();
			return new ArchiveItem(data, instanceId);
		} catch (ClassNotFoundException | IOException e) {
			logger.error("Error reading archive item from stream", e);
		}
		return null;
	}
}
