package main;
import sx.blah.discord.api.*;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.handle.impl.events.shard.LoginEvent;
import main.WolframController;

public class THICCBotMain {

	static String questionIDs[] = { "what", "how", "why", "when", "who", "where" };
	static String waAppID;
	
	public static void main(String[] args) {
		
		String token = args[0];
		
		waAppID = args[1];
		
		IDiscordClient client;
		
		client = BotUtils.getBuiltDiscordClient(token);
		
		client.getDispatcher().registerListener(new Events());
		
		client.login();
	
	}

}