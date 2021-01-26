package com.grondstofapps.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> arrayList;
    private ListView listView;
    private TextView textViewH;
    private ArrayAdapter adapter;
    private String check;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        textViewH = findViewById(R.id.textViewH);
        listView = findViewById(R.id.simpleListView);
        arrayList = new ArrayList<>();
        arrayList = getIntent().getStringArrayListExtra("history");
        check = getIntent().getStringExtra("check");
        if(arrayList.isEmpty()){
            textViewH.setText("Hesaplama geçmişi boş.");
        }
        else{
            textViewH.setText("Hesaplama Geçmişi");
        }
        Collections.reverse(arrayList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, arrayList){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView=view.findViewById(android.R.id.text1);
                if(check.equals("true")){
                    textView.setTextColor(Color.WHITE);
                }
                else{
                    textView.setTextColor(Color.DKGRAY);
                }
                textView.setTextSize(14);
                return view;
            }
        };
        listView.setAdapter(adapter);

    }
}
