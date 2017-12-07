package pasta.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import pasta.archive.ArchivableBaseEntity;
import pasta.archive.Archive;
import pasta.archive.ArchiveEntry;
import pasta.archive.PreviousArchive;
import pasta.domain.BaseEntity;
import pasta.domain.template.UnitTest;
import pasta.repository.ArchiveDAO;
import pasta.repository.BaseDAO;
import pasta.repository.UnitTestDAO;
import pasta.util.ProjectProperties;

@Service("archiveManager")
public class ArchiveManager {

	protected Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private UnitTestDAO unitTestDao;
	
	@Autowired
	private ArchiveDAO archiveDao;
	
	@Autowired
	@Qualifier("baseDAO")
	private BaseDAO baseDao;
	
	public void test1() {
		
		List<UnitTest> allUnitTests = unitTestDao.getAllUnitTests();
		UnitTest ut = allUnitTests.get(allUnitTests.size() - 1);
		if(!ut.getName().toLowerCase().startsWith("th")) {
			return;
		}
		ArchiveEntry entry = new ArchiveEntry(ut);
		
		File f = new File(ProjectProperties.getInstance().getProjectLocation(), "test.ser");
		ObjectOutputStream out = null;
		try {
			FileOutputStream fileOut = new FileOutputStream(f);
			out = new ObjectOutputStream(fileOut);
			
			entry.archiveTo(out);
			out.close();
			
//			baseDao.delete(ut);
		} catch (IOException e) {
			logger.error("Error serialising test.", e);
		}
		
	}
	public void test2() {
		
		File f = new File(ProjectProperties.getInstance().getProjectLocation(), "test.ser");
		ObjectInputStream in = null;
		try {
			FileInputStream fileIn = new FileInputStream(f);
			in = new ObjectInputStream(fileIn);
			
			Archive archive = new Archive(in);
			while(archive.hasMoreItems()) {
				ArchiveEntry item2 = archive.getNextItem();
				logger.info(item2.getData());
				logger.info(item2.isFromThisInstance());
				save(item2, archive);
			}
			in.close();
			
		} catch (IOException e) {
			logger.error("Error serialising test.", e);
		}
		
	}
	
	public void save(ArchiveEntry entry, Archive archive) {
		ArchivableBaseEntity archivedEntity = entry.getData();
		
		// Item is already unarchived
		if(archive.isUnarchived(entry)) {
			logger.info("Skipping " + archivedEntity + " as it is already unarchived.");
			return;
		}
		
		PreviousArchive previousArchive = archiveDao.getPreviousArchive(
				(Class<? extends ArchivableBaseEntity>) archivedEntity.getClass(), 
				archivedEntity.getId(), archivedEntity.getVersion(), entry.getInstanceId());
		ArchivableBaseEntity previous = archiveDao.getPreviouslyMappedEntity(previousArchive);
		
		// Item has been unarchived before
		if(previous != null) {
			// Item has been updated since previous archive
			if(previous.getVersion() > archivedEntity.getVersion()) {
				logger.info("Replacing previous unarchive of " + archivedEntity + " as it has been modified since last unarchive.");
				replaceExistingEntity(entry, archive, previous);
			} else {
				logger.info("Skipping " + archivedEntity + " as it has been unarchived previously.");
				archive.register(entry, previous);
			}
			return;
		}
		
		if(entry.isFromThisInstance()) {
			ArchivableBaseEntity existing = (ArchivableBaseEntity) baseDao.get(archivedEntity.getClass(), archivedEntity.getId());
			if(existing == null || existing.getVersion() < archivedEntity.getVersion()) {
				logger.info("Saving local " + archivedEntity + " as it doesn't exist yet (even though it is from this instance).");
//				baseDao.evict(archivedEntity);
				saveToDatabase(entry, archive);
			} else if (existing.getVersion() > archivedEntity.getVersion()) {
				logger.info("Replacing " + archivedEntity + " as the existing version has changed.");
				replaceExistingEntity(entry, archive, existing);
			} else {
				logger.info("Skipping " + archivedEntity + " as the existing version hasn't changed.");
				archive.register(entry, existing);
			}
		} else {
			logger.info("Saving foreign " + archivedEntity + " as it doesn't exist yet.");
			saveToDatabase(entry, archive);
		}
	}
	
	private void saveToDatabase(ArchiveEntry entry, Archive archive) {
		ArchivableBaseEntity archivedEntity = entry.getData();
		archivedEntity.setId(null);
		archivedEntity.setVersion(null);
		archivedEntity.rebuildFromArchive(archive, null);
		Long newId = baseDao.save(archivedEntity);
		archiveDao.savePreviousArchive(entry, newId);
		archive.register(entry, archivedEntity);
	}
	
	private void replaceExistingEntity(ArchiveEntry entry, Archive archive, ArchivableBaseEntity existing) {
		archiveDao.savePreviousArchive(entry, existing.getId());
		ArchivableBaseEntity archivedEntity = entry.getData();
//		archivedEntity.setId(existing.getId());
//		archivedEntity.setVersion(existing.getVersion());
		archivedEntity.rebuildFromArchive(archive, existing);
		logger.info("Name before:" + ((UnitTest)archivedEntity).getName());
//		baseDao.evict(existing);
//		baseDao.delete(existing);
//		baseDao.replicate(archivedEntity);
		baseDao.merge(archivedEntity);
		logger.info("Name after:" + ((UnitTest)archivedEntity).getName());
		archive.register(entry, archivedEntity);
	}
}
