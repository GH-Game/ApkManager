package com.gh.apkmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import com.gh.apkmanager.R;
import com.gh.apkmanager.beans.Apk;
import com.gh.apkmanager.utils.HttpUtils;
import com.gh.apkmanager.utils.JsonTools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.provider.Settings;

public class MainActivity extends Activity {
	
	private Handler handler;
	
	private TextView textView;
	private Button button;
	private EditText edt;
	private Activity activity;
	
	//private String DOWNLOAD_DIR = Environment.getExternalStorageDirectory() + "/GH-Game/download";
	private String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/GH-Game/download/";
	//private String SERVER_URL = "http://hello13.net/projects/game/";
	private String SERVER_URL = "http://192.168.2.169:3000/data/";

	private String TAG = "apkm";
	
	private List<Apk> updateList = new ArrayList<Apk>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(R.layout.activity_main);
        
        activity = this;
        
        handler = new Handler(){
			public void handleMessage(Message msg){			
				switch(msg.what){
					case 1:
						Log.i(TAG, (String)msg.obj);
					    edt.append((String)msg.obj + '\n');
					    break;
					default:
						break;
				}
			} 	
        };
        
	    edt = (EditText) findViewById(R.id.log); 

        printScreen("Please click 妙蛙种子 to START apkm...\n\n");

        textView = (TextView) findViewById(R.id.android_id);  
        textView.setText("Apkm's running now...");
        
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
				// EMPTY console.log
				edt.setText("");
				
				new Thread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
				        printScreen("***** START apkm *****");

						//  GET command
						// String path = SERVER_URL + "resp.json";
				        String path = "http://192.168.2.169:3000/pull";
						printScreen("* Connection: " + path); 
				        String jsonstring = HttpUtils.getJsonContent(path);  
				        // printScreen("* Resp:" + jsonstring);
				        
				        int code = Integer.parseInt(JsonTools.getJSON("type", jsonstring));
				        // printScreen("* Type: " + code + " | 0:NO ACT; 1: INSTALL; 2: UNINSTALL;");  
				        
				        switch(code){
				        	case 0:
				        		break;
				        	case 1:	
				        		printScreen("* START install.");
				        		install(jsonstring);
						        break;
				        	case 2:
				        		printScreen("* START uninstall.");
				        		uninstall(jsonstring);
						    default:
						    	break;
				        }    
				        
						printScreen("\n***** EXIT apkm *****");
//				        activity.finish();
//						System.exit(0);		        
					}
				}).start();
			}
        });     
    }

	private void printScreen(String string) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.obj = string;   
		msg.what = 1;
		
		handler.sendMessage(msg);
	}

	protected void install(final String json) {
		// TODO Auto-generated method stub

		    	try {   
		            ProcessBuilder pb = new ProcessBuilder("/system/bin/sh");  
		            //java.lang.ProcessBuilder:  Creates operating system processes.  
		            pb.directory(new File("/"));
		              
		    	    Process proc = pb.start();   
		    	    //获取输入流，可以通过它获取SHELL的输出。    
		    	    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));   
		    	    BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));   
		    	    //获取输出流，可以通过它向SHELL发送命令。    
		    	    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc   
		    	                    .getOutputStream())), true); 
		    	    // 执行这一句时会弹出对话框（以下程序要求授予最高权限...），要求用户确认。
		    	    out.println("su root");    
		    	    // 删除指定的apk. 这个目录在系统中要求有root权限才可以访问的。  
		    	    // out.println("pm uninstall com.sankuai.meituan"); 
		    	    
	        		updateList = JsonTools.getJSONs("list", json); 
	        		printScreen("** GET UpdateList finish.");
			        
		    	    for( int i = 0, len = updateList.size(); i < len; i++ ){
		    	    	String filename = updateList.get(i).getFname();
		    	    	String packname = updateList.get(i).getPname();
		    	    	printScreen("\n** INSTALL " + filename + " start.");
		    	    	
		    	    	printScreen("*** DOWNLOAD start.");
		    	    	download(filename);
		    	    	printScreen("*** DOWNLOAD finish.");
		    	    	
		    	    	String path = DOWNLOAD_DIR + filename;
		    	    	printScreen("*** Excute command: pm install -r " + path);
		    	    	out.println("pm install -r " + path); 
		    	    	printScreen("** INSTALL " + filename + " finish.");
		    	    }	    

		    	    out.flush();

		    	    in.close();
		    	    out.close();
		            proc.waitFor();

		    	} catch (Exception e) {   
		    		printScreen("!!! INSTALL failed.");
		    	    System.out.println("exception:" + e); 
		    	}				
	}

	protected void uninstall(final String json) {
		// TODO Auto-generated method stub

		    	try {   
		            ProcessBuilder pb = new ProcessBuilder("/system/bin/sh");  
		            //java.lang.ProcessBuilder:  Creates operating system processes.  
		            pb.directory(new File("/"));
		              
		    	    Process proc = pb.start();   
		    	    //获取输入流，可以通过它获取SHELL的输出。    
		    	    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));   
		    	    BufferedReader err = new BufferedReader(new InputStreamReader(proc.getErrorStream()));   
		    	    //获取输出流，可以通过它向SHELL发送命令。    
		    	    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc   
		    	                    .getOutputStream())), true); 
		    	    // 执行这一句时会弹出对话框（以下程序要求授予最高权限...），要求用户确认。
		    	    out.println("su root");    
		    	    // 删除指定的apk. 这个目录在系统中要求有root权限才可以访问的。  
		    	    
	        		updateList = JsonTools.getJSONs("list", json); 
	        		printScreen("** GET UpdateList finish.");
			        
		    	    for( int i = 0, len = updateList.size(); i < len; i++ ){
		    	    	String filename = updateList.get(i).getFname();
		    	    	String packagename = updateList.get(i).getPname();
		    	    	
		    	    	printScreen("\n** UNINSTALL " + filename + " start.");
		    	    	printScreen("*** Excute command: pm uninstall -r " + packagename);
			    	    out.println("pm uninstall " + packagename); 
		    	    	printScreen("** UNINSTALL " + filename + " finish.");
		    	    }	    

		    	    out.flush();

		    	    in.close();
		    	    out.close();
		            proc.waitFor();

		    	} catch (Exception e) {  
		    		printScreen("!!! UNINSTALL failed.");
		    	    System.out.println("exception:" + e); 
		    	}				
	}

    // 下载服务器文件
    protected void download(String filename) {
		// TODO Auto-generated method stub
        
    	// 服务器资源地址
    	String RESOURCE_URL = SERVER_URL + filename;
    	printScreen("*** GET apk: " + RESOURCE_URL);
   	
    	// 检测目录是否存在
    	File dir = new File(DOWNLOAD_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
			printScreen("*** MKDIR success.");
		}
		
		String filepath = DOWNLOAD_DIR + filename;
		// Log.i(TAG, "NEW is " + filename);
    	
		//如果目标文件已经存在，则删除。产生覆盖旧文件的效果
		File file = new File(filepath);
		if(file.exists()){
			file.delete();
		}
    	
		try {
			URL url = new URL(RESOURCE_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			
			InputStream is = conn.getInputStream();
			
			// 1K的数据缓冲   
			byte[] bs = new byte[1024];   
			// 读取到的数据长度   
			int len;   
			// 输出的文件流   

			OutputStream os = new FileOutputStream(filepath);   
			// 开始读取   
			while ((len = is.read(bs)) != -1) {   
				os.write(bs, 0, len);   
			}  
			// 完毕，关闭所有链接   
			os.close();  
			is.close();	
		} catch (Exception e) {
			// TODO Auto-generated catch block
    		printScreen("!!! DONWLOAD failed.");
			e.printStackTrace();
		}
	}

    /** 
     * 生产一个指定长度的随机字符串 
     * @param length 字符串长度 
     * @return 
     */
	protected String generateRandomString(int length) {
		// TODO Auto-generated method stub
        final String POSSIBLE_CHARS="0123456789abcde";  

        StringBuilder sb = new StringBuilder(length);  
        SecureRandom random = new SecureRandom();  
        for (int i = 0; i < length; i++) {  
            sb.append(POSSIBLE_CHARS.charAt(random.nextInt(POSSIBLE_CHARS.length())));  
        }  
        return sb.toString();  
	}

}
