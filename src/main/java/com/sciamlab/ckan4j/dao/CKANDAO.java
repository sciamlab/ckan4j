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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sciamlab.common.dao.SciamlabDAO;

/**
 * 
 * @author SciamLab
 *
 */

public abstract class CKANDAO extends SciamlabDAO{
	
	private static final Logger logger = Logger.getLogger(CKANDAO.class);

	/**
	 * 
	 * @return
	 */
	public Map<String, Integer> getCategoriesCount() {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// Execute the query
			rs = stmt.executeQuery("select pe.value as category_id, count(*) as count"
					+ " from package_extra as pe join package as p on pe.package_id=p.id"
					+ " where pe.key='category_id' and p.private=false and p.state='active' group by pe.value;");

			// Loop through the result set
			while (rs.next())
				map.put(rs.getString("category_id"),rs.getInt("count"));

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return map;
	}
	
	/**
	 * 
	 * @param n
	 * @return
	 */
	public Map<String, Integer> getTagsMapLimitTo(int n) {
		Map<String, Integer> map_top40 = new LinkedHashMap<String, Integer>();
		Map<String, Integer> map = getTagsMapSorted();
		int i = 1;
		for(String s : map.keySet()){
			map_top40.put(s, map.get(s));
			if(i>=n) break;
			i++;
		}
		return map_top40;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, Integer> getTagsMapSorted() {
		return sortByValue(getTagsMap());
	}
	
	/**
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, Integer> sortByValue(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {

			public int compare(Map.Entry<String, Integer> m1, Map.Entry<String, Integer> m2) {
				return (m2.getValue()).compareTo(m1.getValue());
			}
		});

		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, Integer> getTagsMap() {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// Execute the query
			rs = stmt.executeQuery("select name, count from " +
					"(select tag_id, count(*) count from package_tag t join package p on t.package_id=p.id AND p.state='active' AND t.state='active' group by tag_id) as tags " +
					"join tag on tag.id = tags.tag_id order by count desc; ");

			// Loop through the result set
			while (rs.next())
				map.put(rs.getString("name"),rs.getInt("count"));

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return map;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String,Map<String,Integer>> getEurovocMapData() {
		
		Map <String,Map<String,Integer>> map = new HashMap<String,Map<String,Integer>>();
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// Execute the query
			rs = stmt.executeQuery( "select replace(eurovoc_theme,'\"','') as eurovoc_theme, " +
									"   replace(eurovoc_microtheme,'\"','') as eurovoc_microtheme,  " +
									"   count(eurovoc_microtheme) as microtheme_count " +
									"from eurovoc_view group by eurovoc_theme,eurovoc_microtheme "+ 
									"order by eurovoc_theme,eurovoc_microtheme");
			// Loop through the result set
			Map <String,Integer> eurovocMicrotheme = new LinkedHashMap<String, Integer>();
			String previousTheme="";
			while (rs.next()) {
				String theme = rs.getString("eurovoc_theme");
				if (!theme.equals(previousTheme)) {
					eurovocMicrotheme = new LinkedHashMap<String, Integer>();
				} 
				eurovocMicrotheme.put(rs.getString("eurovoc_microtheme"), rs.getInt("microtheme_count"));
				map.put(theme, eurovocMicrotheme);
				previousTheme = theme;
			}
			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}
		return map;	
		
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, Integer> getCatalogsMap() {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// Execute the query
			rs = stmt.executeQuery("select g.title, p.c from public.group g join " +
					"(select owner_org, count(*) as c from package where state = 'active' group by owner_org) p " +
					"on g.id = p.owner_org where g.state = 'active' and g.type = 'organization' order by p.c desc;;");

			// Loop through the result set
			while (rs.next())
				map.put(rs.getString("title"),rs.getInt("c"));

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return map;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getOrgCount() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		Integer count = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// Execute the query
			rs = stmt.executeQuery("select count(*) as count from \"group\" where type = 'organization' and state='active';");

			// Loop through the result set
			while (rs.next())
				count = rs.getInt("count");

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return count;
	}

	/**
	 * 
	 * @param orgName
	 * @return
	 */
	public String getOrgId(String orgName) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		String orgId = null;

		try {
			connection = getConnection();
			statement = connection.prepareStatement("select id from \"group\" where lower(title) = ?");
			statement.setString(1, orgName.toLowerCase());
			logger.debug(statement);
			statement.execute();
			rs = statement.getResultSet();

			// pick the result set
			if (rs.next())
				orgId = rs.getString("id");

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(statement!=null) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(connection!=null) try { connection.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return orgId;
	}	

	/**
	 * 
	 * @param orgName
	 * @return
	 */
	public  String getOrgLastUpdate(String orgName) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String orgLastUpdate = null;

		try {
			conn = getConnection();
			stmt = conn.prepareStatement("select to_char(max(revision_timestamp),'DD-MM-YYYY HH24:MI') as last_update"
					+ " from package_revision where owner_org in (select id from \"group\" where name = ?)");
			stmt.setString(1, orgName.toLowerCase());
			logger.debug(stmt);
			stmt.execute();
			rs = stmt.getResultSet();

			// pick the result set
			if (rs.next())
				orgLastUpdate = rs.getString("last_update");

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return orgLastUpdate;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getDatasetCount() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		Integer count = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// Execute the query
			rs = stmt.executeQuery("select count(*) count from package where state='active';");

			// Loop through the result set
			while (rs.next())
				count = rs.getInt("count");

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return count;
	}

	public Map<String, String> getOrgLastUpdateMap() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			// Execute the query
			rs = stmt.executeQuery("SELECT \"group\".name, "+ 
					"COALESCE(to_char(max(package_revision.revision_timestamp),'DD-MM-YYYY HH24:MI'),'non disponibile') as last_revision "+
					"FROM "+ 
					"  \"group\" left join "+ 
					"  package_revision on \"group\".id = package_revision.owner_org "+
					"WHERE " +
					"  \"group\".is_organization = true AND "+
					"  \"group\".state = 'active' " +
					"GROUP BY "+
					"  \"group\".name "+
					"ORDER BY "+
					"  \"group\".name");

			// Loop through the result set
			while (rs.next())
				map.put(rs.getString("name"),rs.getString("last_revision"));

			// Close the result set, statement and the connection

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
		}

		return map;
	}
	
}
