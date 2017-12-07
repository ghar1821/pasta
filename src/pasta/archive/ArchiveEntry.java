package pasta.archive;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;

import pasta.util.ProjectProperties;

public class ArchiveEntry implements Serializable {
	private static final long serialVersionUID = -8781057876124034321L;

	protected static Logger logger = Logger.getLogger(ArchiveEntry.class);
	
	private ArchivableBaseEntity data;
	private long instanceId;
	
	public ArchiveEntry(ArchivableBaseEntity data) {
		this.data = data;
		this.instanceId = getThisInstanceID();
	}
	
	private ArchiveEntry(ArchivableBaseEntity data, long instanceId) {
		this.data = data;
		this.instanceId = instanceId;
	}
	
	public ArchivableBaseEntity getData() {
		return data;
	}
	
	public long getInstanceId() {
		return instanceId;
	}

	private long getThisInstanceID() {
		return ProjectProperties.getInstance().getInstanceId();
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
	
	public static ArchiveEntry readFrom(ObjectInputStream in) throws EOFException {
		try {
			ArchivableBaseEntity data = (ArchivableBaseEntity) in.readObject();
			long instanceId = in.readLong();
			return new ArchiveEntry(data, instanceId);
		} catch (EOFException e) {
			throw e;
		} catch (ClassNotFoundException | IOException e) {
			logger.error("Error reading archive item from stream", e);
		}
		return null;
	}
}
