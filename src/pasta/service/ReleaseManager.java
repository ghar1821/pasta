package pasta.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.release.ClassRule;
import pasta.domain.release.DateRule;
import pasta.domain.release.MarkCompareRule;
import pasta.domain.release.ReleaseAllResultsRule;
import pasta.domain.release.ReleaseAndRule;
import pasta.domain.release.ReleaseOrRule;
import pasta.domain.release.ReleaseResultsRule;
import pasta.domain.release.ReleaseRule;
import pasta.domain.release.StreamRule;
import pasta.domain.release.SubmissionCountRule;
import pasta.domain.release.UsernameRule;
import pasta.domain.release.form.AssessmentReleaseForm;
import pasta.domain.template.Assessment;
import pasta.repository.AssessmentDAO;
import pasta.repository.ReleaseDAO;
import pasta.util.PASTAUtil;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 20 Apr 2015
 */
@Service("releaseManager")
@Repository
public class ReleaseManager {
	public static final Logger logger = Logger.getLogger(ReleaseManager.class);
	
	@Autowired
	private ReleaseDAO releaseDAO;
	@Autowired
	private AssessmentDAO assessmentDAO;
	
	public ReleaseRule getReleaseRule(long id) {
		return releaseDAO.getReleaseRule(id);
	}
	
	public void updateRelease(Assessment assessment, AssessmentReleaseForm form) {		
		ReleaseRule rule = null;
		if(!assessment.isReleased()) {
			rule = createInstance(form.getRuleName());
		} else {
			rule = assessment.getReleaseRule();
			if(rule.getId() != form.getRuleId()) {
				releaseDAO.delete(assessment.getReleaseRule());
				rule = createInstance(form.getRuleName());
			}
		}
		
		updateReleaseRule(rule, form);
		
		assessment.setReleaseRule(rule);
		assessmentDAO.saveOrUpdate(assessment);
	}
	
	private void updateReleaseRule(ReleaseRule rule, AssessmentReleaseForm form) {
		if(rule == null) {
			return;
		}
		
		if(rule instanceof ReleaseResultsRule || rule instanceof ReleaseAllResultsRule) {
			long existingId = 0;
			Assessment existingAssessment = rule instanceof ReleaseResultsRule ?
					((ReleaseResultsRule) rule).getCompareAssessment() :
					((ReleaseAllResultsRule) rule).getCompareAssessment();
			
			if(existingAssessment != null) {
				existingId = existingAssessment.getId();
			}

			long formId = form.getCompareAssessment() == null ? 0 : form.getCompareAssessment().getId();
			if(existingId != formId) {
				Assessment assessment = null;
				if(formId != 0) {
					assessment = assessmentDAO.getAssessment(formId);
				}
				if(rule instanceof ReleaseResultsRule) {
					((ReleaseResultsRule) rule).setCompareAssessment(assessment);
				} else {
					((ReleaseAllResultsRule) rule).setCompareAssessment(assessment);
				}
			}
		}
		
		if(rule instanceof ClassRule) {
			Set<String> newClasses = form.getClasses();
			if(newClasses == null) {
				newClasses = new TreeSet<String>();
			}
			((ClassRule) rule).setClasses(newClasses);
		} else if(rule instanceof DateRule) {
			if(!((DateRule) rule).getStrDate().equals(form.getStrDate())) {
				((DateRule) rule).setStrDate(form.getStrDate());
			}
		} else if(rule instanceof MarkCompareRule) {
			((MarkCompareRule) rule).setCompareMode(form.getCompareMode());
			((MarkCompareRule) rule).setCompareMark(form.getCompareMark());
			((MarkCompareRule) rule).setAsPercentage(form.isAsPercentage());
			((MarkCompareRule) rule).setMarkType(form.getMarkType());
		} else if(rule instanceof StreamRule) {
			Set<String> newStreams = form.getStreams();
			if(newStreams == null) {
				newStreams = new TreeSet<String>();
			}
			((StreamRule) rule).setStreams(newStreams);
		} else if(rule instanceof SubmissionCountRule) {
			((SubmissionCountRule) rule).setSubmissionCount(form.getSubmissionCount());
			((SubmissionCountRule) rule).setCompareMode(form.getCompareMode());
		} else if(rule instanceof UsernameRule) {
			Set<String> newUsernames = form.getUsernames();
			if(newUsernames == null) {
				newUsernames = new TreeSet<String>();
			}
			((UsernameRule) rule).setUsernames(newUsernames);
		} else if(rule instanceof ReleaseAndRule || rule instanceof ReleaseOrRule) {
			Map<Long, AssessmentReleaseForm> subFormsMap = new LinkedHashMap<>();
			List<AssessmentReleaseForm> newSubForms = new LinkedList<AssessmentReleaseForm>();
			if(form.getRules() != null) {
				// Remove deleted rules from list (they do not pass a name)
				Iterator<AssessmentReleaseForm> itSubRules = form.getRules().iterator();
				while(itSubRules.hasNext()) {
					if(itSubRules.next().getRuleName() == null) {
						itSubRules.remove();
					}
				}
				// Identify which rules are new and which are updated
				for(AssessmentReleaseForm subForm : form.getRules()) {
					if(subForm.getRuleId() != 0) {
						subFormsMap.put(subForm.getRuleId(), subForm);
					} else {
						newSubForms.add(subForm);
					}
				}
			}
			
			Iterator<ReleaseRule> it;
			if(rule instanceof ReleaseAndRule) {
				it = ((ReleaseAndRule) rule).getRules().iterator();
			} else {
				it = ((ReleaseOrRule) rule).getRules().iterator();
			}
			while(it.hasNext()) {
				ReleaseRule subRule = it.next();
				if(!subFormsMap.containsKey(subRule.getId())) {
					it.remove();
					continue;
				}
				updateReleaseRule(subRule, subFormsMap.get(subRule.getId()));
				subFormsMap.remove(subRule.getId());
			}
			
			for(AssessmentReleaseForm newSubForm : newSubForms) {
				ReleaseRule newRule = createInstance(newSubForm.getRuleName());
				updateReleaseRule(newRule, newSubForm);
				if(rule instanceof ReleaseAndRule) {
					((ReleaseAndRule) rule).addRule(newRule);
				} else {
					((ReleaseOrRule) rule).addRule(newRule);
				}
			}
		}
	}

	public static Set<ReleaseRule> getOneOfEach() {
		Set<ReleaseRule> results = new TreeSet<ReleaseRule>();
		for(Class<? extends ReleaseRule> subclass : PASTAUtil.getSubclasses(ReleaseRule.class, "pasta/domain/release")) {
			try {
				Constructor<? extends ReleaseRule> constr = subclass.getConstructor();
				results.add(constr.newInstance());
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				logger.warn("No default constructor found for " + subclass.getName());
				continue;
			}
		}
		return results;
	}
	
	public static ReleaseRule createInstance(String className) {
		if(className == null) {
			return null;
		}
		ReleaseRule rule = null;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends ReleaseRule> ruleClass = (Class<? extends ReleaseRule>) Class.forName(className);
			Constructor<? extends ReleaseRule> defaultConst = ruleClass.getConstructor();
			rule = defaultConst.newInstance();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		}
		return rule;
	}
}
