package pt.ulisboa.tecnico.cmov.airdesk.domain;


import java.util.List;

public class Workspace {
    private String name;
    private int quota;
    private int isPublic;
    private String keywords;
    private List<String> users;


    public Workspace(String name, int quota, int isPublic, String keywords, List<String> users) {
        this.name = name;
        this.quota = quota;
        this.isPublic = isPublic;
        this.keywords = keywords;
        this.users = users;
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

    public List<String> getUsers() {
        return users;
    }

    public void addUser(String user) {
        //TODO: Check if the user isn't already in the list
        users.add(user);
    }
}
