package ua.adeptius.myapplications.orders;


import org.json.JSONObject;

public class TaskHistory {

    private String type_name;
    private String datetime;
    private String[] comment;

    public TaskHistory(String json) {
        try{
            JSONObject allInfo = new JSONObject(json.trim());
            setType_name(allInfo.getString("type_name"));
            setDatetime(allInfo.getString("datetime"));
            setComment(allInfo.getString("comment"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public TaskHistory() {
    }

    public String getDatetime() {
        return datetime;
    }

    public String[] getComments() {
        return comment;
    }

    public String getType_name() {
        return type_name;
    }

    public void setComment(String comment) {
        String[] splittedComments = comment.split("<coment>");
        for (int i = 0; i < splittedComments.length; i++) {
            String s = splittedComments[i];
            s = s.replace("<stroka>", "\n")
                    .replace("&amp;quot;", "\"")
                    .replace("\\t", "-")
                    .replace("\\u21161", "№")
                    .replace("[nolink]", "[Нет линка по оптике]")
                    .replace("[to]", "[Тех. обслуживание ВОЛС]")
                    .replace("[rebreach]", "[Перепротяжка]");
            if (s.length() > 0 && s.charAt(0) == '\n')
                s = s.substring(1); // убираемлишний перенос в начале строки
            splittedComments[i] = s;
        }
        this.comment = splittedComments;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }
}
