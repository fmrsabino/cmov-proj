package pt.ulisboa.tecnico.cmov.airdesk.domain;


import java.util.ArrayList;
import java.util.List;

public class Workspace {
    private String name;
    private int quota;
    private boolean isPublic;
    private List<User> users;

    public Workspace(String name, double quota, boolean isPublic, List<User> users) {
        this.name = name;
        this.quota = quota;
        this.isPublic = isPublic;
        this.users = users;
    }

    public Workspace(String name, int quota, boolean isPublic) {
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

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        //TODO: Check if the user isn't already in the list
        users.add(user);
    }
}
