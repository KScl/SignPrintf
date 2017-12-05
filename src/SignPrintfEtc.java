
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Additional extra things needed for SignPrintf and its functions.
 * @author STJrInuyasha
 */
public final class SignPrintfEtc {
    private static PropertiesFile settings = new PropertiesFile("signprintf.properties");

    private static long updateTime = 500;
    private static boolean anyoneCanBreak = true;
    private static boolean debug = false;
    private static List<String> warpGroups;

    /**
     * For those people that wanted Updatr support.
     */
    public static void writeUpdatr()
    {
        File UpdatrDir = new File("Updatr");
        if (!UpdatrDir.exists())
            return;

        File Updatr = new File("Updatr/SignPrintf.updatr");
        if (Updatr.exists())
            return;

        try
        {
            String urlBase = "minecraft.srb2.org/files/";
            BufferedWriter fout = new BufferedWriter(new FileWriter(Updatr));
            fout.write("name = SignPrintf");
            fout.newLine();

            fout.write("version = " + SignPrintf.version);
            fout.newLine();

            fout.write("url = " + urlBase + "updatr/SignPrintf.updatr");
            fout.newLine();

            fout.write("file = " + urlBase + "plugins/SignPrintf-v" + SignPrintf.version + ".zip");
            fout.newLine();

            fout.write("notes = ");
            fout.close();
            SignPrintf.log.info("[SignPrintf] Updatr information created.");
        }
        catch (java.io.IOException ioe)
        {
            ioe.printStackTrace();
            SignPrintf.log.info("[SignPrintf] Error writing Updatr file.");
        }
    }

    /**
     * Initializes settings.
     */
    public static void doSettings()
    {
        updateTime     = settings.getLong("update-time", 500);
        anyoneCanBreak = settings.getBoolean("breakable", true);
        debug          = settings.getBoolean("debug", false);

        warpGroups = new ArrayList();
        String[] groups = settings.getString("warp-groups", "").split(",");
        for (String g : groups)
        {
            if (g.equals(""))
                continue;

            warpGroups.add(g);
        }

        if (updateTime < 100)
            updateTime = 100;
    }
    
    /**
     * Determines if the player can make a sign warp or waypoint.
     * @param player The player attempting to make a sign warp or waypoint.
     * @return True if we should allow creation.
     */
    public static boolean canMakeSignWarp(Player player)
    {
        if (warpGroups.isEmpty() || player.isAdmin())
            return true;
        
        for (String s : warpGroups)
        {
            if (player.isInGroup(s))
                return true;
        }
        return false;
    }
    
    /**
     * Gets the update interval.
     * @return The update interval.
     */
    public static long getUpdateInterval()
    {
        return updateTime;
    }
    
    /**
     * Determines if the player can break this sign.
     * @param sign The sign in question.
     * @param player The player trying to break the sign.
     * @return True if the player can break the sign.
     */
    public static boolean canBreakSign(AutoSignUpdater sign, Player player)
    {
        if (sign == null)
            return true;

        return (anyoneCanBreak || player.isAdmin() || sign.owner.equalsIgnoreCase(player.getName()));
    }

    /**
     * Gets a warp waypoint location from the requested key.
     * @param key The warp key.
     * @return The location of the requested warp, or null if not found.
     */
    public static Location getWarp(int dkey)
    {
        AutoSignUpdater signUpdater;
        for (int i = 0; i < SignPrintfListener.SignsList.size(); i++)
        {
            signUpdater = SignPrintfListener.SignsList.get(i);

            if (signUpdater.error)
                continue;

            if (signUpdater.waypoint.exists && signUpdater.waypoint.key == dkey)
                return signUpdater.waypoint.destLocation;
        }
        return null;
    }

    /**
     * Determines if the sign is in spawn or not.
     * @param block The sign to test.
     * @return True if the sign is in the spawn area.
     */
    public static boolean inSpawn(AutoSignUpdater block)
    {
        Location spawn = etc.getServer().getSpawnLocation();

        // assuming spawn is 0,0 for now
        int xdist = (int)(Math.floor(spawn.x) - block.posX);
        int zdist = (int)(Math.floor(spawn.z) - block.posZ);

        int spawnSize = etc.getInstance().getSpawnProtectionSize();

        if (Math.abs(xdist) < spawnSize && Math.abs(zdist) < spawnSize)
            return true;

        return false;
    }

    public static boolean isDebugging()
    {
        return debug;
    }
}
