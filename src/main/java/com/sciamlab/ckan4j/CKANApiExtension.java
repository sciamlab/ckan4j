package com.sciamlab.ckan4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sciamlab.common.dao.SciamlabDAO;

public class CKANApiExtension {

	private static final Logger logger = Logger.getLogger(CKANApiExtension.class);
	
	private SciamlabDAO dao;
	
	private CKANApiExtension(CKANApiExtensionBuilder builder) {
		this.dao = builder.dao;
	}
	
	public int getDatasetCount() {
		return ((Long)dao.execQuery("select count(*) count from package where state='active';").get(0).get("count")).intValue();
	}
	
	/**
	 * Return the dataset count but based on opendata license
	 * in case of licenses that identify not opendata these are counted
 	 * as not open datasets
	 */
	public Map<String,String> getDatasetStats() {
		Map <String,String> map = new HashMap<String,String>();
		List<Properties> res = dao.execQuery( "select * from stats_dataset_view;");
		for(Properties p : res){
			map.put(p.getProperty("type"), p.getProperty("dataset_nr"));
		}
		return map;
	}
	
//	/**
//	 * 
//	 * @return
//	 */
//	public Map<String, Integer> getCategoriesCount() {
//		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
//		Connection conn = null;
//		Statement stmt = null;
//		ResultSet rs = null;
//		try {
//			conn = getConnection();
//			stmt = conn.createStatement();
//			// Execute the query
//			rs = stmt.executeQuery("select pe.value as category_id, count(*) as count"
//					+ " from package_extra as pe join package as p on pe.package_id=p.id"
//					+ " where pe.key='category_id' and p.private=false and p.state='active' group by pe.value;");
//
//			// Loop through the result set
//			while (rs.next())
//				map.put(rs.getString("category_id"),rs.getInt("count"));
//
//			// Close the result set, statement and the connection
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally{
//			if(rs!=null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
//			if(stmt!=null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
//			if(conn!=null) try { conn.close(); } catch (SQLException e) { e.printStackTrace();	}
//		}
//
//		return map;
//	}
//	
	
	/**
	 * This method returns a map of the tags with the related count
	 *  
	 * @param limit, use -1 to skip the limit filter
	 * @return
	 */
	public Map<String, Integer> getTagsCount(final int limit) {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		List<Properties> res = (limit!=-1) 
				? dao.execQuery("select name, count from " +
					"(select tag_id, count(*) count from package_tag t join package p on t.package_id=p.id AND p.state='active' AND t.state='active' group by tag_id) as tags " +
					"join tag on tag.id = tags.tag_id order by count desc limit ?;", new ArrayList<Object>(){{add(limit);}})
				: dao.execQuery("select name, count from " +
					"(select tag_id, count(*) count from package_tag t join package p on t.package_id=p.id AND p.state='active' AND t.state='active' group by tag_id) as tags " +
					"join tag on tag.id = tags.tag_id order by count desc;");
		for(Properties p : res){
			map.put(p.getProperty("name"), ((Long) p.get("count")).intValue());
		}
		return map;
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String,Map<String,Integer>> getEurovocMapData() {
		
		Map <String,Map<String,Integer>> map = new HashMap<String,Map<String,Integer>>();
		
		List<Properties> res = dao.execQuery( "select * from eurovoc_view;");
		Map <String,Integer> eurovocMicrotheme = new LinkedHashMap<String, Integer>();
		for(Properties p : res){
			String theme = p.getProperty("eurovoc_theme");
			if(!map.containsKey(theme))
				map.put(theme, new LinkedHashMap<String, Integer>());
			map.get(theme).put(p.getProperty("eurovoc_microtheme"), ((Long)p.get("c")).intValue());
		}
		return map;	
	}
	
	/**
	 * Returns the id of the organization with the given name
	 * 
	 * @param name
	 * @return
	 */
	public String getOrganizationId(final String name) {
		List<Properties> res = dao.execQuery("select id from \"group\" where lower(title) = ?", new ArrayList<Object>(){{add(name);}});
		return (res.size()==0) ? null : res.get(0).getProperty("id");
	}	
	
	/**
	 * Returns a map of all the organization with the related count

	 * @return
	 */
	public Map<String, Integer> getOrganizationCountMap() {
		Map<String, Integer> map = new LinkedHashMap<String, Integer>();
		List<Properties> res = dao.execQuery("select g.name, p.count from public.group g join " +
					"(select owner_org, count(*) as count from package where state = 'active' group by owner_org) p " +
					"on g.id = p.owner_org where g.state = 'active' and g.type = 'organization' order by p.count desc;"); 
		for(Properties p : res){
			map.put(p.getProperty("name"), ((Long) p.get("count")).intValue());
		}
		return map;
	}
	
	/**
	 * Returns the package count of the organization corresponding to the given name 

	 * @return
	 */
	public Integer getOrganizationCount(String name) {
		final String id = getOrganizationId(name);
		if(id == null)
			return null;
		List<Properties> res =dao.execQuery("select p.count from public.group g join " +
				"(select owner_org, count(*) as count from package where state = 'active' group by owner_org) p " +
				"on g.id = p.owner_org where g.state = 'active' and g.type = 'organization' AND owner_org = ?", new ArrayList<Object>(){{add(id);}});
		return (res.size()==0) ? null : ((Long)res.get(0).get("count")).intValue();
	}

	
	/**
	 * Returns a map of all the organization with the related last update date
	 * 
	 * @return
	 */
	public Map<String, Date> getOrganizationLastUpdateMap() {
		Map<String, Date> map = new LinkedHashMap<String, Date>();
		List<Properties> res = dao.execQuery("SELECT * FROM (SELECT \"group\".name, "+ 
					"max(package.metadata_modified) as last_revision "+
					"FROM \"group\" left join package on \"group\".id = package.owner_org "+
					"WHERE \"group\".is_organization = true AND \"group\".state = 'active'" +
					"GROUP BY \"group\".name ORDER BY last_revision desc) t WHERE last_revision IS NOT NULL;");
		for(Properties p : res){
			map.put(p.getProperty("name"), (Date)p.get("last_revision"));
		}
		return map;
	}

	/**
	 * Returns the last update date of the organization corresponding to the given name 
	 * 
	 * @param name
	 * @return
	 */
	public Date getOrganizationLastUpdate(String name) {
		final String id = getOrganizationId(name);
		if(id == null)
			return null;
		List<Properties> res = dao.execQuery("select max(metadata_modified) as last_update"
					+ " from package where owner_org = ?", new ArrayList<Object>(){{add(id);}});
		return (Date) ((res.size()==0) ? null : res.get(0).get("last_update"));
	}
	
    public List<App> getApps(String query, List<String> types, String lang) {
		List<Object> params = new ArrayList<Object>();
		List<String> columns = new ArrayList<String>(){{ add("type");add("title");add("description");add("url");add("image_url"); }};
		String select_apps;
		if(lang==null || "".equals(lang))
			select_apps = "SELECT r.*"
					+ " FROM related r"
					+ " JOIN related_dataset rd on r.id=rd.related_id"
					+ " JOIN package p on rd.dataset_id=p.id"
					+ " WHERE p.state='active' ";
		else{
			select_apps = "SELECT r.*,tt1.term_translation AS trans_title, tt2.term_translation AS trans_description"
					+ " FROM related r"
					+ " JOIN related_dataset rd on r.id=rd.related_id"
					+ " JOIN package p on rd.dataset_id=p.id"
					+ " LEFT JOIN (SELECT * FROM term_translation WHERE lang_code = ?) tt1 ON r.title=tt1.term"
					+ " LEFT JOIN (SELECT * FROM term_translation WHERE lang_code = ?) tt2 ON r.description=tt2.term"
					+ " WHERE p.state='active' ";
			params.add(lang);
			params.add(lang);
//				columns.add("trans_title");
//				columns.add("trans_description");
		}
		if(types!=null && !types.isEmpty()){
			select_apps += " AND (";
			boolean first = true;
			for(String t : types){
				if(first)
					first = false;
				else
					select_apps += " OR ";
				select_apps += "type = ?";
				params.add(t);
			}
			select_apps += ")";
		}

		Set<String> qList = new HashSet<String>();
		if(query!=null && !"".equals(query.trim()))
			qList.addAll(Arrays.asList(query.toLowerCase().trim().split(" ")));
		if(qList!=null && !qList.isEmpty()){
			select_apps += " AND (";
			boolean first = true;
			for(String q : qList){
				if(first)
					first = false;
				else
					select_apps += " OR ";
				if(lang==null || "".equals(lang))
					select_apps += "lower(r.title) LIKE ? OR lower(r.description) LIKE ?";
				else
					select_apps += "lower(tt1.term_translation) LIKE ? OR lower(tt2.term_translation) LIKE ?";
				params.add("%"+q+"%");
				params.add("%"+q+"%");
			}
			select_apps += ")";
		}
		List<Properties> res = dao.execQuery(select_apps, params, columns);
		List<App> apps = new ArrayList<App>();
		for(Properties p : res){
			apps.add(new App(p.getProperty("type"), p.getProperty("title"), p.getProperty("description"), p.getProperty("url"), p.getProperty("image_url")));
		}
		return apps;
	}
    
    public static class App{
    	public String type;
    	public String title;
    	public String description;
    	public String url;
    	public String image_url;
		public App(String type, String title, String description, String url,
				String image_url) {
			super();
			this.type = type;
			this.title = title;
			this.description = description;
			this.url = url;
			this.image_url = image_url;
		}
    }
	
	public static class CKANApiExtensionBuilder{
		
		private final SciamlabDAO dao;
		
		public static CKANApiExtensionBuilder getInstance(SciamlabDAO dao){
			return new CKANApiExtensionBuilder(dao);
		}
		
		private CKANApiExtensionBuilder(SciamlabDAO dao) {
			super();
			this.dao = dao;
		}

		public CKANApiExtension build() {
			return new CKANApiExtension(this);
		}
	}
}
