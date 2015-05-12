package pt.ulisboa.tecnico.cmov.airdesk.domain;

public class User {
    private String nick;
    private String email;
    private String driveID;
    private int driveOptions;

    public User(String nick, String email, String driveID, int driveOptions) {
        this.nick = nick;
        this.email = email;
        this.driveID = driveID;
        this.driveOptions = driveOptions;
    }

    public String getNick(){
        return nick;
    }

    public String getEmail(){
        return email;
    }

    public String getDriveID() { return driveID; }

    public int getDriveOptions() { return driveOptions; }
}
