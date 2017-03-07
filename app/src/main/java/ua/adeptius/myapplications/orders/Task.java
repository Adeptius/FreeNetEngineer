package ua.adeptius.myapplications.orders;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static ua.adeptius.myapplications.util.Utilites.myLog;

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
    private String[] phones;
    private String masc;
    private String name;
    private String[] comment;
    private String id;
    private String passlk;
    private String addr;
    private String user;
    private String card;
    private String gateway;
    private String distrikt;
    private String switch_ip;
    private String who;
    private String garantServise;
    private String sw_place;

    public String getGarantServise() {
        return garantServise;
    }

    public void setGarantServise(String garantServise) {
        this.garantServise = garantServise;
    }

    public String getAddr() {
        return addr;
    }

    public String getCard() {
        return card;
    }

    public String getCity() {
        return city;
    }

    public String[] getComments() {
        return comment;
    }

    public String getSw_place() {
        return sw_place;
    }

    public void setSw_place(String sw_place) {
        this.sw_place = sw_place;
    }

    public String getRterm() {
        return rterm;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getDistrikt() {
        return distrikt;
    }

    public String getGateway() {
        return gateway;
    }

    public String getGerkon() {
        return gerkon;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }


    public String[] getPhones() {
        if (phones == null) searchAllPhonesInThisTask();
        return phones;
    }

    public String getLoglk() {
        return loglk;
    }

    public String getMasc() {
        return masc;
    }

    public String getName() {
        return name;
    }

    public String getPasslk() {
        return passlk;
    }

    public String getPhone() {
        return phone;
    }

    public String getSubject() {
        return subject;
    }

    public String getSwitch_ip() {
        return switch_ip;
    }

    public String getSwitch_port() {
        return switch_port;
    }

    public String getTermin() {
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
        addr = addr.replace("\\/", "/")
                .replace(", кв", " кв")
                .replace(".,", "")
                .replace("проспект,", "проспект");
        this.addr = addr;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public void setCity(String city) {
        this.city = city;
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

    public void setDistrikt(String distrikt) {
        this.distrikt = distrikt;
    }

    public void setRterm(String rterm) {
        try {
            if (rterm.length() > 3) {
                rterm = rterm.substring(0, 3);
                if (rterm.substring(0, 3).equals("0.0"))
                    rterm = "0";
            }
            rterm.replace(".0", "");
        } catch (Exception ignored) {
        }
        this.rterm = rterm;
    }

    public void setTermin(String termin) {
        try {
            if (termin.length() > 3) {
                termin = termin.substring(0, 3);
                if (termin.substring(0, 3).equals("0.0"))
                    termin = "0";
            }
            termin.replace(".0", "");
        } catch (Exception ignored) {
        }
        this.termin = termin;
    }

    public void setGateway(String gateway) {
        if (gateway == null) this.gateway = "Неизвестно";
        else this.gateway = gateway;
    }

    public void setGerkon(String gerkon) {
        if (gerkon == null) this.gerkon = "Неизвестно";
        else this.gerkon = gerkon;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIp(String ip) {
        if (ip == null) this.ip = "Неизвестно";
        else this.ip = ip;
    }

    public void setLoglk(String loglk) {
        if (loglk == null) this.loglk = "Неизвестно";
        else this.loglk = loglk;
    }

    public void setMasc(String masc) {
        if (masc == null) this.masc = "Неизвестно";
        else this.masc = masc;
    }

    public void searchAllPhonesInThisTask() {
        String s = "";
        for (int i = 0; i < getComments().length; i++) {
            s += getComments()[i] + "H";
        }
        s += getPhone();
        s = s.replace("\n", "g")
                .replace(" ", "")
                .replace("\n", "")
                .replace("-", "")
                .replace("(20", "");
        String s1 = "";
        ArrayList<String> phonesArr = new ArrayList<>();
        try {
            Pattern regex = Pattern.compile("(?:\\d{10,12})+");
            Matcher regexMatcher = regex.matcher(s);
            while (regexMatcher.find()) {
                s1 = regexMatcher.group();
                if (s1.length() == 11) s1 = s1.substring(1);
                if (s1.length() == 12) s1 = s1.substring(2);
                if (!phonesArr.contains(s1)) phonesArr.add(s1);
            }
        } catch (PatternSyntaxException ex) {
        }
        String[] result = new String[phonesArr.size()];
        for (int i = 0; i < phonesArr.size(); i++) {
            result[i] = phonesArr.get(i);
        }

        if (result.length == 0) {
            result = new String[1];
            result[0] = "0000000000";
        }
        this.phones = result;
    }

    public void setName(String name) {
        name = name.replace("\\u0406", "І")
                .replace("\\u0404", "Є")
                .replace("&quot;", "\"")
                .replace("\\u00ab", "«")
                .replace("\\u00bb", "»")
                .replace("&#8470;", "№");
        this.name = name;
    }

    public void setPasslk(String passlk) {
        if (passlk == null) this.passlk = "Неизвестно";
        else this.passlk = passlk;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSwitch_ip(String switch_ip) {
        if (switch_ip == null) this.switch_ip = "Неизвестно";
        else this.switch_ip = switch_ip;
    }

    public void setSwitch_port(String switch_port) {
        if (switch_port == null) this.switch_port = "Неизвестно";
        else this.switch_port = switch_port;
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
