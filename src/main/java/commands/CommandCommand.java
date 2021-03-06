package commands;

import data.CommandForbidden;
import data.Guild;
import data.User;
import enums.Language;
import util.Message;
import exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;
import util.Translator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by steve on 14/07/2016.
 */
public class CommandCommand extends AbstractCommand{

    private final static Logger LOG = LoggerFactory.getLogger(CommandCommand.class);

    public CommandCommand(){
        super("cmd","\\s+(\\w+)\\s+(on|off|0|1|true|false)");
        setUsableInMP(false);
    }

    @Override
    public boolean request(IMessage message) {
        if (super.request(message)) {
            User author = User.getUser(message.getGuild(), message.getAuthor());
            Language lg = Translator.getLanguageFrom(message.getChannel());

            if (author.getRights() >= User.RIGHT_MODERATOR) {
                Guild guild = Guild.getGuild(message.getGuild());
                Matcher m = getMatcher(message);
                m.find();
                List<Command> potentialCmds = new ArrayList<>();
                String commandName = m.group(1).trim();
                for (Command command : CommandManager.getCommands())
                    if (command.isPublic() && !command.isAdmin() && command.getName().contains(commandName))
                        potentialCmds.add(command);

                if (potentialCmds.size() == 1){
                    Command command = potentialCmds.get(0);
                    String value = m.group(2);

                    if (command instanceof CommandCommand){
                        Message.sendText(message.getChannel(), Translator.getLabel(lg, "announce.request.1"));
                        return false;
                    }
                    if (value.matches("false") || value.matches("1") || value.matches("off")){
                        if (! guild.getForbiddenCommands().containsKey(command.getName())) {
                            new CommandForbidden(command, guild).addToDatabase();
                            Message.sendText(message.getChannel(), Translator.getLabel(lg, "announce.request.2") + " *" + commandName
                                    + "* " + Translator.getLabel(lg, "announce.request.3"));
                        }
                        else
                            new ForbiddenCommandFoundDiscordException().throwException(message, this);
                    }
                    else if (value.matches("true") || value.matches("0") || value.matches("on")){
                        if (guild.getForbiddenCommands().containsKey(command.getName())) {
                            guild.getForbiddenCommands().get(command.getName()).removeToDatabase();
                            Message.sendText(message.getChannel(), Translator.getLabel(lg, "announce.request.2") + " *" + commandName
                                    + "* " + Translator.getLabel(lg, "announce.request.4"));
                        }
                        else
                            new ForbiddenCommandNotFoundDiscordException().throwException(message, this);
                    }
                    else
                        new BadUseCommandDiscordException().throwException(message, this);
                }
                else if (potentialCmds.isEmpty())
                    new CommandNotFoundDiscordException().throwException(message, this);
                else
                    new TooMuchPossibilitiesDiscordException().throwException(message, this);
            }
            else
                new NotEnoughRightsDiscordException().throwException(message, this);
        }
        return false;
    }

    @Override
    public String help(Language lg, String prefixe) {
        return "**" + prefixe + name + "** " + Translator.getLabel(lg, "command.help");
    }

    @Override
    public String helpDetailed(Language lg, String prefixe) {
        return help(lg, prefixe)
                + "\n" + prefixe + "`"  + name + " *CommandForbidden* true` : " + Translator.getLabel(lg, "command.detailed.1")
                + "\n" + prefixe + "`"  + name + " *CommandForbidden* false` : " + Translator.getLabel(lg, "command.detailed.2") + "\n";
    }
}
