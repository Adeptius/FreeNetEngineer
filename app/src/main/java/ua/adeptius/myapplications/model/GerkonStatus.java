package ua.adeptius.myapplications.model;


import org.json.JSONObject;

public class GerkonStatus {

    private String ip;
    private String port;
    private boolean admin_up;
    private boolean is_monitor;
    private String status;
    private String date;
    private int statusСode;


    public GerkonStatus(String json) {
        try {
            JSONObject allInfo = new JSONObject(json.trim());
            this.ip = allInfo.getString("ip");
            this.port = allInfo.getString("port");
            this.admin_up = allInfo.getString("admin_status").equals("1");
            this.is_monitor = allInfo.getString("is_monitor").equals("1");
            this.status = allInfo.getString("status");
            this.date = allInfo.getString("date");
            this.statusСode = allInfo.getInt("statusСode");
        }catch (Exception ignored){}
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public boolean isAdmin_up() {
        return admin_up;
    }

    public int getStatusСode() {
        return statusСode;
    }

    public boolean is_monitor() {
        return is_monitor;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "GerkonStatus{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", admin_up='" + admin_up + '\'' +
                ", is_monitor=" + is_monitor +
                ", status='" + status + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
