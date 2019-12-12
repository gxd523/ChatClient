package com.demo.chat.client;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by guoxiaodong on 2019-12-12 17:34
 */
public class SocketTask implements Runnable {
    //    private static final String IP_ADDRESS = "118.24.27.110";
    private static final String IP_ADDRESS = "192.168.43.19";
    private OnSocketTaskListener onSocketTaskListener;

    public SocketTask(OnSocketTaskListener onSocketTaskListener) {
        this.onSocketTaskListener = onSocketTaskListener;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(IP_ADDRESS, 9999);

            onSocketTaskListener.onReceivedOutputStream(socket.getOutputStream());
            InputStream inputStream = socket.getInputStream();
            int length;
            byte[] bytes = new byte[1024];

            while ((length = inputStream.read(bytes)) != -1) {
                String s = new String(bytes, 0, length, Charset.defaultCharset());
                Log.d("gxd", "SocketTask.run-->" + s);
                if (s.contains("==>")) {
                    onSocketTaskListener.onReceivedMsg(s);
                } else {
                    List<String> list = new Gson().fromJson(s, new TypeToken<List<String>>() {
                    }.getType());
                    onSocketTaskListener.onReceivedClientList(list);
                }
            }
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
//                String s;
//                while ((s = bufferedReader.readLine()) != null) {
//                    Log.d("gxd", "客户端接收到消息-->" + s);
//                    if (s.contains("==>")) {
//                        msgCallback.call(s);
//                    } else {
//                        clientListCallback.call(s);
//                    }
//                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface OnSocketTaskListener {
        void onReceivedMsg(String msg);

        void onReceivedClientList(List<String> clientList);

        void onReceivedOutputStream(OutputStream outputStream);
    }
}
