package listeners;

import commands.*;
import enums.Language;
import util.ClientConfig;
import data.Constants;
import data.Guild;
import data.User;
import util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import util.Translator;

/**
 * Created by steve on 14/07/2016.
 */
public class GuildCreateListener {

    private final static Logger LOG = LoggerFactory.getLogger(GuildCreateListener.class);

    public GuildCreateListener(){
        super();
    }

        @EventSubscriber
        public void onReady(GuildCreateEvent event) {
            ClientConfig.setSentryContext(event.getGuild(), null, null, null);

            if(!Guild.getGuilds().containsKey(event.getGuild().getStringID())) {
                Guild guild = new Guild(event.getGuild().getStringID(), event.getGuild().getName(),
                        Translator.detectLanguage(event.getGuild().getDefaultChannel()));
                guild.addToDatabase();

                for (IUser user : event.getGuild().getUsers())
                    new User(user.getStringID(), user.getDisplayName(event.getGuild()), User.RIGHT_INVITE, guild)
                            .addToDatabase();

                User.getUser(event.getGuild(), event.getGuild().getOwner()).changeRight(User.RIGHT_ADMIN);
                Language lg = guild.getLanguage();
                LOG.info("La guilde " + guild.getId() + " - " + guild.getName() + " a ajouté "   + Constants.name);

                String customMessage = Translator.getLabel(lg, "welcome.message");

                customMessage = customMessage
                        .replaceAll("\\{name\\}", Constants.name)
                        .replaceAll("\\{game\\}", Constants.game)
                        .replaceAll("\\{prefix\\}", Constants.prefixCommand)
                        .replaceAll("\\{help\\}", new HelpCommand().getName())
                        .replaceAll("\\{server\\}", new ServerCommand().getName())
                        .replaceAll("\\{lang\\}", new LanguageCommand().getName())
                        .replaceAll("\\{twitter\\}", new TwitterCommand().getName())
                        .replaceAll("\\{almanax\\}", new AlmanaxCommand().getName())
                        .replaceAll("\\{rss\\}", new RSSCommand().getName())
                        .replaceAll("\\{owner\\}", event.getGuild().getOwner().mention())
                        .replaceAll("\\{guild\\}", event.getGuild().getName());

                if(event.getGuild().getDefaultChannel() != null && event.getGuild().getDefaultChannel()
                        .getModifiedPermissions(ClientConfig.DISCORD().getOurUser())
                        .contains(Permissions.SEND_MESSAGES))
                    Message.sendText(event.getGuild().getDefaultChannel(), customMessage);
                else
                    Message.sendText(event.getGuild().getOwner().getOrCreatePMChannel(), customMessage);

                Message.sendText(ClientConfig.DISCORD().getChannelByID(Constants.chanReportID),
                        "[NEW] **" + guild.getName() + "**, +" + event.getGuild().getUsers().size()
                                +  " utilisateurs");

            }
        }
}
