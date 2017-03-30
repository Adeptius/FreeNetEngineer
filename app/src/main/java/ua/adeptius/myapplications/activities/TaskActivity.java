package ua.adeptius.myapplications.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.connection.DataBase;
import ua.adeptius.myapplications.dao.GetInfo;
import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.orders.TaskHistory;
import ua.adeptius.myapplications.service.ServiceTaskChecker;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.HANDLER;
import static ua.adeptius.myapplications.util.Utilites.myLog;
import static ua.adeptius.myapplications.util.Visual.MATCH_WRAP;
import static ua.adeptius.myapplications.util.Visual.WRAP_MACH;
import static ua.adeptius.myapplications.util.Visual.WRAP_WRAP;
import static ua.adeptius.myapplications.util.Visual.WRAP_WRAP_WEIGHT1;
import static ua.adeptius.myapplications.util.Visual.makeMyToast;

public class TaskActivity extends AppCompatActivity implements View.OnClickListener {

    private Button commentButton, closeButton, takeButton, googleButton, cableButton, pingButton, historyButton;
    Task task;
    LinearLayout taskScrollView;
    static int slot;
    boolean taskIsYours;
    TextView dogovor;
    LinearLayout currentHeader;
    private LinkedList<View> views = new LinkedList<>();

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

        String taskNumber = intent.getStringExtra("position");

        if (taskNumber != null) {
            slot = Integer.parseInt(taskNumber);
            task = MainActivity.tasks.get(slot);
        } else {
            String taskId = intent.getStringExtra("id");
            task = findTaskById(taskId);
        }


        taskIsYours = !Settings.getCurrentLogin().equals(task.getWho());

        final String phoneForHeader = task.getPhones()[0].substring(0, 3) + " "
                + task.getPhones()[0].substring(3, 6) + " "
                + task.getPhones()[0].substring(6);


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

    private Task findTaskById(String taskId) {
        MainActivity.tasks.get(slot);

        for (Task tas : MainActivity.tasks) {
            if (tas.getId().equals(taskId)) {
                return tas;
            }
        }
        return null;
    }

    void deleteCurrentTask() {
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
        nameField.setText(task.getName());
        nameField.setTextColor(Color.WHITE);
        nameField.setGravity(Gravity.CENTER);
        nameField.setBackgroundColor(Visual.CORPORATE_COLOR);
        nameField.setTextSize(17);
        views.add(nameField);

        views.add(addHorizontalSeparator());

        // Добавляю блок с договором и паролем
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

        // Добавляю блок с айпишками
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
        dogovor.setLayoutParams(WRAP_WRAP_WEIGHT1);
        ips.setLayoutParams(WRAP_WRAP_WEIGHT1);
        horizontalVievForTask.setLayoutParams(MATCH_WRAP);
        views.add(horizontalVievForTask);

        views.add(addHorizontalSeparator());

        //Кто создал и когда
        TextView elseInfo = new TextView(this);
        StringBuffer sb = new StringBuffer("");
        sb.append(task.getCity() + " (" + task.getDistrikt() + ")").append("\n")
                .append("Создал: " + task.getUser() + " " + task.getDatetime()).append("\n")
                .append("Свич" + ": " + task.getSwitch_ip())
                .append(" : " + task.getSwitch_port())
                .append(" Геркон" + ": " + task.getGerkon());
        if (task.getSw_place() != null) {
            if (!"null".equals(task.getSw_place())) {
                sb.append("\nРасположение свича: " + task.getSw_place());
//                        .replaceAll("свич стоит в", ""));
            }
        }

        elseInfo.setText(sb.toString());
        elseInfo.setPadding(10, 10, 0, 10);
        elseInfo.setTextColor(Color.WHITE);
        elseInfo.setBackgroundColor(Visual.CORPORATE_COLOR);
        elseInfo.setLayoutParams(MATCH_WRAP);
        views.add(elseInfo);

        views.add(addHorizontalSeparator());

        //Уведомление "Взять акт"
        if (task.getSubject().equals("Юр")) {
            addAktMessage();
        }

        if (task.getGarantServise().equals("1")) {
            addGarantServiceMessage();
        }

        //Добавляю комментарии
        String[] comments = task.getComments();
        for (int i = 0; i < comments.length; i++) {
            TextView commentView = new TextView(this);
            commentView.setText(comments[i]);
            commentView.setPadding(15, 5, 10, 10);
            commentView.setTextColor(Color.WHITE);
            commentView.setBackgroundColor(Color.parseColor("#1e88e5"));
            if (i % 2 == 0) commentView.setBackgroundColor(Color.parseColor("#1565C0"));
            commentView.setLayoutParams(MATCH_WRAP);
            views.add(commentView);
        }

        // Добавляю кнопку показать на карте
        LinearLayout buttonsLayout2 = new LinearLayout(this);
        buttonsLayout2.setOrientation(LinearLayout.HORIZONTAL);
        googleButton = new Button(this);
        googleButton.setText("Карта");
        googleButton.setOnClickListener(this);
        buttonsLayout2.addView(googleButton, WRAP_MACH);
        ((LinearLayout.LayoutParams) googleButton.getLayoutParams()).weight = 1;
        buttonsLayout2.setLayoutParams(MATCH_WRAP);

        historyButton = new Button(this);
        historyButton.setText("История");
        historyButton.setOnClickListener(this);
        buttonsLayout2.addView(historyButton, WRAP_MACH);
        ((LinearLayout.LayoutParams) historyButton.getLayoutParams()).weight = 1;
        buttonsLayout2.setLayoutParams(MATCH_WRAP);
        views.add(buttonsLayout2);


        //создаём лэйаут с кнопками Пинга и теста кабеля
        LinearLayout buttonsLayout3 = new LinearLayout(this);
        buttonsLayout3.setOrientation(LinearLayout.HORIZONTAL);
        if (!task.getIp().equals("Неизвестно")) {
            pingButton = new Button(this);
            pingButton.setText("Ping");
            pingButton.setOnClickListener(this);
            buttonsLayout3.addView(pingButton, WRAP_WRAP);
            ((LinearLayout.LayoutParams) pingButton.getLayoutParams()).weight = 1;
        }
        if (!task.getSwitch_ip().equals("Неизвестно") || !task.getSwitch_port().equals("Неизвестно")) {
            cableButton = new Button(this);
            cableButton.setText("Порт");
            cableButton.setOnClickListener(this);
            buttonsLayout3.addView(cableButton, WRAP_WRAP);
            ((LinearLayout.LayoutParams) cableButton.getLayoutParams()).weight = 1;
        }
        buttonsLayout3.setLayoutParams(MATCH_WRAP);
        views.add(buttonsLayout3);

        //создаём лэйаут с кнопками комента и закрытия заявки
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        commentButton = new Button(this);
        commentButton.setText("Комент");
        commentButton.setOnClickListener(this);
        buttonsLayout.addView(commentButton, WRAP_WRAP);
        ((LinearLayout.LayoutParams) commentButton.getLayoutParams()).weight = 1;

        closeButton = new Button(this);
        closeButton.setText("Закрыть");
        closeButton.setOnClickListener(this);
        buttonsLayout.addView(closeButton, WRAP_WRAP);
        ((LinearLayout.LayoutParams) closeButton.getLayoutParams()).weight = 1;

        takeButton = new Button(this);
        takeButton.setText("Взять");
        takeButton.setOnClickListener(this);
        if (taskIsYours) {
            buttonsLayout.addView(takeButton, WRAP_WRAP);
            ((LinearLayout.LayoutParams) takeButton.getLayoutParams()).weight = 1;
        }

        buttonsLayout.setLayoutParams(MATCH_WRAP);
        views.add(buttonsLayout);


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

    private void addGarantServiceMessage() {
        TextView garantServiceView = new TextView(getApplicationContext());
        garantServiceView.setPadding(10, 10, 0, 10);
        garantServiceView.setGravity(Gravity.CENTER);
        garantServiceView.setText("Гарантированный сервис");
        garantServiceView.setTextColor(Color.WHITE);
        garantServiceView.setBackgroundColor(Color.parseColor("#2e7d32"));
        garantServiceView.setTextSize(17);
        views.add(garantServiceView);
        views.add(addHorizontalSeparator());

    }

    private void addAktMessage() {
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


    View addHorizontalSeparator() {
        TextView justHorizontalSpace = new TextView(this);
        justHorizontalSpace.setTextSize(2);
        justHorizontalSpace.setLayoutParams(MATCH_WRAP);
        justHorizontalSpace.setPadding(0, 0, 8, 0);
        return justHorizontalSpace;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(googleButton)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:0,0?q=" + task.getAddressForMap()));
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
            showCommentDialog(vv);
        }
        // закрытие заявки
        if (v.equals(closeButton)) {  // Если нажата кнопка закрыть заявку
            showCloseDialog(vv);
        }
        if (v.equals(takeButton)) {
            showTakeDialog(vv);
        }
        if (v.equals(dogovor)) {
            dogovor.setText("Договор: " + task.getCard() + "\nЛогин:     " + task.getLoglk() + "\nПароль:   " + task.getPasslk());
            dogovor.setTextIsSelectable(true);
        }
        if (v.equals(historyButton)) {
            showHistory();
        }
    }

    private void showHistory() {
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<TaskHistory> tasks = GetInfo.getHistory(task.getCard());
                    Collections.sort(tasks, new Comparator<TaskHistory>() {
                        @Override
                        public int compare(TaskHistory o1, TaskHistory o2) {
                            return o2.getDatetime().compareTo(o1.getDatetime());
                        }
                    });
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            if (tasks.size() == 1 || tasks.size() == 0) {  // если история пуста
                                Snackbar.make(taskScrollView, "Это единственная заявка", Snackbar.LENGTH_LONG).show();
                            } else {
                                tasks.remove(0);
                                LinkedList<View> items = new LinkedList<>();
                                for (TaskHistory task1 : tasks) {
                                    items.add(addHorizontalSeparator());
                                    TextView nameField = new TextView(getApplicationContext());
                                    nameField.setPadding(10, 10, 0, 10);
                                    nameField.setText(task1.getDatetime() + "\n" + task1.getType_name());
                                    nameField.setTextColor(Color.WHITE);
                                    nameField.setGravity(Gravity.CENTER);
                                    nameField.setBackgroundColor(Visual.CORPORATE_COLOR);
                                    nameField.setTextSize(17);
                                    items.add(nameField);

                                    String[] comments = task1.getComments();
                                    for (int i = 0; i < comments.length; i++) {
                                        TextView commentView = new TextView(TaskActivity.this);
                                        commentView.setText(comments[i]);
                                        commentView.setPadding(15, 5, 10, 10);
                                        commentView.setTextColor(Color.WHITE);
                                        commentView.setBackgroundColor(Color.parseColor("#1e88e5"));
                                        if (i % 2 == 0)
                                            commentView.setBackgroundColor(Color.parseColor("#1565C0"));
                                        commentView.setLayoutParams(MATCH_WRAP);
                                        items.add(commentView);
                                    }
                                }
                                LinearLayout layout = new LinearLayout(TaskActivity.this);
                                ScrollView scrollView = new ScrollView(TaskActivity.this);
                                scrollView.addView(layout);
                                layout.setOrientation(LinearLayout.VERTICAL);
                                for (View item : items) {
                                    layout.addView(item);
                                }
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TaskActivity.this);
                                builder.setCancelable(true);
                                TextView titleView = new TextView(TaskActivity.this);
                                titleView.setText("История");
                                titleView.setGravity(Gravity.CENTER);
                                titleView.setTextSize(24);
                                titleView.setTypeface(null, Typeface.BOLD);
                                titleView.setTextColor(Color.parseColor("#1976D2"));
                                builder.setCustomTitle(titleView);
                                builder.setView(scrollView);
                                builder.setCustomTitle(titleView);
                                android.app.AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                } catch (Exception e) {
                    HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(taskScrollView, "Ошибка..", Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void showTakeDialog(final View vv) {
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

    private void showCloseDialog(final View vv) {
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
                } else
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

    private void showCommentDialog(final View vv) {
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
                coment = coment.replace("!!!!!!", "!")
                        .replace("!!!!!", "!")
                        .replace("!!!!", "!")
                        .replace("!!!", "!")
                        .replace("!!", "!");
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
                    if (map.get("task").equals("comented")) commentOK = true;
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
                } else
                    Snackbar.make(vv, "Ошибка. Вероятно нет интернета", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    void callToAbon() {
        if (task.getPhones().length == 1) {
            Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + task.getPhones()[0]));
            startActivity(intent1);
        } else {
            String[] cloneForUser = new String[task.getPhones().length];
            for (int i = 0; i < cloneForUser.length; i++) {
                cloneForUser[i] = "(" + task.getPhones()[i].substring(0, 3) + ") "
                        + task.getPhones()[i].substring(3, 6) + " - "
                        + task.getPhones()[i].substring(6, 8) + " - "
                        + task.getPhones()[i].substring(8, 10);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Выберите номер"); // заголовок для диалога
            builder.setItems(cloneForUser, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + task.getPhones()[item]));
                    startActivity(intent1);
                    dialog.dismiss();
                }
            });
            builder.setCancelable(true);
            builder.show();
        }
    }
}

