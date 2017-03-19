package ua.adeptius.myapplications.dao;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ua.adeptius.myapplications.model.GerkonStatus;
import ua.adeptius.myapplications.model.OpeningBoxStatus;
import ua.adeptius.myapplications.orders.TaskHistory;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;


public class GetInfo {

    public static boolean acceptTast(String taskId)throws Exception{
        Utilites.myLog("Запрашиваю назначение заявки на себя " + taskId);
        HashMap<String, String> map = new HashMap<>();
        addLogAndPass(map);
        map.put("act", "accept");
        map.put("task_id", taskId);
        String s = Web.sendPost("http://188.231.188.188/api/task_api_close.php", map);
        if (s.contains("task")){
            return new JSONObject(s).getString("task").equals("accepted");
        }
        return false;
    }

    public static ArrayList<TaskHistory> getHistory(String card) throws Exception{
        Utilites.myLog("Запрашиваю историю заявок по договору " + card);
        HashMap<String, String> map = new HashMap<>();
        addLogAndPass(map);
        map.put("covenant", card);
        String s = Web.sendPost("http://188.231.188.188/api/task_api_arhiv.php", map);
        if (s.equals("[]")) return new ArrayList<>();
        s = s.substring(1, s.length()-1);
        String[] splitted = splitJson(s);
        ArrayList<TaskHistory> historyArrayList = new ArrayList<>();
        for (String s1 : splitted) {
            historyArrayList.add(new TaskHistory(s1));
        }
        return historyArrayList;
    }

    public static OpeningBoxStatus openBox(String box) throws  Exception{
        Utilites.myLog("Запрос открытия ящика " +box);
        return openOrCloseBox(box,true);
    }

    public static OpeningBoxStatus closeBox(String box) throws  Exception{
        Utilites.myLog("Запрос закрытия ящика " +box);
        return openOrCloseBox(box,false);
    }

    private static OpeningBoxStatus openOrCloseBox(String box, boolean needToOpen) throws Exception{
        HashMap<String, String> map = new HashMap<>();
        addLogAndPass(map);
        map.put("tel", Settings.getPhone());
        map.put("pin", Settings.getPin());
        map.put("box", box);
        map.put("action", needToOpen ? "1" : "2");
        String s = Web.sendPost("http://188.231.188.188/api/gerkon_api.php", map);
        return new OpeningBoxStatus(s);
    }

    public static boolean isGerkonExist(String box) throws Exception {
        Utilites.myLog("Запрос наличия геркона");
        HashMap<String, String> map = new HashMap<>();
        addLogAndPass(map);
        map.put("tel", Settings.getPhone());
        map.put("pin", Settings.getPin());
        map.put("box", box);
        String s = Web.sendPost("http://188.231.188.188/api/gerkon_api.php", map);
        return new JSONObject(s).getInt("resbox") == 1;
    }


    public static GerkonStatus getGerkonStatus(String box) throws Exception {
        Utilites.myLog("Запрос состояния геркона");
        HashMap<String, String> map = new HashMap<>();
        addLogin(map);
        map.put("gerkon", box);
        String s = Web.sendPost("http://188.231.188.188/api/gekkon.php", map);
        return new GerkonStatus(s);
    }

    private static HashMap<String, String> addLogAndPass(HashMap<String, String> map){
        addLogin(map);
        addPassword(map);
        return map;
    }

    private static HashMap<String, String> addLogin(HashMap<String, String> map){
        map.put("begun", Settings.getCurrentLogin());
        return map;
    }

    private static HashMap<String, String> addPassword(HashMap<String, String> map){
        map.put("drowssap", Settings.getCurrentPassword());
        return map;
    }

    public static String[] splitJson(String json){
        String[] splittedJson;
        if (json.contains("},{")){
            splittedJson = json.split("\\},\\{");
            for (int i = 0; i < splittedJson.length; i++) {
                if (i==0) splittedJson[i] += "}";
                if (i==splittedJson.length-1) splittedJson[i] = "{" + splittedJson[i];
                if (i!=0 && i!=splittedJson.length-1) splittedJson[i] = "{" + splittedJson[i] + "}";
            }
        }else {
            splittedJson = new String[1];
            splittedJson[0] = json;
        }
        return splittedJson;
    }

}
