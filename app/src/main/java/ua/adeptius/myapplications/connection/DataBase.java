package ua.adeptius.myapplications.connection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.myLog;

public class DataBase implements Callable<ArrayList<Map<String, String>>>{

    private String[] params;

    public DataBase(String... params) {
        this.params = params;
    }

    @Override
    public ArrayList<Map<String, String>> call() throws Exception {
        try {
            String url = params[0];
            myLog("Подключаюсь по ссылке: " + url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            String urlParameters = "";
            for (int i = 1; i < params.length; i++) {
                if(i != 1) urlParameters += "&"; // добавлять "&" в начале не нужно
                urlParameters+= params[i];
            }
            myLog("Передаю параметры: " + urlParameters);

            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush(); wr.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String response = in.readLine();
            myLog("Получил ответ: " + response);
            ArrayList<Map<String, String>> resultMap = new ArrayList<>();
            response = response.replace(" : ", ":").replace("\\/","/");
            String[] splitResult = null;
            if (response.startsWith("[{")) {
                splitResult = response.substring(2, response.length() - 2).split("\\},\\{");
            }else if (response.startsWith("{")){
                splitResult = response.substring(1, response.length() - 1).split("\\},\\{");
            }

            for (int i = 0; i < splitResult.length; i++) {
                String s = splitResult[i].substring(1, splitResult[i].length()-1 );
                myLog("Каждая строка по очереди до создания мапы:" + s);
                Map<String, String> map = new HashMap<>();
                String[] pairs = s.split("\",\"");
                for (int j = 0; j < pairs.length; j++) {
                    String[] keys = pairs[j].split("\":\"");
                    if (keys.length == 1){ map.put(keys[0], "Неизвестно"); // Если у ключа нет значения
                    }else                { map.put(keys[0], keys[1]); }
                }
                resultMap.add(map);
            }
            return resultMap;
        }catch (MalformedURLException e){ e.printStackTrace(); }
        catch (IOException e)          { e.printStackTrace(); }
        return null;
    }

    static String ping(String ip){
        try {
            Log.d("myLogs", "Запрашиваю ping");
            String[] request = new String[2];
            request[0] = "http://188.231.188.188/api/ping_api.php";
            request[1] = "ip=" + ip;
            String result = EXECUTOR.submit(new DataBase(request)).get().get(0).get("Scan report for");
            if(result.contains("Host is up")){
                float ms = Float.parseFloat(result.substring(result.indexOf("up")+4,result.lastIndexOf("s")))*1000;
                int mss = (int)ms;
                if (mss == 0) mss = 1;
                result = "Пинг: " + mss + " мс.";
            }else return "Ответа нет.";
            Log.d("myLogs", "Получен ответ пинг: " + result);
            return result;
        } catch (Exception e){return "Сбой (Возможно нет интернета)";}
    }
}