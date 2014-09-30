package com.sciamlab.ckan4j.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

/**
 * 
 * @author SciamLab
 *
 */

public class SciamlabStreamUtils {
	
	private static final Logger logger = Logger.getLogger(SciamlabStreamUtils.class);
	
	@SuppressWarnings("resource")
	public static InputStream getInputStream(String file) throws FileNotFoundException{
		InputStream is = null;
		StringBuffer log = new StringBuffer();
		try{
			log.append("Attempt to load "+file+" file");
			log.append(" [1st-->");
			is = new FileInputStream(file);
		}catch(FileNotFoundException e1){
			try{
				log.append("KO]");
				log.append(" [2nd-->");
				is = new FileInputStream(System.getProperty("catalina.base")+"/conf/"+file);
			}catch(FileNotFoundException e2){
				try{
					log.append("KO]");
					log.append(" [3rd-->");
					is = new FileInputStream("conf/"+file);
				}catch(FileNotFoundException e){
					log.append("KO]");
		//			try{
					log.append(" [4th-->");
					is = SciamlabStreamUtils.class.getClassLoader().getResourceAsStream(file);
					if(is==null){
						log.append("KO]");
						log.append(" [5th-->");
						is = ClassLoader.getSystemResourceAsStream(file);
					}
				}
			}
		}
		if(is==null){
			log.append("KO]");
			logger.info(log.toString());
			throw new FileNotFoundException(file+" not found!");
		}
		log.append("OK]");
		logger.info(log.toString());
		
		return is;
	}
	
	public static File getFile(String fileName) throws FileNotFoundException{
		File file = null;
		StringBuffer log = new StringBuffer();
		
		log.append("Attempt to load "+fileName+" file");
		log.append(" [1st-->");
		file = new File(fileName);
		if(!file.exists()){
			log.append("KO]");
			log.append(" [2nd-->");
			file = new File("conf/"+fileName);
			if(!file.exists()){
				log.append("KO]");
				log.append(" [3rd-->");
				URL url = SciamlabStreamUtils.class.getClassLoader().getResource(fileName);
				if(url==null || !(new File(url.getFile()).exists())){
					log.append("KO]");
					log.append(" [4th-->");
					url = ClassLoader.getSystemResource(fileName);
					if(url==null || !(new File(url.getFile()).exists())){
						log.append("KO]");
						logger.info(log.toString());
						throw new FileNotFoundException(fileName+" not found!");
					}
				}
				file = new File(url.getFile());
			}
		}
		log.append("OK]");
		logger.info(log.toString());
		
		return file;
	}
	
	/**
	 * method used to get file stream to be returned by Jersey services as text/csv file 
	 * 
	 * @param file_stream
	 * @return
	 */
	public static StreamingOutput getStreamingOutput(final byte[] file_stream) {
        return new StreamingOutput() {
			
            public void write(OutputStream out) throws IOException, WebApplicationException{
                out.write(file_stream);
            }
        };
    }
}
