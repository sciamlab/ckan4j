/**
 * Copyright 2014 Sciamlab s.r.l.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sciamlab.ckan4j.dao;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.sciamlab.ckan4j.exception.DAOException;
import com.sciamlab.ckan4j.exception.InternalServerErrorException;
import com.sciamlab.ckan4j.util.SciamlabStreamUtils;

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
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(query);
			result = statement.executeQuery(query);
			
			Map<String, Properties> map = new TreeMap<String, Properties>();
			while(result.next()){
				LinkedProperties values = new LinkedProperties();
				for(String c : columns){
					values.put(c, (result.getString(c)!=null)?result.getString(c):"");
				}
				map.put(result.getString(key),values);
			}
			return map;
			
		} catch (Exception e) {
			throw new DAOException(e);
		} finally{
			if (result != null) try { result.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
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
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(query);
			result = statement.executeQuery(query);
			
			List<Properties> map = new ArrayList<Properties>();
			while(result.next()){
				LinkedProperties values = new LinkedProperties();
				for(String c : columns){
					values.put(c, (result.getString(c)!=null)?result.getString(c):"");
				}
				map.add(values);
			}
			return map;
			
		} catch (Exception e) {
			throw new DAOException(e);
		} finally{
			if (result != null) try { result.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
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
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(query);
			result = statement.executeQuery(query);
			
			ResultSetMetaData metadata = result.getMetaData();
			
			List<Properties> map = new ArrayList<Properties>();
			while(result.next()){
				LinkedProperties values = new LinkedProperties();
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
			if (result != null) try { result.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
		}
	}
	
	/**
	 * Execs a select statement reading from InputStream returning a list of record as a Properties object.
	 * Each value is labeled with the column name from the select statement
	 * 
	 * @param query
	 * @return
	 */
	public List<Properties> execQuery(InputStream is){
		List<Properties> result = new ArrayList<Properties>();
		try {
			result = this.execQuery(SciamlabStreamUtils.convertStreamToString(is).replaceAll("\n", " "));
		}finally{
			if(is != null) try { is.close(); } catch (IOException e) { logger.error(e.getMessage(), e);	}
		}
		return result;
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
			connection = this.getConnection();
			statement = connection.createStatement();
			logger.debug(stm);
			
			return statement.executeUpdate(stm);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new DAOException(e);
		} finally{
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
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
			connection = this.getConnection();
			statement = connection.createStatement();
			for(String stm : stmList){
				logger.debug(stm);
				statement.addBatch(stm);
			}
			
			return statement.executeBatch();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new DAOException(e);
		} finally{
	        if (statement != null) try { statement.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
	        if (connection != null) try { connection.close(); } catch (SQLException e) { logger.error(e.getMessage(), e); }
		}
	}
	
	public class LinkedProperties extends Properties {

	    private static final long serialVersionUID = 1L;
		private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

	    public Enumeration<Object> keys() {
	        return Collections.<Object>enumeration(keys);
	    }
	    
	    public Set<Object> keySet() {
	        return keys;
	    }

	    public Object put(Object key, Object value) {
	        keys.add(key);
	        return super.put(key, value);
	    }
	}
	
}
