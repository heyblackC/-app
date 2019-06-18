package com.heyblack.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class NetWork {

    private HttpURLConnection conn;

    /**
     * 网络操作相关的子线程
     */
    public void getwebinfo(String urlPath, Map<String,String> params, Handler handler){

        String str = "";

        String dataString = getRequestData(params, "utf-8").toString();
        byte[] data = dataString.getBytes();//获得请求体

        // 在这里进行 http request.网络请求相关操作
        try{

            URL url = new URL(urlPath);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(6 * 10000000);
            conn.setReadTimeout(30000000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            //设置请求体的类型是文本类型
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(data);

            conn.connect();

            if (conn.getResponseCode() != 200)
                throw new RuntimeException("运行异常");

            InputStream is = conn.getInputStream();
            int code = conn.getResponseCode();
            str = convertStreamToString(is);



        }catch (Exception e) {

            e.printStackTrace();
            System.out.println("异常");

        }

        conn.disconnect();

        Message msg = new Message();
        Bundle returnData = new Bundle();
        returnData.putString("value", str);
        msg.setData(returnData);
        handler.sendMessage(msg);

    };

    private static String convertStreamToString(InputStream is)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        }catch (IOException e)
        {}
        finally {
            try {
                is.close();
            }catch (IOException e){}
        }


        return sb.toString();
    }

    /*
     * Function  :   封装请求体信息
     * Param     :   params请求体内容，encode编码格式
     */
    private static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

}
