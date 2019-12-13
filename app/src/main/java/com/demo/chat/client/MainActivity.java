package com.demo.chat.client;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity implements SocketTask.OnSocketTaskListener, TextView.OnEditorActionListener {
    private TextView messageTv;
    private TextView titleTv;

    private int selectPos;
    private PrintWriter printWriter;
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
        if (printWriter != null) {
            threadPool.submit(() -> {
                printWriter.println("close");
                printWriter.close();
            });
        }
        super.onDestroy();
    }

    @Override
    public void onReceivedMsg(String msg) {
        runOnUiThread(() -> messageTv.append(msg + "\n"));
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
        printWriter = new PrintWriter(outputStream, true);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        threadPool.submit(() -> {
            String param1 = v.getText() + "==>" + adapter.getItem(selectPos);
            printWriter.println(param1);
        });
        return false;
    }
}
