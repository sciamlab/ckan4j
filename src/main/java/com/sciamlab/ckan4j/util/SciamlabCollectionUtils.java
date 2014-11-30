package com.sciamlab.ckan4j.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

public class SciamlabCollectionUtils {
	
	public static List<Object> asList(JSONArray array){
		List<Object> list = new ArrayList<Object>();
		for(int i=0 ; i<array.length() ; i++){
			list.add(array.get(i));
		}
		return list;
	}

}
