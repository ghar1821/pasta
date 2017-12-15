/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
