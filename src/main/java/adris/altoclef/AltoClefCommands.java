package adris.altoclef;

import adris.altoclef.commands.*;

import adris.altoclef.commandsystem.CommandException;

/**
 * Initializes altoclef's built in commands.
 */
public class AltoClefCommands {

    public AltoClefCommands(AltoClef mod) throws CommandException {
        // List commands here
        AltoClef.getCommandExecutor().registerNewCommand(
                new HelpCommand(mod),
                new GetCommand(mod),
                new FollowCommand(mod),
                new GiveCommand(mod),
                new EquipCommand(mod),
                new DepositCommand(mod),
                new StashCommand(mod),
                new GotoCommand(mod),
                new IdleCommand(mod),
                new CoordsCommand(mod),
                new StatusCommand(mod),
                new InventoryCommand(mod),
                new LocateStructureCommand(mod),
                new StopCommand(mod),
                new TestCommand(mod),
                new FoodCommand(mod),
                new MeatCommand(mod),
                new ReloadSettingsCommand(mod),
                new GamerCommand(mod),
                new MarvionCommand(mod),
                new PunkCommand(mod),
                new HeroCommand(mod),
                new SetGammaCommand(mod),
                new ListCommand(mod),
                new CoverWithSandCommand(mod),
                new CoverWithBlocksCommand(mod),
                new SelfCareCommand(mod),
                new BranchMineCommand(mod)
                //new TestMoveInventoryCommand(),
                //    new TestSwapInventoryCommand()
        );
    }
}
