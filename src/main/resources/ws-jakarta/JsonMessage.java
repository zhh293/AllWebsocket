package sample.wsjakarta;

public class JsonMessage {
    // 消息类型（例如 echo、chat、notify 等）
    public String type;
    // 负载数据，键值结构便于扩展字段
    public java.util.Map<String,Object> payload;
}