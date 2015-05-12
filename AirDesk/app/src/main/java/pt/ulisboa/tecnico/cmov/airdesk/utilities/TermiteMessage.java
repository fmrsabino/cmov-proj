package pt.ulisboa.tecnico.cmov.airdesk.utilities;

import java.io.Serializable;

public class TermiteMessage implements Serializable {
    public static enum MSG_TYPE {
        WS_LIST,
        WS_LIST_REPLY,
        WS_FILE_LIST,
        WS_FILE_LIST_REPLY
    }

    public MSG_TYPE type;
    public String srcIp;
    public String rcvIp;
    public Object contents;

    public TermiteMessage() {}
    public TermiteMessage(MSG_TYPE type, String srcIp, String rcvIp, Object contents) {
        this.type = type;
        this.srcIp = srcIp;
        this.rcvIp = rcvIp;
        this.contents = contents;
    }
}
