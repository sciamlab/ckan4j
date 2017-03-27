package com.sciamlab.ckan4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.json.JSONArray;
import org.json.JSONException;

import com.sciamlab.ckan4j.exception.CKANException;
import com.sciamlab.common.exception.SciamlabException;
import com.sciamlab.common.model.mdr.EUNamedAuthorityVocabulary;
import com.sciamlab.common.model.mdr.EUNamedAuthorityVocabularyMap;
import com.sciamlab.common.model.mdr.vocabulary.EUNamedAuthorityDataTheme;
import com.sciamlab.common.util.SciamlabStreamUtils;
import com.sciamlab.common.util.SciamlabVelocityHelper;

public class CKANSitemapGenerator {
	
	private static final Logger logger = Logger.getLogger(CKANSitemapGenerator.class);
	
	private final List<String> portal_languages;
	private static final int max_number_of_entries_per_file = 50000;
	private static final SciamlabVelocityHelper VELOCITY = new SciamlabVelocityHelper.Builder(new Properties() {{
			put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
			put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, "Log4JLogChute");
			put(RuntimeConstants.RUNTIME_LOG, "/tmp/velocity.log");
		}}).build();

	private final String sitemap_template ;
	private final CKANApiClient client;
	private final String ckan_portal_baseurl;

	public static class Builder{
		private final CKANApiClient client;
		private final String ckan_portal_baseurl;
		private final List<String> portal_languages = new ArrayList<String>(){{add("");}};
		private String sitemap_template_file = "sitemap-template.xml";
		private String sitemap_template;
		
		public Builder(String portal_base_url, CKANApiClient client){
			this.ckan_portal_baseurl = portal_base_url;
			this.client = client;
		}
		
		public Builder sitemap_template_file(String template){
			this.sitemap_template_file = template;
			return this;
		}
		
		public Builder language(String lang){
			this.portal_languages.add(lang);
			return this;
		}
		
		public Builder languages(List<String> langs){
			this.portal_languages.addAll(langs);
			return this;
		}
		
		public CKANSitemapGenerator build() throws FileNotFoundException, SciamlabException{
			EUNamedAuthorityVocabularyMap.load(EUNamedAuthorityVocabulary.DATA_THEME);
			this.sitemap_template = SciamlabStreamUtils.convertStreamToString(SciamlabStreamUtils.getInputStream(this.sitemap_template_file));
			return new CKANSitemapGenerator(this);
		}
	}
	
	private CKANSitemapGenerator(Builder builder) {
		this.sitemap_template = builder.sitemap_template;
		this.client = builder.client;
		this.ckan_portal_baseurl = builder.ckan_portal_baseurl;
		this.portal_languages = builder.portal_languages;
	}
	
	public List<StringBuffer> generate(String name, String sitemap_baseurl) throws UnsupportedEncodingException, JSONException, CKANException, SciamlabException{
		List<StringBuffer> list = new ArrayList<StringBuffer>();

		String fileName = name+".xml";
		StringBuffer main = new StringBuffer();
		list.add(main);
		main.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		main.append("\n<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		
		List<String> urls;
		if("organization".equals(name))
			urls = getOrganizationsURL();
		else if("package".equals(name))
			urls = getPackagesURL();
		else if("tag".equals(name))
			urls = getTagsURL();
		else if("theme".equals(name))
			urls = getThemesURL();
		else
			throw new SciamlabException("sitemap name not recognized");
		
		/*
		 * considering each url is replicated for each language
		 * the page size is evaluated as the max number of items per page divided by the number of languages
		 */
		final int page_size = max_number_of_entries_per_file/portal_languages.size();
		/*
		 * the number of pages is the number of urls divided by the page size
		 * plus 1 if the rest is not 0
		 */
		int pages = urls.size()/page_size;
		if(urls.size()%page_size!=0)
			pages++;
		int i=0;
		int start=i;
		int end = page_size>urls.size() ? urls.size() : page_size;
		while(true){
			StringBuffer xml = generate(urls.subList(start, end));
			
			Properties props = new Properties();
			props.put("body", xml.toString());
			String out = VELOCITY.getTemplateFromString(props, sitemap_template);
			list.add(new StringBuffer(out));
			
			main.append("\n\t<sitemap>");
			main.append("\n\t\t<loc>"+sitemap_baseurl+"/"+fileName+"/"+(i+1)+"</loc>");
			main.append("\n\t</sitemap>");
			
			i++;
			start = start + page_size;
			if(start>=urls.size())
				break;
			end = end+page_size>urls.size() ? urls.size() : end+page_size;
		}
		main.append("\n</sitemapindex>");
		return list;
	}
	
	private StringBuffer generate(List<String> urls) {
		
		StringBuffer b = new StringBuffer();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String currentDate = df.format(new Date());
		
		for (String url: urls){
			for (String lang : portal_languages) {
				String portalUrl = ckan_portal_baseurl + (!"".equals(lang) ? ("/" + lang) : "") + url;
				b.append("<url>\n");
				b.append("\t<loc>" + portalUrl + "</loc>\n");
				for (String lang_again : portal_languages) {
					if("".equals(lang_again))
						continue;
					String portalLocUrl = ckan_portal_baseurl + "/" + lang_again + url;
					b.append("\t<xhtml:link rel=\"alternate\" hreflang=\"" + lang_again + "\" href=\"" + portalLocUrl + "\"/>\n");
				}
				b.append("\t<lastmod>" + currentDate + "+00:00</lastmod>\n");
				b.append("\t<changefreq>monthly</changefreq>\n");
				b.append("\t<priority>0.8000</priority>\n");
				b.append("</url>\n");
			}
		}
		return b;
	}
	
	private List<String> getOrganizationsURL() throws CKANException, UnsupportedEncodingException, JSONException {
		List<String> list = new ArrayList<String>();
		
		JSONArray jarr = client.organizationList();
				
		for (int i = 0; i < jarr.length(); i++) {
			String url = "/organization/" + URLEncoder.encode(jarr.getString(i), "UTF-8");
			list.add(url);
		}
		
		return list;
	}
	
	private List<String> getPackagesURL() throws CKANException, UnsupportedEncodingException, JSONException {
		List<String> list = new ArrayList<String>();
		
		JSONArray jarr = client.packageList();
		
		for (int i = 0; i < jarr.length(); i++) {
			String url = "/dataset/" + URLEncoder.encode(jarr.getString(i), "UTF-8");
			list.add(url);
		}
		
		return list;
	}
	
	private List<String> getTagsURL() throws CKANException, UnsupportedEncodingException, JSONException {
		List<String> list = new ArrayList<String>();
		
		JSONArray jarr = client.tagList();
		
		for (int i = 0; i < jarr.length(); i++) {
			String url = "/dataset?tags=" + URLEncoder.encode(jarr.getString(i), "UTF-8");
			list.add(url);
		}
		
		return list;
	}
	
	private List<String> getThemesURL() throws CKANException, UnsupportedEncodingException, JSONException {
		List<String> list = new ArrayList<String>();
		
//		for (EUNamedAuthorityDataTheme.Theme theme : Arrays.asList(EUNamedAuthorityDataTheme.Theme.values())) {
		for (EUNamedAuthorityDataTheme theme : EUNamedAuthorityVocabularyMap.<EUNamedAuthorityDataTheme>get(EUNamedAuthorityVocabulary.DATA_THEME).values()) {
			String url = "/dataset?groups=" + theme.authority_code.toLowerCase();
			list.add(url);
		}
		
		return list;
	}
	
}
