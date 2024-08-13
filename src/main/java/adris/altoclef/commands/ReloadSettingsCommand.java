package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.util.helpers.ConfigHelper;
import net.minecraft.command.CommandSource;

public class ReloadSettingsCommand extends Command {
    public ReloadSettingsCommand(AltoClef mod) {
        super("reload_settings", "Reloads bot settings and butler whitelist/blacklist.", mod);
    }
    
    @Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
		builder.executes(context -> {
			try {
		        ConfigHelper.reloadAllConfigs();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        _mod.log("Reload successful!");
	        finish();
            return SINGLE_SUCCESS;
		});
	}
}