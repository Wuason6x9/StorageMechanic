package dev.wuason.storagemechanic.data;

import dev.wuason.libs.boostedyaml.YamlDocument;
import dev.wuason.mechanics.data.Data;
import dev.wuason.mechanics.data.local.LocalDataManager;
import dev.wuason.mechanics.data.mysql.Column;
import dev.wuason.mechanics.data.mysql.SqlManager;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.data.player.PlayerData;
import dev.wuason.storagemechanic.data.player.PlayerDataManager;
import dev.wuason.storagemechanic.data.storage.StorageData;
import dev.wuason.storagemechanic.data.storage.StorageManagerData;
import dev.wuason.storagemechanic.data.storage.type.api.StorageApiData;
import dev.wuason.storagemechanic.data.storage.type.block.BlockStorageData;
import dev.wuason.storagemechanic.data.storage.type.furniture.FurnitureStorageData;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataManager {
    private PlayerDataManager playerDataManager;
    private StorageManagerData storageManagerData;
    private Method method = Method.LOCAL;

    private LocalDataManager localDataManager;
    private SqlManager sqlManager;

    final public Class<?>[] DATA_CLASES = {PlayerData.class, StorageData.class, BlockStorageData.class, FurnitureStorageData.class, StorageApiData.class};
    final public List<Column> COLUMNS = Arrays.asList(
            new Column("id","INT AUTO_INCREMENT PRIMARY KEY"),
            new Column("data_id", "VARCHAR(255)"),
            new Column("data", "LONGTEXT")
    );

    public DataManager(StorageMechanic core){
        playerDataManager = new PlayerDataManager(this);
        storageManagerData = new StorageManagerData(this,core);
        Bukkit.getPluginManager().registerEvents(playerDataManager,StorageMechanic.getInstance());
        load();
        playerDataManager.start();
    }


    public void load(){
        String method = StorageMechanic.getInstance().getManagers().getConfigManager().getMainConfig().getString("data.method").toUpperCase();
        AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance()," <aqua>Method selected: <yellow>" + method);
        try{
            this.method = Method.valueOf(method);
        }
        catch (Exception e){
            AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance(),"<red> Error loading config file. method of data is invalid: <cyan>" + method);
            AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance(),"<red> Valid methods:");
            for(Method m : Method.values()){
                AdventureUtils.sendMessagePluginConsole(StorageMechanic.getInstance(),"<blue> " + m.toString());
            }
        }

        switch (this.method){
            case LOCAL -> {

                startLocalData();

            }
            case DATABASE -> {

                startDataBaseData();

            }

        }

    }

    public void startDataBaseData(){
        YamlDocument config = StorageMechanic.getInstance().getManagers().getConfigManager().getMainConfig();

        String host = config.getString("data.database_config.config_host.host");
        int port = config.getInt("data.database_config.config_host.port");
        String database = config.getString("data.database_config.database_config.database_name");
        String user = config.getString("data.database_config.credentials.user");
        String password = config.getString("data.database_config.credentials.password");
        String driver = config.getString("data.database_config.database_config.driver");

        sqlManager = new SqlManager(StorageMechanic.getInstance(),host,port,database,user,password,driver);

        for(Class<?> c : DATA_CLASES){

            sqlManager.createCustomTable(c.getSimpleName(),COLUMNS);

        }
    }
    public void startLocalData(){
        localDataManager = new LocalDataManager(StorageMechanic.getInstance());
        localDataManager.createDataFolder();

        for(Class<?> c : DATA_CLASES){

            File file = new File(localDataManager.getDir().getPath() + "/" + c.getSimpleName() + "/");
            file.mkdirs();

        }
    }

    public Data getData(String dataType, String dataID){
        switch (method){
            case DATABASE -> {
                return sqlManager.getData(dataType,dataID);
            }
            case LOCAL -> {
                return localDataManager.getData(dataID,dataType);
            }
        }
        return null;
    }
    public boolean existData(String dataType, String dataID){
        switch (method){
            case DATABASE -> {
                return sqlManager.existData(dataType,dataID);
            }
            case LOCAL -> {
                return localDataManager.existData(dataType,dataID);
            }
        }
        return false;
    }
    public void removeData(String dataType, String dataID){
        switch (method){
            case DATABASE -> {
                sqlManager.removeDataStr(dataType,dataID);
            }
            case LOCAL -> {
                localDataManager.removeData(dataType,dataID);
            }
        }
    }
    public void saveData(String dataType, String dataID, String data){
        switch (method){
            case DATABASE -> {
                sqlManager.saveDataStr(dataType,dataID,data);
            }
            case LOCAL -> {
                localDataManager.saveDataStr(data,dataType,dataID);
            }
        }
    }
    public void saveData(Data data){
        switch (method){
            case DATABASE -> {
                sqlManager.saveData(data);
            }
            case LOCAL -> {
                localDataManager.saveData(data);
            }
        }
    }
    public String[] getAllDataIds(String dataType){
        switch (method){
            case DATABASE -> {
                return sqlManager.getAllData(dataType,SqlManager.DATA_ID_NAME_COLUMN).toArray(new String[0]);
            }
            case LOCAL -> {
                return localDataManager.getAllDataIds(dataType);
            }
        }
        return null;
    }
    public Data[] getAllData(String dataType){
        if(!existDataType(dataType)) return null;
        switch (method){
            case DATABASE -> {
                List<String> d = sqlManager.getAllData(dataType,SqlManager.DATA_ID_NAME_COLUMN);
                ArrayList<Data> datas = new ArrayList<>();
                for(String id : d){
                    datas.add(sqlManager.getData(dataType,id));
                }

                return datas.toArray(new Data[0]);
            }
            case LOCAL -> {
                File file = new File(localDataManager.getDir().getPath() + "/" + dataType + "/");
                if(!file.exists()) return null;
                File[] files = Arrays.stream(file.listFiles()).filter(f -> f.getName().endsWith(".mechanic")).toArray(File[]::new);
                if(files.length == 0) return null;
                ArrayList<Data> datas = new ArrayList<>();

                for(File f : files){
                    String id = f.getName().replace(".mechanic","");
                    Data localData = localDataManager.getData(id,dataType);
                    datas.add(localData);
                }
                return datas.toArray(Data[]::new);
            }
        }
        return null;
    }

    public void stop(){
        //SAVE DATA
        saveAllData();
        //STOP CONNECTIONS ETC
        if(method == Method.DATABASE){
            sqlManager.stop();
        }
    }

    public void saveAllData(){
        playerDataManager.saveAllPlayers();
    }

    public enum Method{
        DATABASE,
        LOCAL
    }

    public boolean existDataType(String dataType){
        for(Class<?> c : DATA_CLASES){if(c.getSimpleName().equals(dataType)) return true;}
        return false;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public Method getMethod() {
        return method;
    }

    public LocalDataManager getLocalDataManager() {
        return localDataManager;
    }

    public SqlManager getSqlManager() {
        return sqlManager;
    }

    public StorageManagerData getStorageManagerData() {
        return storageManagerData;
    }

    public Class<?>[] getDATA_CLASES() {
        return DATA_CLASES;
    }

    public List<Column> getCOLUMNS() {
        return COLUMNS;
    }
}
