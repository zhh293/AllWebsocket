package sample.wsjakarta;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;

public class JsonMessageDecoder implements Decoder.Text<JsonMessage> {
    private static final ObjectMapper M = new ObjectMapper();
    @Override
    public JsonMessage decode(String s) throws DecodeException {
        // 将 JSON 文本解析为对象；解析失败抛出 DecodeException 以通知容器
        try { return M.readValue(s, JsonMessage.class); } catch (Exception e) { throw new DecodeException(s, "invalid", e); }
    }
    @Override
    public boolean willDecode(String s) { return s != null && !s.isEmpty(); }
    @Override
    public void init(jakarta.websocket.EndpointConfig config) {}
    @Override
    public void destroy() {}
}