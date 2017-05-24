package ua.adeptius.myapplications.util;

import android.os.Handler;
import android.util.Log;

import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.orders.TaskHistory;

public class Utilites {

    public static final ExecutorService EXECUTOR = new ThreadPoolExecutor(2, 15, 5L, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
    public static final Handler HANDLER = new Handler();

    public static String createMd5(String st) {
        try{MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(st.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }catch (Exception e){}
        return null;
    }

    public static void myLog(String message){
        Log.d("====FreeNetEngineer====", message);
    }
}
