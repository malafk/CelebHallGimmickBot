package lol.maltest.celebhall.listeners;

import lol.maltest.celebhall.DiscordBot;
import lol.maltest.celebhall.impl.AnswerObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class MainListener extends ListenerAdapter {

    private DiscordBot discordBot;
    private JDA jda;

    public MainListener(JDA jda, DiscordBot discordBot) {
        this.jda = jda;
        this.discordBot = discordBot;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getGuild().getId().equals("803645135141208124") || event.getAuthor().isBot()) {
            return;
        }
        if(containsChannel(event.getChannel().getIdLong())) {
            if(containsAnswer(event.getMessage().getContentRaw().toLowerCase(), event.getChannel().getIdLong())) {
                giveRole(event.getMember(), event.getGuild());
            }
            if(!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.getMessage().delete().queue();
            }
            if(event.getChannel().asTextChannel().getSlowmode() == 0) {
                event.getChannel().asTextChannel().getManager().setSlowmode(30).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getGuild() != null && !event.getGuild().getId().equals("803645135141208124")) {
            event.reply("This can only be used in a specific server!").queue();
            return;
        }
        switch(event.getName().toLowerCase()) {
            case "addanswer":
                if(!event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
                    event.reply(":x: You need administrator perms for this!").setEphemeral(true).queue();
                    return;
                }
                OptionMapping answerMapping = event.getOption("answers");
                OptionMapping roleMapping = event.getOption("role");
                OptionMapping roleTakeMapping = event.getOption("roletook");

                ArrayList<String> answers = new ArrayList<>(Arrays.asList(answerMapping.getAsString().split(", ")));
                Long roleId = roleMapping.getAsRole().getIdLong();
                Long takeId = roleTakeMapping.getAsRole().getIdLong();

                discordBot.dataConfig.set("channels." + event.getChannel().getId() + ".answers", answers);
                discordBot.dataConfig.set("channels." + event.getChannel().getId() + ".roleId", roleId);
                discordBot.dataConfig.set("channels." +  event.getChannel().getId() + ".roleTake", takeId);

                try {
                    discordBot.dataConfig.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                HashMap<Long, Long> tempMap = new HashMap<>();
                tempMap.put(event.getChannel().getIdLong(), roleId);
                discordBot.answerCache.add(new AnswerObject(event.getChannel().getIdLong(), roleId, takeId, answers));

                event.reply("✅ Saved the answers!").queue();

                break;
        }
    }

    @Override
    public void onReady(ReadyEvent e) {
        Guild guild = jda.getGuildById("803645135141208124");

        if(guild != null) {
            guild.upsertCommand("addanswer", "Add answer")
                    .addOption(OptionType.STRING, "answers", "Format: one,two,three",true)
                    .addOption(OptionType.ROLE, "role", "Role gaven on correct answer",true)
                    .addOption(OptionType.ROLE, "roletook", "Role taken on correct answer (Optional)",false)
                    .queue();

            System.out.println("Registered commands.");
        } else {
            System.out.println(guild.getId());
        }
    }

    public boolean containsChannel(Long id) {
        for(AnswerObject answerObject : discordBot.answerCache) {
            return answerObject.channel.equals(id);
        }
        return false;
    }

    public boolean containsAnswer(String text, Long id) {
        for(AnswerObject answerObject : discordBot.answerCache) {
            if(answerObject.channel.equals(id)) {
                String[] arr = new String[answerObject.answers.size()];
                arr = answerObject.answers.toArray(arr);
                return stringContainsItemFromList(text.toLowerCase(), arr);
            }
        }
        return false;
    }

    public void giveRole(Member member, Guild guild) {
        for(AnswerObject answerObject : discordBot.answerCache) {
            Role take = guild.getRoleById(answerObject.roleToTake);
            Role role = guild.getRoleById(answerObject.roleToGive);
            if(role != null) {
                System.out.println("The role to give is null");
            }
            guild.addRoleToMember(UserSnowflake.fromId(member.getId()), role).queue();
            if(take != null) {
                guild.removeRoleFromMember(UserSnowflake.fromId(member.getId()), take).queue();
            }
        }
    }

    public static boolean stringContainsItemFromList(String inputStr, String[] items) {
        return Arrays.stream(items).anyMatch(inputStr::contains);
    }
}
