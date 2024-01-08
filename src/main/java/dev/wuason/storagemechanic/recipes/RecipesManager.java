package dev.wuason.storagemechanic.recipes;

import dev.wuason.mechanics.Mechanics;
import dev.wuason.mechanics.items.ItemBuilderMechanic;
import dev.wuason.mechanics.utils.AdventureUtils;
import dev.wuason.storagemechanic.StorageMechanic;
import dev.wuason.storagemechanic.storages.types.block.config.*;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanic;
import dev.wuason.storagemechanic.storages.types.block.mechanics.BlockMechanicManager;
import dev.wuason.storagemechanic.storages.types.block.mechanics.integrated.hopper.HopperBlockMechanic;
import dev.wuason.storagemechanic.utils.StorageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.util.*;

public class RecipesManager {

    private Set<NamespacedKey> recipesKeys = new HashSet<>();

    private StorageMechanic core;

    public RecipesManager(StorageMechanic core) {
        this.core = core;
    }



    public void loadRecipes(){

        for (NamespacedKey namespacedKey : recipesKeys) {
            core.getServer().removeRecipe(namespacedKey);
        }

        recipesKeys = new HashSet<>();

        File base = new File(core.getDataFolder() + "/recipes/");
        base.mkdirs();

        File[] files = Arrays.stream(base.listFiles()).filter(f -> {

            if(f.getName().contains(".yml")) return true;

            return false;

        }).toArray(File[]::new);

        for(File file : files){


            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            ConfigurationSection sectionRecipes = config.getConfigurationSection("recipes");

            if(sectionRecipes != null){

                for(String key : sectionRecipes.getKeys(false)){

                    ConfigurationSection sectionRecipe = sectionRecipes.getConfigurationSection(key);

                    if(sectionRecipe == null) continue;

                    RecipeType recipeType = null;

                    try {
                        recipeType = RecipeType.valueOf(sectionRecipe.getString("type", ".").toUpperCase(Locale.ENGLISH));
                    }
                    catch (Exception e){
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                        AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe type is invalid or invalid!");
                        continue;
                    }

                    switch (recipeType) {
                        case SHAPELESS -> {

                            String resultStr = sectionRecipe.getString("result");
                            if (resultStr == null || getItem(resultStr) == null) {
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe result is null or invalid!");
                                continue;
                            }

                            ItemStack result = new ItemBuilderMechanic(getItem(resultStr), getAmount(resultStr)).build();

                            List<ItemStack> ingredients = new ArrayList<>();

                            for(String ingredientStr : sectionRecipe.getStringList("ingredients")){
                                if(ingredientStr == null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe ingredient is null or invalid!");
                                    continue;
                                }
                                ingredients.add(new ItemBuilderMechanic(ingredientStr, 1).build());
                            }
                            NamespacedKey namespacedKey = new NamespacedKey(core, key);
                            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, result);

                            for(ItemStack ingredient : ingredients){
                                shapelessRecipe.addIngredient(new RecipeChoice.ExactChoice(ingredient));
                            }

                            core.getServer().addRecipe(shapelessRecipe);
                            recipesKeys.add(namespacedKey);
                        }

                        case SHAPED -> {

                            String resultStr = sectionRecipe.getString("result");
                            if (resultStr == null || getItem(resultStr) == null) {
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe result is null or invalid!");
                                continue;
                            }

                            ItemStack result = new ItemBuilderMechanic(getItem(resultStr), getAmount(resultStr)).build();

                            HashMap<Character, ItemStack> ingredients = new HashMap<>();

                            if(sectionRecipe.getConfigurationSection("ingredients") == null){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe ingredients is null or invalid!");
                                continue;
                            }

                            for(String keyShape : sectionRecipe.getConfigurationSection("ingredients").getKeys(false)){
                                if(keyShape.charAt(0) == 'x' || keyShape.charAt(0) == 'X') continue;
                                String ingredientStr = sectionRecipe.getString("ingredients." + keyShape);
                                if(ingredientStr == null){
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                    AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe ingredient is null or invalid!");
                                    continue;
                                }
                                ItemStack ingredient = new ItemBuilderMechanic(ingredientStr, 1).build();
                                ingredients.put(keyShape.charAt(0), ingredient);
                            }

                            List<String> shape = sectionRecipe.getStringList("shape");

                            if(shape == null || shape.size() != 3){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe shape is null or invalid!");
                                continue;
                            }

                            NamespacedKey namespacedKey = new NamespacedKey(core, key);
                            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);

                            if(!isValidShape(shape)){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe shape is null or invalid!");
                                continue;
                            }

                            shapedRecipe.shape(shape.toArray(new String[3]));

                            if(!addIngredientsShapedRecipe(shapedRecipe, ingredients)){
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error loading Recipe! Recipe_id: " + key + " in file: " + file.getName());
                                AdventureUtils.sendMessagePluginConsole(core, "<red>Error: The Recipe ingredients is null or invalid!");
                                continue;
                            }

                            core.getServer().addRecipe(shapedRecipe);
                            recipesKeys.add(namespacedKey);

                        }
                    }
                }
            }
        }

        AdventureUtils.sendMessagePluginConsole(core, "<aqua> Recipes loaded: <yellow>" + recipesKeys.size());

    }

    private boolean addIngredientsShapedRecipe(ShapedRecipe shapedRecipe, HashMap<Character, ItemStack> ingredients){

        for(String keyShape : shapedRecipe.getShape()){
            for(int i = 0; i < keyShape.length(); i++){
                char c = keyShape.charAt(i);
                if(c == 'x' || c == 'X') continue;
                if(!ingredients.containsKey(c)){
                    return false;
                }
                shapedRecipe.setIngredient(c, new RecipeChoice.ExactChoice(ingredients.get(c)));
            }
        }

        return true;
    }

    private boolean isValidShape(List<String> shape){

        if(shape == null || shape.size() != 3) return false;

        for(String keyShape : shape){
            if(keyShape.length() != 3) return false;
        }

        return true;
    }

    private String getItem(String src){

            if(src.contains(";")){

                String[] split = src.split(";");

                if(split.length == 2){

                    return split[0];

                }

            }

            return null;
    }

    private int getAmount(String src){

        if(src.contains(";")){

            String[] split = src.split(";");

            if(split.length == 2){

                try {
                    return Integer.parseInt(split[1]);
                }
                catch (Exception e){
                    return 1;
                }

            }

        }

        return 1;
    }



}
