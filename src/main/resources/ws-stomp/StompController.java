package sample.wsstomp;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {
    private final SimpMessagingTemplate template;
    public StompController(SimpMessagingTemplate template) { this.template = template; }

    @MessageMapping("/broadcast")
    @SendTo("/topic/broadcast")
    public String broadcast(String payload) { return payload; }

    @MessageMapping("/direct")
    public void direct(String payload) { template.convertAndSend("/topic/direct", payload); }

    @MessageMapping("/toUser")
    public void toUser(String payload) { template.convertAndSendToUser("u1", "/queue/msg", payload); }
}