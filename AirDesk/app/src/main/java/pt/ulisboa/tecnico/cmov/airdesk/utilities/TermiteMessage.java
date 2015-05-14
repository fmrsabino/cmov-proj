package pt.ulisboa.tecnico.cmov.airdesk.utilities;

import java.io.Serializable;

public class TermiteMessage implements Serializable {
    public static enum MSG_TYPE {
        WS_LIST,
        WS_LIST_REPLY,
        WS_VIEWERS,
        WS_VIEWERS_REPLY,
        WS_FILE_LIST,
        WS_FILE_LIST_REPLY,
        WS_FILE_CONTENT,
        WS_FILE_CONTENT_REPLY,
        WS_FILE_EDIT,
        WS_FILE_EDIT_REPLY,
        WS_FILE_EDIT_LOCK,
        WS_FILE_EDIT_LOCK_REPLY,
        WS_RELEASE_LOCK,
        WS_FILE_CREATE,
        WS_FILE_CREATE_REPLY,
        WS_FILE_DELETE,
        WS_UNSUBSCRIBE,
        WS_SUBSCRIBE,
        WS_ERROR
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
