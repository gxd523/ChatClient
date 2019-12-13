package com.demo.chat.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by guoxiaodong on 2019-12-12 17:34
 */
public class SocketTask implements Runnable {
    private static final String IP_ADDRESS = "118.24.27.110";
    //    private static final String IP_ADDRESS = "192.168.43.19";
    private OnSocketTaskListener onSocketTaskListener;

    public SocketTask(OnSocketTaskListener onSocketTaskListener) {
        this.onSocketTaskListener = onSocketTaskListener;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(IP_ADDRESS, 9999);

            onSocketTaskListener.onReceivedOutputStream(socket.getOutputStream());

            Gson gson = new Gson();
            String s;
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            while ((s = bufferedReader.readLine()) != null) {
                if (s.contains("==>")) {
                    onSocketTaskListener.onReceivedMsg(s);
                } else {
                    List<String> list = gson.fromJson(s, new TypeToken<List<String>>() {
                    }.getType());
                    onSocketTaskListener.onReceivedClientList(list);
                }
            }
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
