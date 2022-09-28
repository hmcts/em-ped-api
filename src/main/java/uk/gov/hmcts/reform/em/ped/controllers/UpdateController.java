package uk.gov.hmcts.reform.em.ped.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import uk.gov.hmcts.reform.em.ped.domain.ScreenChange;

@Controller
public class UpdateController {

    @MessageMapping("/screen-change/{session-id}")
    @SendTo("/topic/screen-change/{session-id}")
    public ScreenChange screenChange(ScreenChange screenChange) {
        return screenChange;
    }
}
