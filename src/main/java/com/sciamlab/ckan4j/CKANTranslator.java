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
package com.sciamlab.ckan4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.sciamlab.ckan4j.dao.CKANDAO;
import com.sciamlab.ckan4j.exception.DAOException;

public class CKANTranslator {
	
	private CKANDAO dao;
	
	private CKANTranslator(CKANTranslatorBuilder builder) {
		this.dao = builder.dao;
	}
	
	/**
	 * Inserts a translation into CKAN for the given language
	 * 
	 * @param term_text
	 * @param lang_code
	 * @param term_translation
	 */
	public void translate(String term, String lang_code, String term_translation){
		String updateTableSQL = "UPDATE term_translation SET term_translation='"+term_translation+"'"
				+ " WHERE term='"+term+"' AND lang_code='"+lang_code+"'";
		
		// execs the update SQL statement
		int row_count = dao.execUpdate(updateTableSQL);
		if(row_count>1){
			throw new DAOException("UNIQUE constraint on [term, lang_code] violated for ["+term+", "+lang_code+"]. Updated "+row_count+" rows!");
		}else if(row_count==1){
			//the row has been updated
			return;
		}
		
		//the row is not present in the database, so it is inserted
		String insertTableSQL = "INSERT INTO term_translation"
				+ "(term, lang_code, term_translation)" 
				+ " VALUES "
				+ "('"+term+"', '"+lang_code+"', '"+term_translation+"')";
		dao.execUpdate(insertTableSQL);
	}
	
	/**
	 * Retrieves all the terms with related translation.
	 * The result is filtered using the given query and/or language, if any.
	 * The result is paginated using page_num and page_size that must be positive or NULL for no pagination.
	 * NOTE: the number of terms could be huge so the usage of pagination options is suggested.
	 * 
	 * @param q
	 * @param lang_code
	 * @param translated
	 * @param not_translated
	 * @param package_extra_keys
	 * @param page_num
	 * @param page_size
	 * @return the result as map indexed by the term in the original language
	 */
	public Map<String, Properties> getTerms(
			String q, String lang_code, boolean translated, boolean not_translated, List<String> package_extra_keys, Integer page_num, Integer page_size){
		
		String selectTableSQL = "SELECT terms_only.term, lang_code, term_translation"
				+ " FROM ("+generateSQLStatementForTermsList(q.toLowerCase().trim(), package_extra_keys)+") terms_only LEFT JOIN (SELECT * FROM term_translation WHERE lang_code='"+lang_code+"') term_translation_filtered"
				+ " on terms_only.term = term_translation_filtered.term"
				+ " WHERE 1=1"
				+ getTranslatedCondition(translated, not_translated)
				+ ((page_num!=null && page_size!=null && page_num>0 && page_size>0)?" LIMIT "+page_size+" OFFSET "+(page_size*(page_num-1)):"")
				+ ";";
		
		// execs the select SQL statement
		Map<String, Properties> result = dao.execQuery(selectTableSQL, "term", 
				new ArrayList<String>(){{ add("term"); add("lang_code"); add("term_translation"); }});
		
		return result;
	}
	
	/**
	 * Retrieves the terms count
	 * 
	 * @param q
	 * @param lang_code
	 * @param translated
	 * @param not_translated
	 * @param package_extra_keys
	 * @return the result as map indexed by the term in the original language
	 */
	public int getTermsCount(
			String q, String lang_code, boolean translated, boolean not_translated, List<String> package_extra_keys){
		
		String selectTableSQL = "SELECT count(terms_only.term) as terms_count"
				+ " FROM ("+generateSQLStatementForTermsList(q.toLowerCase().trim(), package_extra_keys)+") terms_only LEFT JOIN (SELECT * FROM term_translation WHERE lang_code='"+lang_code+"') term_translation_filtered"
				+ " on terms_only.term = term_translation_filtered.term"
				+ " WHERE 1=1"
				+ getTranslatedCondition(translated, not_translated)
				+ ";";
		
		// execs the select SQL statement
		List<Properties> result = dao.execQuery(selectTableSQL, 
				new ArrayList<String>(){{ add("terms_count"); }});
		int count = Integer.parseInt(result.get(0).getProperty("terms_count"));
		return count;
	}
	
	/**
	 * Build the body of the select statement for getting the terms list
	 * 
	 * @param q
	 * @param package_extra_keys
	 * @return the SQL statement as String
	 */
	private static String generateSQLStatementForTermsList(String q, List<String> package_extra_keys){
		String related1 = "SELECT DISTINCT title AS term FROM related"
				+ " WHERE title IS NOT NULL AND title <> ''"
				+ ((q!=null && !"".equals(q))?" AND lower(title) like lower('%"+q+"%')":"");
		
		String related2 = "SELECT DISTINCT description AS term FROM related"
				+ " WHERE description IS NOT NULL AND description <> ''"
				+ ((q!=null && !"".equals(q))?" AND lower(description) like lower('%"+q+"%')":"");
		
		String groups1 = "SELECT DISTINCT title AS term FROM \"group\""
				+ " WHERE title IS NOT NULL AND title <> ''"
				+ ((q!=null && !"".equals(q))?" AND lower(title) like lower('%"+q+"%')":"");
		
		String groups2 = "SELECT DISTINCT description AS term FROM \"group\""
				+ " WHERE description IS NOT NULL AND description <> ''"
				+ ((q!=null && !"".equals(q))?" AND lower(description) like lower('%"+q+"%')":"");
		
		String tags = "SELECT DISTINCT name AS term FROM tag"
				+ " WHERE name IS NOT NULL AND name <> ''"
				+ ((q!=null && !"".equals(q))?" AND lower(name) like lower('%"+q+"%')":"");
		
		String resources = "SELECT DISTINCT description AS term FROM resource"
				+ " WHERE description IS NOT NULL AND description <> ''"
				+((q!=null && !"".equals(q))?" AND lower(description) like lower('%"+q+"%')":"");
		
		String resources2 = "SELECT DISTINCT name AS term FROM resource"
				+ " WHERE name IS NOT NULL AND name <> ''"
				+((q!=null && !"".equals(q))?" AND lower(name) like lower('%"+q+"%')":"");
		
		String packages2 = "SELECT DISTINCT title AS term FROM package"
				+ " WHERE title IS NOT NULL AND title <> ''"
				+((q!=null && !"".equals(q))?" AND lower(title) like lower('%"+q+"%')":"");
		
		String packages3 = "SELECT DISTINCT notes AS term FROM package"
				+ " WHERE notes IS NOT NULL AND notes <> ''"
				+((q!=null && !"".equals(q))?" AND lower(notes) like lower('%"+q+"%')":"");
		
		String package_extras = "SELECT DISTINCT value AS term FROM package_extra"
				+ " WHERE value IS NOT NULL AND value <> ''"
				+ ((q!=null && !"".equals(q))?" AND lower(value) like lower('%"+q+"%')":"");
		if(package_extra_keys!=null && !package_extra_keys.isEmpty()){
			package_extras += " AND key IN (";
			for(String key : package_extra_keys){
				package_extras += "'"+key+"',";
			}
			package_extras = package_extras.substring(0, package_extras.length()-1);
			package_extras += ")";
		}
		
		String selectTableSQL = "SELECT DISTINCT term, lower(term) FROM ("
				+ related1 +" UNION "
				+ related2 +" UNION "
				+ groups1 +" UNION "
				+ groups2 +" UNION "
				+ tags +" UNION "
				+ resources +" UNION "
				+ resources2 +" UNION "
				+ packages2 +" UNION "
				+ packages3 +" UNION "
				+ package_extras
				+ ") terms_only ORDER BY lower(term)";
		
		return selectTableSQL;
	}
	
	private static String getTranslatedCondition(boolean translated, boolean not_translated){
		String translated_clause = "";
		if(translated && !not_translated){
			translated_clause = " AND term_translation IS NOT NULL";
		}else if(!translated && not_translated){
			translated_clause = " AND term_translation IS NULL";
		}else if(!translated && !not_translated){
			translated_clause = " AND 1=0";
		}
		return translated_clause;
	}
	
	public static class CKANTranslatorBuilder{
		
		private final CKANDAO dao;
		
		public static CKANTranslatorBuilder getInstance(CKANDAO dao){
			return new CKANTranslatorBuilder(dao);
		}
		
		private CKANTranslatorBuilder(CKANDAO dao) {
			super();
			this.dao = dao;
		}

		public CKANTranslator build() {
			return new CKANTranslator(this);
		}
	}
	
	public static void main(String[] args) {
		new CKANTranslatorBuilder(null).build().getTerms("", "en", true, true, new ArrayList<String>(){{add("microtheme");add("theme");}}, 1, 50);
	}

}
