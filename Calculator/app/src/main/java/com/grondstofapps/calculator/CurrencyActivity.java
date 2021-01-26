package com.grondstofapps.calculator;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mariuszgromada.math.mxparser.Expression;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class CurrencyActivity extends AppCompatActivity {

    private ArrayList<String> currencies;
    private EditText editTextVal;
    private TextView textViewResult, textViewTT, textViewTT2;
    private Spinner spinnerfrom, spinnerto;
    private Button buttonconvert;
    private String stringfrom, stringto;
    private String alltext, rub, eur, gbp, jpy, tl, usd;
    private ArrayList<String> currencyNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else {
            connected = false;
        }
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
        currencyNames.add("JPY");
        currencyNames.add("RUB");
        currencyNames.add("TL");


        new exc().execute();


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

    public class exc extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DecimalFormat df = new DecimalFormat("0.0000");
                Document doc = Jsoup.connect("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").get();
                alltext = doc.outerHtml();
                tl = "1";
                String eurPart = alltext.substring(alltext.indexOf("TRY") + 10, alltext.indexOf("TRY") + 30);
                eur = eurPart.substring(0, eurPart.indexOf("/>")-1).replaceAll("\"", "");
                String jpyPart = alltext.substring(alltext.indexOf("JPY") + 10, alltext.indexOf("JPY") + 30);
                jpy = jpyPart.substring(0, jpyPart.indexOf("/>")).replaceAll("\"", "");
                jpy = df.format(new Expression(eur + "/" + jpy).calculate()) + "";
                String usdPart = alltext.substring(alltext.indexOf("USD") + 10, alltext.indexOf("USD") + 30);
                usd = usdPart.substring(0, usdPart.indexOf("/>")).replaceAll("\"", "");
                usd = df.format(new Expression(eur + "/" + usd).calculate())+"";
                String gbpPart = alltext.substring(alltext.indexOf("GBP") + 10, alltext.indexOf("GBP") + 30);
                gbp = gbpPart.substring(0, gbpPart.indexOf("/>")).replaceAll("\"", "");
                gbp = df.format(new Expression(eur + "/" + gbp).calculate())+"";
                String rubPart = alltext.substring(alltext.indexOf("RUB") + 10, alltext.indexOf("RUB") + 30);
                rub = rubPart.substring(0, rubPart.indexOf("/>")).replaceAll("\"", "");
                rub = df.format(new Expression(eur + "/" + rub).calculate())+"";

            } catch (Exception e) {
                e.printStackTrace();
                Thread thread = new Thread() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(CurrencyActivity.this, "Bir hata oluştu. Lütfen tekrar deneyin.", Toast.LENGTH_LONG);
                            }
                        });
                    }
                };
                thread.start();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            currencies.add(usd);
            currencies.add(eur);
            currencies.add(gbp);
            currencies.add(jpy);
            currencies.add(rub);
            currencies.add(tl);
            textViewTT.setText("Dolar (USD)\nEuro (EUR)\nSterlin (GBP)\nJapon Yeni (JPY)\nRus Rublesi (RUB)");
            textViewTT2.setText(usd+"\n"+eur+"\n"+gbp+"\n"+jpy+"\n"+rub);

        }
    }
}


