package koeko.Networking;

import java.io.OutputStream;

public class PayloadForSending {
    private OutputStream outputStream;
    private byte[] payload;
    private String payloadID;

    public PayloadForSending(OutputStream outputStream, byte[] payload, String payloadID) {
        this.outputStream = outputStream;
        this.payload = payload;
        this.payloadID = payloadID;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getPayloadID() {
        return payloadID;
    }

    public void setPayloadID(String payloadID) {
        this.payloadID = payloadID;
    }
}
