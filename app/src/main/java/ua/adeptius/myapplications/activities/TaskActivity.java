package ua.adeptius.myapplications.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.DataBase;
import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.service.ServiceTaskChecker;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Visual.MATCH_WRAP;
import static ua.adeptius.myapplications.util.Visual.WRAP_MACH;
import static ua.adeptius.myapplications.util.Visual.WRAP_WRAP;
import static ua.adeptius.myapplications.util.Utilites.myLog;

public class TaskActivity extends AppCompatActivity implements View.OnClickListener {

    private Button commentButton, closeButton, takeButton, googleButton, cableButton, pingButton;
    Task task;
    LinearLayout taskScrollView;
    static int slot;
    boolean taskIsYours;
    String[] phones;
    TextView dogovor;
    LinearLayout currentHeader;
    private LinkedList<View> views = new LinkedList<>();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        if (Settings.isSwitchPortrait())
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);


        taskScrollView = (LinearLayout) findViewById(R.id.task_scroll_view);
        Intent intent = getIntent();
        slot = Integer.parseInt(intent.getStringExtra("position"));
        task = MainActivity.tasks.get(slot);
        taskIsYours = !Settings.getCurrentLogin().equals(task.getWho());

        phones = getAllNumbers();
        final String phoneForHeader = phones[0].substring(0, 3) + " "
                + phones[0].substring(3, 6) + " "
                + phones[0].substring(6);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callToAbon();
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(task.getAddr());
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(phoneForHeader);
                    isShow = false;
                }
            }
        });
        collapsingToolbarLayout.setTitle(phoneForHeader);


        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                drawAll();
            }
        });
    }

    void deleteCurrentTask(){
        MainActivity.mainScrollView.removeView(currentHeader);
    }

    void drawAll() {
        // основная инфа
        currentHeader = Visual.getHeader(task, this);
        currentHeader.setLayoutParams(MATCH_WRAP);
        views.add(currentHeader);

        views.add(addHorizontalSeparator());

        // Имя абонента
        TextView nameField = new TextView(getApplicationContext());
        nameField.setPadding(10, 10, 0, 10);
        nameField.setText(task.getName()); //+"\n"+task.getPhone()
        nameField.setTextColor(Color.WHITE);
        nameField.setGravity(Gravity.CENTER);
        nameField.setBackgroundColor(Visual.CORPORATE_COLOR);
        nameField.setTextSize(17);
        views.add(nameField);

        views.add(addHorizontalSeparator());

        //айпи, пароли....
        LinearLayout horizontalVievForTask = new LinearLayout(this);
        horizontalVievForTask.setOrientation(LinearLayout.HORIZONTAL);
        dogovor = new TextView(getApplicationContext());
        dogovor.setText("Договор: " + task.getCard() + "\nЛогин:     " + task.getLoglk() + "\nПароль: ******");
        dogovor.setBackgroundColor(Visual.CORPORATE_COLOR);
        dogovor.setTextColor(Color.WHITE);
        dogovor.setTextSize(16);
        dogovor.setPadding(20, 0, 0, 0);
        horizontalVievForTask.addView(dogovor, WRAP_MACH);
        dogovor.setOnClickListener(this);

        TextView justVerticalSpace = new TextView(getApplicationContext());
        justVerticalSpace.setText(" ");
        horizontalVievForTask.addView(justVerticalSpace, WRAP_MACH);
        TextView ips = new TextView(this);
        ips.setGravity(Gravity.RIGHT);
        ips.setText(task.getIp() + "\n" + task.getMasc() + "\n" + task.getGateway());
        ips.setTextColor(Color.WHITE);
        ips.setBackgroundColor(Visual.CORPORATE_COLOR);
        ips.setPadding(0, 0, 20, 0);
        ips.setTextSize(16);
        horizontalVievForTask.addView(ips, WRAP_MACH);

        dogovor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ips.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        horizontalVievForTask.setLayoutParams(MATCH_WRAP);
        views.add(horizontalVievForTask);

        views.add(addHorizontalSeparator());

        TextView elseInfo = new TextView(this);
        StringBuffer sb = new StringBuffer("");
        sb.append(task.getCity() + " (" + task.getDistrikt() + ")").append("\n")
                .append("Создал: " + task.getUser() + " " + task.getDatetime()).append("\n")
                .append("Свич" + ": " + task.getSwitch_ip())
                .append(" : " + task.getSwitch_port())
                .append(" Геркон" + ": " + task.getGerkon());
        elseInfo.setText(sb.toString());
        elseInfo.setPadding(10, 10, 0, 10);
        elseInfo.setTextColor(Color.WHITE);
        elseInfo.setBackgroundColor(Visual.CORPORATE_COLOR);
        elseInfo.setLayoutParams(MATCH_WRAP);
        views.add(elseInfo);

        views.add(addHorizontalSeparator());

        if (task.getSubject().equals("Юр")) {
            TextView aktField = new TextView(getApplicationContext());
            aktField.setPadding(10, 10, 0, 10);
            aktField.setGravity(Gravity.CENTER);
            aktField.setText("VIP! ВЗЯТЬ АКТ!");
            aktField.setTextColor(Color.WHITE);
            aktField.setBackgroundColor(Visual.NEW_TASKS_COLOR);
            aktField.setTextSize(17);
            views.add(aktField);

            views.add(addHorizontalSeparator());
        }

        String[] comments = task.getComments();
        for (int i = 0; i < comments.length; i++) {
            TextView commentView = new TextView(this);
            commentView.setText(comments[i]);
            commentView.setPadding(15, 5, 10, 10);
            commentView.setTextColor(Color.WHITE);
            commentView.setBackgroundColor(Color.parseColor("#1e88e5"));
            if (i % 2 == 0) commentView.setBackgroundColor(Color.parseColor("#1976d2"));
            commentView.setLayoutParams(MATCH_WRAP);
            views.add(commentView);
        }

        // создаю кнопку показать на карте
        LinearLayout buttonsLayout2 = new LinearLayout(this);
        buttonsLayout2.setOrientation(LinearLayout.HORIZONTAL);
        googleButton = new Button(this);
        googleButton.setText("Показать абонента на карте");
        googleButton.setOnClickListener(this);
        buttonsLayout2.addView(googleButton, WRAP_MACH);
        LinearLayout.LayoutParams lParams22 = (LinearLayout.LayoutParams) googleButton.getLayoutParams();
        lParams22.weight = 1;
        buttonsLayout2.setLayoutParams(MATCH_WRAP);
        views.add(buttonsLayout2);


        //создаём лэйаут с кнопками Пинга и теста кабеля
        LinearLayout buttonsLayout3 = new LinearLayout(this);
        buttonsLayout3.setOrientation(LinearLayout.HORIZONTAL);
        pingButton = new Button(this);
        cableButton = new Button(this);
        pingButton.setText("Ping");
        cableButton.setText("Порт");
        pingButton.setOnClickListener(this);
        cableButton.setOnClickListener(this);
        buttonsLayout3.addView(pingButton, WRAP_WRAP);
        buttonsLayout3.addView(cableButton, WRAP_WRAP);
        buttonsLayout3.setLayoutParams(MATCH_WRAP);
        views.add(buttonsLayout3);

        LinearLayout.LayoutParams lParams3 = (LinearLayout.LayoutParams) pingButton.getLayoutParams();
        LinearLayout.LayoutParams lParams4 = (LinearLayout.LayoutParams) cableButton.getLayoutParams();
        lParams3.weight = 1;
        lParams4.weight = 1;

        if (task.getIp().equals("Неизвестно"))
        handler.post(new Runnable() {
            @Override
            public void run() {
                pingButton.setEnabled(false);
            }
        });

        if (task.getSwitch_ip().equals("Неизвестно") || task.getSwitch_port().equals("Неизвестно"))
            handler.post(new Runnable() {
                @Override
                public void run() {
                    cableButton.setEnabled(false);
                }
            });


        //создаём лэйаут с кнопками комента и закрытия заявки
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        commentButton = new Button(this);
        closeButton = new Button(this);
        takeButton = new Button(this);
        commentButton.setText("Комент");
        closeButton.setText("Закрыть");
        takeButton.setText("Взять");
        commentButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        takeButton.setOnClickListener(this);
        buttonsLayout.addView(commentButton, WRAP_WRAP);
        buttonsLayout.addView(closeButton, WRAP_WRAP);
        if (taskIsYours) buttonsLayout.addView(takeButton, WRAP_WRAP);
        buttonsLayout.setLayoutParams(MATCH_WRAP);
        views.add(buttonsLayout);

        LinearLayout.LayoutParams lParams1 = (LinearLayout.LayoutParams) commentButton.getLayoutParams();
        LinearLayout.LayoutParams lParams2 = (LinearLayout.LayoutParams) closeButton.getLayoutParams();
        if (taskIsYours) {
            LinearLayout.LayoutParams lParams5 = (LinearLayout.LayoutParams) takeButton.getLayoutParams();
            lParams5.weight = 1;
        }
        lParams1.weight = 1;
        lParams2.weight = 1;

        for (View v : views) {
            final View view = v;

                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        taskScrollView.addView(view);
                        view.startAnimation(AnimationUtils.loadAnimation(TaskActivity.this, R.anim.task_screen_trans));

                    }
                });
            if (v.getPaddingRight() != 8) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    View addHorizontalSeparator() {
        TextView justHorizontalSpace = new TextView(this);
        justHorizontalSpace.setTextSize(2);
        justHorizontalSpace.setLayoutParams(MATCH_WRAP);
        justHorizontalSpace.setPadding(0,0,8,0);
        return justHorizontalSpace;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(googleButton)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + task.getCity() + " "
                    + task.getAddr().substring(0, task.getAddr().lastIndexOf("кв") - 1)));
            startActivity(intent);
        }
        if (v.equals(pingButton)) {
            Intent intent = new Intent(this, PingActivity.class);
            String ip = task.getIp();
            if (ip.contains(" ")) ip = ip.substring(0, task.getIp().indexOf(' '));
            intent.putExtra("ip", ip);
            startActivity(intent);
        }
        if (v.equals(cableButton)) {
            Intent intent = new Intent(this, CableTestActivity.class);
            intent.putExtra("switch", task.getSwitch_ip());
            intent.putExtra("port", task.getSwitch_port());
            startActivity(intent);
        }
        // коментирование заявки
        final View vv = v;
        if (v.equals(commentButton)) { // Если нажата кнопка комментарий
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText commentView = new EditText(this);
            commentView.setCursorVisible(true);
            commentView.hasFocus();
            builder.setMessage("Коментарий:");
            builder.setCancelable(true);
            builder.setView(commentView);
            builder.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String coment = commentView.getText().toString();
                    coment = coment.replace("!!!!!!", "!");
                    coment = coment.replace("!!!!!", "!");
                    coment = coment.replace("!!!!", "!");
                    coment = coment.replace("!!!", "!");
                    coment = coment.replace("!!", "!");
                    boolean commentOK = false;
                    myLog("ввели коментарий: " + coment + ". И нажали добавить");
                    try {
                        String[] request = new String[6];
                        request[0] = "http://188.231.188.188/api/task_api_close.php";
                        request[1] = "begun=" + Settings.getCurrentLogin();
                        request[2] = "drowssap=" + Settings.getCurrentPassword();
                        request[3] = "act=comment";
                        request[4] = "comment=" + URLEncoder.encode(coment, "UTF-8");
                        request[5] = "task_id=" + task.getId();

                        Map<String, String> map = EXECUTOR.submit(new DataBase(request)).get().get(0);
                        if (map.get("task").equals("comented"))  commentOK = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (commentOK)
                        Snackbar.make(vv, "Коментарий добавлен", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    else
                        Snackbar.make(vv, "Ошибка. Вероятно нет интернета", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        // закрытие заявки
        if (v.equals(closeButton)) {  // Если нажата кнопка закрыть заявку
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText commentView = new EditText(this);
            builder.setMessage("Коментарий:");
            builder.setCancelable(true);
            builder.setView(commentView);
            builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() { // просто коментарий
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String coment = commentView.getText().toString();
                    boolean commentOK = false;
                    myLog("ввели коментарий: " + coment + ". И нажали закрыть");
                    try {
                        String[] request = new String[7];
                        request[0] = "http://188.231.188.188/api/task_api_close.php";
                        request[1] = "begun=" + Settings.getCurrentLogin();
                        request[2] = "drowssap=" + Settings.getCurrentPassword();
                        request[3] = "act=close";
                        request[4] = "comment=" + URLEncoder.encode(coment, "UTF-8");
                        request[5] = "task_id=" + task.getId();
                        request[6] = "dlina=0";

                        Map<String, String> map = EXECUTOR.submit(new DataBase(request)).get().get(0);
                        if (map.get("task").equals("closed")) {
                            commentOK = true;
                        }

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (Exception ignored) {
                    }

                    if (commentOK) {
                        Snackbar.make(vv, "Заявка закрыта", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        deleteCurrentTask();
                    }else
                        Snackbar.make(vv, "Ошибка. Вероятно нет интернета", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("Указать метраж и закрыть", new DialogInterface.OnClickListener() { // еще и кабель
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String coment = commentView.getText().toString();
                    closeWithCable(coment, vv);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if (v.equals(takeButton)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Взять заявку?");
            builder.setCancelable(true);
            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean takeOK = false;
                    try {
                        String[] request = new String[5];
                        request[0] = "http://188.231.188.188/api/task_api_close.php";
                        request[1] = "begun=" + Settings.getCurrentLogin();
                        request[2] = "drowssap=" + Settings.getCurrentPassword();
                        request[3] = "act=accept";
                        request[4] = "task_id=" + task.getId();
                        Map<String, String> map = EXECUTOR.submit(new DataBase(request)).get().get(0);
                        if (map.get("task").equals("accepted")) {
                            takeOK = true;
                            ServiceTaskChecker.wasTasksIds.add(task.getId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (takeOK)
                        Snackbar.make(vv, "Заявка взята", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    else
                        Snackbar.make(vv, "Ошибка. Вероятно нет интернета", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if (v.equals(dogovor)){
            dogovor.setText("Договор: " + task.getCard() + "\nЛогин:     " + task.getLoglk() + "\nПароль:   " + task.getPasslk());
        }
    }

    // Закрытие заявки с указанием метража
    public void closeWithCable(final String coment, final View vv) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Сколько кабеля использовано?");
        final EditText cableView = new EditText(this);
        cableView.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(cableView);
        builder.setCancelable(true);
        builder.setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String cable = cableView.getText().toString();
                myLog("ввели коментарий: " + coment + ", кабель: " + cable + "м. И нажали добавить");
                boolean commentOK = false;
                myLog("ввели коментарий: " + coment + ". И нажали закрыть");
                try {
                    String[] request = new String[7];
                    request[0] = "http://188.231.188.188/api/task_api_close.php";
                    request[1] = "begun=" + Settings.getCurrentLogin();
                    request[2] = "drowssap=" + Settings.getCurrentPassword();
                    request[3] = "act=close";
                    request[4] = "comment=" + URLEncoder.encode(coment, "UTF-8");
                    request[5] = "task_id=" + task.getId();
                    request[6] = "dlina=" + cable;
                    Map<String, String> map = EXECUTOR.submit(new DataBase(request)).get().get(0);
                    if (map.get("task").equals("closed")) commentOK = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (commentOK == true) {
                    Snackbar.make(vv, "Заявка закрыта", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    deleteCurrentTask();
                }else
                    Snackbar.make(vv, "Ошибка. Вероятно нет интернета", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    void callToAbon() {
        if (phones.length == 1) {
            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phones[0]));
            startActivity(intent1);
        } else {
            String[] cloneForUser = new String[phones.length];
            for (int i = 0; i < cloneForUser.length; i++) {
                cloneForUser[i] = "(" + phones[i].substring(0, 3) + ") "
                        + phones[i].substring(3, 6) + " - "
                        + phones[i].substring(6, 8) + " - "
                        + phones[i].substring(8, 10);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Выберите номер"); // заголовок для диалога
            builder.setItems(cloneForUser, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + phones[item]));
                    startActivity(intent1);
                    dialog.dismiss();
                }
            });
            builder.setCancelable(true);
            builder.show();
        }
    }

    String[] getAllNumbers() {
        String s = "";
        for (int i = 0; i < task.getComments().length; i++) {
            s += task.getComments()[i] + "H";
        }
        s += task.getPhone();
        s = s.replace("\n", "g");
        ArrayList<String> phones = new ArrayList<>();
        s = s.replace(" ", "");
        s = s.replace("\n", "");
        s = s.replace("-", "");
        s = s.replace("(20", "");
        String s1 = "";
        try {
            Pattern regex = Pattern.compile("(?:\\d{10,12})+");
            Matcher regexMatcher = regex.matcher(s);
            while (regexMatcher.find()) {
                s1 = regexMatcher.group();
                if (s1.length() == 11) s1 = s1.substring(1);
                if (s1.length() == 12) s1 = s1.substring(2);
                if (!phones.contains(s1)) phones.add(s1);
            }
        } catch (PatternSyntaxException ex) {
        }
        String[] result = new String[phones.size()];
        for (int i = 0; i < phones.size(); i++) {
            result[i] = phones.get(i);
            myLog("пропарсил номер" + result[i]);
        }

        if (result.length == 0){
            result = new String[1];
            result[0] = "0000000000";
        }
        return result;
    }
}

