package ua.adeptius.myapplications.connection;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static ua.adeptius.myapplications.util.Utilites.EXECUTOR;
import static ua.adeptius.myapplications.util.Utilites.myLog;


public class Network {

    public static boolean isAuthorizationOk(String login, String password){
        try {
            myLog("Проверяем авторизацию");
            String[] request = new String[3];
            request[0] = "http://188.231.188.188/api/task_api_aut.php";
            request[1] = "begun=" + login;
            request[2] = "drowssap=" + password;
            Map<String, String> map = EXECUTOR.submit(new DataBase(request)).get().get(0);
            if (map.get("authentication").equals("success")) return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Загрузка новой версии c прогресс-диалогом
     */
}
