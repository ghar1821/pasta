package pasta.scheduler;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pasta.domain.Execution;
import pasta.util.ProjectProperties;


/**
 * Execution Scheduler.
 * 
 * It will handle all interactions with the mysql databse.
 * 
 * Uses singleton pattern.
 * 
 * @author Alex
 *
 */
public class ExecutionScheduler {
	protected final Log logger = LogFactory.getLog(getClass());
	protected static ExecutionScheduler instance = null;
	
	protected Connection databaseConnection = null;
	
	public Connection getDatabaseConnection(){
//		try {
//			if(databaseConnection == null || databaseConnection.isClosed()){
//				Class.forName("com.mysql.jdbc.Driver");
//				databaseConnection = DriverManager.getConnection(ProjectProperties.getInstance().getUrl(),
//						ProjectProperties.getInstance().getUser(),
//						ProjectProperties.getInstance().getPass());
//				databaseConnection.setAutoCommit(false);
//				// create query statement
//				PreparedStatement st1 = databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS `jobs` ( `unikey` text NOT NULL, `assessmentName` text NOT NULL, `executionDate` datetime NOT NULL);\n");
//				// execute and commit
//				st1.executeUpdate();
//				databaseConnection.commit();
//				
//				// create query statement
//				PreparedStatement st2 = databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS `students` ( `unikey` text NOT NULL, `tutorialClass` text NOT NULL);\n");
//				// execute and commit
//				st2.executeUpdate();
//				databaseConnection.commit();
//			}
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			return null;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return null;
//		}
//		return databaseConnection;
		return null;
	}
	
	/**
	 * Get the instance
	 * @return the instance of ExecutionScheduler
	 */
	public static ExecutionScheduler getInstance(){
		if(instance == null){
			instance = new ExecutionScheduler();
		}
		return instance;
	}
	
	/**
	 * Schedule an execution for now.
	 * @param exec
	 */
	public void scheduleExecution(Execution exec){
		// open up connection
		Connection con = getDatabaseConnection();
		if(con != null){
		PreparedStatement st = null;
			try {
				// create query statement
				st = con.prepareStatement("INSERT INTO jobs (unikey, assessmentName, executionDate) VALUES (?,?,NOW());\n");
				
				// set values
				st.setString(1, exec.getUnikey());
				st.setString(2, exec.getAssessmentName());
				
				// execute and commit
				st.executeUpdate();
				con.commit();
				
				// cleanup
				st.close();
				st = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.info(e);
				if(con != null){
					try {
						con.rollback();
					} catch (SQLException e1) {
						logger.info(e1);
					}
				}
			}
//			try {
////				if (st != null && !st.isClosed()) {
////					st.close();
////				}
//			} catch (SQLException e) {
////				e.printStackTrace();
//			}
		}
	}
	
	/**
	 * Get the next execution in line to be executed.
	 * @return the next execution
	 * @return null - there are no further executions
	 */
	public Execution nextExecution(){
//		// connect
//		Connection con = getDatabaseConnection();
//		if(con != null){
//			PreparedStatement st = null;
//			ResultSet result = null;
//			try {
//				// create prepared statement
//				st = con.prepareStatement("SELECT unikey, assessmentName, executionDate FROM jobs WHERE executionDate <= NOW() GROUP BY executionDate LIMIT 0,1;\n");
//				
//				// execute query
//				result = st.executeQuery();
//				
//				Execution exec = null;
//				
//				// get data
//				if(result.next()){
//				exec = new Execution(result.getString("unikey"),
//						result.getString("assessmentName"),
//						result.getDate("executionDate"));
//				}
//				
//				// cleanup
//				result.close();
//				st.close();
//				st = null;
//				result = null;
//				
//				return exec;
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				if(con != null){
//					try {
//						con.rollback();
//					} catch (SQLException e1) {
//						// TODO Auto-generated catch block
////						e1.printStackTrace();
//					}
//				}
//			} 
//		}
		return null;
	}
	
	/**
	 * The execution is completed and should be removed from the database
	 * @param exec
	 */
	public void completedExecution(Execution exec){
		// conect
		Connection con = getDatabaseConnection();
		if(con!= null){
		PreparedStatement st = null;
			try {
				// create prepared statement
				st = con.prepareStatement("DELETE FROM jobs WHERE unikey = ? AND assessmentName = ?;\n");
				
				// set values
				st.setString(1, exec.getUnikey());
				st.setString(2, exec.getAssessmentName());
				
				// execute and commit
				st.executeUpdate();
				con.commit();
				
				// cleanup
				st.close();
				st = null;
			} catch (SQLException e) {
				logger.info(e);
				if(con != null){
					try {
						con.rollback();
					} catch (SQLException e1) {
						logger.info(e1);
					}
				}
			} 
		}
	}
}
