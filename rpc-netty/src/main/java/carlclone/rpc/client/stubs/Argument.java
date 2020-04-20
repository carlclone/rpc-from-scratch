package carlclone.rpc.client.stubs;

public class Argument {
    private Class<?> type;
    private byte[] value;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}