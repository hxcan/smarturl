package com.stupidbeauty.smarturl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.stupidbeauty.victoriafresh.VFile;
import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;
import com.stupidbeauty.hxlauncher.bean.VoicePackageMapJsonItem;
import com.stupidbeauty.hxlauncher.bean.VoicePackageUrlMapData;
import com.stupidbeauty.hxlauncher.bean.WakeLockPackageNameSetData;
import com.stupidbeauty.hxlauncher.datastore.RuntimeInformationStore;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.koushikdutta.async.*;
import java.net.InetSocketAddress;
import com.koushikdutta.async.callback.ConnectCallback;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import java.util.Date;    
import java.time.format.DateTimeFormatter;
import java.io.File;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.ListenCallback;
import org.apache.commons.io.FileUtils;
import com.koushikdutta.async.callback.ConnectCallback;
import java.net.InetSocketAddress;
import android.text.format.Formatter;
import android.net.wifi.WifiManager;
import java.util.Random;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SmartUrl
{
    private AsyncSocket socket; //!< 当前的客户端连接。
    private static final String TAG ="ControlConnectHandler"; //!<  输出调试信息时使用的标记。
    private Context context; //!< 执行时使用的上下文。
    private AsyncSocket data_socket; //!< 当前的数据连接。
    private File writingFile; //!< 当前正在写入的文件。
    private boolean isUploading=false; //!< 是否正在上传。陈欣
    private InetAddress host;
    private File rootDirectory=null; //!< 根目录。
    private List<VoicePackageMapJsonItem> urlList; //!< 网址前缀列表。
    
    /**
    * 从数据套接字处接收数据。陈欣
    */
    private void receiveDataSocket( ByteBufferList bb)
    {
        byte[] content=bb.getAllByteArray(); // 读取全部内容。
        
        boolean appendTrue=true;

        try
        {
          FileUtils.writeByteArrayToFile(writingFile, content, appendTrue); // 写入。
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
    } //private void                         receiveDataSocket( ByteBufferList bb)

    public SmartUrl(Context context, boolean allowActiveMode, InetAddress host)
    {
      this.context=context;
      this.host=host;
      
      loadVoicePackageUrlMap(); // 载入网址前缀列表。陈欣。
    }
    
      /**
	 * 载入语音识别结果与包下载地址之间的映射。
	 */
	private void loadVoicePackageUrlMap()
	{
		String qrcFileName="voicePackageUrlMap.json"; //文件名。
		String fullQrcFileName=":/VoicePackageUrlMapInternationalization/"+qrcFileName; //构造完整的qrc文件名。

		        int victoriaFreshDataFileId=context.getResources().getIdentifier("victoriafreshdata_smarturl", "raw", context.getPackageName()); //获取数据文件编号。
        int victoriaFreshIndexFileId=context.getResources().getIdentifier("victoriafresh_smarturl", "raw", context.getPackageName()); //获取索引文件编号。

		VFile qrcHtmlFile=new VFile(context, victoriaFreshIndexFileId, victoriaFreshDataFileId, fullQrcFileName); //qrc网页文件。

		String fileContent=qrcHtmlFile.getFileTextContent(); //获取文件的完整内容。

		Gson gson=new Gson();

		VoicePackageUrlMapData voicePackageUrlMapData = gson.fromJson(fileContent, VoicePackageUrlMapData.class); //解析。

// 		voicePackageUrlMap=new HashMap<>(); //创建映射。
// 		packageNameUrlMap=new HashMap<>(); //创建映射
// 		packageNameVersionNameMap=new HashMap<>(); // 创建映射。陈欣
// 		packageNameApplicationNameMap=new HashMap<>(); //创建映射

		if (voicePackageUrlMapData!=null) //解析得到的映射数据不为空。
		{
		urlList=voicePackageUrlMapData.getVoicePackageMapJsonItemList(); // 获取列表。
			for(VoicePackageMapJsonItem currentItem: voicePackageUrlMapData.getVoicePackageMapJsonItemList()) //一个个地添加。
			{
// 				voicePackageUrlMap.put(currentItem.voiceCommand, currentItem.packageUrl); //加入映射。
// 				packageNameUrlMap.put(currentItem.getPackageName(), currentItem.packageUrl); //加入映射。
// 				packageNameVersionNameMap.put(currentItem.getPackageName(), currentItem.versionName); // 加入映射。
// 				packageNameApplicationNameMap.put( currentItem.getPackageName(),currentItem.voiceCommand); //加入映射，包名与应用程序名的映射
			} //for(VoicePackageMapJsonItem currentItem: voicePackageUrlMapData.getVoicePackageMapJsonItemList()) //一个个地添加。
		} //if (voicePackageUrlMapData!=null) //解析得到的映射数据不为空。
	} //private void loadVoicePackageUrlMap()

    
    /**
    * 以二进制模式发送字符串内容。
    */
    private void sendStringInBinaryMode(String stringToSend)
    {
        Util.writeAll(socket, stringToSend.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully wrote message");
            }
        });
    } //private sendStringInBinaryMode(String stringToSend)
    
    public void notifyFileNotExist() // 告知文件不存在
    {
//         controlConnectHandler.notifyFileNotExist(); // 告知文件不存在。
//         String replyString="216 " + "\n"; // 回复内容。
        String replyString="550 File not exist\n"; // File does not exist.
// 陈欣
        Log.d(TAG, "reply string: " + replyString); //Debug.
        
        sendStringInBinaryMode(replyString); // 发送。
    } //private void notifyFileNotExist()

    /**
    * 告知已经发送文件内容数据。
    */
    public void notifyFileSendCompleted() 
    {
        String replyString="216 " + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.
        
        sendStringInBinaryMode(replyString); // 发送。
    } //private void notifyFileSendCompleted()

        /**
     * 启动应用。
     * @param launchIntent 启动用的意图。
     */
    public boolean launchApplication(Intent launchIntent, String url)
    {
        boolean result=false; //结果

        Log.d(TAG, "launchApplication, launch intent: " + launchIntent); //Debug.
        try //尝试启动活动，并且捕获可能的异常。
        {
            if (launchIntent!=null) //启动意图存在。
            {
//                     Intent i = new Intent(Intent.ACTION_VIEW);
//       i.setData(Uri.parse(url));
                      launchIntent.setAction(Intent.ACTION_VIEW);
      launchIntent.setData(Uri.parse(url));

                       context.startActivity(launchIntent); //启动活动。
            } //                    if (launchIntent!=null) //启动意图存在。

            result=true; //启动成功
        } //try //尝试启动活动，并且捕获可能的异常。
        catch (ActivityNotFoundException exception)
        {
            exception.printStackTrace(); //报告错误。
        } //catch (ActivityNotFoundException exception)
        catch (SecurityException exception) //安全异常。
        {
            exception.printStackTrace(); //报告错误。
        } //catch (SecurityException exception) //安全异常。

        return result;
    } //private void launchApplication(Intent launchIntent)

    /**
    * 告知上传完成。
    */
    private void notifyStorCompleted() 
    {
        String replyString="226 Stor completed." + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        sendStringInBinaryMode(replyString);
    } //private void notifyStorCompleted()
    
    /**
    * 告知，数据连接未建立。
    */
    private void notifyLsFailedDataConnectionNull() 
    {
        String replyString="426 no data connection for file content "  + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                Log.d(TAG, "notifyLsFailedDataConnectionNull, [Server] Successfully wrote message");
            }
        });
    } //private void notifyLsFailedDataConnectionNull()

    /**
     * 告知已经发送目录数据。
     */
    private void notifyLsCompleted()
    {
//        send_data "216 \n"

        String replyString="226 Data transmission OK. ChenXin" + "\n"; // 回复内容。

        Log.d(TAG, "reply string: " + replyString); //Debug.

        Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully wrote message");
            }
        });
    } //private void notifyLsCompleted()

    /**
    *  打开网址。陈欣。
    */
    public void openUrl(String url)
    {
      for(VoicePackageMapJsonItem currentMap : urlList)
      {
        if (url.startsWith(currentMap.packageUrl)) // 找到匹配的了。
        {
          String packageName=currentMap.getPackageName();
          
        Log.d(TAG, "openUrl, package name: " + packageName + ", package url: " + currentMap.packageUrl + ", url: " + url); // Debug.
          
          
          launchApplicationByPackageName(packageName, url); // 启动应用。
        
          break;
        } //if (url.startsWith(currentMap.packageUrl)) // 找到匹配的了。
      } //for(VoicePackageMapJsonItem : urlList)
    } //private String getDirectoryContentList(String wholeDirecotoryPath)
    
        /**
     * 根据包名启动应用程序。
     * @param packageName 包名。
     */
    private boolean launchApplicationByPackageName(String packageName, String url)
    {
        boolean result=false; //启动结果
        PackageManager packageManager=context.getPackageManager(); //获取软件包管理器。

        Intent launchIntent= packageManager.getLaunchIntentForPackage(packageName); //获取当前软件包的启动意图。

        if (launchIntent!=null) //意图存在。
        {
            try //尝试启动活动，并且捕获可能的异常。
            {
              launchApplication(launchIntent, url); //启动活动。

              result=true; //成功
            } //try //尝试启动活动，并且捕获可能的异常。
            catch (ActivityNotFoundException exception)
            {
                exception.printStackTrace(); //报告错误。
            } //catch (ActivityNotFoundException exception)
        } //if (launchIntent!=null) //意图存在。
        //意图不存在，则说明对应的应用不存在，后续应当触发自动下载。
        //else //意图不存在，则说明对应的应用不存在，后续应当触发自动下载。

        return result;
    } //private void launchApplicationByPackageName(String packageName)


    /**
    * 获取文件或目录的权限。
    */
    private String  getPermissionForFile(File path)
    {
        String permission="-rw-r--r--"; // 默认权限。
        
        Log.d(TAG, "getPermissionForFile, path: " + path + ", is directory: " + path.isDirectory()); // Debug.
        
        if (path.isDirectory())
        {
            permission="drw-r--r--"; // 目录默认权限。
        }
        
        return permission;
    } //private String  getPermissionForFile(File path)

    /**
     * 处理命令。
     * @param command 命令关键字
     * @param content 整个消息内容。
     */
    private void processCommand(String command, String content)
    {
        Log.d(TAG, "command: " + command + ", content: " + content); //Debug.

        if (command.equals("USER")) // 用户登录
        {
            Util.writeAll(socket, "331 Send password\n".getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });
        } //if (command.equals("USER")) // 用户登录
        else if (command.equals("PASS")) // 密码
        {
            Util.writeAll(socket, "230 Loged in.\n".getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });
        } //else if (command.equals("PASS")) // 密码
        else if (command.equals("TYPE")) // 传输类型
        {
            String replyString="200 binery type set" + "\n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });
        } //else if (command.equals("TYPE")) // 传输类型
        else if (command.equals("EPSV")) // 扩展被动模式
        {
            String replyString="202 \n"; // 回复内容。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });
        } //else if (command.equals("EPSV")) // 扩展被动模式
        else  // 其它命令
        {
            String replyString="150 \n"; // 回复内容。正在打开数据连接

            replyString="502 " + content.trim()  +  " not implemented\n"; // 回复内容。未实现。

            Log.d(TAG, "reply string: " + replyString); //Debug.

            Util.writeAll(socket, replyString.getBytes(), new CompletedCallback() 
            {
                @Override
                public void onCompleted(Exception ex) 
                {
                    if (ex != null) throw new RuntimeException(ex);
                    Log.d(TAG, "[Server] Successfully wrote message");
                }
            });
        } //else if (command.equals("EPSV")) // Extended passive mode.
    } //private void processCommand(String command, String content)

    /**
    * 上传文件内容。
    */
    private void startStor(String data51, String currentWorkingDirectory) 
    {
        String wholeDirecotoryPath= rootDirectory.getPath() + currentWorkingDirectory+data51; // 构造完整路径。
                    
        wholeDirecotoryPath=wholeDirecotoryPath.replace("//", "/"); // 双斜杠替换成单斜杠
                    
        Log.d(TAG, "startStor: wholeDirecotoryPath: " + wholeDirecotoryPath); // Debug.
                    
        File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
            
        writingFile=photoDirecotry; // 记录文件。
        isUploading=true; // 记录，处于上传状态。

//             陈欣

        if (photoDirecotry.exists())
        {
            photoDirecotry.delete();
        }
        
        try //尝试构造请求对象，并且捕获可能的异常。
		{
            FileUtils.touch(photoDirecotry); //创建文件。
        } //try //尝试构造请求对象，并且捕获可能的异常。
		catch (Exception e)
		{
			e.printStackTrace();
		}
    } //private void startStor(String data51, String currentWorkingDirectory) // 上传文件内容。

    /**
     * 接受新连接
     * @param socket 新连接的套接字对象
     */
    public void handleAccept(final AsyncSocket socket)
    {
        this.socket=socket;
        System.out.println("[Server] New Connection " + socket.toString());

        socket.setDataCallback(
                new DataCallback()
                {
            @Override
            public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                String content = new String(bb.getAllByteArray());
                Log.d(TAG, "[Server] Received Message " + content); // Debug

                String command = content.split(" ")[0]; // Get the command.


                command=command.trim();

                processCommand(command, content); // 处理命令。
            }
        });

        socket.setClosedCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) {
//                 throw new RuntimeException(ex);
ex.printStackTrace(); // 报告错误。
                }
                else
                {
                System.out.println("[Server] Successfully closed connection");
                }
                
            }
        });

        socket.setEndCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) // 有异常出现
                {
//                 throw new RuntimeException(ex);
                    ex.printStackTrace(); // 报告。
                }
                else // 无异常
                {
                    Log.d(TAG, "ftpmodule [Server] Successfully end connection");
                } //else // 无异常
            }
        });

        //发送初始命令：
//        send_data "220 \n"

        Util.writeAll(socket, "220 BuiltinFtp Server\n".getBytes(), new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) 
            {
                if (ex != null) throw new RuntimeException(ex);
                System.out.println("[Server] Successfully wrote message");
            } //public void onCompleted(Exception ex) 
        });
    } //private void handleAccept(final AsyncSocket socket)
}
