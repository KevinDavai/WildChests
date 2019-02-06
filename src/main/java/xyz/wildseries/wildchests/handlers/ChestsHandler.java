package xyz.wildseries.wildchests.handlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import xyz.wildseries.wildchests.api.handlers.ChestsManager;
import xyz.wildseries.wildchests.api.objects.chests.Chest;
import xyz.wildseries.wildchests.api.objects.chests.StorageChest;
import xyz.wildseries.wildchests.api.objects.data.ChestData;
import xyz.wildseries.wildchests.api.objects.chests.LinkedChest;
import xyz.wildseries.wildchests.api.objects.chests.RegularChest;
import xyz.wildseries.wildchests.objects.WLocation;
import xyz.wildseries.wildchests.objects.chests.WLinkedChest;
import xyz.wildseries.wildchests.objects.chests.WRegularChest;
import xyz.wildseries.wildchests.objects.chests.WStorageChest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public final class ChestsHandler implements ChestsManager {

    private final Set<ChestData> chestsData = new HashSet<>();
    private final Map<WLocation, Chest> chests = new HashMap<>();

    @Override
    public Chest getChest(Location location) {
        return getChest(WLocation.of(location), RegularChest.class);
    }

    @Override
    public LinkedChest getLinkedChest(Location location) {
        return getChest(WLocation.of(location), LinkedChest.class);
    }

    @Override
    public StorageChest getStorageChest(Location location) {
        return getChest(WLocation.of(location), StorageChest.class);
    }

    @Override
    public Chest addChest(UUID placer, Location location, ChestData chestData){
        Chest chest = null;

        if(!isChest(location)){
            switch (chestData.getChestType()){
                case CHEST:
                    chest = new WRegularChest(placer, WLocation.of(location), chestData);
                    break;
                case LINKED_CHEST:
                    chest = new WLinkedChest(placer, WLocation.of(location), chestData);
                    break;
                case STORAGE_UNIT:
                    chest = new WStorageChest(placer, WLocation.of(location), chestData);
                    break;
            }
        }

        if(chest != null){
            chests.put(WLocation.of(location), chest);
        }

        return chest;
    }

    private boolean isChest(Location location) {
        if(location.getBlock().getType() != Material.CHEST)
            chests.remove(WLocation.of(location));

        return chests.containsKey(WLocation.of(location));
    }

    @Override
    public void removeChest(Chest chest) {
        chests.remove(WLocation.of(chest.getLocation()));
    }

    @Override
    public List<LinkedChest> getAllLinkedChests(LinkedChest linkedChest) {
        List<LinkedChest> linkedChests = new ArrayList<>();

        LinkedChest originalLinkedChest = linkedChest.isLinkedIntoChest() ? linkedChest.getLinkedChest() : linkedChest;

        for (Chest chest : chests.values()) {
            if (chest instanceof LinkedChest) {
                LinkedChest targetChest = (LinkedChest) chest;
                if (targetChest.equals(originalLinkedChest) ||
                        (targetChest.isLinkedIntoChest() && targetChest.getLinkedChest().equals(originalLinkedChest)))
                    linkedChests.add(targetChest);
            }
        }

        return linkedChests;
    }

    @Override
    public ChestData getChestData(String name) {
        for(ChestData chestData : chestsData){
            if(chestData.getName().equalsIgnoreCase(name))
                return chestData;
        }
        return null;
    }

    @Override
    public ChestData getChestData(ItemStack itemStack) {
        for(ChestData chestData : chestsData){
            if(chestData.getItemStack().isSimilar(itemStack))
                return chestData;
        }
        return null;
    }

    @Override
    public List<Chest> getChests() {
        return new ArrayList<>(chests.values());
    }

    @Override
    public List<ChestData> getAllChestData() {
        return new ArrayList<>(chestsData);
    }

    private <T extends Chest> T getChest(WLocation location, Class<T> chestClass){
        try {
            return isChest(location.getLocation()) ? chestClass.cast(chests.get(location)) : null;
        }catch(ClassCastException ex){
            return null;
        }
    }

}
