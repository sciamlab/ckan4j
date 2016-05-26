package com.sciamlab.ckan4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import com.sciamlab.ckan4j.exception.CKANException;
import com.sciamlab.common.model.mdr.vocabulary.EUNamedAuthorityDataTheme;
import com.sciamlab.common.util.SciamlabMailUtils.SciamlabVelocityHelper;
import com.sciamlab.common.util.SciamlabStreamUtils;

public class CKANSitemapGenerator {
	
	private static final Logger logger = Logger.getLogger(CKANSitemapGenerator.class);
	
	private final List<String> portal_languages;
	private static final int max_number_of_entries_per_file = 50000;
	
	private final String sitemap_template ;
	private final CKANApiClient client;
	private final String ckan_portal_baseurl;

	public static class Builder{
		private final CKANApiClient client;
		private final String ckan_portal_baseurl;
		private final List<String> portal_languages = new ArrayList<String>();
		private String sitemap_template_file = "sitemap-template.xml";
		
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
		
		public CKANSitemapGenerator build() throws FileNotFoundException{
			return new CKANSitemapGenerator(this);
		}
	}
	
	private CKANSitemapGenerator(Builder builder) throws FileNotFoundException {
		this.sitemap_template = SciamlabStreamUtils.convertStreamToString(SciamlabStreamUtils.getInputStream(builder.sitemap_template_file));
		this.client = builder.client;
		this.ckan_portal_baseurl = builder.ckan_portal_baseurl;
		this.portal_languages = builder.portal_languages;
	}
	
	public void generate() throws IOException, JSONException, CKANException{
		
		StringBuffer main = new StringBuffer();
		main.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		main.append("\n<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put("organization", getOrganizationsURL());
		map.put("package", getPackagesURL());
		map.put("tag", getTagsURL());	
		map.put("theme", getThemesURL());	
		
		for(Entry<String, List<String>> entry : map.entrySet()){
				
			List<StringBuilder> bArr = buildXML(entry.getValue());
			
			for (int i = 0; i < bArr.size(); i++) {
				String fileName = "sitemap_"+entry.getKey()+"_" + i + ".xml";
		
				Properties props = new Properties();
				props.put("body", bArr.get(i).toString());
				String out = SciamlabVelocityHelper.getTemplateFromString(null, props, sitemap_template);
				FileWriter writer = new FileWriter(new File(fileName));
				writer.append(out);
				writer.close();
				
				logger.info(fileName + " generated");
				main.append("\n\t<sitemap>");
				main.append("\n\t\t<loc>"+ckan_portal_baseurl+"/"+fileName+"</loc>");
				main.append("\n\t</sitemap>");
			}
		
		}
		
		main.append("\n</sitemapindex>");
		FileWriter writer = new FileWriter(new File("sitemap.xml"));
		writer.append(main.toString());
		writer.close();
		logger.info("sitemap.xml generated");
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
		
		for (EUNamedAuthorityDataTheme.Theme theme : Arrays.asList(EUNamedAuthorityDataTheme.Theme.values())) {
			String url = "/dataset?extras_dcat-category-id=" + URLEncoder.encode(theme.name(), "UTF-8");
			list.add(url);
		}
		
		return list;
	}
	
	private List<StringBuilder> buildXML(List<String> urls) {
		List<StringBuilder> bArr = new ArrayList<StringBuilder>();
		StringBuilder b = new StringBuilder();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		String currentDate = df.format(new Date());
		
		int num = 0;
		for (String url: urls){
			
			for (String lang : portal_languages) {
				if (num%max_number_of_entries_per_file == max_number_of_entries_per_file-1) {
					logger.debug("Reached maximum number of entries per sitemap file: "+num+". Creating a new file...");
					bArr.add(b);
					b = new StringBuilder();
				}
					
				String portalUrl = ckan_portal_baseurl + "/" + lang + url;
				
				b.append("<url>\n");
				b.append("\t<loc>" + portalUrl + "</loc>\n");
				
				for (String lang_again : portal_languages) {
					String portalLocUrl = ckan_portal_baseurl + "/" + lang_again + url;
					b.append("\t<xhtml:link rel=\"alternate\" hreflang=\"" + lang_again + "\" href=\"" + portalLocUrl + "\"/>\n");
				}
				
				b.append("\t<lastmod>" + currentDate + "+00:00</lastmod>\n");
				b.append("\t<changefreq>monthly</changefreq>\n");
				b.append("\t<priority>0.8000</priority>\n");
				b.append("</url>\n");
				
				num++;
			}
		}
		
		bArr.add(b);
		b = new StringBuilder();
				
		return bArr;
	}
	
}
