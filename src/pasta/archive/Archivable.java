package pasta.archive;

import java.io.Serializable;

public interface Archivable<T> extends Serializable {
	public T rebuild(RebuildOptions options) throws InvalidRebuildOptionsException;
}
