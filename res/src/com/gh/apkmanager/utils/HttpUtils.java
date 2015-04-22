package com.gh.apkmanager.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpUtils {  
    public static String getJsonContent(String path){  
        try {  
        	URL url=new URL(path);  
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();  
            connection.setConnectTimeout(3000);  
            connection.setRequestMethod("GET");  
            
            connection.setDoInput(true);  
            
            int code=connection.getResponseCode(); 
            // Log.i("apkm","Path is " + path); 
            // Log.i("apkm","Code is " + code); 
            if(code==200){  
            	// Log.i("apkm","Connection success."); 
                return changeInputString(connection.getInputStream());  
        	}  
        } catch (Exception e) {  
            // TODO: handle exception 
        	e.printStackTrace();   
        }
        return "";
    }  
  
    private static String changeInputString(InputStream inputStream) {  
          
        String jsonString="";  
        
        ByteArrayOutputStream outPutStream=new ByteArrayOutputStream();  
        byte[] data=new byte[1024];  
        int len=0;  
        try {  
            while((len=inputStream.read(data))!=-1){  
                outPutStream.write(data, 0, len);  
            }  
            jsonString=new String(outPutStream.toByteArray());     
            
        } catch (Exception e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }  
        return jsonString;  
    }  
  
}