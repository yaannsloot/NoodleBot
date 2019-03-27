package main.IanSloat.thiccbot.commands;

import java.awt.Color;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.lavaplayer.GuildMusicManager;
import main.IanSloat.thiccbot.tools.GuildSettingsManager;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import main.IanSloat.thiccbot.tools.TBMLSettingsParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PlayCommand extends Command {
	
	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PLAY, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return (command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")
				|| command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add"));
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		try {
			String videoURL = "";
			if (event.getMessage().getAttachments().size() == 0) {
				videoURL = event.getMessage().getContentRaw().substring((BotUtils.BOT_PREFIX + "play ").length());
			} else {
				videoURL = event.getMessage().getAttachments().get(0).getUrl();
			}
			VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
			if (voiceChannel != null) {
				event.getGuild().getAudioManager().openAudioConnection(voiceChannel);
				EmbedBuilder thinkingMsg = new EmbedBuilder();
				thinkingMsg.setTitle("Loading audio...");
				thinkingMsg.setColor(new Color(192, 255, 0));
				Message message = event.getChannel().sendMessage(thinkingMsg.build()).submit().get();
				GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild(), event.getTextChannel());
				if (!(videoURL.startsWith("http://") || videoURL.startsWith("https://")
						|| videoURL.startsWith("scsearch:"))) {
					videoURL = "ytsearch:" + videoURL;
				}
				GuildSettingsManager setMgr = new GuildSettingsManager(event.getGuild());
				TBMLSettingsParser setParser = setMgr.getTBMLParser();
				setParser.setScope(TBMLSettingsParser.DOCROOT);
				setParser.addObj("PlayerSettings");
				setParser.setScope("PlayerSettings");
				if (setParser.getFirstInValGroup("volume").equals(""))
					setParser.addVal("volume", "100");
				musicManager.scheduler.setVolume(Integer.parseInt(setParser.getFirstInValGroup("volume")));
				final String URI = videoURL;
				playerManager.loadItem("" + videoURL, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						logger.info("A track was loaded");
						if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
							musicManager.scheduler.stop();
						} else {
							Message result;
							try {
								result = event.getChannel().sendMessage("Added " + track.getInfo().title + " to queue").submit().get();
								result.delete().queueAfter(5, TimeUnit.SECONDS);
							} catch (InterruptedException | ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							musicManager.scheduler.updateStatus();
						}
						musicManager.scheduler.queue(track);
						message.delete();
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						logger.info("A track playlist was loaded");
						if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
							musicManager.scheduler.stop();
						}
						if ((!URI.startsWith("ytsearch:") || !URI.startsWith("scsearch:")
								|| setParser.getFirstInValGroup("autoplay").equals("on"))
								&& event.getMessage().getContentRaw().toLowerCase()
										.startsWith(BotUtils.BOT_PREFIX + "play")) {
							Message trackMessage;
							try {
								trackMessage = event.getChannel().sendMessage("Loaded " + playlist.getTracks().size() + " tracks").submit().get();
								trackMessage.delete().queueAfter(5, TimeUnit.SECONDS);
							} catch (InterruptedException | ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							for (AudioTrack track : playlist.getTracks()) {
								musicManager.scheduler.queue(track);
							}
						} else {
							logger.info("Was a search and autoplay is off so only one track was loaded");
							musicManager.scheduler.queue(playlist.getTracks().get(0));
							if (event.getMessage().getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "add")) {
								try {
									Message result = event.getChannel().sendMessage("Added " + playlist.getTracks().get(0).getInfo().title + " to queue").submit().get();
									result.delete().queueAfter(5, TimeUnit.SECONDS);
								} catch (InterruptedException | ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								musicManager.scheduler.updateStatus();
							}
						}
						message.delete().queue();
					}

					@Override
					public void noMatches() {
						// Notify the user that we've got nothing
						EmbedBuilder newMsg = new EmbedBuilder();
						newMsg.setTitle("No results found");
						newMsg.setColor(new Color(255, 0, 0));
						message.editMessage(newMsg.build()).queue();
						message.delete().queueAfter(5, TimeUnit.SECONDS);
						logger.info("Audio track search returned no results");
					}

					@Override
					public void loadFailed(FriendlyException throwable) {
						// Notify the user that everything exploded
						EmbedBuilder newMsg = new EmbedBuilder();
						newMsg.setTitle("An error occurred while attempting to load the requested audio\n"
								+ "The URL may be invalid\n"
								+ "If the URL is a stream, the stream can only be played if it is live");
						newMsg.setColor(new Color(255, 0, 0));
						message.editMessage(newMsg.build()).queue();
						message.delete().queueAfter(5, TimeUnit.SECONDS);
						logger.info("An error occurred while attempting to load an audio track");
					}
				});
				musicManager.scheduler.unpauseTrack();
			} else {
				event.getChannel().sendMessage("Get in a voice channel first");
			}
		} catch (java.lang.StringIndexOutOfBoundsException | InterruptedException | ExecutionException e) {
			Message errorMessage;
			try {
				errorMessage = event.getChannel().sendMessage("Play what?").submit().get();
				errorMessage.delete().queueAfter(5, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
