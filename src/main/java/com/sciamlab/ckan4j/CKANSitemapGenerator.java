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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;

import com.sciamlab.ckan4j.CKANApiClient.CKANApiClientBuilder;
import com.sciamlab.ckan4j.exception.CKANException;
import com.sciamlab.common.util.SciamlabMailUtils.SciamlabVelocityHelper;
import com.sciamlab.common.util.SciamlabStreamUtils;

public class CKANSitemapGenerator {
	
	private final List<String> portal_languages = new ArrayList<String>();
	private static final int max_number_of_entries_per_file = 50000;
	
	private final InputStream sitemap_is ;
	private final CKANApiClient client;
	private final String ckan_portal_baseurl;

	public static class Builder{
		private final CKANApiClient client;
		private final String ckan_portal_baseurl;
		private final List<String> portal_languages = new ArrayList<String>();
		private String sitemap_template = "sitemap-template.xml";
		
		public Builder(String portal_base_url, CKANApiClient client){
			
			this.ckan_portal_baseurl = portal_base_url;
			this.client = client;
		}
		
		public Builder sitemap_template(String template){
			this.sitemap_template = template;
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
		this.sitemap_is = SciamlabStreamUtils.getInputStream(builder.sitemap_template);
		this.client = builder.client;
		this.ckan_portal_baseurl = builder.ckan_portal_baseurl;
	}
	
	public void generate() throws IOException, JSONException, CKANException{
		
		
		ArrayList<String> urls = new ArrayList<String>();
		
		//String portal_language = portal_languages[i];
		//String portalUrl = portal_language.isEmpty()? ckan_portal_baseurl: ckan_portal_baseurl + "/" + portal_language;
		
		urls.addAll(getOrganizationsURL());
		urls.addAll(getPackagesURL());
		urls.addAll(getTagsURL());		
		
//		System.out.println(urls);
		
		StringBuffer main = new StringBuffer();
		main.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		main.append("\n<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
				
		List<StringBuilder> bArr = buildXML(urls);
		
		for (int i = 0; i < bArr.size(); i++) {
			String fileName = "sitemap" + i + ".xml";
			File file = new File(fileName);
	
			Properties props = new Properties();
			props.put("body", bArr.get(i).toString());
			String out = SciamlabVelocityHelper.getTemplateFromInputStream(null, props, sitemap_is);
			
			FileWriter writer = new FileWriter(file.getAbsoluteFile());
			writer.append(out);
			writer.close();
			
			System.out.println(fileName + " generated!");
			main.append("\n\t<sitemap>");
			main.append("\n\t\t<loc>"+file.getAbsolutePath()+"</loc>");
			main.append("\n\t</sitemap>");
		}
		
		main.append("\n</sitemapindex>");
		File sitemap = new File("sitemap.xml");
		FileWriter writer = new FileWriter(sitemap.getAbsoluteFile());
		writer.append(main.toString());
		writer.close();
		System.out.println("sitemap.xml generated!");
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
	
	private List<StringBuilder> buildXML(List<String> urls) {
		List<StringBuilder> bArr = new ArrayList<StringBuilder>();
		StringBuilder b = new StringBuilder();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		String currentDate = df.format(new Date());
		
		int num = 0;
		for (String url: urls){
			
			for (String lang : portal_languages) {
				if (num%max_number_of_entries_per_file == max_number_of_entries_per_file-1) {
					System.out.println(num);
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
