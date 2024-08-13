package adris.altoclef.mixins;

import java.util.ArrayList;
import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Settings;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandExecutor;
import adris.altoclef.util.helpers.ConfigHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;


@Mixin(ClientPlayNetworkHandler.class)
public abstract class ChatInputMixin extends ClientCommonNetworkHandler {
	@Shadow
    private ClientWorld world;

    @Shadow
    public abstract void sendChatMessage(String content);

    @Unique
    private boolean ignoreChatMessage;

    @Unique
    private boolean worldNotNull;
    
    protected ChatInputMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }
    
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (ignoreChatMessage) return;
		String prefix = ConfigHelper.getConfig(Settings.SETTINGS_PATH, Settings::new, Settings.class).getCommandPrefix();
        if (message.startsWith(prefix)) {
            try {
            	if (message.contains(";")) {
//            		CommandExecutor.dispatch(message.split(";")[0].substring(prefix.length()));
                	Collection<Command> commands = new ArrayList<>(AltoClef.getCommandExecutor().allCommands());
                	commands.removeIf((command) -> !message.contains(command.getName()));
                	AltoClef.getCommandExecutor().executeRecursive(commands.toArray(new Command[commands.size()]), message.split(";"), 0, () -> {
                    }, ex -> Debug.logWarning(ex.getMessage()));
            	} else CommandExecutor.dispatch(message.substring(prefix.length()));
            } catch (CommandSyntaxException e) {
                Debug.logWarning(e.getMessage());
            } catch (IllegalArgumentException e) {
                Debug.logWarning(e.getMessage());
            }

            client.inGameHud.getChatHud().addToMessageHistory(message);
            ci.cancel();
        }
    }
}