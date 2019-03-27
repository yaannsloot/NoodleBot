package main.IanSloat.thiccbot.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.STOP, user);
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
		try {
			VoiceChannel voiceChannel = event.getGuild().getAudioManager().getConnectedChannel();
			if (voiceChannel != null) {
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
				musicManager.scheduler.stop();
				Message commandMessage = event.getChannel().sendMessage("Stopped the current track").submit().get();
				commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
			} else {
				Message commandMessage = event.getChannel().sendMessage("Not currently connected to any voice channels")
						.submit().get();
				commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
