package com.grondstofapps.calculator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mariuszgromada.math.mxparser.Expression;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class CurrencyActivity extends AppCompatActivity {

    private ArrayList<String> currencies;
    private EditText editTextVal;
    private TextView textViewResult, textViewTT, textViewTT2;
    private Spinner spinnerfrom, spinnerto;
    private Button buttonconvert;
    private String stringfrom, stringto;
    private String alltext = "", btc = "", eur = "", gbp = "", gold = "", tl = "", usd = "";
    private ArrayList<String> currencyNames;

    public static boolean checkInternetConnection(Context context) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (conMgr.getActiveNetworkInfo() != null && conMgr.getActiveNetworkInfo().isAvailable() && conMgr.getActiveNetworkInfo().isConnected())
                return true;
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        boolean connected = checkInternetConnection(getApplicationContext());
        if (!connected) {
            Toast.makeText(getApplicationContext(), "Döviz hesabı için geçerli bir internet bağlantısı gereklidir.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        editTextVal = findViewById(R.id.editTextVal);
        textViewResult = findViewById(R.id.textViewResult);
        textViewTT = findViewById(R.id.textViewTT);
        textViewTT2 = findViewById(R.id.textViewTT2);
        buttonconvert = findViewById(R.id.buttonconvert);
        spinnerfrom = findViewById(R.id.spinnerfrom);
        spinnerto = findViewById(R.id.spinnerto);
        currencies = new ArrayList<>();

        currencyNames = new ArrayList<>();
        currencyNames.add("USD");
        currencyNames.add("EUR");
        currencyNames.add("GBP");
        currencyNames.add("GOLD");
        currencyNames.add("BTC");
        currencyNames.add("TL");

        final DatabaseReference reference = FirebaseDatabase.getInstance("https://calculator-28cc6-default-rtdb.firebaseio.com").getReference();

        textViewTT.setText("Dolar (USD)\nEuro (EUR)\nSterlin (GBP)\n1 Gram Altın (GOLD)\nBitcoin (BTC)");


        final ArrayAdapter<String> currencyAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencyNames);
        currencyAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerfrom.setAdapter(currencyAdapter1);

        spinnerfrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (getIntent().getBooleanExtra("check", false) == true) {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                }
                stringfrom = currencyNames.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<String> currencyAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencyNames);
        currencyAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerto.setAdapter(currencyAdapter2);

        spinnerto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (getIntent().getBooleanExtra("check", false) == true) {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                }
                stringto = currencyNames.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usd = dataSnapshot.child("USD").getValue(String.class);
                eur = dataSnapshot.child("EUR").getValue(String.class);
                gbp = dataSnapshot.child("GBP").getValue(String.class);
                gold = dataSnapshot.child("GOLD").getValue(String.class);
                btc = dataSnapshot.child("BTC").getValue(String.class);
                tl = "1";
                currencies.clear();
                currencies.add(usd);
                currencies.add(eur);
                currencies.add(gbp);
                currencies.add(gold);
                currencies.add(btc);
                currencies.add(tl);

                textViewTT2.setText(usd+"\n"+eur+"\n"+gbp+"\n"+gold+"\n"+btc);

                buttonconvert.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editTextVal.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Invalid operation, please enter a value.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (int i = 0; i < currencies.size(); i++) {
                            for (int j = 0; j < currencies.size(); j++) {
                                if (stringfrom.equals(currencyNames.get(i)) && stringto.equals(currencyNames.get(j))) {
                                    Expression e = new Expression(editTextVal.getText().toString() + "*" + currencies.get(i) + "/" + currencies.get(j));
                                    DecimalFormat df = new DecimalFormat("0.0000");
                                    String result = e.calculate()+"";
                                    if(result.substring(result.length()-2).equals(".0")){
                                        result = result.substring(0,result.length()-2);
                                        textViewResult.setText(editTextVal.getText() + " " + stringfrom + " = " + result + " " + stringto);
                                        if (textViewResult.getText().toString().length() > 20) {
                                            textViewResult.setTextSize(20);
                                        }
                                        return;
                                    }
                                    result = df.format(e.calculate())+"";
                                    textViewResult.setText(editTextVal.getText() + " " + stringfrom + " = " + result + " " + stringto);
                                    if (textViewResult.getText().toString().length() > 20) {
                                        textViewResult.setTextSize(20);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}


