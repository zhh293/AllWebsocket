package sample.wsjakarta;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Encoder;

public class JsonMessageEncoder implements Encoder.Text<JsonMessage> {
    private static final ObjectMapper M = new ObjectMapper();
    @Override
    public String encode(JsonMessage object) {
        // 将对象编码为 JSON 文本；编码异常时返回空对象以保证通道可用
        try { return M.writeValueAsString(object); } catch (Exception e) { return "{}"; }
    }
    @Override
    public void init(jakarta.websocket.EndpointConfig config) {}
    @Override
    public void destroy() {}
}