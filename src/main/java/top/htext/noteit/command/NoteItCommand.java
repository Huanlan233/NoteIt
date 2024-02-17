package top.htext.noteit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class NoteItCommand {
	public static void register( CommandDispatcher<ServerCommandSource> dispatcher ) {
		LiteralArgumentBuilder<ServerCommandSource> command = literal("noteit")
				.executes((context -> {
					context.getSource().sendFeedback(() -> Text.literal("NoteIt by Huanlan233"), false);
					return 1;
				}));
		dispatcher.register(command);
	}
}
