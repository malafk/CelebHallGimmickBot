package lol.maltest.celebhall.impl;

import java.util.ArrayList;

public class AnswerObject {

    public final Long channel;
    public final Long roleToGive;
    public final Long roleToTake;
    public final ArrayList<String> answers;

    public AnswerObject(Long channel, Long roleToGive, Long roleToTake, ArrayList<String> answers) {
        this.channel = channel;
        this.roleToGive = roleToGive;
        this.roleToTake = roleToTake;
        this.answers = answers;
    }
}
