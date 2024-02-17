package top.htext.noteit;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.htext.noteit.command.NoteItCommand;

public class NoteIt implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger(NoteIt.class);
	@Override
	public void onInitialize() {
		LOGGER.info("NoteIt initialized.");
		CommandRegistrationCallback.EVENT.register((( dispatcher, registryAccess, environment ) -> NoteItCommand.register(dispatcher) ));
	}
}
