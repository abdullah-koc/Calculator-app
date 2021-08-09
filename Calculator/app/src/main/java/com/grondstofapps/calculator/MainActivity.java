package com.grondstofapps.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.Toast;

import org.mariuszgromada.math.mxparser.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button buttonpi, buttonHistory, buttonCurrency, buttonMode;
    private EditText editText, editTextT;
    private Intent goToHistory;
    private ArrayList<String> list;
    private boolean check, checkLang = false;
    private boolean gotocur = false;
    private SharedPreferences sharedPreferences;
    public static final String MyPREFERENCES = "nightModePrefs";
    public static final String KEY_ISNIGHTMODE = "isNightMode";

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
        setContentView(R.layout.activity_main);

        buttonMode = findViewById(R.id.buttonMode);
        buttonCurrency = findViewById(R.id.buttonCurrency);
        editText = findViewById(R.id.editText);
        editTextT = findViewById(R.id.editTextT);
        editTextT.setKeyListener(null);
        editTextT.setTextIsSelectable(true);
        buttonpi = findViewById(R.id.buttonpi);
        buttonHistory = findViewById(R.id.buttonHistory);
        goToHistory = new Intent(MainActivity.this, HistoryActivity.class);
        list = new ArrayList<>();
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        checkNightMode();
        buttonMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    saveNightModeState(true);
                    check = true;
                    gotocur = check;
                    recreate();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    saveNightModeState(false);
                    check = false;
                    gotocur = check;
                    recreate();
                }
            }
        });
        editText.setShowSoftInputOnFocus(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        buttonCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection(getApplicationContext())) {
                    Intent intent = new Intent(MainActivity.this, CurrencyActivity.class);
                    intent.putExtra("check", gotocur);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Döviz hesabı için geçerli bir internet bağlantısı gereklidir.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHistory.putStringArrayListExtra("history", list);
                goToHistory.putExtra("check", check + "");
                startActivity(goToHistory);
            }
        });

        buttonpi.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Expression e = new Expression("pi");
                String pi = e.calculate() + "";
                int cursorPos = editText.getSelectionStart();
                String txt = editText.getText().toString();
                if (txt.length() == 0) {
                    updateText(pi);
                    editText.setSelection(cursorPos + 17);
                    return true;
                }
                if (txt.charAt(cursorPos - 1) != '×' && txt.charAt(cursorPos - 1) != '÷' && txt.charAt(cursorPos - 1) != '+' && txt.charAt(cursorPos - 1) != '-') {
                    updateText("×" + pi);
                    editText.setSelection(cursorPos + 18);
                    return true;
                }
                updateText(pi);
                editText.setSelection(cursorPos + 17);
                return true;
            }
        });

    }

    private void saveNightModeState(boolean b) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(KEY_ISNIGHTMODE, b);
        editor.apply();
    }

    private void checkNightMode() {
        if (sharedPreferences.getBoolean(KEY_ISNIGHTMODE, false)) {
            check = true;
            gotocur = check;
            buttonMode.setText("GÜNDÜZ");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            check = false;
            gotocur = check;
            buttonMode.setText("GECE");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void updateText(String txt) {
        String oldstr = editText.getText().toString();
        int cursorPos = editText.getSelectionStart();
        String leftStr = oldstr.substring(0, cursorPos);
        String rightStr = oldstr.substring(cursorPos);

        if(oldstr.length() > 0 && cursorPos == 0){
            editText.setText(txt + oldstr);
            editTextT.setText(new Expression((editText.getText() + "").replaceAll("×", "*").replaceAll("÷", "/")).calculate() + "");
            editText.setSelection(cursorPos + 1);
            setTextSize(editText);
            return;
        }

        if (oldstr.length() > 0 && oldstr.charAt(cursorPos - 1) == ')' &&
                (txt == "0" || txt == "1" || txt == "2" || txt == "3" || txt == "4" || txt == "5" ||
                        txt == "6" || txt == "7" || txt == "8" || txt == "9")) {
            String txtT = "×" + txt;
            editText.setText(String.format("%s%s%s", leftStr, txtT, rightStr));
            editText.setSelection(cursorPos + 2);
            setTextSize(editText);
            editTextT.setText(new Expression((editText.getText() + "").replaceAll("×", "*").replaceAll("÷", "/")).calculate() + "");
            return;
        }
        if (oldstr.length() > 0 && (txt.equals("-") || txt.equals("+") || txt.equals("÷") || txt.equals("×"))) {
            if (oldstr.charAt(oldstr.length() - 1) == '+' || oldstr.charAt(oldstr.length() - 1) == '-' || oldstr.charAt(oldstr.length() - 1) == '×' || oldstr.charAt(oldstr.length() - 1) == '÷') {
                editText.setText(oldstr.substring(0, oldstr.length() - 1) + txt);
                editText.setSelection(cursorPos);
                editTextT.setText(new Expression((editText.getText() + "").replaceAll("×", "*").replaceAll("÷", "/")).calculate() + "");
                return;
            }
        }
        editText.setText(String.format("%s%s%s", leftStr, txt, rightStr));
        editTextT.setText(new Expression((editText.getText().toString()).replaceAll("×", "*").replaceAll("÷", "/")).calculate() + "");
        editText.setSelection(cursorPos + 1);
        setTextSize(editText);

    }

    private void setTextSize(EditText editText) {
        if (editText.getText().toString().length() < 10) {
            editText.setTextSize(36);
        }
        if (editText.getText().toString().length() > 10 && editText.getText().toString().length() < 20) {
            editText.setTextSize(30);
        }
        if (editText.getText().toString().length() > 20) {
            editText.setTextSize(22);
        }
    }

    public void zerobutton(View view) {
        updateText("0");
    }

    public void onebutton(View view) {
        updateText("1");
    }

    public void twobutton(View view) {
        updateText("2");
    }

    public void threebutton(View view) {
        updateText("3");
    }

    public void fourbutton(View view) {
        updateText("4");
    }

    public void fivebutton(View view) {
        updateText("5");
    }

    public void sixbutton(View view) {
        updateText("6");
    }

    public void sevenbutton(View view) {
        updateText("7");
    }

    public void eightbutton(View view) {
        updateText("8");
    }

    public void ninebutton(View view) {
        updateText("9");
    }

    public void powerbutton(View view) {
        updateText("^");
    }

    public void multiplybutton(View view) {
        updateText("×");
    }

    public void dividebutton(View view) {
        updateText("÷");
    }

    public void addbutton(View view) {
        updateText("+");
    }

    public void subtractbutton(View view) {
        updateText("-");
    }

    public void percentagebutton(View view) {
        if (editText.getText().length() > 0) {
            updateText("%");
        }
    }

    public void clearbutton(View view) {
        editText.setText("");
        editTextT.setText("");
    }

    public void dotbutton(View view) {
        if (editText.getText().toString().length() == 0) {
            updateText("0.");
            editText.setSelection(editText.getSelectionStart() + 1);
            return;
        }
        int noTemp = 0;
        String text = editText.getText().toString();
        String temp = text;
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == '+' || temp.charAt(i) == '-' || temp.charAt(i) == '×' || temp.charAt(i) == '÷') {
                noTemp++;
            }
        }
        if (noTemp == 0) {
            if (text.contains(".")) {
                return;
            } else {
                updateText(".");
                return;
            }
        }
        int temp2 = 0;
        int saveI = 0;
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) == '+' || temp.charAt(i) == '-' || temp.charAt(i) == '×' || temp.charAt(i) == '÷') {
                temp2++;
                if (temp2 == noTemp) {
                    saveI = i;
                }
            }
        }
        temp = temp.substring(saveI + 1);
        if (temp.contains(".")) {
            return;
        } else {
            updateText(".");
        }
        //updateText(".");
    }

    public void pibutton(View view) {
        int cursorPos = editText.getSelectionStart();
        String txt = editText.getText().toString();
        if (txt.length() == 0) {
            updateText("3.14");
            editText.setSelection(cursorPos + 4);
            return;
        }
        if (txt.charAt(cursorPos - 1) != '×' && txt.charAt(cursorPos - 1) != '÷' && txt.charAt(cursorPos - 1) != '+' && txt.charAt(cursorPos - 1) != '-') {
            updateText("×3.14");
            editText.setSelection(cursorPos + 5);
            return;
        }
        updateText("3.14");
        editText.setSelection(cursorPos + 4);
    }

    public void reversebutton(View view) {
        String expression = "(" + editText.getText().toString() + ")";
        ;
        String addToList = "1/" + expression;
        expression = expression.replaceAll("÷", "/");
        expression = expression.replaceAll("×", "*");
        expression = expression.replaceAll(",", ".");
        int openp = 0;
        int closep = 0;
        for (int i = 0; i < expression.length() - 2; i++) {
            if (editText.getText().toString().substring(i, i + 1).equals("(")) {
                openp += 1;
            }
            if (editText.getText().toString().substring(i, i + 1).equals(")")) {
                closep += 1;
            }
        }
        if (openp > closep) {
            for (int i = 0; i < openp - closep; i++) {
                expression += ")";
            }
        }

        Expression e = new Expression("1/" + expression);

        String result = String.valueOf(e.calculate());


        if (result.substring(result.length() - 2).equals(".0")) {
            result = result.substring(0, result.length() - 2);
        }
        addToList += " = " + result;
        list.add(addToList);
        editText.setText(result);
        editTextT.setText("");
        editText.setSelection(result.length());
        setTextSize(editText);
    }

    public void equalsbutton(View view) {
        editTextT.setText("");
        String expression = editText.getText().toString();

        int openp = 0;
        int closep = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (editText.getText().toString().substring(i, i + 1).equals("(")) {
                openp += 1;
            }
            if (editText.getText().toString().substring(i, i + 1).equals(")")) {
                closep += 1;
            }
        }
        if (openp > closep) {
            for (int i = 0; i < openp - closep; i++) {
                expression += ")";
            }
        }
        String addToList = expression;
        expression = expression.replaceAll("÷", "/");
        expression = expression.replaceAll("×", "*");
        expression = expression.replaceAll(",", ".");

        if (expression.contains("%")) {
            if (expression.charAt(expression.length() - 1) == '%') {
                expression = expression.replaceAll("%", "*(1/100)");
            } else {
                expression = expression.replaceAll("%", "*(1/100)*");
            }
        }
        Expression e = new Expression(expression);

        String result = String.valueOf(e.calculate());


        if (result.equals("NaN")) {
            editText.setText("Invalid Expression");
        }

        if (result.substring(result.length() - 2).equals(".0")) {
            result = result.substring(0, result.length() - 2);
        }
        addToList += " = " + result;
        if (!addToList.substring(0, addToList.indexOf(" =")).equals(addToList.substring(addToList.indexOf(" =") + 3))) {
            list.add(addToList);
        }
        editText.setText(result);
        editText.setSelection(result.length());
        setTextSize(editText);
    }

    public void deletebutton(View view) {
        int cursorPos = editText.getSelectionStart();
        int textLength = editText.getText().length();
        if (cursorPos != 0 && textLength != 0) {
            SpannableStringBuilder selection = (SpannableStringBuilder) editText.getText();
            selection.replace(cursorPos - 1, cursorPos, "");
            editText.setText(selection);
            editText.setSelection(cursorPos - 1);
            setTextSize(editText);
        }
        if (editText.length() == 0) {
            editTextT.setText("");
            return;
        }
        editTextT.setText(new Expression(editText.getText() + "").calculate() + "");
    }

    public void openclosepbutton(View view) {
        try{
            int cursorPos = editText.getSelectionStart();
            int openp = 0;
            int closep = 0;
            int textLength = editText.getText().length();
            for (int i = 0; i < cursorPos; i++) {
                if (editText.getText().toString().substring(i, i + 1).equals("(")) {
                    openp += 1;
                }
                if (editText.getText().toString().substring(i, i + 1).equals(")")) {
                    closep += 1;
                }
            }
            if (openp == closep || editText.getText().toString().substring(textLength - 1, textLength).equals("(")) {
                if (editText.getText().toString().length() == 0) {
                    updateText("(");
                    editText.setSelection(cursorPos + 1);
                    return;
                }
                if (!(editText.getText().toString().charAt(cursorPos - 1) == '+' ||
                        editText.getText().toString().charAt(cursorPos - 1) == '-' ||
                        editText.getText().toString().charAt(cursorPos - 1) == '÷' ||
                        editText.getText().toString().charAt(cursorPos - 1) == '(' ||
                        editText.getText().toString().charAt(cursorPos - 1) == '^' ||
                        editText.getText().toString().charAt(cursorPos - 1) == '%' ||
                        editText.getText().toString().charAt(cursorPos - 1) == '×')) {
                    updateText("×(");
                    editText.setSelection(cursorPos + 2);
                    return;
                }
                updateText("(");
                editText.setSelection(cursorPos + 1);
            } else if (closep < openp && !editText.getText().toString().substring(textLength - 1, textLength).equals("(")) {
                updateText(")");
                editText.setSelection(cursorPos + 1);
            }
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Geçersiz operasyon", Toast.LENGTH_LONG).show();
        }
    }

    public void sqrtbutton(View view) {
        editTextT.setText("");
        String expression = "(" + editText.getText().toString() + ")";
        String addToList = "sqrt" + expression;
        expression = expression.replaceAll("÷", "/");
        expression = expression.replaceAll("×", "*");
        expression = expression.replaceAll(",", ".");

        int openp = 0;
        int closep = 0;
        for (int i = 0; i < expression.length() - 2; i++) {
            if (editText.getText().toString().substring(i, i + 1).equals("(")) {
                openp += 1;
            }
            if (editText.getText().toString().substring(i, i + 1).equals(")")) {
                closep += 1;
            }
        }
        if (openp > closep) {
            for (int i = 0; i < openp - closep; i++) {
                expression += ")";
            }
        }

        Expression e = new Expression(expression + "^(1/2)");

        String result = String.valueOf(e.calculate());

        if (result.substring(result.length() - 2).equals(".0")) {
            result = result.substring(0, result.length() - 2);
        }
        addToList += " = " + result;
        list.add(addToList);
        editText.setText(result);
        editText.setSelection(result.length());
        setTextSize(editText);

    }


}
