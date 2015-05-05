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
import java.util.Timer;
import java.util.TimerTask;

import com.gh.apkmanager.R;
import com.gh.apkmanager.beans.Apk;
import com.gh.apkmanager.utils.HttpUtils;
import com.gh.apkmanager.utils.JsonTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.provider.Settings;

public class MainActivity extends Activity {
	
	private static final String TAG = "apkm";
	
	private static final int ERROR = 0;
	private static final int LOG = 1;
	private static final int PROGRESS = 2;
	
	private static final String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().getPath() + "/GH-Game/download/";
	private static final String SERVER_URL = "http://192.168.2.99:3000/";
	private static final String RESOURCE_URL = SERVER_URL + "data/";

	private Handler handler;
	
	private TextView textView;
	private Button button;
	private EditText edt;
	private Activity activity;
	
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
					case 0:
						Log.i(TAG, (String)msg.obj);
						int length = msg.obj.toString().length();
						
						SpannableStringBuilder style=new SpannableStringBuilder((String)msg.obj);  
				        style.setSpan(new ForegroundColorSpan(Color.RED),0,length,Spannable.SPAN_EXCLUSIVE_INCLUSIVE);  
				        
					    edt.append(style);
					    edt.append("\n");
					    break;
					case 1:
						Log.i(TAG, (String)msg.obj);
					    edt.append((String)msg.obj + '\n');
					    break;
					case 2:
						String content = edt.getText().toString();
						String percent = (String)msg.obj;
						
						// 填充
						switch(percent.length()){
							case 1:
								percent = "  " + percent;
								break;
							case 2:
								percent = " " + percent;
								break;
							default:
								break;
						}
				
						int curr_len = content.length();
						
						StringBuffer sb = new StringBuffer(content);
						sb.replace(curr_len - 5, curr_len - 2, percent); 

						edt.setText(sb.toString());
						break;
					default:
						break;
				}
				
				// 选中edittext底部
				edt.setSelection(edt.getText().length(), edt.getText().length());
			} 	
        };
       
	    edt = (EditText) findViewById(R.id.log); 

        printScreen("Please click 妙蛙种子 to START apkm...\n\n", LOG);

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
				        printScreen("***** START apkm *****", LOG);

						//  GET command
						String path = SERVER_URL + "pull";
						printScreen("* Connection: " + path, LOG); 
				        String jsonstring = HttpUtils.getJsonContent(path);  
			        
				        int code = Integer.parseInt(JsonTools.getJSON("type", jsonstring));
				        
				        switch(code){
				        	case 0:
				        		break;
				        	case 1:	
				        		printScreen("* START install.", LOG);
				        		install(jsonstring);
						        break;
				        	case 2:
				        		printScreen("* START uninstall.", LOG);
				        		uninstall(jsonstring);
						    default:
						    	break;
				        }    
				        
						printScreen("\n***** EXIT apkm *****", LOG);
//				        activity.finish();
//						System.exit(0);		        
					}
				}).start();
			}
        });     
    }

	private void printScreen(String string, int type) {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.obj = string;   
		msg.what = type;
		
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
	    	    
	        		updateList = JsonTools.getJSONs("list", json); 
	        		printScreen("** GET UpdateList finish.", LOG);
			        
		    	    for( int i = 0, len = updateList.size(); i < len; i++ ){
		    	    	String filename = updateList.get(i).getFname();
		    	    	String pname = updateList.get(i).getPname();
		    	    	printScreen("\n** INSTALL " + filename + " start.", LOG);
		    	    	
		    	    	printScreen("*** DOWNLOAD start.", LOG);
		    	    	download(filename);
		    	    	printScreen("*** DOWNLOAD finish.", LOG);
		    	    	
		    	    	String path = DOWNLOAD_DIR + filename;
		    	    	printScreen("*** Excute command: pm install -r " + path, LOG);

		    	    	out.println("pm install -r " + path);     
		    	        
		    	    	printScreen("** INSTALL " + filename + " is running.", LOG);
		    	    }	    

		    	    out.flush();

		    	    in.close();
		    	    out.close();
		    	    proc.waitFor();

		    	} catch (Exception e) {   
		    		printScreen("!!! INSTALL failed.", ERROR);
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
		    	    
		    	    updateList = JsonTools.getJSONs("list", json); 
	        		printScreen("** GET UpdateList finish.", LOG);
			        
		    	    for( int i = 0, len = updateList.size(); i < len; i++ ){
		    	    	String filename = updateList.get(i).getFname();
		    	    	String packagename = updateList.get(i).getPname();
		    	    	
		    	    	printScreen("\n** UNINSTALL " + filename + " start.", LOG);
		    	    	printScreen("*** Excute command: pm uninstall -r " + packagename, LOG);
			    	    out.println("pm uninstall " + packagename); 
		    	    	printScreen("** UNINSTALL " + filename + " finish.", LOG);
		    	    }	    

		    	    out.flush();

		    	    in.close();
		    	    out.close();
		            proc.waitFor();

		    	} catch (Exception e) {  
		    		printScreen("!!! UNINSTALL failed.", ERROR);
		    	    System.out.println("exception:" + e); 
		    	}				
	}

    // 下载服务器文件
    protected void download(String filename) {
		// TODO Auto-generated method stub
        
    	// 服务器资源地址
    	String DATA_URL = RESOURCE_URL + filename;
    	printScreen("*** GET apk: " + DATA_URL, LOG);
   	
    	// 检测目录是否存在
    	File dir = new File(DOWNLOAD_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
			printScreen("*** MKDIR success.", LOG);
		}
		
		String filepath = DOWNLOAD_DIR + filename;
    	
		//如果目标文件已经存在，则删除。产生覆盖旧文件的效果
		File file = new File(filepath);
		if(file.exists()){
			file.delete();
		}
    	
		try {
			URL url = new URL(DATA_URL);
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
			
			// 获取文件大小
			long total = conn.getContentLength();
			long curr = 0;
			long tmp = 0;
			int progress = 0;
			
			printScreen("*** DOWNLOAD is running...  00%", LOG);        
			
			// 开始读取   
			while ((len = is.read(bs)) != -1) {   
				os.write(bs, 0, len); 
				curr += len;
				progress = (int) ((float) curr / total * 100);				
				
				if( tmp != progress ){
					// 通知进度条更新     
					printScreen(progress + "", PROGRESS);   
					tmp = progress;
				}
			}  

			// 完毕，关闭所有链接   
			os.close();  
			is.close();	
		} catch (Exception e) {
			// TODO Auto-generated catch block
    		printScreen("!!! DONWLOAD failed.", ERROR);
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
	
    /** 
     * 判断apk是否安装
     * @param context 环境 
     * @param pname apk包名
     * @return 
     */
	protected boolean isInstall(Context context, String pname){
        final PackageManager packageManager = context.getPackageManager();
        
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for ( int i = 0; i < pinfo.size(); i++ )
        {
            if(pinfo.get(i).packageName.equalsIgnoreCase(pname))
                return true;
        }
        return false;
	}

	public void execCommand(String command) throws IOException {  
	    // start the ls command running  
	    Runtime runtime = Runtime.getRuntime();    
	    Process proc = runtime.exec(command);        
	    //这句话就是shell与高级语言间的调用  
	    //如果有参数的话可以用另外一个被重载的exec方法  
	    //实际上这样执行时启动了一个子进程,它没有父进程的控制台  
	    //也就看不到输出,所以我们需要用输出流来得到shell执行后的输出  
	    InputStream inputstream = proc.getInputStream();  
	    InputStreamReader inputstreamreader = new InputStreamReader(inputstream);  
	    BufferedReader bufferedreader = new BufferedReader(inputstreamreader);  
	    // read the ls output  
	    String line = "";  
	    StringBuilder sb = new StringBuilder(line);  
	    while ((line = bufferedreader.readLine()) != null) {  
	        sb.append(line);  
	        sb.append('\n');  
	    }  
	        //tv.setText(sb.toString());  
	        //使用exec执行不会等执行成功以后才返回,它会立即返回  
	        //所以在某些情况下是很要命的(比如复制文件的时候)  
	        //使用wairFor()可以等待命令执行完成以后才返回  
	    try {  
	        if (proc.waitFor() != 0) {  
	            System.err.println("exit value = " + proc.exitValue());  
	        }  
	    } catch (InterruptedException e) {    
	        System.err.println(e);  
	    }  
	} 
}
