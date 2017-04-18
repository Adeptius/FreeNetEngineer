package ua.adeptius.myapplications.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ua.adeptius.myapplications.model.GerkonStatus;
import ua.adeptius.myapplications.model.OpeningBoxStatus;
import ua.adeptius.myapplications.orders.Task;
import ua.adeptius.myapplications.orders.TaskHistory;
import ua.adeptius.myapplications.util.Settings;
import ua.adeptius.myapplications.util.Utilites;

import static ua.adeptius.myapplications.R.id.map;


public class GetInfo {


    public static ArrayList<String> getAllTasksIds() throws Exception{
        Utilites.myLog("Запрашиваю id заявок");
        HashMap<String, String> map = new HashMap<>();
        addLogAndPass(map);
        ArrayList<String> tasks = new ArrayList<>();
        String result = Web.sendPost("http://188.231.188.188/api/task_api_id.php", map);
//        String result = Web.sendPost("http://195.181.208.31/web/support/tasks", map);
        JSONArray array = new JSONArray(result);
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            String id = jsonObject.getString("id");
            if (!"".equals(id)) {
                tasks.add(id);
            }
        }
        return tasks;
    }

    public static ArrayList<Task> getAssignedTask() throws Exception{
        Utilites.myLog("Запрашиваю назначенные заявки");
        return  getTasks(new HashMap<>(), "http://188.231.188.188/api/task_api.php");
    }


    public static ArrayList<Task> getNotAssignedTask() throws Exception{
        Utilites.myLog("Запрашиваю неназначенные заявки");
        HashMap<String, String> map = new HashMap<>();
        map.put("taccepted", "notassigned");
        return getTasks(map, "http://188.231.188.188/api/notassigned_api.php");
    }

    private static ArrayList<Task> getTasks(HashMap map, String url) throws Exception{
        ArrayList<Task> allTasks = new ArrayList<>();
        addLogAndPass(map);
        String s = Web.sendPost(url, map);
        if (s.equals("[{\"error\":\"No_tasks\"}]")) return new ArrayList<>();
        JSONArray jsonArray = new JSONArray(s);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            Task task = new Task(object);
            allTasks.add(task);
        }
        return allTasks;
    }

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

    private static String[] splitJson(String json){
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
