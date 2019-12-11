package com.demo.chat.client;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private TextView messageTv;
    private int selectPos;
    private XFunc1<String> myListener;
    private XFunc0 closeSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageTv = findViewById(R.id.message_tv);
        Spinner spinnerView = findViewById(R.id.spinner_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_spinner_item, new ArrayList<>());
        spinnerView.setAdapter(adapter);
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        EditText editText = findViewById(R.id.edit_text);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (myListener != null) {
                String param1 = v.getText() + "==>" + adapter.getItem(selectPos);
                Executors.newSingleThreadExecutor().submit(() -> myListener.call(param1));
            }
            return false;
        });

        Executors.newSingleThreadExecutor().submit(new SocketTask(
                s -> {
                    List<String> list = new Gson().fromJson(s, new TypeToken<List<String>>() {
                    }.getType());
                    runOnUiThread(() -> {
                        adapter.clear();
                        adapter.addAll(list);
                    });
                },
                msg -> messageTv.append(msg),
                printWriter -> {
                    myListener = new XFunc1<String>() {
                        @Override
                        public void call(String x) {
                            printWriter.println(x);
                        }
                    };

                    closeSocket = new XFunc0() {
                        @Override
                        public void call() {
                            printWriter.println("close");
                        }
                    };
                }
        ));
    }

    @Override
    protected void onDestroy() {
        if (closeSocket != null) {
            Executors.newSingleThreadExecutor().submit(() -> closeSocket.call());
        }
        super.onDestroy();
    }

    private static class SocketTask implements Runnable {
        private XFunc1<String> msgCallback;
        private XFunc1<String> listCallback;
        private XFunc1<PrintWriter> printWriterXFunc1;

        public SocketTask(XFunc1<String> listCallback, XFunc1<String> msgCallback, XFunc1<PrintWriter> printWriterXFunc1) {
            this.listCallback = listCallback;
            this.msgCallback = msgCallback;
            this.printWriterXFunc1 = printWriterXFunc1;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket("192.168.43.19", 9999);

                printWriterXFunc1.call(new PrintWriter(socket.getOutputStream(), true));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (s.contains("==>")) {
                        msgCallback.call(s);
                    } else {
                        listCallback.call(s);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
