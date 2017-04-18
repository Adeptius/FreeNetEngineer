package ua.adeptius.myapplications.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.service.BackgroundService;
import ua.adeptius.myapplications.orders.Task;

public class Visual {
    public static int CORPORATE_COLOR = Color.parseColor("#3f51b5");
    public static int NEW_TASKS_COLOR = Color.parseColor("#ef6c00");
    public static final ViewGroup.LayoutParams WRAP_MACH = new ViewGroup
            .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
            , ViewGroup.LayoutParams.MATCH_PARENT);
    public static final ViewGroup.LayoutParams WRAP_WRAP = new ViewGroup
            .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
            , ViewGroup.LayoutParams.WRAP_CONTENT);
    public static final ViewGroup.LayoutParams MATCH_WRAP = new ViewGroup
            .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
            , ViewGroup.LayoutParams.WRAP_CONTENT);
    public static final LinearLayout.LayoutParams WRAP_WRAP_WEIGHT1 = new LinearLayout
            .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);




    public static LinearLayout getHeader(Task task, Context context){
        //горизонтальный лейаут для элементов view
        LinearLayout horizontalVievForTask = new LinearLayout(context);
        horizontalVievForTask.setOrientation(LinearLayout.HORIZONTAL);
        horizontalVievForTask.setLayoutParams(MATCH_WRAP);
        if (BackgroundService.newTasksIds.contains(task.getId()))
            horizontalVievForTask.setBackgroundColor(NEW_TASKS_COLOR);
        else horizontalVievForTask.setBackgroundColor(CORPORATE_COLOR);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            horizontalVievForTask.setElevation(10);
        }

        //добавляем в него слева вью для картинки
        ImageView iv = new ImageView(context);
        iv.setLayoutParams(new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT
                , 0.01f));
        if (task.getSubject().equals("Юр")){
            iv.setImageResource(R.drawable.vip);
        }else iv.setImageResource(getIcon(task.getType_name()));

        iv.setScaleX(1);
        iv.setScaleY(1);
        iv.setPadding(5,0,0,0);
        iv.setScaleX(0.6f);
        iv.setScaleY(0.6f);

        horizontalVievForTask.addView(iv, WRAP_MACH);

        //теперь в правую половину добавляем вертикальный лейаут, в который будем вставлять 2 текста
        LinearLayout rightLayout = new LinearLayout(context);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        horizontalVievForTask.addView(rightLayout, WRAP_WRAP);
        rightLayout.setLayoutParams(new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT
                , 0.99f));

        // Основной текст
        TextView tv = new TextView(context);
        tv.setText(task.getAddr() + "\n" + task.getType_name());
        tv.setTextColor(Color.WHITE);
        tv.setPadding(0,0,0,0);
        rightLayout.addView(tv, WRAP_WRAP);
        TextView tv2 = new TextView(context);
        tv2.setTextColor(Color.WHITE);

        try{
            float rterm = Float.parseFloat(task.getRterm());
            float term = Float.parseFloat(task.getTermin());
            if (rterm > 5){
                task.setRterm("" + (int) rterm);
                task.setTermin("" + (int) term);
            }

            if (task.getType_name().equals("СТК") || task.getType_name().equals("Нет линка по оптике")){
                tv2.setTextColor(Color.YELLOW);
                if (rterm >= 1) tv2.setTextColor(Color.parseColor("#ef5350"));
            }else{
                tv2.setTextColor(Color.GREEN);
                if (rterm >= 1) tv2.setTextColor(Color.YELLOW);
                if (rterm >= 2) tv2.setTextColor(Color.parseColor("#ef5350"));
            }
        }catch (Exception e){e.printStackTrace();}
        String termToShow = "" + task.getRterm();
        if (!task.getRterm().equals(task.getTermin()))
            termToShow = "" + task.getRterm() + "(" + task.getTermin() + ")";
        termToShow = termToShow.replace(".0","");
        tv2.setText("Срок заявки: " + termToShow +" дней");

        tv2.setPadding(0,0,0,0);
        rightLayout.addView(tv2, WRAP_WRAP);
        return horizontalVievForTask;
    }

    public static int getIcon(String s){
        if (s.equals("СТК")) return R.drawable.stk2;
        if (s.equals("Нет линка по оптике")) return R.drawable.stk2;
        if (s.equals("Нет пинга шлюза")) return R.drawable.noping;
        if (s.equals("Потери пакетов")) return R.drawable.loss;
        if (s.equals("Счастливец")) return R.drawable.scastlivect;
        if (s.equals("Недоабонент")) return R.drawable.half;
        if (s.equals("Перепротяжка")) return R.drawable.pereprotyagka;
        if (s.equals("IPTV")) return R.drawable.iptv;
        if (s.equals("Другое")) return R.drawable.some_else;
        if (s.equals("Лояльность сервиса")) return R.drawable.loyal;
        if (s.equals("Платная")) return R.drawable.platnaya;
        if (s.equals("По скорости")) return R.drawable.speed;
        if (s.equals("Роутер (аренда)")) return R.drawable.router;
        if (s.equals("Роутер (покупка)")) return R.drawable.router;
        if (s.equals("Роутер (настройка)")) return R.drawable.router;
        if (s.equals("Видеонаблюдение")) return R.drawable.camera;
        if (s.equals("Разделение колец")) return R.drawable.rings;
        if (s.equals("Divan TV Продажа")) return R.drawable.olltv;
        if (s.equals("Divan TV Обслуживание")) return R.drawable.olltv;
        if (s.equals("Oll-TV Продажа")) return R.drawable.olltv;
        if (s.equals("Oll-TV Обслуживание")) return R.drawable.olltv;
        if (s.equals("Проверка возможности подключения")) return R.drawable.pvp;
        if (s.equals("Падение коммутатора")) return R.drawable.shutdown;
        if (s.equals("Техническое обслуживание ВОЛС")) return R.drawable.pvp;
        if (s.equals("Удержание")) return R.drawable.uderzhanie;
        if (s.equals("Срочный вызов")) return R.drawable.emergency2;
        return 0;
    }

    public static int getIconForMap(String s){
        if (s.equals("СТК")) return R.drawable.map_stk2;
        if (s.equals("Нет линка по оптике")) return R.drawable.map_stk2;
        if (s.equals("Нет пинга шлюза")) return R.drawable.map_noping;
        if (s.equals("Потери пакетов")) return R.drawable.map_loss;
        if (s.equals("Счастливец")) return R.drawable.map_scastlivect;
        if (s.equals("Недоабонент")) return R.drawable.map_half;
        if (s.equals("Перепротяжка")) return R.drawable.map_pereprotyagka;
        if (s.equals("IPTV")) return R.drawable.map_iptv;
        if (s.equals("Другое")) return R.drawable.map_some_else;
        if (s.equals("Лояльность сервиса")) return R.drawable.map_loyal;
        if (s.equals("Платная")) return R.drawable.map_platnaya;
        if (s.equals("По скорости")) return R.drawable.map_speed;
        if (s.equals("Роутер (аренда)")) return R.drawable.map_router;
        if (s.equals("Роутер (покупка)")) return R.drawable.map_router;
        if (s.equals("Роутер (настройка)")) return R.drawable.map_router;
        if (s.equals("Видеонаблюдение")) return R.drawable.map_camera;
        if (s.equals("Разделение колец")) return R.drawable.map_rings;
        if (s.equals("Divan TV Продажа")) return R.drawable.map_olltv;
        if (s.equals("Divan TV Обслуживание")) return R.drawable.map_olltv;
        if (s.equals("Oll-TV Продажа")) return R.drawable.map_olltv;
        if (s.equals("Oll-TV Обслуживание")) return R.drawable.map_olltv;
        if (s.equals("Проверка возможности подключения")) return R.drawable.map_pvp;
        if (s.equals("Падение коммутатора")) return R.drawable.map_shutdown;
        if (s.equals("Техническое обслуживание ВОЛС")) return R.drawable.map_pvp;
        if (s.equals("Удержание")) return R.drawable.map_uderzhanie;
        if (s.equals("Срочный вызов")) return R.drawable.map_emergency2;
        return 0;
    }

    public  static void makeMyToast(String s, Context context, LayoutInflater inflate, View view){
        LayoutInflater inflater = inflate;
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) view);
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(s);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, 150);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
