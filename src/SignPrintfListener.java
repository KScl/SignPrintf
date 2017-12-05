import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * SignPrintfListener -
 * Keeps track of sign objects as they are sent/created/destroyed.
 * @author STJrInuyasha
 */
public class SignPrintfListener extends PluginListener {
    public static List<AutoSignUpdater> SignsList;
    public Timer signTimer;

    /**
     * Enables the listener.
     */
    public void enable()
    {
        SignPrintfEtc.doSettings();
        SignPrintfEtc.writeUpdatr();

        SignsList = new ArrayList<AutoSignUpdater>();
        try
        {
            this.readSigns();
            SignPrintf.log.info("[SignPrintf] Loaded " + SignsList.size() + " signs from file.");
        }
        catch (Exception e)
        {
            SignPrintf.log.info("[SignPrintf] Failed to load sign file.");
            //No point printing a stack trace for a (likely) missing file.
            //e.printStackTrace();
        }
        signTimer = new Timer();
        signTimer.schedule(new SignUpdater(), 0, SignPrintfEtc.getUpdateInterval());

        etc.getInstance().addCommand("/signwarp", "[number] - Warps to specified sign waypoint number");
        etc.getInstance().addCommand("/waypoints", "- Lists the sign waypoint numbers you have used");
        etc.getInstance().addCommand("/signhelp", "<page> - Quick help on using SignPrintf");
    }

    /**
     * Disables the listener, clears all signs, and cancels the timer.
     */
    public void disable()
    {
        // Right before we die, save the signs.
        this.writeSigns();
        SignsList.clear();
        signTimer.cancel();
                
        etc.getInstance().removeCommand("/signwarp");
        etc.getInstance().removeCommand("/waypoints");
        etc.getInstance().removeCommand("/signhelp");
    }

    /**
     * Writes signs to the output text file for future storage.
     */
    public void writeSigns()
    {
        try
        {
            File outputFile = new File("signpf.txt");
            BufferedWriter fout = new BufferedWriter(new FileWriter(outputFile));
            AutoSignUpdater signUpdater;

            for (int i = 0; i < SignsList.size(); i++)
            {
                signUpdater = SignsList.get(i);

                if (signUpdater.error)
                    continue;

                fout.write(signUpdater.save());
                fout.newLine();
            }
            fout.close();
            if (SignPrintfEtc.isDebugging())
                SignPrintf.log.info("[SignPrintf] Signs saved.");
        }
        catch (java.io.IOException ioe)
        {
            ioe.printStackTrace();
            SignPrintf.log.info("[SignPrintf] Error writing signpf.txt to save signs.");
        }
    }

    /**
     * Reads signs from the file.
     * @throws Exception
     */
    public void readSigns() throws Exception
    {
        File inputFile = new File("signpf.txt");
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        String line = null;
        while ((line=reader.readLine()) != null)
        {
            AutoSignUpdater x = new AutoSignUpdater(line);
            SignsList.add(x);
        }
        reader.close();
    }

    /**
     * SignUpdater task for the timer.
     */
    public class SignUpdater extends TimerTask
    {
        public void run()
        {
            runSignUpdates();
        }
    }

    /**
     * Runs sign updates every half a second.
     * Not every updated sign gets displayed, mind you --
     * if a sign returns false for its update function,
     * it does not get displayed that run-through.
     */
    public void runSignUpdates()
    {
        for (AutoSignUpdater signUpdater : SignsList)
        {
            if (signUpdater.sign == null)
                continue;

            if (signUpdater.error)
            {
                SignsList.remove(signUpdater);
                continue;
            }

            if (!etc.getServer().isChunkLoaded(signUpdater.posX, signUpdater.posY, signUpdater.posZ))
            {
                // No sign? Shut up then.
                if (SignPrintfEtc.isDebugging())
                    SignPrintf.log.info("[SignPrintf] The sign at  " + signUpdater.posX + ", " + signUpdater.posY + ", " + signUpdater.posZ + " was unloaded?");

                signUpdater.sign = null;
                continue;
            }
        
            signUpdater.update(false);
        }
    }




    /**************************************************************************
     **************************************************************************
     **                      Overridden game functions                       **
     **************************************************************************
     **************************************************************************/

    /**
     * Called when the player changes a complex block (aka, a sign.)
     * This function ignores anything that isn't a sign, obviously.
     * @param player The player that changed this sign.
     * @param block The block that was changed.
     * @return Always returns false (so that the change is not undone).
     */
    public boolean onSignChange(Player player, Sign sign)
    {
        String signText;
        for (int i = 0; i < 4; i++)
        {
            signText = sign.getText(i);
            if (signText.contains("AllDo:") || signText.contains("PlayerDo:") || signText.contains("ServerDo:"))
            {
                if (SignPrintfEtc.isDebugging())
                    SignPrintf.log.info("[SignPrintf] Ignoring " + sign.getX() + ", " + sign.getY() + ", " + sign.getZ() + "; SignCommands string detected");

                return false;
            }
            if (signText.contains("%"))
            {
                AutoSignUpdater newSign = new AutoSignUpdater(sign, player);
                if (newSign.error)
                    break;

                SignsList.add(newSign);
                this.writeSigns();
                if ((newSign.counter.type != 0 || newSign.warp.exists) && SignPrintfEtc.inSpawn(newSign))
                {
                    player.sendMessage(Colors.LightGray + "The sign you just placed is inside the spawn protection area.");
                    player.sendMessage(Colors.LightGray + "Non-admins can't use the counter or warp in your sign!");
                }

                if (SignPrintfEtc.isDebugging())
                    SignPrintf.log.info("[SignPrintf] Sign block at " + newSign.posX + ", " + newSign.posY + ", " + newSign.posZ + " created.");
                break;
            }
        }
        return false;
    }

    /**
     * Called when we send a complex block to the player.
     * This is only used to activate signs that were not able to be
     * activated when the listener was first enabled (because they
     * were in unloaded chunks, for example.)
     * @param player The player this block is being sent to.
     * @param block The block that was sent.
     * @return Always false, to ensure the sign gets sent properly.
     */
    public void onSignShow(Player player, Sign sign)
    {
        AutoSignUpdater signUpdater;

        for (int i = 0; i < SignsList.size(); i++)
        {
            signUpdater = SignsList.get(i);

            if (sign.getX() == signUpdater.posX && sign.getY() == signUpdater.posY && sign.getZ() == signUpdater.posZ)
            {
                if (signUpdater.sign == null)
                {
                    signUpdater.sign = sign;
                    signUpdater.update(true);

                    if (SignPrintfEtc.isDebugging())
                        SignPrintf.log.info("[SignPrintf] Sign block at " + signUpdater.posX + ", " + signUpdater.posY + ", " + signUpdater.posZ + " enabled.");
                }
            }
        }
    }

    /**
     * Called when a player left clicks on a block.
     * Used to reset certain sign counters, and to update the list
     * if the player destroys a sign.
     * @param player The player that destroyed/clicked on this block.
     * @param block The block destroyed/clicked on.
     * @return True if we don't want the player destroying our precious sign!
     */
    public boolean onBlockDestroy(Player player, Block block)
    {
        if (!(block.getType() == 63 || block.getType() == 68))
            return false;

        AutoSignUpdater asign;

        for (int i = 0; i < SignsList.size(); i++)
        {
            asign = SignsList.get(i);
            if (asign.sign == null)
                continue;
            if (asign.posX == block.getX() && asign.posY == block.getY() && asign.posZ == block.getZ())
            {
                if (block.getStatus() == 0)
                {
                    asign.counter.clear();
                }

                if (!SignPrintfEtc.canBreakSign(asign, player))
                {
                    // Nope! You can't break this.
                    return true;
                }

                if (block.getStatus() == 2)
                {
                    SignsList.remove(i);
                    this.writeSigns();
                    if (SignPrintfEtc.isDebugging())
                        SignPrintf.log.info("[SignPrintf] Sign block at " + asign.posX + ", " + asign.posY + ", " + asign.posZ + " removed.");
                    //player.sendMessage("You killed one of the sign blocks.");
                }

                return false;
            }
        }
        return false;
    }

    /**
     * Called when the player right clicks on a block.
     * Used to increment counters.
     * @param player The player that right clicked.
     * @param placed The block that was/would have been placed, if applicable. (Ignored.)
     * @param inHand The item in the player's hand at the time of the click.
     */
    public void onBlockRightClicked(Player player, Block clicked, Item inHand)
    {
        ComplexBlock thisBlock = etc.getServer().getComplexBlock(clicked);

        if (thisBlock == null || !(thisBlock instanceof Sign))
            return;

        AutoSignUpdater asign;
        for (int i = 0; i < SignsList.size(); i++)
        {
            asign = SignsList.get(i);
            if (asign.sign == null)
                continue;
            if (asign.posX == clicked.getX() && asign.posY == clicked.getY() && asign.posZ == clicked.getZ())
            {
                if (inHand.getItemId() == 288 && (player.isAdmin() || player.getName().equals(asign.owner)))
                {
                    Calendar tmpcal = Calendar.getInstance();
                    tmpcal.setTimeInMillis(asign.datePlaced);

                    if (player.getName().equals(asign.owner))
                        player.sendMessage(Colors.LightGray + "You placed this sign on " + tmpcal.getTime().toString() + ".");
                    else
                        player.sendMessage(Colors.LightGray + "Placed by " + asign.owner + " on " + tmpcal.getTime().toString() + ".");

                    if (asign.warp.exists)
                        player.sendMessage(Colors.LightGray + "This sign warps to waypoint #"+asign.warp.key+".");
                    if (asign.waypoint.exists)
                        player.sendMessage(Colors.LightGray + "This sign is waypoint #"+asign.waypoint.key+".");
                    return;
                }
                asign.counter.add(inHand.getItemId());
                asign.warp.handle(player);
                return;
            }
        }
        return;
    }

    /**
     * Support for a couple of commands to help people placing custom signs.
     * @param player The player sending the command
     * @param split The commands.
     * @return True if we handled a command and the server should not send the command
     *         to any other plugins.
     */
    public boolean onCommand(Player player, String[] split)
    {

        // /signwarp -- warp to a sign (perhaps enable if requested?)
        if (split[0].equalsIgnoreCase("/signwarp") && player.canUseCommand("/signwarp"))
        {
            if (split.length > 1)
            {
                Location destWarp;

                try
                {
                    destWarp = SignPrintfEtc.getWarp(Integer.parseInt(split[1].toString()));
                    player.teleportTo(destWarp.x, destWarp.y, destWarp.z, player.getRotation(), player.getPitch());
                    player.sendMessage(Colors.LightGray + "Teleported to waypoint #" + split[1] + "...");
                }
                catch (Exception e)
                {
                    player.sendMessage(Colors.LightGray + "Usage is /signwarp <warp number>");
                }
            }
            else
                player.sendMessage(Colors.LightGray + "Usage is /signwarp <warp number>");
            return true;
        }

        // /waypoints -- check waypoints (perhaps enable if requested?)
        if (split[0].equalsIgnoreCase("/waypoints") && player.canUseCommand("/waypoints"))
        {
            AutoSignUpdater signUpdater;
            StringBuilder list = new StringBuilder();
            boolean first = true;

            String playername = player.getName();
            if (player.isAdmin() && split.length > 1)
                playername = split[1];

            for (int i = 0; i < SignsList.size(); i++)
            {
                signUpdater = SignsList.get(i);

                if (signUpdater.error || !signUpdater.waypoint.exists)
                    continue;
                if (!playername.equalsIgnoreCase("-a") && !signUpdater.owner.equalsIgnoreCase(playername))
                    continue;

                if (first)
                    first = false;
                else
                    list.append(", ");

                list.append(signUpdater.waypoint.key);
            }

            if (list.length() <= 0)
            {
                if (playername.equalsIgnoreCase("-a"))
                    player.sendMessage(Colors.LightGray + "Nobody has placed a waypoint sign.");
                else
                    player.sendMessage(Colors.LightGray + ((playername.equalsIgnoreCase(player.getName())) ? "You have" : playername + " has")
                                       + " placed no waypoint signs.");
            }
            else
            {
                if (playername.equalsIgnoreCase("-a"))
                    player.sendMessage(Colors.LightGray + "List of all the waypoint numbers currently in use:");
                else
                    player.sendMessage(Colors.LightGray + "List of all the waypoint numbers "
                                       + ((playername.equalsIgnoreCase(player.getName())) ? "you are" : playername + " is")
                                       + " currently using:");
                player.sendMessage(list.toString());
            }

            return true;
        }

        // /signhelp - help using signprintf.
        if (split[0].equalsIgnoreCase("/signhelp") && player.canUseCommand("/signhelp"))
        {
            int page = 1;
            if (split.length > 1)
            {
                try
                {
                    page = Integer.parseInt(split[1].toString());
                }
                catch (Exception e) {}
            }
            SignPrintfHelp.sendHelp(player, page);
            return true;
        }
        return false;
    }
}
