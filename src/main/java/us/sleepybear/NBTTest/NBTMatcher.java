package us.sleepybear.NBTTest;

import com.nukkitx.nbt.NbtMap;

import java.util.Set;

public class NBTMatcher {
    private Set<NbtMap> PALETTE;

    public NBTMatcher(Set<NbtMap> source) {
        this.PALETTE = source;
    }

    public boolean matchTag(NbtMap tag) {
        String blockName;
        if (tag.containsKey("name"))
            blockName = tag.getString("name");
        else
            blockName = tag.getCompound("block").getString("name");
        for (NbtMap block : PALETTE) {
            String matchName;
            NbtMap states;
            if (block.containsKey("name")) {
                matchName = block.getString("name");
                states = block.getCompound("states");
            } else {
                matchName = block.getCompound("block").getString("name");
                states = block.getCompound("block").getCompound("states");
            }

            if (matchName.equalsIgnoreCase(blockName)) {
                if (tag.getCompound("states").equals(states)) {
                    return true;
                }
            }
        }
        return false;
    }
}
