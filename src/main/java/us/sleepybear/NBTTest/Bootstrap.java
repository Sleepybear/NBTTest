package us.sleepybear.NBTTest;

import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtType;
import com.nukkitx.nbt.NbtUtils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Bootstrap {


    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Syntax: java -jar <NBTTestJarFile> <path/to/palette1> <path/to/palette2>");
            System.exit(1);
        }
        Path palette1 = Paths.get(args[0]);
        Path palette2 = Paths.get(args[1]);
        if (Files.notExists(palette1)) {
            System.out.println("Unable to locate file: " + args[0]);
            System.exit(1);
        }
        if (Files.notExists(palette2)) {
            System.out.println("Unable to locate file: " + args[1]);
            System.exit(1);
        }

        Set<NbtMap> ourPalette = null;
        Set<NbtMap> newPalette = null;
        System.out.println("Loading block Palettes...");

        System.out.println("Loading Palette 1:");
        try (FileInputStream stream = new FileInputStream(palette1.toFile()); NBTInputStream nbtStream = NbtUtils.createGZIPReader(stream)) {
            NbtMap tag = (NbtMap) nbtStream.readTag();
            ourPalette = new HashSet<>(tag.getList("blocks", NbtType.COMPOUND));
        } catch (Exception e) {
            System.out.println("Error reading first palette: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Loaded " + ourPalette.size() + " block states.");

        System.out.println("Loading Palette 2:");
        try (FileInputStream stream = new FileInputStream(palette2.toFile()); NBTInputStream nbtStream = NbtUtils.createGZIPReader(stream)) {
            NbtMap tag = (NbtMap) nbtStream.readTag();
            newPalette = new HashSet<>(tag.getList("blocks", NbtType.COMPOUND));
        } catch (Exception e) {
            System.out.println("Error reading second block palette: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Loaded " + newPalette.size() + " block states.");

        System.out.println("Creating Block Matcher");

        NBTMatcher matcher = new NBTMatcher(newPalette);

        Set<NbtMap> unmatchedOld = new HashSet<>();
        Set<NbtMap> unmatchedNew = new HashSet<>();
        System.out.println("Matching first to second");
        for (NbtMap tag : ourPalette) {
            if (!matcher.matchTag(tag)) {
                unmatchedOld.add(tag);
            }
        }

        System.out.println("Found " + unmatchedOld.size() + " unmatched block states.");

        System.out.println("Matching second to first");
        matcher = new NBTMatcher(ourPalette);
        for (NbtMap tag : newPalette) {
            if (!matcher.matchTag(tag)) {
                unmatchedNew.add(tag);
            }
        }
        System.out.println("Found " + unmatchedNew.size() + " unmatched block states.");


        try (FileWriter report = new FileWriter("report.txt")) {
            report.write("Unmatched states: First: " + unmatchedOld.size() + " Second: " + unmatchedNew.size() + "\n\rUnmatched First States:\n\r\n\r");
            unmatchedOld.stream().sorted(Comparator.comparing(nbt -> nbt.getString("name"))).forEach(tag -> {
                try {
                    report.write("Block Name: " + tag.getString("name") + "\n\r");
                    report.write("Block States: " + tag.getCompound("states").toString() + "\n\r\n\r");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            report.write("==== End of Unmatched First Palette States ===\n\r\n\rUnmatched Second Palette States:\n\r\n\r");
            unmatchedNew.stream().sorted(Comparator.comparing(nbt -> nbt.getString("name"))).forEach(tag -> {
                try {
                    report.write("Block Name: " + tag.getString("name") + "\n\r");
                    report.write("Block States: " + tag.getCompound("states").toString() + "\n\r\n\r");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            report.write("=== End of Second Palette States");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Created report.txt");

    }

}
