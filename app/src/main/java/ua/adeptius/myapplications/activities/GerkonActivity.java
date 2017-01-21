package ua.adeptius.myapplications.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.DataBase;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;

public class GerkonActivity extends AppCompatActivity implements TextView.OnEditorActionListener {


    EditText editBoxNumber;
    Button buttonOpen, buttonClose;
    ProgressDialog progressDialog;
    LinearLayout mainLayout;
    TextView textResult;
    LinearLayout buttonLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerkon);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        String gerkon = intent.getStringExtra("gerkon");

        editBoxNumber = (EditText) findViewById(R.id.editText_box_number);
        editBoxNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        editBoxNumber.setOnEditorActionListener(this);
        buttonOpen = (Button) findViewById(R.id.button_open);
        buttonClose = (Button) findViewById(R.id.button_close);
        mainLayout = (LinearLayout) findViewById(R.id.activity_gerkon);
        textResult = (TextView) findViewById(R.id.text_result);
        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        buttonLayout.setVisibility(View.INVISIBLE);

        editBoxNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonClose.setVisibility(View.VISIBLE);
                buttonOpen.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBox(true);
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBox(false);
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void checkBox(final int box) {
        progressDialogShow();

        String[] request = new String[6];
        request[0] = "http://188.231.188.188/api/gerkon_api.php";
        request[1] = "begun=" + Settings.getCurrentLogin();
        request[2] = "drowssap=" + Settings.getCurrentPassword();
        request[3] = "tel=" + Settings.getPhone();
        request[4] = "pin=" + Settings.getPin();
        request[5] = "box=" + box;

        final DataBase dataBase = new DataBase(request);

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    Map<String, String> map = dataBase.call().get(0);
                    int code = Integer.parseInt(map.get("resbox"));
                    if (code==1){
                        setButtonsVisible(true);
                        setTextResult("");
                    }else {
                        setButtonsVisible(false);
                        setTextResult("Нет такого ящика");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    setButtonsVisible(false);
                    setTextResult("Ошибка. Возможно нет интернета.");
                }finally {
                    hideProgressDialog();
                }
            }
        });
    }

    private void setButtonsVisible(final boolean visible){
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (visible){
                    buttonLayout.setVisibility(View.VISIBLE);
                }else {
                    buttonLayout.setVisibility(View.INVISIBLE);

                }
            }
        });
    }

    private void openBox(boolean needToOpen){
        progressDialogShow();
        String box = editBoxNumber.getText().toString();
        String[] request = new String[7];
        request[0] = "http://188.231.188.188/api/gerkon_api.php";
        request[1] = "begun=" + Settings.getCurrentLogin();
        request[2] = "drowssap=" + Settings.getCurrentPassword();
        request[3] = "tel=" + Settings.getPhone();
        request[4] = "pin=" + Settings.getPin();
        request[5] = "box=" + box;
        request[6] = "action=" + (needToOpen ? "1" : "2");


        final DataBase dataBase = new DataBase(request);

        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    Map<String, String> map = dataBase.call().get(0);
                    String result = map.get("respact");
                    int code = Integer.parseInt(map.get("respcode"));
                    setButtonEnabled(code);
                    setTextResult(result);
                }catch (Exception e){
                    e.printStackTrace();
                    setTextResult("Ошибка. Возможно нет интернета.");
                }finally {
                    hideProgressDialog();
                }
            }
        });
    }

    private void setButtonEnabled(final int code){
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (code==8){
                    buttonClose.setVisibility(View.INVISIBLE);
                    buttonOpen.setVisibility(View.VISIBLE);
                }else if (code==9 || code==7){
                    buttonClose.setVisibility(View.VISIBLE);
                    buttonOpen.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    protected void progressDialogShow() {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(GerkonActivity.this, R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Подождите..");
                progressDialog.show();
            }
        });
    }

    protected void hideProgressDialog() {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }


    protected void setTextResult(final String message) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                textResult.setText(message);
            }
        });
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            checkBox(Integer.parseInt(editBoxNumber.getText().toString()));
        }
        return true;
    }
}
