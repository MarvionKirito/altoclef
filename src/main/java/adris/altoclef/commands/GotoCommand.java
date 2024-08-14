package adris.altoclef.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commands.arguments.GotoTargetArgumentType;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.GotoTarget;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;

/**
 * Out of all the commands, this one probably demonstrates
 * why we need a better arg parsing system. Please.
 */
public class GotoCommand extends Command {

    public GotoCommand(AltoClef mod) throws CommandException {
        // x z
        // x y z
        // x y z dimension
        // (dimension)
        // (x z dimension)
        super("goto", "Tell bot to travel to a set of coordinates", mod
                /*new Arg(GotoTarget.class, "[x y z dimension]/[x z dimension]/[y dimension]/[dimension]/[x y z]/[x z]/[y]")*/
        );
    }

    public static Task getMovementTaskFor(GotoTarget target) {
        return switch (target.getType()) {
            case XYZ ->
                    new GetToBlockTask(new BlockPos(target.getX(), target.getY(), target.getZ()), target.getDimension());
            case XZ -> new GetToXZTask(target.getX(), target.getZ(), target.getDimension());
            case Y -> new GetToYTask(target.getY(), target.getDimension());
            case NONE -> new DefaultGoToDimensionTask(target.getDimension());
        };
    }

//    @Override
//    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
//        GotoTarget target = parser.get(GotoTarget.class);
//        mod.runUserTask(getMovementTaskFor(target), this::finish);
//    }

	@Override
	public void build(LiteralArgumentBuilder<CommandSource> builder) {
//		builder.then(argument("dimension", DimensionArgumentType.dimension()).executes(context -> {
//			
//			
//			
//			return SINGLE_SUCCESS;
//		}));
//
//		builder.then(argument("pos", Vec3ArgumentType.vec3()).then(argument("dimension", DimensionArgumentType.dimension()).executes(context -> {
//			
//			
//			
//			return SINGLE_SUCCESS;
//		})));
		
		builder.then(argument("gotoTarget", GotoTargetArgumentType.create()).executes(context -> {
	        Debug.logMessage("gotoTarget");
	        try {
				
			GotoTarget target = GotoTargetArgumentType.get(context);
			_mod.runUserTask(getMovementTaskFor(target), this::finish);

			} catch (Exception e) {
				// TODO: handle exception
			}
			
			return SINGLE_SUCCESS;
		}));
		
	}
}
