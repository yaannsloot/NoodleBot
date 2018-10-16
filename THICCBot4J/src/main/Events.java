package main;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.util.audio.AudioPlayer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;


import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;
import main.THICCBotMain;

public class Events {
    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){
    	if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX)) {
	    	
    		System.out.println("Message recieved from: " + event.getAuthor().getName() + " server=" + event.getGuild().getName() + " Content=\"" + event.getMessage() + "\"");
	    	if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "ping"))
	            BotUtils.sendMessage(event.getChannel(), "pong");
	        
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "help")) {
	        	
	        	String help = "help - Lists available commands\n"
	        	+ "die - No u\n"
	        	+ "play <video> - Plays a youtube video. You can enter the video name or the video URL\n"
	        	+ "volume <0-2> - Changes the volume of the video thats playing. Volume ranges from 0-2\n"
	        	+ "stop - Stops the current playing video\n"
	        	+ "leave - Leaves the voice chat\n"
	        	+ "what <question> - Asks ThiccBot a question\n"
	        	+ "info - Prints info about the bot\n\n"
	        	+ "Reminder: the calling word \'thicc\' is not case sensitive\n"
	        	+ "This is to accommodate for mobile users";
	        	BotUtils.sendMessage(event.getChannel(), help);
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "die")) {
	        	BotUtils.sendMessage(event.getChannel(), "no u");
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX) && BotUtils.checkForWords(event.getMessage().getContent(), THICCBotMain.questionIDs, false, true)) {
	        	System.out.println(event.getMessage().getContent().substring(8));
	        	WolframController waClient = new WolframController(THICCBotMain.waAppID);
	        	waClient.askQuestionAndSend(event.getMessage().getContent().substring(8), event.getChannel());
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "info")) {
	        	EmbedBuilder response = new EmbedBuilder();
	        	response.appendField("Current server location", "University of Illinois at Urbana-Champaign", false);
	        	response.appendField("Powered by", "Java", false);
	        	response.appendField("Bot Version", "v0.6alpha", false);
	        	response.appendField("Status", "Currently being ported from python build\nAwaiting deployment to main bot", false);
	        	response.appendField("Current shard count", event.getClient().getShardCount() + " Shards active", false);
	        	response.appendField("Current amount of threads running on server", Thread.activeCount() + " Active threads", false);
	        	response.withTitle("Bot Info");
	        	response.withColor(0, 255, 0);
	        	RequestBuffer.request(() -> event.getChannel().sendMessage(response.build()));
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "play")) {
	        	IVoiceChannel voiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
	        	if(voiceChannel != null) {
	        		voiceChannel.join();
	        		String videoURL = event.getMessage().getContent().substring(13);
	        		YoutubeDLRequest request = new YoutubeDLRequest('\"' + videoURL + '\"');
	        		request.setOption("default-search", "auto");
	        		request.setOption("format", "bestaudio");
	        		request.setOption("dump-json");
	        		request.setOption("simulate");
	        		YoutubeDLResponse response;
	        		
					try {
						response = YoutubeDL.execute(request);
						System.out.println("Request performed");
						System.out.println(response.getOut());
						ytdlOutputProcessor vInfo = new ytdlOutputProcessor(response.getOut());
						System.out.println(vInfo.getUploader());
						System.out.println(vInfo.getUrl());
						System.out.println(vInfo.getVideoUrl());
						System.out.println(vInfo.getDuration());
						AudioPlayer audioP = AudioPlayer.getAudioPlayerForGuild(event.getGuild());
						audioP.clear();
						URL url;
						try {
							url = new URL(vInfo.getUrl());
							try {
								audioP.queue(url);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (UnsupportedAudioFileException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} catch (YoutubeDLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        	}
	        }
	        else if(event.getMessage().getContent().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "leave")) {
	        	IVoiceChannel voiceChannel = event.getGuild().getConnectedVoiceChannel();
	        	if(voiceChannel != null) {
	        		voiceChannel.leave();
	        	}
	        }
    	}
    }
    @EventSubscriber
    public void onBotLogin(LoginEvent event){
		System.out.println("Logged in.");
		event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "thicc4j help");
	}
}