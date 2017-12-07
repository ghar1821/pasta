package pasta.archive;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import pasta.domain.BaseEntity;

public class Archive {

	private Map<ItemLookupKey, ArchivableBaseEntity> unarchived;
	private LinkedList<ArchiveEntry> toDo;
	
	public Archive(ObjectInputStream inputStream) {
		unarchived = new HashMap<>();
		toDo = new LinkedList<>();
		try {
			while(true) {
				ArchiveEntry item = (ArchiveEntry) ArchiveEntry.readFrom(inputStream);
				toDo.add(item);
			}
		} catch (EOFException e) {}
	}
	
	public ArchiveEntry getNextItem() {
		return toDo.removeFirst();
	}
	
	public boolean hasMoreItems() {
		return !toDo.isEmpty();
	}
	
	public void register(ArchiveEntry item, ArchivableBaseEntity unarchived) {
		ItemLookupKey key = createKey(item);
		this.unarchived.put(key, unarchived);
	}
	
	public ArchivableBaseEntity getUnarchived(ArchiveEntry item) {
		ItemLookupKey key = createKey(item);
		return this.unarchived.get(key);
	}
	
	public boolean isUnarchived(ArchiveEntry item) {
		return getUnarchived(item) != null;
	}
	
	private ItemLookupKey createKey(ArchiveEntry item) {
		return new ItemLookupKey((Class<? extends ArchivableBaseEntity>) item.getData().getClass(), ((BaseEntity)item.getData()).getId());
	}
	
	private static class ItemLookupKey {
		private Class<? extends ArchivableBaseEntity> itemClass;
		private Long itemId;
		public ItemLookupKey(Class<? extends ArchivableBaseEntity> itemClass, Long itemId) {
			this.itemClass = itemClass;
			this.itemId = itemId;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((itemClass == null) ? 0 : itemClass.hashCode());
			result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ItemLookupKey other = (ItemLookupKey) obj;
			if (itemClass == null) {
				if (other.itemClass != null)
					return false;
			} else if (!itemClass.equals(other.itemClass))
				return false;
			if (itemId == null) {
				if (other.itemId != null)
					return false;
			} else if (!itemId.equals(other.itemId))
				return false;
			return true;
		}
	}
}
