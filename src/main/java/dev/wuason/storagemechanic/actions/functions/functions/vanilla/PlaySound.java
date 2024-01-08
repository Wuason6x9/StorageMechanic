package dev.wuason.storagemechanic.actions.functions.functions.vanilla;

import dev.wuason.libs.kyori.adventure.audience.Audience;
import dev.wuason.libs.kyori.adventure.key.Key;
import dev.wuason.libs.kyori.adventure.sound.Sound;
import dev.wuason.mechanics.Mechanics;
import dev.wuason.storagemechanic.actions.Action;
import dev.wuason.storagemechanic.actions.functions.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Locale;

public class PlaySound extends Function {
    public static ArrayList<String> ARGS = new ArrayList<>(){{
        add("player".toUpperCase(Locale.ENGLISH).intern());
        add("sound".toUpperCase(Locale.ENGLISH).intern());
        add("volume".toUpperCase(Locale.ENGLISH).intern());
        add("pitch".toUpperCase(Locale.ENGLISH).intern());
    }};
    public PlaySound() {
        super("playSound".toUpperCase(Locale.ENGLISH).intern(), ARGS);
    }

    @Override
    public void execute(Action action, Player player, Object... objects) {
        String playerStr = "$PLAYER$";
        if(objects[0] != null) playerStr = ((String) objects[0]).trim().intern();
        Object playerPlaySound = null;
        if(action.getPlaceholders().containsKey(playerStr)) playerPlaySound = action.getPlaceholders().get(playerStr);
        if(playerPlaySound == null) playerPlaySound = Bukkit.getPlayer(playerStr);
        String sound = ((String) objects[1]).trim();
        String volume = "1";
        String pitch = "1";
        if(objects[2] != null) volume = (String) objects[2];
        if(objects[3] != null) pitch = (String) objects[3];
        Audience audience = Mechanics.getAdventureAudiences().player(((Player)playerPlaySound));
        audience.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER,Float.parseFloat(volume),Float.parseFloat(pitch)));
    }
}
