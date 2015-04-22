package com.gh.apkmanager.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.gh.apkmanager.beans.Apk;

public class JsonTools {  
	  
    public static String getJSON(String key, String jsonString) {  
        
        try {  
            JSONObject jsonObject = new JSONObject(jsonString);  

            return jsonObject.getString("type");
        } catch (Exception e) {  
            // TODO: handle exception  
        	e.printStackTrace();
        }
		return "-1";          
    }  
  
    public static List<Apk> getJSONs(String key, String jsonString) {  
        List<Apk> list = new ArrayList<Apk>();  
        try {  
            JSONObject jsonObject = new JSONObject(jsonString);  
            JSONArray jsonArray = jsonObject.getJSONArray(key);  
            for (int i = 0; i < jsonArray.length(); i++) {  
                JSONObject apkObject = jsonArray.getJSONObject(i);  
                
                Apk apk = new Apk();  
                apk.setPname(apkObject.getString("package_name"));  
                apk.setFname(apkObject.getString("file_name"));  
                list.add(apk);  
            }  
        } catch (Exception e) {  
            // TODO: handle exception  
        }  
        return list;  
    }  
  
    public static List<String> getlistString(String key, String jsonString) {  
        List<String> list = new ArrayList<String>();  
        try {  
            JSONObject jsonObject = new JSONObject(jsonString);  
            JSONArray jsonArray = jsonObject.getJSONArray(key);  
            for (int i = 0; i < jsonArray.length(); i++) {  
                String msg = jsonArray.getString(i);  
                list.add(msg);  
            }   
        } catch (Exception e) {  
            // TODO: handle exception  
        }  
  
        return list;  
    }  
  
    public static List<Map<String,Object>> getlistMap(String key, String jsonString){  
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();  
        try {  
            JSONObject jsonObject = new JSONObject(jsonString);  
            JSONArray jsonArray = jsonObject.getJSONArray(key);  
            for (int i = 0; i < jsonArray.length(); i++) {  
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);  
                  Map<String,Object> map=new HashMap<String, Object>();  
                  Iterator<String> iterator=jsonObject2.keys();  
                    
                  while(iterator.hasNext()){  
                      String json_key=iterator.next();  
                      Object json_value=jsonObject2.get(json_key);  
                      if(json_value==null){  
                          json_value="";  
                      }  
                      map.put(json_key, json_value);  
                  }  
               list.add(map);  
            }  
            }catch(Exception e){  
                e.printStackTrace();  
            }  
        return list;  
    }  
}
