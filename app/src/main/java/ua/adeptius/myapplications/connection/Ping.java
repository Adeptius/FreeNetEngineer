package ua.adeptius.myapplications.connection;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ua.adeptius.myapplications.R;
import ua.adeptius.myapplications.activities.PingActivity;
import ua.adeptius.myapplications.util.Visual;

import static ua.adeptius.myapplications.util.Utilites.HANDLER;


public class Ping implements Runnable {

    private LinearLayout pingScrollView;
    private boolean interrupt = false;
    private String ip;
    private ProgressBar proBar;
    private float pingCounter = 0;
    private float lossed = 0;
    private Context context;
    private TextView statisticView;

    public Ping(PingActivity pingActivity) {
        this.context = pingActivity;
        ip = pingActivity.ip;
        proBar = pingActivity.proBar;
        pingScrollView = pingActivity.pingScrollView;
        statisticView = pingActivity.statisticView;
    }

    public void interrupt() {
        interrupt = true;
    }

    @Override
    public void run() {
        while (!interrupt) {
            String p = DataBase.ping(ip);

            if (p.substring(0, 4).equals("Пинг") || p.substring(0, 4).equals("Сбой")) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            HANDLER.post(new PingReport(p));
        }
    }

    private class PingReport implements Runnable {

        private String pingResult;

        public PingReport(String pingResult) {
            this.pingResult = pingResult;
        }

        @Override
        public void run() {

            TextView pingView = new TextView(context);
            if (pingResult.equals("stop")) proBar.setVisibility(View.INVISIBLE);
            else {
                if (!interrupt) {
                    pingView.setText(pingResult);
                    pingView.setPadding(20, 0, 0, 0);
                    if (pingResult.equals("Ответа нет.")) {
                        pingView.setBackgroundColor(Color.RED);
                        lossed++;
                    }
                    else if (pingResult.equals("Сбой (Нет интернета на устройстве)"))
                        pingView.setBackgroundColor(Color.CYAN);
                    else pingView.setBackgroundColor(Color.GREEN);
                    pingCounter++;

                    Float loss = (lossed / pingCounter)*100;
                    String cutted = loss.toString().substring(0,3);
                    if (cutted.endsWith(".")) cutted = loss.toString().substring(0,2);
                    statisticView.setText("Пинг " +  ip + "\n" + cutted + "% потерь");


                    if (pingScrollView.getChildCount() > 15) {
                        pingScrollView.getChildAt(15).startAnimation(
                                AnimationUtils.loadAnimation(context, R.anim.mytransforpingdelete));
                        pingScrollView.removeViewAt(15);
                    }

                    pingView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.mytransforping));
                    pingScrollView.addView(pingView, 1, Visual.MATCH_WRAP);
                }
            }
        }
    }
}
