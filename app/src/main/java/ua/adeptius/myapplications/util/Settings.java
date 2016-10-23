package ua.adeptius.myapplications.util;


import android.content.SharedPreferences;


public class Settings {

    public static SharedPreferences sPref;
    public static SharedPreferences.Editor settingsEditor;

    public static void setsPref(SharedPreferences sPref) {
        if (Settings.sPref == null) {
            Settings.sPref = sPref;
            Settings.settingsEditor = sPref.edit();
        }
    }

    public static int getMessageOftheWeek() {
        try{
            return Integer.parseInt(sPref.getString("messageOfWeek", ""));
        }catch (Exception e){
            return -1;
        }
    }

    public static void setMessageOfTheWeek(int messageOfTheWeek) {
        settingsEditor.putString("messageOfWeek", "" + messageOfTheWeek);
        settingsEditor.commit();
    }

    //Login
    public static void setCurrentLogin(String currentLogin) {
        settingsEditor.putString("login", currentLogin);
        settingsEditor.commit();
    }

    public static String getCurrentLogin() {
        return sPref.getString("login", "");
    }

    //Password
    public static void setCurrentPassword(String currentPassword) {
        settingsEditor.putString("password", currentPassword);
        settingsEditor.commit();
    }

    public static String getCurrentPassword() {
        return sPref.getString("password", "");
    }

    //HoursFrom
    public static void setHoursFrom(int hoursFrom) {
        settingsEditor.putString("hoursFrom", "" + hoursFrom);
        settingsEditor.commit();
    }

    public static int getHoursFrom() {
        try {
            return Integer.parseInt(sPref.getString("hoursFrom", ""));
        } catch (NumberFormatException e) {
            return 8;
        }
    }

    //HoursTo
    public static void setHoursTo(int hoursTo) {
        settingsEditor.putString("hoursTo", "" + hoursTo);
        settingsEditor.commit();
    }

    public static int getHoursTo() {
        try {
            return Integer.parseInt(sPref.getString("hoursTo", ""));
        } catch (NumberFormatException e) {
            return 18;
        }
    }

    //Notify
    public static void setNotifyNewTasks(boolean notifyNewTasks) {
        settingsEditor.putString("notifyNewTasks", "" + notifyNewTasks);
        settingsEditor.commit();
    }

    public static boolean isNotifyNewTasks() {
        if (sPref.getString("notifyNewTasks", "").equals("")) return true;
        else return Boolean.parseBoolean(sPref.getString("notifyNewTasks", ""));
    }

    //Portrait
    public static void setSwitchPortrait(boolean switchPortrait) {
        settingsEditor.putString("switchPortrait", "" + switchPortrait);
        settingsEditor.commit();
    }

    public static boolean isSwitchPortrait() {
        if (sPref.getString("switchPortrait", "").equals("")) return true;
        else return Boolean.parseBoolean(sPref.getString("switchPortrait", ""));
    }

    //EnableSound
    public static void setSwitchSound(boolean switchSound) {
        settingsEditor.putString("switchSound", "" + switchSound);
        settingsEditor.commit();
    }

    public static boolean isSwitchSound() {
        if (sPref.getString("switchSound", "").equals("")) return true;
        else return Boolean.parseBoolean(sPref.getString("switchSound", ""));
    }

    //NotifySubbota
    public static void setSwitchSubbota(boolean switchSubbota) {
        settingsEditor.putString("switchSubbota", "" + switchSubbota);
        settingsEditor.commit();
    }

    public static boolean isSwitchSubbota() {
        if (sPref.getString("switchSubbota", "").equals("")) return true;
        return Boolean.parseBoolean(sPref.getString("switchSubbota", ""));
    }

    //VibroEnable
    public static void setSwitchVibro(boolean switchVibro) {
        settingsEditor.putString("switchVibro", "" + switchVibro);
        settingsEditor.commit();
    }

    public static boolean isSwitchVibro() {
        return Boolean.parseBoolean(sPref.getString("switchVibro", ""));
    }

    //NotifyVoskresenye
    public static void setSwitchVoskresenye(boolean switchVoskresenye) {
        settingsEditor.putString("switchVoskresenye", "" + switchVoskresenye);
        settingsEditor.commit();
    }

    public static boolean isSwitchVoskresenye() {
        return Boolean.parseBoolean(sPref.getString("switchVoskresenye", ""));
    }


    public static void eraseLoginAndPassword() {
        setCurrentLogin("");
        setCurrentPassword("");
    }
}
