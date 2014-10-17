package com.sciamlab.ckan4j.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * 
 * @author SciamLab
 *
 */

public class SciamlabStringUtils {
	
	private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$");
	
	private static void minLength(String str, int len) throws IllegalArgumentException {
		if (str==null || str.length() < len) {
			throw new IllegalArgumentException();
		}
	}

	private static void maxLength(String str, int len) throws IllegalArgumentException {
		if (str==null || str.length() > len) {
			throw new IllegalArgumentException();
		}
	}

	public static void validEmail(String email) throws IllegalArgumentException {
		minLength(email, 4);
        maxLength(email, 255);
		if (!email.contains("@") || email.contains(" ") || !email.substring(email.indexOf("@")).contains("\\.")) {
			throw new IllegalArgumentException(email);
		}
	}

    public static boolean isValidUuid(String uuid) {
        return UUID_PATTERN.matcher(uuid).matches();
    }
    
    public static String stackTraceToString(Exception e){
    	Writer writer = null;
    	PrintWriter printWriter = null;
		try {
			writer = new StringWriter();
			printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String s = writer.toString();
			return s;
		} finally{
			if(printWriter!=null) printWriter.close();
			if(writer!=null) try { writer.close(); } catch (IOException e1) { e1.printStackTrace(); }
		}
	}    
    
    public static String replaceSpecialCharsAddDashesAndLowerCase(String s){
    	if(s==null) return null;
    	s = replaceSpecialChars(s.toLowerCase());
    	return s.replaceAll("   ", " ")
    			.replaceAll("  ", " ")
    			.replaceAll(" - ", "-")
    			.replaceAll("' ", "'")
				.replaceAll(" ", "-")
				.replaceAll("'", "")
				.toLowerCase();
    }
    
    public static String replaceSpecialChars(String s){
    	return s.replaceAll("ß", "b")
				.replaceAll("ç", "c")
				.replaceAll("â", "a")
				.replaceAll("à", "a")
				.replaceAll("a'", "a")
				.replaceAll("ê", "e")
				.replaceAll("è", "e")
				.replaceAll("é", "e")
				.replaceAll("e'", "e")
				.replaceAll("ì", "i")
				.replaceAll("i'", "i")
				.replaceAll("ò", "o")
				.replaceAll("ô", "o")
				.replaceAll("ö", "o")
				.replaceAll("o'", "o")
				.replaceAll("ü", "u")
				.replaceAll("ù", "u")
				.replaceAll("u'", "u");
    }
    
}
