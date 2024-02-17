package top.htext.noteit.mixin;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.PlaySoundCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PlaySoundCommand.class)
public class PlaySoundCommandMixin {
	@Inject(method = "makeArgumentsForCategory", at = @At("RETURN"), cancellable = true)
	private static void modifyPitchFloatArg( SoundCategory category, CallbackInfoReturnable<LiteralArgumentBuilder<ServerCommandSource>> cir ) {
		cir.setReturnValue(makeArgumentsForCategory(category));
	}

	// 以下方法直接复制粘贴MC源码，然后覆写的。嗯，我是懒狗
	@Unique
	private static LiteralArgumentBuilder<ServerCommandSource> makeArgumentsForCategory( SoundCategory category) {
		return CommandManager.literal(category.getName())
				.then(
						CommandManager.argument("targets", EntityArgumentType.players())
								.executes(
										context -> execute(
												context.getSource(),
												EntityArgumentType.getPlayers(context, "targets"),
												IdentifierArgumentType.getIdentifier(context, "sound"),
												category,
												context.getSource().getPosition(),
												1.0F,
												1.0F,
												0.0F
										)
								)
								.then(
										CommandManager.argument("pos", Vec3ArgumentType.vec3())
												.executes(
														context -> execute(
																context.getSource(),
																EntityArgumentType.getPlayers(context, "targets"),
																IdentifierArgumentType.getIdentifier(context, "sound"),
																category,
																Vec3ArgumentType.getVec3(context, "pos"),
																1.0F,
																1.0F,
																0.0F
														)
												)
												.then(
														CommandManager.argument("volume", FloatArgumentType.floatArg(0.0F))
																.executes(
																		context -> execute(
																				context.getSource(),
																				EntityArgumentType.getPlayers(context, "targets"),
																				IdentifierArgumentType.getIdentifier(context, "sound"),
																				category,
																				Vec3ArgumentType.getVec3(context, "pos"),
																				context.getArgument("volume", Float.class),
																				1.0F,
																				0.0F
																		)
																)
																.then(
																		CommandManager.argument("pitch", FloatArgumentType.floatArg(0.0F, 10.0F)) // 改得就是这里
																				.executes(
																						context -> execute(
																								context.getSource(),
																								EntityArgumentType.getPlayers(context, "targets"),
																								IdentifierArgumentType.getIdentifier(context, "sound"),
																								category,
																								Vec3ArgumentType.getVec3(context, "pos"),
																								context.getArgument("volume", Float.class),
																								context.getArgument("pitch", Float.class),
																								0.0F
																						)
																				)
																				.then(
																						CommandManager.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F))
																								.executes(
																										context -> execute(
																												context.getSource(),
																												EntityArgumentType.getPlayers(context, "targets"),
																												IdentifierArgumentType.getIdentifier(context, "sound"),
																												category,
																												Vec3ArgumentType.getVec3(context, "pos"),
																												context.getArgument("volume", Float.class),
																												context.getArgument("pitch", Float.class),
																												context.getArgument("minVolume", Float.class)
																										)
																								)
																				)
																)
												)
								)
				);
	}

	@Unique
	private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier sound, SoundCategory category, Vec3d pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {
		RegistryEntry<SoundEvent> registryEntry = RegistryEntry.of(SoundEvent.of(sound));
		double d = MathHelper.square(registryEntry.value().getDistanceToTravel(volume));
		int i = 0;
		long l = source.getWorld().getRandom().nextLong();
		for (ServerPlayerEntity serverPlayerEntity : targets) {
			double e = pos.x - serverPlayerEntity.getX();
			double f = pos.y - serverPlayerEntity.getY();
			double g = pos.z - serverPlayerEntity.getZ();
			double h = e * e + f * f + g * g;
			Vec3d vec3d = pos;
			float j = volume;
			if (h > d) {
				if (minVolume <= 0.0f) continue;
				double k = Math.sqrt(h);
				vec3d = new Vec3d(serverPlayerEntity.getX() + e / k * 2.0, serverPlayerEntity.getY() + f / k * 2.0, serverPlayerEntity.getZ() + g / k * 2.0);
				j = minVolume;
			}
			serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(registryEntry, category, vec3d.getX(), vec3d.getY(), vec3d.getZ(), j, pitch, l));
			++i;
		}
		if (i == 0) {
			throw new SimpleCommandExceptionType(Text.translatable("commands.playsound.failed")).create();
		}
		if (targets.size() == 1) {
			source.sendFeedback(() -> Text.translatable("commands.playsound.success.single", sound, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
		} else {
			source.sendFeedback(() -> Text.translatable("commands.playsound.success.multiple", sound, targets.size()), true);
		}
		return i;
	}
}
