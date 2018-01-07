package tc.oc.pgm.polls;

import com.google.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import tc.oc.commons.core.chat.Audiences;

import java.util.HashMap;
import java.util.Map;

public abstract class Poll implements Runnable {

    public static String boldAqua = ChatColor.BOLD + "" + ChatColor.AQUA;
    public static String normalize = ChatColor.RESET + "" + ChatColor.DARK_AQUA;
    public static String separator = ChatColor.RESET + " | ";

    protected final PollManager pollManager;
    protected final String initiator;
    protected final Audiences audiences;

    @Inject public Poll(PollManager pollManager, String initiator, Audiences audiences) {
        this.pollManager = pollManager;
        this.initiator = initiator;
        this.audiences = audiences;
        this.voteFor(initiator);
    }

    protected final Map<String, Boolean> votes = new HashMap<String, Boolean>();
    protected int timeLeftSeconds = 60;

    public String getInitiator() {
        return this.initiator;
    }

    public int getTotalVotes() {
        return this.votes.size();
    }

    public int getVotesFor() {
        return this.getVotes(true);
    }

    public int getVotesAgainst() {
        return this.getVotes(false);
    }

    private int getVotes(boolean filterValue) {
        int total = 0;
        for(boolean vote : this.votes.values()) {
            if(vote == filterValue) total += 1;
        }
        return total;
    }


    public int getTimeLeftSeconds() {
        return timeLeftSeconds;
    }

    private void decrementTimeLeft() {
        timeLeftSeconds -= 5;
    }

    public boolean isSuccessful() {
        return this.getVotesFor() > this.getVotesAgainst();
    }

    public abstract void executeAction();

    public abstract String getActionString();

    public abstract String getDescriptionMessage();

    public String getStatusMessage() {
        String message = boldAqua + "[Poll] " + this.getTimeLeftSeconds() + normalize + " seconds left" + separator;
        message += getActionString() + separator + formatForAgainst();

        return message;
    }

    protected String formatForAgainst() {
        return normalize + "Yes: " + boldAqua + this.getVotesFor() + " " + normalize + "No: " + boldAqua + this.getVotesAgainst();
    }

    public static String tutorialMessage() {
        return normalize + "Use " + boldAqua + "/vote [yes|no]" + normalize + " to vote";
    }

    public boolean hasVoted(String playerName) {
        return this.votes.containsKey(playerName);
    }

    public void voteFor(String playerName) {
        this.votes.put(playerName, true);
    }

    public void voteAgainst(String playerName) {
        this.votes.put(playerName, false);
    }

    @Override
    public void run() {
        int timeLeftSeconds = this.getTimeLeftSeconds();
        if(timeLeftSeconds <= 0) {
            this.pollManager.endPoll(PollEndReason.Completed);
        } else if(timeLeftSeconds % 15 == 0 || (timeLeftSeconds < 15 && timeLeftSeconds % 5 == 0)) {
            this.audiences.localServer().sendMessage(this.getStatusMessage());
        }
        this.decrementTimeLeft();
    }
}
