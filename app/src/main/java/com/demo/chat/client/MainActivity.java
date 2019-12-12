package com.demo.chat.client;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements SocketTask.OnSocketTaskListener, TextView.OnEditorActionListener {
    private TextView messageTv;
    private TextView titleTv;

    private int selectPos;
    private BufferedWriter bufferedWriter;
    private ArrayAdapter<String> adapter;
    private ExecutorService threadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threadPool = Executors.newFixedThreadPool(3);

        messageTv = findViewById(R.id.message_tv);
        titleTv = findViewById(R.id.title_tv);

        Spinner spinnerView = findViewById(R.id.spinner_view);
        adapter = new ArrayAdapter<>(this, R.layout.layout_spinner_item, new ArrayList<>());
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
        editText.setOnEditorActionListener(this);

        threadPool.submit(new SocketTask(this));
    }

    @Override
    protected void onDestroy() {
        if (bufferedWriter != null) {
            threadPool.submit(() -> {
                try {
                    bufferedWriter.write("close");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        super.onDestroy();
    }

    @Override
    public void onReceivedMsg(String msg) {
        Log.d("gxd", "MainActivity.onReceivedMsg->");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("gxd", "MainActivity.run-->" + msg);
                messageTv.append(msg + "\n");
            }
        });
    }

    @Override
    public void onReceivedClientList(List<String> clientList) {
        runOnUiThread(() -> {
            adapter.clear();
            adapter.addAll(clientList);
            if (TextUtils.isEmpty(titleTv.getText())) {
                titleTv.setText(clientList.get(clientList.size() - 1));
            }
        });
    }

    @Override
    public void onReceivedOutputStream(OutputStream outputStream) {
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        threadPool.submit(() -> {
            String param1 = v.getText() + "==>" + adapter.getItem(selectPos);
            try {
                bufferedWriter.write(param1);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return false;
    }
}
