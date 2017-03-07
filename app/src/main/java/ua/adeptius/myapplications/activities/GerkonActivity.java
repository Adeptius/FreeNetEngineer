package ua.adeptius.myapplications.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.dao.GetInfo;
import ua.adeptius.myapplications.model.GerkonStatus;
import ua.adeptius.myapplications.model.OpeningBoxStatus;
import ua.adeptius.myapplications.util.Settings;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;

public class GerkonActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    EditText editBoxNumber;
    Button buttonOpen, buttonClose;
    ProgressDialog progressDialog;
    LinearLayout mainLayout, mainInfoLayout;
    TextView textResult;
    LinearLayout buttonLayout;


    TextView textIp, textPort, textAdminStatus, textCurrentIn, textLastDoind, textLastDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerkon);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        editBoxNumber = (EditText) findViewById(R.id.editText_box_number);
        editBoxNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        editBoxNumber.setOnEditorActionListener(this);
        buttonOpen = (Button) findViewById(R.id.button_open);
        buttonClose = (Button) findViewById(R.id.button_close);
        mainLayout = (LinearLayout) findViewById(R.id.activity_gerkon);
        textResult = (TextView) findViewById(R.id.text_result);
        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        textIp = (TextView) findViewById(R.id.ip);
        textPort = (TextView) findViewById(R.id.port);
        textAdminStatus = (TextView) findViewById(R.id.adminStatus);
        textCurrentIn = (TextView) findViewById(R.id.currentIn);
        textLastDoind = (TextView) findViewById(R.id.LastDoing);
        textLastDate = (TextView) findViewById(R.id.lastDate);
        mainInfoLayout = (LinearLayout) findViewById(R.id.mainInfoLayout);
        mainInfoLayout.setVisibility(View.INVISIBLE);
        buttonClose.setVisibility(View.INVISIBLE);
        buttonOpen.setVisibility(View.INVISIBLE);

        editBoxNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonClose.setVisibility(View.INVISIBLE);
                buttonOpen.setVisibility(View.INVISIBLE);
                mainInfoLayout.setVisibility(View.INVISIBLE);
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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            checkBox(editBoxNumber.getText().toString());
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editBoxNumber.getWindowToken(), 0);
        }
        return true;
    }


    private void checkBox(final String box) {
        progressDialogShow();
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    boolean gerkonExist = GetInfo.isGerkonExist(box);
                    GerkonStatus status = GetInfo.getGerkonStatus(box);
                    setButtonEnabled(status.getStatusСode());

                    if (gerkonExist){
                        HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                mainInfoLayout.setVisibility(View.VISIBLE);
                            }
                        });
                        updateGerkonStatus(status);
//                        setButtonsVisible(true);
                        setTextResult("");
                    }else {
//                        setButtonsVisible(false);
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

    private void updateGerkonStatus(final GerkonStatus status){
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                textIp.setText(status.getIp());
                textPort.setText(status.getPort());
                if (status.isAdmin_up()){
                    textAdminStatus.setText("UP");
                    textAdminStatus.setTextColor(Color.GREEN);
                }else {
                    textAdminStatus.setText("DOWN");
                    textAdminStatus.setTextColor(Color.RED);
                }

                if (status.is_monitor()){
                    textCurrentIn.setText("На Мониторинге");
                    textCurrentIn.setTextColor(Color.GREEN);
                }else {
                    textCurrentIn.setText("Снят с мониторинга");
                    textCurrentIn.setTextColor(Color.RED);
                }
                textLastDoind.setText(status.getStatus());
                textLastDate.setText(status.getDate());
            }
        });
    }

    private void setButtonsVisible(final boolean visible){
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (visible){
                    buttonClose.setVisibility(View.INVISIBLE);
                    buttonOpen.setVisibility(View.VISIBLE);
                }else {
                    buttonClose.setVisibility(View.VISIBLE);
                    buttonOpen.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void openBox(final boolean needToOpen){
        progressDialogShow();
        final String box = editBoxNumber.getText().toString();
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    OpeningBoxStatus openingBoxStatus;
                    if (needToOpen){
                        openingBoxStatus = GetInfo.openBox(box);
                    }else {
                        openingBoxStatus = GetInfo.closeBox(box);
                    }
                    GerkonStatus status = GetInfo.getGerkonStatus(box);
                    updateGerkonStatus(status);
                    setButtonEnabled(openingBoxStatus.getAnswerCode());
//                    setTextResult(openingBoxStatus.getAnswer());
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
}
