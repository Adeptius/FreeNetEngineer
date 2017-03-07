package ua.adeptius.myapplications.model;


import org.json.JSONObject;

public class OpeningBoxStatus {

    private String answer;
    private int answerCode;

    public OpeningBoxStatus(String json) {
        try{
            JSONObject allInfo = new JSONObject(json.trim());
            this.answer = allInfo.getString("respact");
            this.answerCode = allInfo.getInt("respcode");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public String getAnswer() {
        return answer;
    }

    public int getAnswerCode() {
        return answerCode;
    }

    @Override
    public String toString() {
        return "OpeningBoxStatus{" +
                "answer='" + answer + '\'' +
                ", answerCode=" + answerCode +
                '}';
    }
}
