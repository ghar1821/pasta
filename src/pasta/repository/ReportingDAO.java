package pasta.repository;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pasta.domain.UserPermissionLevel;
import pasta.domain.reporting.Report;
import pasta.domain.reporting.ReportPermission;

@Transactional
@Repository("reportingDAO")
@DependsOn("projectProperties")
public class ReportingDAO {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public ReportingDAO() {
	}
	
	@SuppressWarnings("unchecked")
	public List<Report> getAllReports() {
		return sessionFactory.getCurrentSession().createCriteria(Report.class).list();
	}
	
	public Report getReportById(String id) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(Report.class);
		cr.add(Restrictions.eq("id", id));
		return (Report) cr.uniqueResult();
	}
	
	public void saveOrUpdate(Report report) {
		sessionFactory.getCurrentSession().saveOrUpdate(report);
	}

	@SuppressWarnings("unchecked")
	public List<ReportPermission> getPermissions(Report report, UserPermissionLevel permissionLevel) {
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(ReportPermission.class);
		cr.add(Restrictions.eq("id.report", report));
		cr.add(Restrictions.eq("id.permissionLevel", permissionLevel));
		return cr.list();
	}
}
