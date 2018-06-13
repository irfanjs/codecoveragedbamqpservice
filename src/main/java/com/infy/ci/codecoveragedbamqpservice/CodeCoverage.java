package com.infy.ci.codecoveragedbamqpservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CodeCoverage {
	
	int projectid;
	@Autowired
	private CodeCoverageDBHelper c;
	
	public CodeCoverage(int projectid)
	{
		this.projectid = projectid;
	}
	
	public void setProjectid(int projectid) {
		this.projectid = projectid;
	}
	
	public CodeCoverage() {
		
	}
	
	public boolean insert(int buildintoId, int packages, int files,
			int classes, int methods, int linesofcode, int conditions)
					throws SQLException, ClassNotFoundException {
		Connection conn = null;
		PreparedStatement prepStatement = null;
		try {
			conn = c.getConnection();
			prepStatement = conn
					.prepareStatement("insert into codecoverage(buildinfo_id,packages,files,classes,methods,linesofcode,conditions) values(?,?,?,?,?,?,?)");

			prepStatement.setInt(1, buildintoId);
			prepStatement.setInt(2, packages);
			prepStatement.setInt(3, files);
			prepStatement.setInt(4, classes);
			prepStatement.setInt(5, methods);
			prepStatement.setInt(6, linesofcode);
			prepStatement.setInt(7, conditions);

			prepStatement.executeUpdate();

		} finally {
			CodeCoverageDBHelper.close(conn, prepStatement, null);
		}
		return true;
	}

	public List<Map<String, Object>> getCodeCoverageDataForLatestBuildId()
			throws SQLException, ClassNotFoundException {
		
		String sql = "select cc.packages,"
					+ "cc.files," + "cc.classes," + "cc.methods,"
					+ "cc.linesofcode," + "cc.conditions," + "bi.id,"
					+ "bi.buildnumber " + "from codecoverage cc, buildinfo bi "
					+ "where bi.id = cc.buildinfo_id "
					+ "order by datetime desc " + "limit 1;";
		
		return executeQuery(sql);
		
	}

	public List<Map<String, Object>> getCodeCoverageForBuildId(int buildnumber)
			throws SQLException, ClassNotFoundException {

		String sql = "select cc.packages," + "cc.files," + "cc.classes,"
				+ "cc.methods," + "cc.linesofcode," + "cc.conditions,"
				+ "bi.id," + "bi.buildnumber "
				+ "from codecoverage cc, buildinfo bi "
				+ "where bi.id = cc.buildinfo_id " + "and bi.buildnumber = "
				+ buildnumber;
		return executeQuery(sql);
	}

	public List<Map<String, Object>> executeQuery(String sql)
			throws SQLException {
		Connection conn = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			conn = c.getConnection();
			statement = conn.createStatement();
			resultSet = statement.executeQuery(sql);

			return c.getEntitiesFromResultSet(resultSet);
		}

		finally {
			CodeCoverageDBHelper.close(conn, statement, resultSet);
		}

	}
	
	public List<Map<String, Object>> getProjectNamesId() throws SQLException, ClassNotFoundException {

		String sql = "select id,name from projects;";

		return executeQuery(sql);
	}
	
	public List<Map<String, Object>> getBuildArtifactsForLatestNightlyBuild()
			throws SQLException, ClassNotFoundException {

		// String sql = "select sum(subut.total) total,sum(subut.pass)
		// pass,sum(subut.fail) fail, sum(subut.skip) skip from (select
		// modulename, datetime as dt,max(id) id from buildinfo subbi where
		// subbi.nightlybuild_id in (select id from nightlybuild where datetime
		// in (select max(datetime) from nightlybuild where status = 1)) and
		// project_id = " + this.projectid + " group by modulename) suborig LEFT
		// JOIN unittest subut ON suborig.id = subut.buildinfo_id;";
		// query to get data from so e2e snapshot
		String sql = "select nb.buildnumber,bi.loc,bi.result,bi.reason,bi.datetime,nb.reviewidcount from nightlybuild nb inner join buildinfo bi on bi.nightlybuild_id = nb.id where project_id = "
				+ this.projectid + " order by nb.buildnumber desc limit 20;";
		return executeQuery(sql);
	}


	public List<Map<String, Object>> getCodeCoverageDataForNightlyBuildId(
			int nightlybuildnumber) throws SQLException, ClassNotFoundException {
		String sql = "select cc.packages,"
				+ "cc.files," + "cc.classes," + "cc.methods,"
				+ "cc.linesofcode," + "cc.conditions," + "bi.id,"
				+ "bi.buildnumber "
				+ "from codecoverage cc, buildinfo bi, nightlybuild nb "
				+ "where nb.id = bi.nightlybuild_id "
				+ "and bi.id = cc.buildinfo_id "
				+ "and nb.nightlybuildnumber = " + nightlybuildnumber;
		return executeQuery(sql);
	}

	public List<Map<String, Object>> getAggregatedCodeCoverageDataForNightlyBuildId(
			int nightlybuildnumber) throws SQLException, ClassNotFoundException {

		String sql = "select nb.id,"
				+ "sum(cc.total) total," + "sum(cc.pass) pass,"
				+ "sum(cc.fail) fail "
				+ "from codecoverage cc, buildinfo bi, nightlybuild nb "
				+ "where nb.id = bi.nightlybuild_id "
				+ "and bi.id = cc.buildinfo_id "
				+ "and nb.nightlybuildnumber = " + nightlybuildnumber
				+ " group by nb.id;";

		return executeQuery(sql);
	}

	public List<Map<String, Object>> getAllModulesCodeCoverageForLatestNightlyBuild()
			throws SQLException, ClassNotFoundException {
		String sql = "select bi.modulename ,ROUND(cc.packages) packages,ROUND(cc.files) files,ROUND(cc.classes) classes,ROUND(cc.methods) methods,ROUND(cc.linesofcode) loc,ROUND(cc.conditions) conditions from (select id,sub.modulename from buildinfo sub where sub.nightlybuild_id in (select id from nightlybuild where datetime in (select max(datetime) from nightlybuild where status = 1)) group by modulename) bi LEFT JOIN codecoverage cc on cc.buildinfo_id = bi.id;";

		return executeQuery(sql);

	}

	public List<Map<String, Object>> getAggregatedCodeCoverageDataForLatestNightlyBuild()
			throws SQLException, ClassNotFoundException {
	//	String sql = "select ROUND((avg(cc.packages) + avg(cc.files)+ avg(cc.classes) + avg(cc.methods) + avg(cc.linesofcode) + avg(cc.conditions)) /6) coverage from buildinfo bi INNER JOIN codecoverage cc on bi.id = cc.buildinfo_id where bi.nightlybuild_id in (select id from nightlybuild where datetime in (select max(datetime) from nightlybuild where status = 1));";
//		String sql = "select ROUND(avg(cc.linesofcode)) coverage from buildinfo bi INNER JOIN codecoverage cc on bi.id = cc.buildinfo_id where bi.nightlybuild_id in (select id from nightlybuild where datetime in (select max(datetime) from nightlybuild where status = 1)) and project_id = " + this.projectid + ";";
	String sql = "select ROUND(cc.linesofcode) coverage from (select max(id) id from buildinfo where project_id = " + this.projectid + " and nightlybuild_id != 'NULL') tempbi inner join codecoverage cc on cc.buildinfo_id = tempbi.id;";	
		return executeQuery(sql);

	}

	public List<Map<String, Object>> getAggregatedCodeCoverageDataForLatestBuild()
			throws SQLException, ClassNotFoundException {

		String sql = "select ROUND((avg(cc.packages) + avg(cc.files)+ avg(cc.classes) + avg(cc.methods) + avg(cc.linesofcode) + avg(cc.conditions)) /6) coverage from codecoverage cc where cc.buildinfo_id in (select id from buildinfo bi INNER JOIN ( select modulename, max(datetime) as dt from buildinfo group by modulename) sub on sub.modulename = bi.modulename and bi.datetime = sub.dt);";

		return executeQuery(sql);

	}

	public List<Map<String, Object>> getAllTypeAggregatedCodeCoverageDataForLatestBuild()
			throws SQLException, ClassNotFoundException {
		String sql = "select ROUND(avg(cc.packages)) packages,ROUND(avg(cc.files)) files,ROUND(avg(cc.classes)) classes,ROUND(avg(cc.methods)) methods,ROUND(avg(cc.linesofcode)) loc,ROUND(avg(cc.conditions)) conditions from codecoverage cc where cc.buildinfo_id in (select id from buildinfo bi INNER JOIN ( select modulename, max(datetime) as dt from buildinfo group by modulename) sub on sub.modulename = bi.modulename and bi.datetime = sub.dt);";

		return executeQuery(sql);
	}

	public List<Map<String, Object>> getAllTypeAggregatedCodeCoverageDataForLatestNightlyBuild()
			throws SQLException, ClassNotFoundException {
		
		String sql = "select ROUND(avg(cc.packages)) packages,ROUND(avg(cc.files)) files,ROUND(avg(cc.classes)) classes,ROUND(avg(cc.methods)) methods,ROUND(avg(cc.linesofcode)) loc,ROUND(avg(cc.conditions)) conditions from buildinfo bi INNER JOIN codecoverage cc on bi.id = cc.buildinfo_id where bi.nightlybuild_id in (select id from nightlybuild where datetime in (select max(datetime) from nightlybuild where status = 1));";

		return executeQuery(sql);
	}

	public List<Map<String, Object>> getAllModulesAggregatedCodeCoverageDataForLatestBuild()
			throws SQLException, ClassNotFoundException {
  //  String sql = "select modulename ,ROUND((cc.packages + cc.files + cc.classes + cc.methods + cc.linesofcode + cc.conditions)/6) coverage from (select id,sub.modulename from buildinfo bi INNER JOIN ( select modulename, max(datetime) as dt from buildinfo group by modulename) sub on sub.modulename = bi.modulename and bi.datetime = sub.dt) bi LEFT JOIN codecoverage cc on cc.buildinfo_id = bi.id;";
	String sql = "select modulename ,ROUND(cc.linesofcode) coverage from (select id,sub.modulename from buildinfo bi INNER JOIN ( select modulename, max(datetime) as dt from buildinfo where project_id = " + this.projectid + " and nightlybuild_id is NULL group by modulename) sub on sub.modulename = bi.modulename and bi.datetime = sub.dt) bi LEFT JOIN codecoverage cc on cc.buildinfo_id = bi.id;";
    
    return executeQuery(sql);
		
	}

	public List<Map<String, Object>> getAllModulesAggregatedCodeCoverageDataForLatestNightlyBuild()
			throws SQLException, ClassNotFoundException {
     String sql = "select modulename ,ROUND((cc.packages + cc.files + cc.classes + cc.methods + cc.linesofcode + cc.conditions)/6) coverage from (select id,sub.modulename from buildinfo sub where sub.nightlybuild_id in (select id from nightlybuild where datetime in (select max(datetime) from nightlybuild where status = 1)) group by modulename) bi LEFT JOIN codecoverage cc on cc.buildinfo_id = bi.id;";
		
     return executeQuery(sql);
	}

	public List<Map<String, Object>> getAllModulesCodeCoverageForLatestBuild()
			throws SQLException, ClassNotFoundException {

		String sql = "select bi.modulename ,ROUND(cc.packages) packages,ROUND(cc.files) files,ROUND(cc.classes) classes,ROUND(cc.methods) methods,ROUND(cc.linesofcode) loc,ROUND(cc.conditions) conditions from (select id,sub.modulename from buildinfo bi INNER JOIN ( select modulename, max(datetime) as dt from buildinfo group by modulename) sub on sub.modulename = bi.modulename and bi.datetime = sub.dt) bi LEFT JOIN codecoverage cc on cc.buildinfo_id = bi.id;";
		
		return executeQuery(sql);

	}
	
	public List<Map<String, Object>> getWeekCcAggregateDataNightlyBuild() throws SQLException, ClassNotFoundException{
		 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 Date date = new Date();
		  System.out.println("current date is :" + dateFormat.format(date) );
	        Calendar cal = Calendar.getInstance();
	        
	        cal.add(Calendar.DATE, -7);
	        System.out.println("last week dats is :" + dateFormat.format(cal.getTime()));
	        
		//String sql = "select ni.buildnumber,round(avg(linesofcode)) linesCoverage from codecoverage cc inner join (select * from buildinfo where nightlybuild_id in (select id from nightlybuild where datetime > '2014-02-18 15:19:01' and datetime < '2014-03-20 01:36:41' and status =1 )) tempBI on cc.buildinfo_id = tempBI.id inner join nightlybuild ni on tempBI.nightlybuild_id = ni.id group by nightlybuild_id;";
	      //  String sql = "select ni.buildnumber,round(avg(linesofcode)) linesCoverage from codecoverage cc inner join (select * from buildinfo where nightlybuild_id in (select id from nightlybuild where datetime >" + " '" +dateFormat.format(cal.getTime())+ "'" + " and datetime < '" + dateFormat.format(date)+ "'" + " and status =1 )) tempBI on cc.buildinfo_id = tempBI.id inner join nightlybuild ni on tempBI.nightlybuild_id = ni.id group by nightlybuild_id;";
	  String sql = "select bi.buildnumber,round(cc.linesofcode) linesCoverage from buildinfo bi inner join codecoverage cc on bi.id = cc.buildinfo_id where bi.datetime >" + " '" +dateFormat.format(cal.getTime())+ "'" + " and bi.datetime < '" + dateFormat.format(date)+ "'" + " and bi.project_id = " + this.projectid + " and bi.nightlybuild_id is not NULL;";
		
		return executeQuery(sql);
	}
	
	
	public List<Map<String, Object>> getccspecificbldno(int buildnumber) throws SQLException, ClassNotFoundException{
	//	String sql = "select bi.buildnumber,ut.total,ut.pass,ut.fail,ut.skip from buildinfo bi inner join codecoverage cc on bi.id = ut.buildinfo_id where bi.project_id = " + this.projectid + " and bi.buildnumber = " + buildnumber + " and bi.nightlybuild_id is not NULL;";
//	String sql = "select ROUND(cc.linesofcode) coverage from (select max(id) id from buildinfo where project_id = " + this.projectid + " and nightlybuild_id != 'NULL' and buildnumber = " + buildnumber + ") tempbi inner join codecoverage cc on cc.buildinfo_id = tempbi.id;";
		
	String sql = "select cc.linesofcode coverage from nightlybuild nt inner join buildinfo bi on nt.id = bi.nightlybuild_id and nt.buildnumber= " + buildnumber + " inner join codecoverage cc on bi.id = cc.buildinfo_id where bi.project_id = " + this.projectid + ";";
	
		return executeQuery(sql);
	}
	
	public List<Map<String, Object>> getMonthCcAggregateDataNightlyBuild() throws SQLException, ClassNotFoundException{
		 DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		 Date date = new Date();
		  System.out.println("current date is :" + dateFormat.format(date) );
	        Calendar cal = Calendar.getInstance();
	        
	        cal.add(Calendar.DATE, -30);
	        System.out.println("last week dats is :" + dateFormat.format(cal.getTime()));
	        
		//String sql = "select ni.buildnumber,round(avg(linesofcode)) linesCoverage from codecoverage cc inner join (select * from buildinfo where nightlybuild_id in (select id from nightlybuild where datetime > '2014-02-18 15:19:01' and datetime < '2014-03-20 01:36:41' and status =1 )) tempBI on cc.buildinfo_id = tempBI.id inner join nightlybuild ni on tempBI.nightlybuild_id = ni.id group by nightlybuild_id;";
	  //      String sql = "select ni.buildnumber,round(avg(linesofcode)) linesCoverage from codecoverage cc inner join (select * from buildinfo where nightlybuild_id in (select id from nightlybuild where datetime >" + " '" +dateFormat.format(cal.getTime())+ "'" + " and datetime < '" + dateFormat.format(date)+ "'" + " and status =1 )) tempBI on cc.buildinfo_id = tempBI.id inner join nightlybuild ni on tempBI.nightlybuild_id = ni.id group by nightlybuild_id;";	        
	        String sql = "select bi.buildnumber,round(cc.linesofcode) linesCoverage from buildinfo bi inner join codecoverage cc on bi.id = cc.buildinfo_id where bi.datetime >" + " '" +dateFormat.format(cal.getTime())+ "'" + " and bi.datetime < '" + dateFormat.format(date)+ "'" + " and bi.project_id = " + this.projectid + " and bi.nightlybuild_id is not NULL;";
	        
		return executeQuery(sql);
	}
	
	public List<Map<String, Object>> getTrendCustomCcData(String todate,String fromdate) throws SQLException, ClassNotFoundException{
	
		String dateString1 = new String(todate);
		String dateString2 = new String(fromdate);
		
		String finalfromdate = null;
		String finaltodate = null;
		
		    java.util.Date dtDate = new Date();
		//	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			SimpleDateFormat sdfAct = new SimpleDateFormat("dd/MM/yyyy");
			try
			{
			dtDate = sdfAct.parse(dateString1);
			System.out.println("Date After parsing in required format:"+(sdf.format(dtDate)));
			finaltodate = (sdf.format(dtDate));
			}
			catch (ParseException e)
			{
			System.out.println("Unable to parse the date string");
			e.printStackTrace();
			}
			
			try
			{
			dtDate = sdfAct.parse(dateString2);
			System.out.println("Date After parsing in required format:"+(sdf.format(dtDate)));
			finalfromdate = (sdf.format(dtDate));
			}
			catch (ParseException e)
			{
			System.out.println("Unable to parse the date string");
			e.printStackTrace();
			}

		    //   String sql = "select ni.buildnumber,round(avg(linesofcode)) linesCoverage from codecoverage cc inner join (select * from buildinfo where nightlybuild_id in (select id from nightlybuild where datetime >" + " '" +finalfromdate+ "'" + " and datetime < '" + finaltodate+ "'" + " and status =1 )) tempBI on cc.buildinfo_id = tempBI.id inner join nightlybuild ni on tempBI.nightlybuild_id = ni.id group by nightlybuild_id;";
			
		       String sql = "select bi.buildnumber,round(cc.linesofcode) linesCoverage from buildinfo bi inner join codecoverage cc on bi.id = cc.buildinfo_id where bi.datetime >" + " '" + finalfromdate + "'" + " and bi.datetime < '" + finaltodate+ "'" + " and bi.project_id = " + this.projectid + " and bi.nightlybuild_id is not NULL;";		
				return executeQuery(sql);
	}
	
}
