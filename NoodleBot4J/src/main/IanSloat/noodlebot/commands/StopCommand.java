package main.IanSloat.noodlebot.commands;

import java.util.concurrent.TimeUnit;

import main.IanSloat.noodlebot.BotUtils;
import main.IanSloat.noodlebot.lavaplayer.GuildMusicManager;
import main.IanSloat.noodlebot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(getCommandId(), user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "stop");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		VoiceChannel voiceChannel = event.getGuild().getAudioManager().getConnectedChannel();
		if (voiceChannel != null) {
			GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
			musicManager.scheduler.stop();
			event.getChannel().sendMessage("Stopped the current track")
					.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
		} else {
			event.getChannel().sendMessage("Not currently connected to any voice channels")
					.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
		}
	}

	@Override
	public String getHelpSnippet() {
		return "**nood stop** - Stops the currently playing song and clears the queue";
	}

	@Override
	public String getCommandId() {
		return "stop";
	}

	@Override
	public String getCommandCategory() {
		return Command.CATEGORY_PLAYER;
	}
}