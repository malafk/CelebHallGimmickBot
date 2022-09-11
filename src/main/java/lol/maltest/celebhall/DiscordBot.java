package lol.maltest.celebhall;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lol.maltest.celebhall.impl.AnswerObject;
import lol.maltest.celebhall.listeners.MainListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DiscordBot {
    public JDA jda;

    public YamlDocument botConfig;
    public YamlDocument dataConfig;

    // Channel, Role, Answers
    public ArrayList<AnswerObject> answerCache = new ArrayList<>();

    public DiscordBot() {

        try {
        botConfig = YamlDocument.create(new File("data", "config.yml"), getClass().getClassLoader().getResource("config.yml").openStream(),
                GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());

        dataConfig = YamlDocument.create(new File("data", "data.yml"), getClass().getClassLoader().getResource("data.yml").openStream(),
                GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version")).build());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            jda = JDABuilder.createLight(botConfig.getString("token"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT).build();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
        if(dataConfig.getSection("channels") != null) {
            dataConfig.getSection("channels").getKeys().forEach(key -> {
                HashMap<Long, Long> tempHash = new HashMap<>();
                Long roleToGive = dataConfig.getLong("channels." + key + ".roleId");
                Long roleToTake = dataConfig.getLong("channels." + key + ".roleTake");
                tempHash.put(Long.valueOf(key.toString()), dataConfig.getLong("channels." + key + ".roleId"));
                answerCache.add(new AnswerObject(Long.parseLong(key.toString()), roleToGive, roleToTake, (ArrayList<String>) dataConfig.getStringList("channels." + key + ".answers")));
            });
        }
        System.out.println("Cached " + answerCache.size() + " channels.");

        jda.addEventListener(new MainListener(jda, this));
    }
}
