package dev.wuason.storagemechanic.data.player;

import dev.wuason.mechanics.data.Data;
import dev.wuason.storagemechanic.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerDataManager implements Listener {
    private HashMap<String,PlayerData> players;
    final private DataManager dataManager;
    final public String DATA_TYPE = PlayerData.class.getSimpleName();


    public PlayerDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    public void start(){
        players = new HashMap<>();
        for(Player player : Bukkit.getOnlinePlayers()){

            createPlayerData(player);

        }
    }
    @EventHandler
    public void joinEvent(PlayerJoinEvent playerJoinEvent){

        Player player = playerJoinEvent.getPlayer();
        createPlayerData(player);

    }

    @EventHandler
    public void leaveEvent(PlayerQuitEvent event){
        Player player = event.getPlayer();
        savePlayerData(player);
    }

    private void savePlayerData(Player player){

        PlayerData playerData = null;

        if(players.containsKey(player.getUniqueId().toString())){
            playerData = players.get(player.getUniqueId().toString());
        }
        if(playerData == null){
            playerData = new PlayerData(player.getUniqueId());
        }


        Data data = null;

        if(dataManager.existData(DATA_TYPE,player.getUniqueId().toString())){
            data = dataManager.getData(DATA_TYPE,player.getUniqueId().toString());
        }
        if(data == null){
            data = new Data(player.getUniqueId().toString());
        }

        data.setDataObject(playerData);

        dataManager.saveData(data);
    }
    private void createPlayerData(Player player){
        PlayerData playerData = null;

        if(dataManager.existData( DATA_TYPE,player.getUniqueId().toString() )){
            playerData = (PlayerData) dataManager.getData(DATA_TYPE,player.getUniqueId().toString()).getDataObject();
        }
        if(playerData == null){
            playerData = new PlayerData(player.getUniqueId());
        }

        if(!players.containsKey(player.getUniqueId().toString())){
            players.put(player.getUniqueId().toString(),playerData);
        }
    }

    public void saveAllPlayers(){
        for(String uuid : players.keySet()){
            OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

            if(offlineplayer.isOnline()){
                savePlayerData(offlineplayer.getPlayer());
            }
        }
    }
    //METHODS BASICS
    public PlayerData getPlayerData(UUID uuid){
        String id = uuid.toString();
        if(players.containsKey(id)) return players.get(id);
        if(dataManager.existData(DATA_TYPE,id)) return loadPlayerData(uuid);
        return null;
    }
    public boolean existPlayerData(UUID uuid){
        String id = uuid.toString();
        if(players.containsKey(id)) return true;
        if(dataManager.existData(DATA_TYPE,id)) return true;
        return false;
    }
    public void removePlayerData(UUID uuid){
        String id = uuid.toString();
        if(players.containsKey(id)) players.remove(id);
        if(dataManager.existData(DATA_TYPE,id)) dataManager.removeData(DATA_TYPE,id);
    }
    public PlayerData loadPlayerData(UUID uuid){
        String id = uuid.toString();
        if(dataManager.existData(DATA_TYPE,id)){
            PlayerData playerData = (PlayerData) dataManager.getData(DATA_TYPE,id).getDataObject();
            if(!players.containsKey(id)) players.put(id,playerData);
            return playerData;
        }
        return null;
    }
    public void savePlayerData(UUID uuid){
        String id = uuid.toString();
        Data data = null;
        PlayerData playerData = null;
        if(players.containsKey(id)) playerData = players.get(id);
        if(playerData == null) playerData = new PlayerData(uuid);
        if(dataManager.existData(DATA_TYPE,id)) data = dataManager.getData(DATA_TYPE,id);
        if(data == null) data = new Data(id);
        data.setDataObject(playerData);
        dataManager.saveData(data);
    }

    public HashMap<String, PlayerData> getPlayers() {
        return players;
    }


}
