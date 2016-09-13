package ua.adeptius.myapplications.util;

import android.os.Handler;
import android.util.Log;

import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ua.adeptius.myapplications.orders.Task;

public class Utilites {

    public static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
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


    public static Task createTask(Map<String, String> temp){
        Task task = new Task();
        task.setType_name(temp.get("type_name"));
        task.setCity(temp.get("city"));
        task.setSwitch_port(temp.get("switch_port"));
        task.setSubject(temp.get("subject"));
        task.setLoglk(temp.get("loglk"));
        task.setIp(temp.get("ip"));
        task.setTermin(temp.get("termin"));
        task.setType(temp.get("type"));
        task.setGerkon(temp.get("gerkon"));
        task.setDatetime(temp.get("datetime"));
        task.setPhone(temp.get("phone"));
        task.setMasc(temp.get("masc"));
        task.setName(temp.get("name"));
        task.setComment(temp.get("comment"));
        task.setId(temp.get("id"));
        task.setPasslk(temp.get("passlk"));
        task.setAddr(temp.get("addr"));
        task.setUser(temp.get("user"));
        task.setCard(temp.get("card"));
        task.setGateway(temp.get("gateway"));
        task.setDistrikt(temp.get("distrikt"));
        task.setSwitch_ip(temp.get("switch_ip"));
        task.setWho(temp.get("who"));
        task.setRterm(temp.get("rterm"));
        return task;
    }

    public static void myLog(String message){
        Log.d("====FreeNetEngineer====", message);
    }
}
