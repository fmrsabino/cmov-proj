package pt.ulisboa.tecnico.cmov.airdesk.utilities;

import java.io.Serializable;

public class TermiteMessage implements Serializable {
    public static enum MSG_TYPE {
        WS_FILE_LIST
    }

    public MSG_TYPE type;
    public Object contents;

    public TermiteMessage() {}
    public TermiteMessage(MSG_TYPE type, Object contents) {
        this.type = type;
        this.contents = contents;
    }
}
