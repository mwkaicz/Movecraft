/*
 * This file is part of Movecraft.
 *
 *     Movecraft is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Movecraft is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Movecraft.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.countercraft.movecraft.craft;

import net.countercraft.movecraft.config.Settings;
import net.countercraft.movecraft.utils.BlockContainer;
import net.countercraft.movecraft.utils.BlockLimitManager;
import net.countercraft.movecraft.utils.LegacyUtils;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

final public class CraftType {
    private final boolean blockedByWater;
    private final boolean requireWaterContact;
    private final boolean tryNudge;
    private final boolean canBeNamed;
    private final boolean canCruise;
    private final boolean canTeleport;
    private final boolean canStaticMove;
    private final boolean canHover;
    private final boolean canDirectControl;
    private final boolean useGravity;
    private final boolean canHoverOverWater;
    private final boolean moveEntities;
    private final boolean onlyMovePlayers;
    private final boolean allowHorizontalMovement;
    private final boolean allowVerticalMovement;
    private final boolean allowRemoteSign;
    private final boolean allowCannonDirectorSign;
    private final boolean allowAADirectorSign;
    private final boolean cruiseOnPilot;
    private final boolean allowVerticalTakeoffAndLanding;
    private final boolean rotateAtMidpoint;
    private final boolean halfSpeedUnderwater;
    private final boolean focusedExplosion;
    private final boolean mustBeSubcraft;
    private final boolean keepMovingOnSink;
    private final int maxSize;
    private final int minSize;
    private final int minHeightLimit;
    private final int maxHeightLimit;
    private final int maxHeightAboveGround;
    private final int cruiseOnPilotVertMove;
    private final int maxStaticMove;
    private final int cruiseSkipBlocks;
    private final int vertCruiseSkipBlocks;
    private final int cruiseTickCooldown;
    private final int staticWaterLevel;
    private final int sinkRateTicks;
    private final int smokeOnSink;
    private final int tickCooldown;
    private final int hoverLimit;
    private final int maxTravelDistance;
    private final Set<Material> dynamicFlyBlocks;
    private final double fuelBurnRate;
    private final double sinkPercent;
    private final double overallSinkPercent;
    private final double detectionMultiplier;
    private final double underwaterDetectionMultiplier;
    private final double dynamicLagSpeedFactor;
    private final double dynamicFlyBlockSpeedFactor;
    private final double chestPenalty;
    private final float explodeOnCrash;
    private final float collisionExplosion;
    @NotNull private final String craftName;
    @NotNull private final BlockContainer allowedBlocks;
    @NotNull private final BlockContainer forbiddenBlocks;
    @NotNull private final String[] forbiddenSignStrings;
    @NotNull private final BlockLimitManager flyBlocks;
    @NotNull private final BlockLimitManager moveBlocks;
    @NotNull private final List<Material> harvestBlocks;
    @NotNull private final List<Material> harvesterBladeBlocks;
    @NotNull private final Set<Material> passthroughBlocks;
    @NotNull private final int effectRange;
    @NotNull private final Map<PotionEffect,Integer> potionEffectsToApply;
    @NotNull private final Map<PotionEffect, Integer> potionEffectDelays = Collections.emptyMap();
    @NotNull private final Set<Material> forbiddenHoverOverBlocks;
    private final int gravityDropDistance;
    private final int gravityInclineDistance;
    private final int keepMovingOnSinkMaxMove;

    @SuppressWarnings("unchecked")
    public CraftType(File f) throws CraftTypeException{
        final Map data;
        try {
            InputStream input = new FileInputStream(f);
            Yaml yaml = new Yaml();
            data = (Map) yaml.load(input);
            input.close();
        } catch (IOException e) {
            throw new TypeNotFoundException("No file found at path " + f.getAbsolutePath());
        }


        try {
        //Required craft flags
        craftName = (String) data.get("name");
        maxSize = integerFromObject(data.get("maxSize"));
        minSize = integerFromObject(data.get("minSize"));
        allowedBlocks = new BlockContainer(data.get("allowedBlocks"));

        forbiddenBlocks = data.containsKey("forbiddenBlocks") ? new BlockContainer(data.get("forbiddenBlocks")) : new BlockContainer();;
        forbiddenSignStrings = stringListFromObject(data.get("forbiddenSignStrings"));
        tickCooldown = (int) Math.ceil(20 / (doubleFromObject(data.get("speed"))));
        flyBlocks = new BlockLimitManager(data.get("flyblocks"));

        //Optional craft flags
        blockedByWater = (boolean) (data.containsKey("canFly") ? data.get("canFly") : data.getOrDefault("blockedByWater", true));
        requireWaterContact = (boolean) data.getOrDefault("requireWaterContact", false);
        tryNudge = (boolean) data.getOrDefault("tryNudge", false);
        cruiseTickCooldown = (int) Math.ceil(20 / (doubleFromObject(data.getOrDefault("cruiseSpeed", tickCooldown))));
        moveBlocks = new BlockLimitManager(data.getOrDefault("moveblocks", new HashMap<>()));
        canCruise = (boolean) data.getOrDefault("canCruise", false);
        canTeleport = (boolean) data.getOrDefault("canTeleport", false);
        canBeNamed = (boolean) data.getOrDefault("canBeNamed", true);
        cruiseOnPilot = (boolean) data.getOrDefault("cruiseOnPilot", false);
        cruiseOnPilotVertMove = integerFromObject(data.getOrDefault("cruiseOnPilotVertMove", 0));
        allowVerticalMovement = (boolean) data.getOrDefault("allowVerticalMovement", true);
        rotateAtMidpoint = (boolean) data.getOrDefault("rotateAtMidpoint", false);
        allowHorizontalMovement = (boolean) data.getOrDefault("allowHorizontalMovement", true);
        allowRemoteSign = (boolean) data.getOrDefault("allowRemoteSign", true);
        allowCannonDirectorSign = (boolean) data.getOrDefault("allowCannonDirectorSign", true);
        allowAADirectorSign = (boolean) data.getOrDefault("allowAADirectorSign", true);
        canStaticMove = (boolean) data.getOrDefault("canStaticMove", false);
        maxStaticMove = integerFromObject(data.getOrDefault("maxStaticMove", 10000));
        cruiseSkipBlocks = integerFromObject(data.getOrDefault("cruiseSkipBlocks", 0));
        vertCruiseSkipBlocks = integerFromObject(data.getOrDefault("vertCruiseSkipBlocks", cruiseSkipBlocks));
        halfSpeedUnderwater = (boolean) data.getOrDefault("halfSpeedUnderwater", false);
        focusedExplosion = (boolean) data.getOrDefault("focusedExplosion", false);
        mustBeSubcraft = (boolean) data.getOrDefault("mustBeSubcraft", false);
        staticWaterLevel = integerFromObject(data.getOrDefault("staticWaterLevel", 0));
        fuelBurnRate = doubleFromObject(data.getOrDefault("fuelBurnRate", 0d));
        sinkPercent = doubleFromObject(data.getOrDefault("sinkPercent", 0d));
        overallSinkPercent = doubleFromObject(data.getOrDefault("overallSinkPercent", 0d));
        detectionMultiplier = doubleFromObject(data.getOrDefault("detectionMultiplier", 0d));
        underwaterDetectionMultiplier = doubleFromObject(data.getOrDefault("underwaterDetectionMultiplier", detectionMultiplier));
        sinkRateTicks = data.containsKey("sinkSpeed") ? (int) Math.ceil(20 / (doubleFromObject(data.get("sinkSpeed")))) : integerFromObject(data.getOrDefault("sinkTickRate", 0));
        keepMovingOnSink = (Boolean) data.getOrDefault("keepMovingOnSink", false);
        keepMovingOnSinkMaxMove = integerFromObject(data.getOrDefault("keepMovingOnSinkMaxMove", -1));
        smokeOnSink = integerFromObject(data.getOrDefault("smokeOnSink", 0));
        explodeOnCrash = floatFromObject(data.getOrDefault("explodeOnCrash", 0F));
        collisionExplosion = floatFromObject(data.getOrDefault("collisionExplosion", 0F));
        minHeightLimit = Math.max(0, integerFromObject(data.getOrDefault("minHeightLimit", 0)));
        int value = Math.min(integerFromObject(data.getOrDefault("maxHeightLimit", 254)), 255);
        if (value <= minHeightLimit) {
            value = 255;
        }

        maxHeightLimit = value;
        maxHeightAboveGround = integerFromObject(data.getOrDefault("maxHeightAboveGround", -1));
        canDirectControl = (boolean) data.getOrDefault("canDirectControl", true);
        canHover = (boolean) data.getOrDefault("canHover", false);
        canHoverOverWater = (boolean) data.getOrDefault("canHoverOverWater", true);
        moveEntities = (boolean) data.getOrDefault("moveEntities", true);
        onlyMovePlayers = (boolean) data.getOrDefault("onlyMovePlayers", true);
        useGravity = (boolean) data.getOrDefault("useGravity", false);
        hoverLimit = Math.max(0, integerFromObject(data.getOrDefault("hoverLimit", 0)));
        harvestBlocks = new ArrayList<>();
        harvesterBladeBlocks = new ArrayList<>();
        if (data.containsKey("harvestBlocks")) {
            ArrayList objList = (ArrayList) data.get("harvestBlocks");
            for (Object i : objList) {
                if (i instanceof String) {
                    String str = (String) i;
                    if (str.startsWith("ALL_")) {
                        str = str.replace("ALL_", "");
                        for (Material type : Material.values()) {

                        }
                    }
                    Material mat = Material.getMaterial((String) i);
                    harvestBlocks.add(mat);
                } else {
                    Material mat = Material.getMaterial((Integer) i);
                    harvestBlocks.add(mat);
                }

            }
        }
        if (data.containsKey("harvesterBladeBlocks")) {
                ArrayList objList = (ArrayList) data.get("harvesterBladeBlocks");
                for (Object i : objList) {
                    if (i instanceof String) {
                        String str = (String) i;
                        str = str.toUpperCase();
                        if (str.startsWith("ALL_")){
                            for (Material type : Material.values()){
                                if (!type.name().endsWith(str.replace("ALL_", "_"))){
                                    continue;
                                }
                                harvesterBladeBlocks.add(type);
                            }
                        } else {
                            Material mat = Material.getMaterial(str);
                            harvesterBladeBlocks.add(mat);
                        }

                    } else {
                        Material mat = Material.getMaterial((Integer) i);
                        harvesterBladeBlocks.add(mat);
                    }
                }
        }
            passthroughBlocks = new HashSet<>();
            if (data.containsKey("passthroughBlocks")) {
                ArrayList objList = (ArrayList) data.get("passthroughBlocks");
                for (Object i : objList) {
                    if (i instanceof String) {
                        Material mat = Material.getMaterial(((String) i).toUpperCase());
                        passthroughBlocks.add(mat);
                    } else {
                        Material mat = Material.getMaterial((Integer) i);
                        passthroughBlocks.add(mat);
                    }
                }
            }
            if (!blockedByWater) {
                passthroughBlocks.add(Material.WATER);
                if (Settings.IsLegacy) {
                    passthroughBlocks.add(LegacyUtils.STATIONARY_WATER);
                } else {
                    passthroughBlocks.add(Material.getMaterial("BUBBLE_COLUMN"));
                }

            }


            forbiddenHoverOverBlocks = new HashSet<>();
            if (data.containsKey("forbiddenHoverOverBlocks")){
                final ArrayList objList = (ArrayList) data.get("forbiddenHoverOverBlocks");
                for (Object i : objList){
                    if (i instanceof Integer){
                        forbiddenHoverOverBlocks.add(Material.getMaterial((int) i));
                    } else if (i instanceof String){
                        forbiddenHoverOverBlocks.add(Material.getMaterial(((String) i).toUpperCase()));
                    }
                }
            }
            if (!canHoverOverWater){
                forbiddenHoverOverBlocks.add(Material.WATER);
                if (Settings.IsLegacy){
                    forbiddenHoverOverBlocks.add(Material.STATIONARY_WATER);
                } else {
                    forbiddenHoverOverBlocks.add(Material.getMaterial("BUBBLE_COLUMN"));
                }

            }
            dynamicFlyBlocks = new HashSet<>();
            if (data.containsKey("dynamicFlyBlocks")) {
                ArrayList entries = (ArrayList) data.get("dynamicFlyBlocks");
                for (Object o : entries) {
                    if (o instanceof String) {
                        String str = (String) o;
                        str = str.toUpperCase();
                        if (str.startsWith("ALL_")){
                            str = str.replace("ALL_", "_");
                            for (Material type : Material.values()){
                                if (!type.name().endsWith(str)){
                                    continue;
                                }
                                dynamicFlyBlocks.add(type);
                            }
                            continue;
                        }
                        dynamicFlyBlocks.add(Material.getMaterial(str));
                    } else {
                        dynamicFlyBlocks.add(Material.getMaterial((Integer) o));
                    }
                }
            }
            maxTravelDistance = data.containsKey("maxTravelDistance") ? (int) data.get("maxTravelDistance") : 500;
            effectRange = data.containsKey("effectRange") ? integerFromObject(data.get("effectRange")) : 0;
        if (!canHoverOverWater){
            forbiddenHoverOverBlocks.add(Material.WATER);
            if (Settings.IsLegacy) {
                forbiddenHoverOverBlocks.add(LegacyUtils.STATIONARY_WATER);
            } else {
                forbiddenHoverOverBlocks.add(Material.getMaterial("BUBBLE_COLUMN"));
            }

        }
            potionEffectsToApply = data.containsKey("potionEffectsToApply") ? effectListFromObject(data.get("potionEffectsToApply")) : Collections.emptyMap();
            allowVerticalTakeoffAndLanding = (boolean) data.getOrDefault("allowVerticalTakeoffAndLanding", true);
            dynamicLagSpeedFactor = doubleFromObject(data.getOrDefault("dynamicLagSpeedFactor", 0d));
            dynamicFlyBlockSpeedFactor = doubleFromObject(data.getOrDefault("dynamicFlyBlockSpeedFactor", 0d));
            chestPenalty = doubleFromObject(data.getOrDefault("chestPenalty", 0));
            gravityInclineDistance = integerFromObject(data.getOrDefault("gravityInclineDistance", -1));
            int dropdist = integerFromObject(data.getOrDefault("gravityDropDistance", -8));
            gravityDropDistance = dropdist > 0 ? -dropdist : dropdist;
        } catch (Exception e){
            throw new CraftTypeException("Craft file " + f.getName() + " is malformed", e);
        }

    }

    private int integerFromObject(Object obj) {
        if (obj instanceof Double) {
            return ((Double) obj).intValue();
        }
        return (Integer) obj;
    }

    private float floatFromObject(Object obj) {
        if (obj instanceof Double) {
            return ((Double) obj).floatValue();
        } else if (obj instanceof Integer) {
            return ((Integer) obj).floatValue();
        }
        return (float) obj;
    }

    private double doubleFromObject(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        }
        return (double) obj;
    }
    private Material materialFromObject(Object obj){
        if (obj instanceof Integer){
            return Material.getMaterial((Integer) obj);
        } else {
            return (Material) obj;
        }

    }
    private HashMap<PotionEffect, Integer> effectListFromObject(Object obj){
        Map<Object,Object> objMap = (Map<Object, Object>) obj;
        HashMap<PotionEffect,Integer> ret = new HashMap<>();
        for (Object o : objMap.keySet()){
            PotionEffectType effect = null;
            int duration = 0;
            int amplifier = 1;
            int delay = 0;
            if (o instanceof String){
                String string = (String) o;
                effect = PotionEffectType.getByName(string);
            }
            Map<Object, Object> subObjMap = (Map<Object, Object>) objMap.get(o);
            for (Object i : subObjMap.keySet()){
                String str = (String) i;
                int integer = (int) subObjMap.get(i);
                if (str.equals("duration")){
                    duration = 20 * integer;
                }
                if (str.equals("amplifier")){
                    amplifier = integer;
                }
                if (str.equals("delay")){
                    delay = integer;
                }
            }
            if (effect == null){
                continue;
            }
            PotionEffect potEffect = new PotionEffect(effect,duration,amplifier,true,true);
            ret.put(potEffect,delay);
        }
        return ret;
    }

    private int[] blockIDListFromObject(Object obj) {
        ArrayList<Integer> returnList = new ArrayList<>();
        ArrayList objList = (ArrayList) obj;
        for (Object i : objList) {
            if (i instanceof String) {
                String str = (String) i;
                if (str.contains(":")) {
                    String[] parts = str.split(":");
                    Integer typeID = Integer.valueOf(parts[0]);
                    Integer metaData = Integer.valueOf(parts[1]);
                    returnList.add(10000 + (typeID << 4) + metaData);  // id greater than 10000 indicates it has a meta data / damage value
                } else {
                    Integer typeID = Integer.valueOf(str);
                    returnList.add(typeID);
                }
            } else {
                Integer typeID = (Integer) i;
                returnList.add(typeID);
            }
        }
        int[] output = new int[returnList.size()];
        for (int i = 0; i < output.length; i++)
            output[i] = returnList.get(i);
        return output;
    }
    private Map<Material, List<Integer>> materialMapFromObject(Object obj){
        HashMap<Material, List<Integer>> returnMap = new HashMap<>();
        if (obj instanceof ArrayList<?>){
            for (Object o : (ArrayList<Object>) obj){
                if (o instanceof Integer){
                    Integer id = (Integer) o;
                    Material type;
                    try {
                         type = Material.getMaterial(id);
                    } catch (Throwable t){
                        throw new CraftTypeException("Numerical block IDs are not supported by your current Minecraft version", t);
                    }
                    returnMap.put(type, Collections.emptyList());
                } else if (o instanceof String){
                    String string = (String) o;
                    string = string.toUpperCase();
                    if (string.contains(":")){
                        String[] parts = string.split(":");
                        Material type;
                        Integer data = Integer.valueOf(parts[1]);
                        if (parts[0].contains("0")||parts[0].contains("1")||parts[0].contains("2")||parts[0].contains("3")||parts[0].contains("4")||parts[0].contains("5")||parts[0].contains("6")||parts[0].contains("7")||parts[0].contains("8")||parts[0].contains("9")){
                            Integer typeID = Integer.valueOf(parts[0]);
                            type = Material.getMaterial(typeID);

                        } else {
                            type = Material.getMaterial(parts[0]);
                        }
                        if (returnMap.containsKey(type)){
                            returnMap.get(type).add(data);
                        } else {
                            returnMap.put(type, new ArrayList<>(data));
                        }
                    } else {
                        if (string.toUpperCase().startsWith("ALL_")){
                            string = string.replace("ALL_", "");
                            for (Material m : Material.values()){
                                if (!m.name().endsWith(string)){
                                    continue;
                                }
                                returnMap.put(m, Collections.emptyList());
                            }
                        } else {
                            Material type = Material.getMaterial(string);
                            returnMap.put(type, Collections.emptyList());
                        }
                    }
                } else {
                    Material type = (Material) o;
                    returnMap.put(type, Collections.emptyList());
                }
            }
        } else {
            HashMap<Object, Object> objMap = (HashMap<Object, Object>) obj;

            for (Object o : objMap.keySet()) {
                Material type;
                if (o instanceof Integer) {
                    Integer id = (Integer) o;
                    type = Material.getMaterial(id);
                } else if (o instanceof String) {
                    String str = (String) o;
                    type = Material.getMaterial(str);
                } else {
                    type = (Material) o;
                }
                ArrayList<Object> objList = (ArrayList<Object>) objMap.get(o);
                List<Integer> dataList = Collections.emptyList();
                if (objList != null) {
                    for (Object dataObj : objList) {
                        if (dataObj instanceof String) {
                            String s = (String) dataObj;
                            Integer data = Integer.valueOf(s);
                            dataList.add(data);
                        } else {
                            Integer data = (Integer) dataObj;
                            dataList.add(data);
                        }
                    }
                }
                returnMap.put(type, dataList);
            }
        }
        return returnMap;
    }

    private String[] stringListFromObject(Object obj) {
        ArrayList<String> returnList = new ArrayList<>();
        if (obj == null) {
            return returnList.toArray(new String[1]);
        }
        ArrayList objList = (ArrayList) obj;
        for (Object i : objList) {
            if (i instanceof String) {
                String str = (String) i;
                returnList.add(str);
            }
        }
        return returnList.toArray(new String[1]);
    }
    private Map<Map<Material, List<Integer>>, List<Double>> materialMapListFromObject(Object obj){
        HashMap<Map<Material, List<Integer>>, List<Double>> returnMap = new HashMap<>();
        HashMap<Object, Object> objMap = (HashMap<Object, Object>) obj;
        for (Object i : objMap.keySet()) {
            HashMap<Material, List<Integer>> materialMap = new HashMap<>();
            if (i instanceof ArrayList<?>) {
                for (Object o : (ArrayList<Object>) i) {
                    if (o instanceof Integer) {
                        Integer typeID = (Integer) o;
                        Material type = Material.getMaterial(typeID);
                        materialMap.put(type, new ArrayList<>());
                    } else if (o instanceof String) {
                        String string = (String) o;
                        string = string.toUpperCase();
                        if (string.contains(":")) {
                            String[] parts = string.split(":");
                            Material type;
                            Integer data = Integer.valueOf(parts[1]);
                            if (parts[0].contains("0") || parts[0].contains("1") || parts[0].contains("2") || parts[0].contains("3") || parts[0].contains("4") || parts[0].contains("5") || parts[0].contains("6") || parts[0].contains("7") || parts[0].contains("8") || parts[0].contains("9")) {
                                Integer typeID = Integer.valueOf(parts[0]);
                                type = Material.getMaterial(typeID);

                            } else {
                                type = Material.getMaterial(parts[0]);
                            }
                            if (materialMap.containsKey(type)) {
                                List<Integer> dataList = materialMap.get(type);
                                dataList.add(data);
                                materialMap.put(type, dataList);
                            } else {
                                List<Integer> dataList = new ArrayList<>();
                                dataList.add(data);
                                materialMap.put(type, dataList);
                            }

                        } else if (string.toUpperCase().startsWith("ALL_")){
                            string = string.replace("ALL_", "");
                            for (Material m : Material.values()){
                                if (!m.name().endsWith(string)){
                                    continue;
                                }
                                materialMap.put(m, new ArrayList<>());
                            }
                        } else {
                            Material type;
                            if (string.contains("0") || string.contains("1") || string.contains("2") || string.contains("3") || string.contains("4") || string.contains("5") || string.contains("6") || string.contains("7") || string.contains("8") || string.contains("9")) {
                                Integer typeID = Integer.valueOf(string);
                                type = Material.getMaterial(typeID);

                            } else {
                                type = Material.getMaterial(string);
                            }
                            materialMap.put(type, new ArrayList<>());
                        }
                    } else {
                        Material type = (Material) o;
                        materialMap.put(type, new ArrayList<>());
                    }
                }
            } else if (i instanceof Map) {
                HashMap<Object, Object> objMapKey = (HashMap<Object, Object>) i;
                for (Object o : objMapKey.keySet()) {
                    if (o instanceof Integer) {
                        Integer typeID = (Integer) o;
                        Material type = Material.getMaterial(typeID);
                        List<Integer> dataList = (List<Integer>) objMapKey.get(o);
                        materialMap.put(type, dataList);
                    } else {
                        Material type = (Material) o;
                        List<Integer> dataList = (List<Integer>) objMapKey.get(o);
                        materialMap.put(type, dataList);
                    }
                }
            } else if (i instanceof Integer) {
                if (!Settings.IsLegacy){
                    throw new IllegalArgumentException("Numerical IDs are not supported by the current server version!");
                }
                Integer typeID = (Integer) i;
                Material type = Material.getMaterial(typeID);
                materialMap.put(type, new ArrayList<>());
            } else if (i instanceof String) {
                String string = (String) i;
                string = string.toUpperCase();
                if (string.contains(":")) {
                    String[] parts = string.split(":");
                    Material type;
                    Integer data = Integer.valueOf(parts[1]);
                    if (parts[0].contains("0") || parts[0].contains("1") || parts[0].contains("2") || parts[0].contains("3") || parts[0].contains("4") || parts[0].contains("5") || parts[0].contains("6") || parts[0].contains("7") || parts[0].contains("8") || parts[0].contains("9")) {
                        Integer typeID = Integer.valueOf(parts[0]);
                        type = Material.getMaterial(typeID);

                    } else {
                        type = Material.getMaterial(parts[0]);
                    }
                    if (materialMap.containsKey(type)) {
                        List<Integer> dataList = materialMap.get(type);
                        dataList.add(data);
                        materialMap.put(type, dataList);
                    } else {
                        List<Integer> dataList = new ArrayList<>();
                        dataList.add(data);
                        materialMap.put(type, dataList);
                    }
                } else {
                    Material type;
                    boolean all = false;
                    if (string.toUpperCase().startsWith("ALL_")) {
                        string = string.replace("ALL_", "");
                        for (Material m : Material.values()){
                            if (m.name().endsWith(string)){
                                materialMap.put(m, new ArrayList<>());
                            }
                            all = true;
                        }
                    }
                    if (string.contains("0") || string.contains("1") || string.contains("2") || string.contains("3") || string.contains("4") || string.contains("5") || string.contains("6") || string.contains("7") || string.contains("8") || string.contains("9")) {
                        Integer typeID = Integer.valueOf(string);
                        type = Material.getMaterial(typeID);

                    }  else {
                        type = Material.getMaterial(string);
                    }
                    if (!all) {
                        if (materialMap.containsKey(type)) {
                            List<Integer> dataList = materialMap.get(type);
                            materialMap.put(type, dataList);
                        } else {
                            List<Integer> dataList = new ArrayList<>();
                            materialMap.put(type, dataList);
                        }
                    }
                }
            } else {
                Material type = (Material) i;
                materialMap.put(type, new ArrayList<>());
            }

            // then read in the limitation values, low and high
            ArrayList<Object> objList = (ArrayList<Object>) objMap.get(i);
            ArrayList<Double> limitList = new ArrayList<>();
            for (Object limitObj : objList) {
                if (limitObj instanceof String) {
                    String str = (String) limitObj;
                    if (str.contains("N")) { // a # indicates a specific quantity, IE: #2 for exactly 2 of the block
                        String[] parts = str.split("N");
                        Double val = Double.valueOf(parts[1]);
                        limitList.add(10000d + val);  // limit greater than 10000 indicates an specific quantity (not a ratio)
                    } else {
                        Double val = Double.valueOf(str);
                        limitList.add(val);
                    }
                } else if (limitObj instanceof Integer) {
                    Double ret = ((Integer) limitObj).doubleValue();
                    limitList.add(ret);
                } else {
                    limitList.add((Double) limitObj);
                }
            }
            returnMap.put(materialMap, limitList);
        }
        return returnMap;
    }
    private Map<List<Integer>, List<Double>> blockIDMapListFromObject(Object obj) {
        HashMap<List<Integer>, List<Double>> returnMap = new HashMap<>();
        HashMap<Object, Object> objMap = (HashMap<Object, Object>) obj;
        for (Object i : objMap.keySet()) {
            ArrayList<Integer> rowList = new ArrayList<>();

            // first read in the list of the blocks that type of flyblock. It could be a single string (with or without a ":") or integer, or it could be multiple of them
            if (i instanceof ArrayList<?>) {
                for (Object o : (ArrayList<Object>) i) {
                    if (o instanceof String) {
                        String str = (String) o;
                        if (str.contains(":")) {
                            String[] parts = str.split(":");
                            Integer typeID = Integer.valueOf(parts[0]);
                            Integer metaData = Integer.valueOf(parts[1]);
                            rowList.add(10000 + (typeID << 4) + metaData);  // id greater than 10000 indicates it has a meta data / damage value
                        } else {
                            Integer typeID = Integer.valueOf(str);
                            rowList.add(typeID);
                        }
                    } else {
                        Integer typeID = (Integer) o;
                        rowList.add(typeID);
                    }
                }
            } else if (i instanceof String) {
                String str = (String) i;
                if (str.contains(":")) {
                    String[] parts = str.split(":");
                    Integer typeID = Integer.valueOf(parts[0]);
                    Integer metaData = Integer.valueOf(parts[1]);
                    rowList.add(10000 + (typeID << 4) + metaData);  // id greater than 10000 indicates it has a meta data / damage value
                } else {
                    Integer typeID = Integer.valueOf(str);
                    rowList.add(typeID);
                }
            } else {
                Integer typeID = (Integer) i;
                rowList.add(typeID);
            }

            // then read in the limitation values, low and high
            ArrayList<Object> objList = (ArrayList<Object>) objMap.get(i);
            ArrayList<Double> limitList = new ArrayList<>();
            for (Object limitObj : objList) {
                    if (limitObj instanceof String) {
                        String str = (String) limitObj;
                        if (str.contains("N")) { // a # indicates a specific quantity, IE: #2 for exactly 2 of the block
                            String[] parts = str.split("N");
                            Double val = Double.valueOf(parts[1]);
                            limitList.add(10000d + val);  // limit greater than 10000 indicates an specific quantity (not a ratio)
                        } else {
                            Double val = Double.valueOf(str);
                            limitList.add(val);
                        }
                    } else if (limitObj instanceof Integer) {
                        Double ret = ((Integer) limitObj).doubleValue();
                        limitList.add(ret);
                    } else
                        limitList.add((Double) limitObj);
            }
            returnMap.put(rowList, limitList);
        }
        return returnMap;
    }

    @NotNull
    public String getCraftName() {
        return craftName;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinSize() {
        return minSize;
    }

    @NotNull
    public BlockContainer getAllowedBlocks() {
        return allowedBlocks;
    }

    @NotNull
    public BlockContainer getForbiddenBlocks() {
        return forbiddenBlocks;
    }

    public String[] getForbiddenSignStrings() {
        return forbiddenSignStrings;
    }

    public boolean blockedByWater() {
        return blockedByWater;
    }

    public boolean getRequireWaterContact() {
        return requireWaterContact;
    }

    public boolean getCanCruise() {
        return canCruise;
    }

    public int getCruiseSkipBlocks() {
        return cruiseSkipBlocks;
    }

    public int getVertCruiseSkipBlocks() {
        return vertCruiseSkipBlocks;
    }

    public boolean getCanBeNamed(){
        return canBeNamed;
    }

    public int maxStaticMove() {
        return maxStaticMove;
    }

    public int getStaticWaterLevel() {
        return staticWaterLevel;
    }

    public boolean getCanTeleport() {
        return canTeleport;
    }

    public boolean getCanStaticMove() {
        return canStaticMove;
    }

    public boolean getCruiseOnPilot() {
        return cruiseOnPilot;
    }

    public int getCruiseOnPilotVertMove() {
        return cruiseOnPilotVertMove;
    }

    public boolean allowVerticalMovement() {
        return allowVerticalMovement;
    }

    public boolean rotateAtMidpoint() {
        return rotateAtMidpoint;
    }

    public boolean allowHorizontalMovement() {
        return allowHorizontalMovement;
    }

    public boolean allowRemoteSign() {
        return allowRemoteSign;
    }

    public boolean allowCannonDirectorSign() {
        return allowCannonDirectorSign;
    }

    public boolean allowAADirectorSign() {
        return allowAADirectorSign;
    }

    public double getFuelBurnRate() {
        return fuelBurnRate;
    }

    public double getSinkPercent() {
        return sinkPercent;
    }

    public double getOverallSinkPercent() {
        return overallSinkPercent;
    }

    public double getDetectionMultiplier() {
        return detectionMultiplier;
    }

    public double getUnderwaterDetectionMultiplier() {
        return underwaterDetectionMultiplier;
    }

    public int getSinkRateTicks() {
        return sinkRateTicks;
    }

    public boolean getKeepMovingOnSink() {
        return keepMovingOnSink;
    }

    public float getExplodeOnCrash() {
        return explodeOnCrash;
    }

    public int getSmokeOnSink() {
        return smokeOnSink;
    }

    public float getCollisionExplosion() {
        return collisionExplosion;
    }

    public int getTickCooldown() {
        return tickCooldown;
    }

    public int getCruiseTickCooldown() {
        return cruiseTickCooldown;
    }

    public boolean getHalfSpeedUnderwater() {
        return halfSpeedUnderwater;
    }

    public boolean getFocusedExplosion() {
        return focusedExplosion;
    }

    public boolean getMustBeSubcraft() {
        return mustBeSubcraft;
    }

    public boolean isTryNudge() {
        return tryNudge;
    }

    @NotNull
    public BlockLimitManager getFlyBlocks() {
        return flyBlocks;
    }

    @NotNull
    public BlockLimitManager getMoveBlocks() {
        return moveBlocks;
    }

    public int getMaxHeightLimit() {
        return maxHeightLimit;
    }

    public int getMinHeightLimit() {
        return minHeightLimit;
    }

    public int getMaxHeightAboveGround() {
        return maxHeightAboveGround;
    }

    public boolean getCanHover() {
        return canHover;
    }

    public boolean getCanDirectControl() {
        return canDirectControl;
    }

    public int getHoverLimit() {
        return hoverLimit;
    }

    @NotNull
    public List<Material> getHarvestBlocks() {
        return harvestBlocks;
    }

    @NotNull
    public List<Material> getHarvesterBladeBlocks() {
        return harvesterBladeBlocks;
    }

    public boolean getCanHoverOverWater() {
        return canHoverOverWater;
    }

    public boolean getMoveEntities() {
        return moveEntities;
    }

    public boolean getUseGravity() {
        return useGravity;
    }

    public boolean allowVerticalTakeoffAndLanding() {
        return allowVerticalTakeoffAndLanding;
    }

    public double getDynamicLagSpeedFactor() {
        return dynamicLagSpeedFactor;
    }

    public double getDynamicFlyBlockSpeedFactor() {
        return dynamicFlyBlockSpeedFactor;
    }

    public Set<Material> getDynamicFlyBlocks() {
        return dynamicFlyBlocks;
    }

    public double getChestPenalty() {
        return chestPenalty;
    }

    @NotNull
    public Set<Material> getPassthroughBlocks() {
        return passthroughBlocks;
    }

    public boolean getOnlyMovePlayers() {
        return onlyMovePlayers;
    }

    @NotNull
    public Map<PotionEffect, Integer> getPotionEffectsToApply() {
        return potionEffectsToApply;
    }

    public int getEffectRange() {
        return effectRange;
    }

    public int getMaxTravelDistance() {
        return maxTravelDistance;
    }

    public Set<Material> getForbiddenHoverOverBlocks() {
        return forbiddenHoverOverBlocks;
    }

    public int getGravityDropDistance() {
        return gravityDropDistance;
    }

    public int getGravityInclineDistance() {
        return gravityInclineDistance;
    }

    public int getKeepMovingOnSinkMaxMove() {
        return keepMovingOnSinkMaxMove;
    }

    private class TypeNotFoundException extends RuntimeException {
        public TypeNotFoundException(String s) {
            super(s);
        }
    }

    private class CraftTypeException extends RuntimeException{
        public CraftTypeException(String message, Throwable cause){
            super(message,cause);
        }
    }

}
