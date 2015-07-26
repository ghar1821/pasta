package pasta.repository;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.template.Arena;
import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.WeightedCompetition;

@Transactional
@Repository("competitionDAO")
public class CompetitionDAO {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public Competition saveOrUpdate(Competition comp) {
		sessionFactory.getCurrentSession().saveOrUpdate(comp);
		logger.info("Saved competition " + comp.getName());
		return comp;
	}
	
	public void delete(Competition comp) {
		sessionFactory.getCurrentSession().delete(comp);
		logger.info("Deleted Competition " + comp.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<Competition> getAllCompetitions() {
		return sessionFactory.getCurrentSession().createCriteria(Competition.class).list();
	}

	public Competition getCompetition(long id) {
		return (Competition) sessionFactory.getCurrentSession().get(Competition.class, id);
	}
	
	public WeightedCompetition getWeightedCompetition(long id) {
		return (WeightedCompetition) sessionFactory.getCurrentSession().get(WeightedCompetition.class, id);
	}
	
	public void save(Arena arena) {
		sessionFactory.getCurrentSession().save(arena);
		logger.info("Created Arena " + arena.getName());
	}
	
	public void update(Arena arena) {
		sessionFactory.getCurrentSession().update(arena);
		logger.info("Updated Arena " + arena.getName());
	}
	
	public void delete(Arena arena) {
		sessionFactory.getCurrentSession().delete(arena);
		logger.info("Deleted Arena " + arena.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<Arena> getAllArenas() {
		return sessionFactory.getCurrentSession().createCriteria(Arena.class).list();
	}
	
	public Arena getArena(long id) {
		return (Arena) sessionFactory.getCurrentSession().get(Arena.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<Assessment> getAssessmentsUsingCompetition(Competition comp) {
		DetachedCriteria assessmentIds = DetachedCriteria.forClass(WeightedCompetition.class);
		assessmentIds.createCriteria("competition").add(Restrictions.eq("id", comp.getId()));
		assessmentIds.setProjection(Property.forName("assessment").getProperty("id"));
		
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(Assessment.class);
		cr.add(Subqueries.propertyIn("id", assessmentIds));
		cr.add(Property.forName("id").in(assessmentIds));
		return cr.list();
	}

	public boolean isLive(Competition comp) {
		return !getAssessmentsUsingCompetition(comp).isEmpty();
	}

	public boolean isRunning(Competition comp) {
		return isLive(comp) && (comp.isCalculated() || !comp.isFinished());
	}

	
}
