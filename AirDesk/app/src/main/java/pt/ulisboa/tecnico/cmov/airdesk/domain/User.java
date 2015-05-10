package pt.ulisboa.tecnico.cmov.airdesk.domain;

public class User {
    private String nick;
    private String email;
    private String driveID;

    public User(String nick, String email, String driveID) {
        this.nick = nick;
        this.email = email;
        this.driveID = driveID;
    }

    public String getNick(){
        return nick;
    }

    public String getEmail(){
        return email;
    }

    public String getDriveID() { return driveID; }
}
