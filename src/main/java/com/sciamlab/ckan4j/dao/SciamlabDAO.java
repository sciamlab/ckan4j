package com.sciamlab.ckan4j.dao;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.sciamlab.ckan4j.exceptions.DAOException;
import com.sciamlab.ckan4j.utils.SciamlabStringUtils;

/**
 * 
 * @author SciamLab
 *
 */

public abstract class SciamlabDAO {
	
	private static final Logger logger = Logger.getLogger(SciamlabDAO.class);
	
	protected abstract Connection getConnection() throws SQLException;
	
	public Map<String, Properties> execQuery(String query, String key, List<String> columns){
		Connection connection = null;
		Statement statement = null;
		ResultSet result = null;
		try {
//				String query = "SELECT FROM WHERE";
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(query);
			result = statement.executeQuery(query);
			
			Map<String, Properties> map = new TreeMap<String, Properties>();
			while(result.next()){
				Properties values = new Properties();
				for(String c : columns){
					values.put(c, (result.getString(c)!=null)?result.getString(c):"");
				}
				map.put(result.getString(key),values);
			}
			return map;
			
		} catch (Exception e) {
			throw new DAOException(e);
		} finally{
			if (result != null) try { result.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
		}
	}
	
	/**
	 * Execs a select statement returning a list of record as a Properties object.
	 * Each value is labeled with the column name from the "columns" list
	 * 
	 * @param query
	 * @param columns
	 * @return
	 */
	public List<Properties> execQuery(String query, List<String> columns){
		Connection connection = null;
		Statement statement = null;
		ResultSet result = null;
		try {
//				String query = "SELECT FROM WHERE";
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(query);
			result = statement.executeQuery(query);
			
			List<Properties> map = new ArrayList<Properties>();
			while(result.next()){
				Properties values = new Properties();
				for(String c : columns){
					values.put(c, (result.getString(c)!=null)?result.getString(c):"");
				}
				map.add(values);
			}
			return map;
			
		} catch (Exception e) {
			throw new DAOException(e);
		} finally{
			if (result != null) try { result.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
		}
	}
	
	/**
	 * Execs a select statement returning a list of record as a Properties object.
	 * Each value is labeled with the column name from the select statement
	 * 
	 * @param query
	 * @return
	 */
	public List<Properties> execQuery(String query){
		Connection connection = null;
		Statement statement = null;
		ResultSet result = null;
		try {
//				String query = "SELECT FROM WHERE";
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(query);
			result = statement.executeQuery(query);
			
			ResultSetMetaData metadata = result.getMetaData();
			
			List<Properties> map = new ArrayList<Properties>();
			while(result.next()){
				Properties values = new Properties();
				for(int i=1 ; i<=metadata.getColumnCount() ; i++){
					String c = metadata.getColumnName(i);
					values.put(c, (result.getString(c)!=null)?result.getString(c):"");
				}
				map.add(values);
			}
			return map;
			
		} catch (Exception e) {
			throw new DAOException(e);
		} finally{
			if (result != null) try { result.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
		}
	}
	
	/**
	 * Transforms a List of Properties into a JSONArray of JSONObject
	 * 
	 * @param list
	 * @return
	 */
	public JSONArray transformToJSONArray(List<Properties> list) {
		JSONArray array = new JSONArray();
		for(Properties p : list){
			JSONObject gara = new JSONObject();
			for(Object col : p.keySet()){ //columns){
				String val = p.getProperty((String)col).trim();
				gara.put((String)col, (!"".equals(val)) ? val : "-");
			}
			array.add(gara);
		}
		return array;
	}

	/**
	 * Transforms a List of Properties into a JSONArray of JSONObject ordered using the "columns" list
	 * 
	 * @param list
	 * @param columns
	 * @return
	 */
	public JSONArray transformToJSONArray(List<Properties> list, List<String> columns) {
		JSONArray array = new JSONArray();
		for(Properties p : list){
			JSONObject json = new JSONObject();
			for(String col : columns){
				String val = p.getProperty((String)col);
				json.put(col, (val!=null && !"".equals(val)) ? val.trim() : "-");
			}
			array.add(json);
		}
		return array;
	}

	/**
	 * Execs a SQL update
	 * 
	 * @param stm
	 * @return
	 */
	public int execUpdate(String stm){
		Connection connection = null;
		Statement statement = null;
		try {
//				String query = "SELECT FROM WHERE";
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(stm);
			
			return statement.executeUpdate(stm);
			
		} catch (Exception e) {
			logger.error(SciamlabStringUtils.stackTraceToString(e));
			throw new DAOException(e);
		} finally{
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
		}
	}
	
	/**
	 * Execs a batch SQL update
	 * 
	 * @param stmList
	 * @return
	 */
	public int[] execBatchUpdate(List<String> stmList){
		Connection connection = null;
		Statement statement = null;
		try {
//				String query = "SELECT FROM WHERE";
			connection = this.getConnection();
			statement = connection.createStatement();
			for(String stm : stmList){
				logger.debug(stm);
				statement.addBatch(stm);
			}
			
			return statement.executeBatch();
			
		} catch (Exception e) {
			logger.error(SciamlabStringUtils.stackTraceToString(e));
			throw new DAOException(e);
		} finally{
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(SciamlabStringUtils.stackTraceToString(e)); }
		}
	}
	
}
