package com.stupidbeauty.smarturl;

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
    }
    
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
    *  获取目录的完整列表。
    */
    private String getDirectoryContentList(String wholeDirecotoryPath, String nameOfFile)
    {
        nameOfFile=nameOfFile.trim(); // 去除空白字符。陈欣
    
        String result=""; // 结果。
        File photoDirecotry= new File(wholeDirecotoryPath); //照片目录。
            
        File[]   paths = photoDirecotry.listFiles();
         
         // for each pathname in pathname array
        for(File path:paths) 
        {
            // -rw-r--r-- 1 nobody nobody     35179727 Oct 16 07:31 VID_20201015_181816.mp4

            String fileName=path.getName(); // 获取文件名。

            Date date=new Date(path.lastModified());  
                            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
//   String time= date.format(formatter);
            String time="8:00";

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM");

            String dateString="30";
                            
            long fileSize=path.length(); // 文件尺寸。
                            
            String group="cx";
                            
            String user = "ChenXin";
                            
            String linkNumber="1";
                            
//             String permission="-rw-r--r--"; // 权限。
            String permission=getPermissionForFile(path); // 权限。

            String month="Jan"; // 月份 。
            String currentLine = permission + " " + linkNumber + " " + user + " " + group + " " + fileSize + " " + month + " " + dateString + " " + time + " " + fileName + "\n" ; // 构造当前行。
            
            if (fileName.equals(nameOfFile)  || (nameOfFile.isEmpty())) // 名字匹配。
            {
            result=result+currentLine; // 构造结果。
            } //if (fileName.equals(nameOfFile)) // 名字匹配。
         }

         return result;
    } //private String getDirectoryContentList(String wholeDirecotoryPath)
    
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
        else if (command.equals("SYST")) // 系统信息
        {
            //        send_data "200 UNIX Type: L8\n"

            Util.writeAll(socket, "215 UNIX Type: L8\\n".getBytes(), new CompletedCallback() {
                @Override
                public void onCompleted(Exception ex) {
                    if (ex != null) throw new RuntimeException(ex);
                    System.out.println("[Server] Successfully wrote message");
                }
            });
        } //else if (command.equals("SYST")) // 系统信息
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
