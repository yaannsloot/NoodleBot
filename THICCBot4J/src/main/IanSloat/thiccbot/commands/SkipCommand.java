package main.IanSloat.thiccbot.commands;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SkipCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.SKIP, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "skip");
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
				List<AudioTrack> tracks = musicManager.scheduler.getPlaylist();
				if (tracks.isEmpty()) {
					if (musicManager.player.getPlayingTrack() != null) {
						musicManager.player.playTrack(null);
						musicManager.scheduler.nextTrack();
						Message skipMessage = event.getChannel().sendMessage("Track skipped").submit().get();
						skipMessage.delete().queueAfter(5, TimeUnit.SECONDS);
					}
				} else if (tracks.size() > 0) {
					musicManager.player.playTrack(null);
					musicManager.scheduler.nextTrack();
					Message skipMessage = event.getChannel().sendMessage("Track skipped").submit().get();
					skipMessage.delete().queueAfter(5, TimeUnit.SECONDS);
				} else {
					Message skipMessage = event.getChannel().sendMessage("No tracks are playing or queued").submit()
							.get();
					skipMessage.delete().queueAfter(5, TimeUnit.SECONDS);
				}
			} else {
				Message skipMessage = event.getChannel().sendMessage("Not currently connected to any voice channels")
						.submit().get();
				skipMessage.delete().queueAfter(5, TimeUnit.SECONDS);
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
