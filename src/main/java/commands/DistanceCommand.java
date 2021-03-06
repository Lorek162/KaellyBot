package commands;

import data.Position;
import enums.Transport;
import enums.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IMessage;
import util.Message;
import util.Translator;

import java.util.regex.Matcher;

/**
 * Created by steve on 14/07/2016.
 */
public class DistanceCommand extends AbstractCommand{

    private final static Logger LOG = LoggerFactory.getLogger(DistanceCommand.class);

    public DistanceCommand(){
        super("dist", "\\s+\\[?(-?\\d{1,2})\\s*[,|\\s]\\s*(-?\\d{1,2})\\]?");
    }

    @Override
    public boolean request(IMessage message) {
        if (super.request(message)) {
            Matcher m = getMatcher(message);
            m.find();
            Language lg = Translator.getLanguageFrom(message.getChannel());
            Position position = new Position(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
            StringBuilder st = new StringBuilder();
            Transport zaap = null;
            Transport transportLimited = null;

            if(! position.isNull()) {
                double minDist = Double.MAX_VALUE;
                double minDistLimited = Double.MAX_VALUE;
                for (Transport transport : Transport.values()) {
                    double tmp = transport.getPosition().getDistance(position);
                    if (transport.isFreeAccess() && (zaap == null || minDist > tmp)){
                        zaap = transport;
                        minDist = tmp;
                    }
                    if (! transport.isFreeAccess() && (transportLimited == null || minDistLimited > tmp)){
                        transportLimited = transport;
                        minDistLimited = tmp;
                    }
                }

                st.append(Translator.getLabel(lg, "distance.request.1")).append(" ").append(zaap.toString());
                if (minDist > minDistLimited)
                    st.append("\n").append(Translator.getLabel(lg, "distance.request.2")).append(" ").append(transportLimited.toString());
            }
            else
                st.append(Translator.getLabel(lg, "distance.request.3"));

            Message.sendText(message.getChannel(), st.toString());
        }
        return false;
    }

    @Override
    public String help(Language lg, String prefixe) {
        return "**" + prefixe + name + "** " + Translator.getLabel(lg, "distance.help");
    }

    @Override
    public String helpDetailed(Language lg, String prefixe) {
        return help(lg, prefixe)
                + "\n" + prefixe + "`"  + name + "` [POS, POS]` : " + Translator.getLabel(lg, "distance.help") + "\n";
    }
}
