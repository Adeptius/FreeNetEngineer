package ua.adeptius.myapplications.orders;

public class Task {

    private String type_name;
    private String city;
    private String switch_port;
    private String subject;
    private String loglk;
    private String ip;
    private String termin;
    private String rterm;
    private String type;
    private String gerkon;
    private String datetime;
    private String phone;
    private String masc;
    private String name;
    private String comment;
    private String id;
    private String passlk;
    private String addr;
    private String user;
    private String card;
    private String gateway;
    private String distrikt;
    private String switch_ip;
    private String who;

    public String getAddr() {
        String s = addr;
        s = s.replace("\\/","/");
        s = s.replace(", кв", " кв");
        s = s.replace(".,","");
        s = s.replace("проспект,","проспект");
        return s;
    }

    public String getCard() {
        return card;
    }

    public String getCity() {
        return city;
    }

    public String[] getComments() {
        String[] sortedComment = this.comment.split("<coment>");
        for (int i = 0; i < sortedComment.length; i++) {
            String s = sortedComment[i];
            s = s.replace("<stroka>","\n");
            s = s.replace("&amp;quot;","\"");
            s = s.replace("\\t","-");
            s = s.replace("\\u21161","№");
            if (s.length() > 0 && s.charAt(0) == '\n') s = s.substring(1); // убираемлишний перенос в начале строки
            sortedComment[i] = s;
        }
        return sortedComment;
    }

    public String getRterm() {
        try {
            if (rterm.length() > 3) {
                rterm = rterm.substring(0, 3);
                if (rterm.substring(0, 3).equals("0.0"))
                    rterm = "0";
            }
            rterm.replace(".0", "");
        }catch (Exception ignored){}
        return rterm;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getDistrikt() {
        return distrikt;
    }

    public String getGateway() {
        if(gateway==null) return "Неизвестно";
        return gateway;
    }

    public String getGerkon() {
        if(gerkon==null) return "Неизвестно";
        return gerkon;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        if(ip==null) return "Неизвестно";
        return ip;
    }

    public String getLoglk() {
        if(loglk==null) return "Неизвестно";
        return loglk;
    }

    public String getMasc() {
        if(masc==null) return "Неизвестно";
        return masc;
    }

    public String getName() {
        String s = name;
        s = s.replace("\\u0406","І");
        s = s.replace("\\u0404","Є");
        s = s.replace("&quot;","\"");
        s = s.replace("\\u00ab","«");
        s = s.replace("\\u00bb","»");
        s = s.replace("&#8470;","№");
        return s;
    }

    public String getPasslk() {
        if(passlk==null) return "Неизвестно";
        return passlk;
    }

    public String getPhone() {
        return phone;
    }

    public String getSubject() {
        return subject;
    }

    public String getSwitch_ip() {
        if(switch_ip==null) return "Неизвестно";
        return switch_ip;
    }

    public String getSwitch_port() {
        if(switch_port==null) return "Неизвестно";
        return switch_port;
    }

    public String getTermin() {
        try {
            if (termin.length() > 3){
                termin = termin.substring(0,3);
                if (termin.substring(0,3).equals("0.0"))
                    termin = "0";
            }
            termin.replace(".0", "");
        } catch (Exception ignored) {}
        return termin;
    }

    public String getType() {
        return type;
    }

    public String getType_name() {
        return type_name;
    }

    public String getUser() {
        return user;
    }

    public String getWho() {
        return who;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setDistrikt(String distrikt) {
        this.distrikt = distrikt;
    }

    public void setRterm(String rterm) {
        this.rterm = rterm;
    }


    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public void setGerkon(String gerkon) {
        this.gerkon = gerkon;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setLoglk(String loglk) {
        this.loglk = loglk;
    }

    public void setMasc(String masc) {
        this.masc = masc;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPasslk(String passlk) {
        this.passlk = passlk;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSwitch_ip(String switch_ip) {
        this.switch_ip = switch_ip;
    }

    public void setSwitch_port(String switch_port) {
        this.switch_port = switch_port;
    }

    public void setTermin(String termin) {
        this.termin = termin;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setWho(String who) {
        this.who = who;
    }
}
