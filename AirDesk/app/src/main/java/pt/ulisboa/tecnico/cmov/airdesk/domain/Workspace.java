package pt.ulisboa.tecnico.cmov.airdesk.domain;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Workspace {
    private String name;
    private int quota;
    private int isPublic;
    private String keywords;
    private ArrayList<String> users;


    public Workspace(String name, int quota, int isPublic, String keywords, ArrayList<String> users) {
        this.name = name;
        this.quota = quota;
        this.isPublic = isPublic;
        this.keywords = keywords;
        this.users = users;
    }

    public Workspace(String name, int quota, int isPublic) {
        this.name = name;
        this.quota = quota;
        this.isPublic = isPublic;
        this.users = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int isPublic() {
        return isPublic;
    }

    public void setPublic(int isPublic) {
        this.isPublic = isPublic;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void addUser(String user) {
        //TODO: Check if the user isn't already in the list
        users.add(user);
    }
}
