package main.IanSloat.thiccbot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import main.IanSloat.thiccbot.BotUtils;
import main.IanSloat.thiccbot.ThiccBotMain;
import main.IanSloat.thiccbot.tools.HierarchyUtils;
import main.IanSloat.thiccbot.tools.PermissionsManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SetPermissionCommand extends Command {

	@Override
	public boolean CheckUsagePermission(Member user, PermissionsManager permMgr) {
		return permMgr.authUsage(permMgr.PERMMGR, user);
	}

	@Override
	public boolean CheckForCommandMatch(Message command) {
		return command.getContentRaw().toLowerCase().startsWith(BotUtils.BOT_PREFIX + "permission");
	}

	@Override
	public void execute(MessageReceivedEvent event) throws NoMatchException {
		if (!(CheckForCommandMatch(event.getMessage()))) {
			throw new NoMatchException();
		}
		event.getMessage().delete().queue();
		if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
			String command = BotUtils.normalizeSentence(event.getMessage().getContentStripped().toLowerCase()
					.substring((BotUtils.BOT_PREFIX + "permission").length()));
			String[] words = command.split(" ");
			List<Member> users = new ArrayList<Member>();
			users.addAll(event.getMessage().getMentionedMembers());
			List<Role> roles = new ArrayList<Role>();
			roles.addAll(event.getMessage().getMentionedRoles());
			List<Role> authorRoles = new ArrayList<Role>();
			authorRoles.addAll(event.getMember().getRoles());
			if (users.size() == 0 && roles.size() == 0) {
				event.getMessage().delete().queue();
				event.getChannel().sendMessage("You must specify at least one role or user to apply a permission to")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			} else if (users.size() == 1 && users.get(0).equals(event.getMember()) && roles.size() == 0) {
				event.getMessage().delete().queue();
				event.getChannel().sendMessage("You cannot apply a permission to yourself")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			} else if (!(BotUtils.stringArrayContains(PermissionsManager.commandWords, words[0]))) {
				event.getMessage().delete().queue();
				event.getChannel().sendMessage("You must reference a valid command identifier or command group")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			} else if (!(BotUtils.stringArrayContains(new String[] { "allow", "deny" }, words[1]))) {
				event.getMessage().delete().queue();
				event.getChannel().sendMessage(
						"You must explicitly state whether to allow or deny usage of this command or command group")
						.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
			} else {
				if(!event.getAuthor().equals(ThiccBotMain.botOwner)) {
					List<Member> userCopy = users;
					List<Role> roleCopy = roles;
					for (int i = 0; i < userCopy.size(); i++) {
						if (HierarchyUtils.isMemberLowerThan(event.getMember(), userCopy.get(i))) {
							users.remove(userCopy.get(i));
						}
					}
					for (int i = 0; i < roleCopy.size(); i++) {
						if (HierarchyUtils.isRoleLowerThan(HierarchyUtils.getMemberHighestRole(event.getMember()),
								roleCopy.get(i))) {
							roles.remove(roleCopy.get(i));
						}
					}
					users = userCopy;
					roles = roleCopy;
				}
				if (users.size() == 0 && roles.size() == 0) {
					event.getChannel().sendMessage(
							"The users/roles you mentioned all have higher positions than you and you cannot set their permissions")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				} else {
					PermissionsManager permMgr = getPermissionsManager(event.getGuild());
					event.getMessage().delete().queue();
					String permission = "";
					String authorException = "";
					if (BotUtils.stringArrayContains(new String[] { "player", "management", "utility", "misc" },
							words[0])) {
						if (words[1].equals("allow")) {
							permission = permMgr.ALLOW_GLOBAL;
						} else {
							permission = permMgr.DENY_GLOBAL;
							authorException = permMgr.ALLOW_GLOBAL;
						}
					} else {
						if (words[1].equals("allow")) {
							permission = permMgr.ALLOW;
						} else {
							permission = permMgr.DENY;
							authorException = permMgr.ALLOW;
						}
					}
					for (Member user : users) {
						permMgr.SetPermission(words[0], user, permission);
					}
					for (Role role : roles) {
						permMgr.SetPermission(words[0], role, permission);
					}
					if (BotUtils.checkForElement(roles, authorRoles) && words[1].equals("deny")) {
						permMgr.SetPermission(words[0], event.getMember(), authorException);
					}
					if (users.contains(event.getMember()) && words[1].equals("deny")) {
						permMgr.SetPermission(words[0], event.getMember(), authorException);
					}
					event.getChannel().sendMessage("Permissions set successfully")
							.queue((message) -> message.delete().queueAfter(5, TimeUnit.SECONDS));
				}
			}
		} else {
			event.getChannel().sendMessage("You must be an administrator of this server to manage permissions").queue();
		}
	}

}
