package pt.ulisboa.tecnico.cmov.airdesk.domain;

public class User {
    private String nick;
    private String email;

    public User(String nick, String email) {
        this.nick = nick;
        this.email = email;
    }

    public String getNick(){
        return nick;
    }

    public String getEmail(){
        return email;
    }
}
