package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.WeightedCompetition;

@Repository("competitionDAO")
public class CompetitionDAO extends HibernateDaoSupport {

	protected final Log logger = LogFactory.getLog(getClass());
	
	// Default required by hibernate
	@Autowired
	public void setMySession(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
	
	public Competition saveOrUpdate(Competition comp) {
		getHibernateTemplate().saveOrUpdate(comp);
		logger.info("Saved competition " + comp.getName());
		return comp;
	}
	
	public void delete(Competition comp) {
		getHibernateTemplate().delete(comp);
		logger.info("Deleted Competition " + comp.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<Competition> getAllCompetitions() {
		return getHibernateTemplate().find("FROM Competition");
	}

	public Competition getCompetition(long id) {
		return getHibernateTemplate().get(Competition.class, id);
	}
	
	public WeightedCompetition getWeightedCompetition(long id) {
		return getHibernateTemplate().get(WeightedCompetition.class, id);
	}
	
	@Deprecated
	public Competition getCompetition(String name) {
		@SuppressWarnings("unchecked")
		List<Competition> results = getHibernateTemplate().find("FROM Competition WHERE name = ?", name);
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	public void save(Arena arena) {
		getHibernateTemplate().save(arena);
		logger.info("Created Arena " + arena.getName());
	}
	
	public void update(Arena arena) {
		getHibernateTemplate().update(arena);
		logger.info("Updated Arena " + arena.getName());
	}
	
	public void delete(Arena arena) {
		getHibernateTemplate().delete(arena);
		logger.info("Deleted Arena " + arena.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<Arena> getAllArenas() {
		return getHibernateTemplate().find("FROM Arena");
	}
	
	public Arena getArena(long id) {
		return getHibernateTemplate().get(Arena.class, id);
	}
	
	@Deprecated
	public Arena getArena(String name) {
		@SuppressWarnings("unchecked")
		List<Arena> results = getHibernateTemplate().find("FROM Arena WHERE name = ?", name);
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public List<Assessment> getAssessmentsUsingCompetition(Competition comp) {
		DetachedCriteria assessmentIds = DetachedCriteria.forClass(WeightedCompetition.class);
		assessmentIds.createCriteria("competition").add(Restrictions.eq("id", comp.getId()));
		assessmentIds.setProjection(Property.forName("assessment").getProperty("id"));
		
		DetachedCriteria cr = DetachedCriteria.forClass(Assessment.class);
		cr.add(Property.forName("id").in(assessmentIds));
		return getHibernateTemplate().findByCriteria(cr);
	}

	public boolean isLive(Competition comp) {
		return !getAssessmentsUsingCompetition(comp).isEmpty();
	}

	public boolean isRunning(Competition comp) {
		return isLive(comp) && (comp.isCalculated() || !comp.isFinished());
	}

	
}
