package com.sciamlab.ckan4j.util;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.json.JSONArray;
import org.json.JSONException;

import com.sciamlab.common.exception.SciamlabException;
import com.sciamlab.common.util.HTTPClient;
import com.sciamlab.common.util.SciamlabStreamUtils;
import com.sciamlab.common.util.SciamlabVelocityHelper;

public class BlogSitemapGenerator {
	
	private static final Logger logger = Logger.getLogger(BlogSitemapGenerator.class);
	
	private static final int max_number_of_entries_per_file = 50000;
	private static final SciamlabVelocityHelper VELOCITY = new SciamlabVelocityHelper.Builder(new Properties() {{
			put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
			put(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, "Log4JLogChute");
			put(RuntimeConstants.RUNTIME_LOG, "/tmp/velocity.log");
		}}).build();

	private final String sitemap_template ;
	private final String blog_baseurl;
	private final String blog_repourl;

	public static class Builder{
		private final String blog_baseurl;
		private final String blog_repourl;
		private final List<String> portal_languages = new ArrayList<String>(){{add("");}};
		private String sitemap_template_file = "sitemap-template.xml";
		private String sitemap_template;
		
		public Builder(String blog_baseurl, String blog_repourl){
			this.blog_baseurl = blog_baseurl;
			this.blog_repourl = blog_repourl;
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
		
		public BlogSitemapGenerator build() throws FileNotFoundException, SciamlabException{
			this.sitemap_template = SciamlabStreamUtils.convertStreamToString(SciamlabStreamUtils.getInputStream(this.sitemap_template_file));
			return new BlogSitemapGenerator(this);
		}
	}
	
	private BlogSitemapGenerator(Builder builder) {
		this.sitemap_template = builder.sitemap_template;
		this.blog_baseurl = builder.blog_baseurl;
		this.blog_repourl = builder.blog_repourl;
	}
	
	public List<StringBuffer> generate(String sitemap_baseurl) throws JSONException, MalformedURLException {
		List<StringBuffer> list = new ArrayList<StringBuffer>();

		String fileName = "blog.xml";
		StringBuffer main = new StringBuffer();
		list.add(main);
		main.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		main.append("\n<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
		
		List<String> urls = getPostsURL();
		final int page_size = max_number_of_entries_per_file;
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
				String portalUrl = blog_baseurl + url;
				b.append("<url>\n");
				b.append("\t<loc>" + portalUrl + "</loc>\n");
				b.append("\t<lastmod>" + currentDate + "+00:00</lastmod>\n");
				b.append("\t<changefreq>monthly</changefreq>\n");
				b.append("\t<priority>0.8000</priority>\n");
				b.append("</url>\n");
		}
		return b;
	}
	
	private static final HTTPClient http = new HTTPClient();
	
	private List<String> getPostsURL() throws JSONException, MalformedURLException {
		List<String> list = new ArrayList<String>();
		
		JSONArray jarr = new JSONArray(http.doGET(new URL(blog_repourl)).readEntity(String.class));
				
		for (int i = 0; i < jarr.length(); i++) {
			String name = jarr.getJSONObject(i).getString("name");
			String yyyy = name.split("-")[0];
			String mm = name.split("-")[1];
			String dd = name.split("-")[2];
			String url = "/post/"+yyyy+"/"+mm+"/"+dd+"/"+name.replace(yyyy + "-" + mm + "-" + dd + "-", "").replace(".md", "");
			list.add(url);
		}
		
		return list;
	}
	
}
