package adris.altoclef.eventbus.events;

import net.minecraft.text.Text;
import java.lang.String;

/**
 * Whenever chat appears
 */
public class ChatMessageEvent {
    public String message;


    public ChatMessageEvent(Text message) {
        this.message = message.getString();
    }
}
