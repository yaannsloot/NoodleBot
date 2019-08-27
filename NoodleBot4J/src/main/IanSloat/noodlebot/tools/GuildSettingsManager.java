package main.IanSloat.noodlebot.tools;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import main.IanSloat.noodlebot.BotUtils;
import net.dv8tion.jda.api.entities.Guild;

public class GuildSettingsManager {

	private Guild guild;
	private static final Logger logger = LoggerFactory.getLogger(GuildSettingsManager.class);
	private File settingsDirectory;
	private File settingsFile;

	public GuildSettingsManager(Guild guild) {
		this.guild = guild;
		settingsDirectory = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings"
				+ BotUtils.PATH_SEPARATOR + guild.getId());
		settingsFile = new File(settingsDirectory.getAbsolutePath() + BotUtils.PATH_SEPARATOR + "settings.guild");
		logger.info(
				"Started guild settings manager for guild: " + guild.getName() + "(id:" + guild.getId() + ')');
	}

	public static void CreateSettingsDirectoriesForGuilds(List<Guild> Guilds) {
		for (Guild guild : Guilds) {
			File SettingDirectory = new File(System.getProperty("user.dir") + BotUtils.PATH_SEPARATOR + "GuildSettings"
					+ BotUtils.PATH_SEPARATOR + guild.getId());
			if (!(SettingDirectory.exists())) {
				SettingDirectory.mkdirs();
				logger.info("Settings directory added for guild:" + guild.getName() + "(id:" + guild.getId()
						+ ") at path " + SettingDirectory.getAbsolutePath());
			}
		}
	}

	public void RemoveSettings() {
		if (settingsDirectory.exists()) {
			FileSystemUtils.deleteRecursively(settingsDirectory);
			logger.info("Guild settings for guild: " + guild.getName() + "(id:" + guild.getId()
					+ ") removed successfully");
		} else {
			logger.info("Guild: " + guild.getName() + "(id:" + guild.getId()
					+ ") had no settings so no files were deleted");
		}
	}

	public void CreateSettings() {
		if (settingsDirectory.exists()) {
			settingsDirectory.delete();
			settingsDirectory.mkdirs();
		} else {
			settingsDirectory.mkdirs();
		}
	}
	
	public NBMLSettingsParser getTBMLParser() {
		return new NBMLSettingsParser(settingsFile);
	}

}