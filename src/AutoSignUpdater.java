import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * AutoSignUpdater -
 * Contains functions for automatically updating and displaying edited signs.
 * @author STJrInuyasha
 */
public final class AutoSignUpdater
{
    // Holds position.  Mostly used for keeping track of the sign's position in the saved text file.
    int posX, posY, posZ;

    // The sign itself.
    public Sign sign;

    // Strings we need.  Raw string is what we base the formatting off of,
    // compiled string stores the last update results.
    private String[] rawStrings = {"","","",""};

    // Set on initalization.  To reduce checking time, this keeps track of
    // the sign strings that actually have things to format.
    private boolean[] check = {false, false, false, false};

    // Counters and warps, joy!
    public SignCounter counter;
    public SignWarp warp;
    public SignWaypoint waypoint;

    // Calendar.  For time based events.
    private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    // Keeps track of the player placing this sign, and at what time it happened.
    public String owner;
    public long datePlaced = cal.getTimeInMillis();

    // Set to true if this sign is errored.  This will remove the sign from the list.
    public boolean error = false;

    /**
     * Initializes a sign updater from a (presumably) just placed sign. 
     * @param newSign The sign that this updater should be based on.
     */
    public AutoSignUpdater(Sign newSign, Player player)
    {
        this.sign = newSign;
        this.posX = this.sign.getX();
        this.posY = this.sign.getY();
        this.posZ = this.sign.getZ();

        for (int i = 0; i < 4; i++)
        {
            this.rawStrings[i] = newSign.getText(i);
            if (this.rawStrings[i].contains("%"))
                check[i] = true;
        }

        try
        {
            this.initializeCounter();
            this.initializeWarpsAndWaypoints(player);
            //this.autoCompleteNames();
        }
        catch (SignParserException e)
        {
            error(e.error, e.line, e.point);
            return;
        }

        this.owner = player.getName();

        this.update(true);
    }

    /**
     * Initializes a sign updater from a text string -- IE from a file.
     * @param loadFromText The string that we're loading from.
     * @throws Exception
     */
    public AutoSignUpdater(String loadFromText) throws Exception
    {
        String[] parameters = loadFromText.split("`",-1);
        this.posX = Integer.parseInt(parameters[0]);
        this.posY = Integer.parseInt(parameters[1]);
        this.posZ = Integer.parseInt(parameters[2]);
        this.sign = (Sign)etc.getServer().getComplexBlock(this.posX, this.posY, this.posZ);

        for (int i = 0; i < 4; i++)
        {
            this.rawStrings[i] = parameters[i+3];
            if (this.rawStrings[i].contains("%"))
                check[i] = true;
        }

        this.counter = new SignCounter(Integer.parseInt(parameters[7]), Integer.parseInt(parameters[8]));
        
        if (parameters[9].equals("NIL"))
            this.warp = new SignWarp(); // dummy.
        else
            this.warp = new SignWarp(Integer.parseInt(parameters[9]));

        if (parameters[10].equals("NIL"))
            this.waypoint = new SignWaypoint(); // dummy.
        else
            this.waypoint = new SignWaypoint(Integer.parseInt(parameters[10]), Double.parseDouble(parameters[11]),
                                             Double.parseDouble(parameters[12]), Double.parseDouble(parameters[13]));

        this.owner = parameters[14];
        this.datePlaced  = Long.parseLong(parameters[15]);

        this.update(true);
    }

    /**
     * Returns all necessary sign data in a single line string, perfect for saving.
     * @return The line that should be outputted into a file.
     */
    public String save()
    {
        StringBuilder sout = new StringBuilder();
        sout.append(this.posX).append("`");
        sout.append(this.posY).append("`");
        sout.append(this.posZ).append("`");
        for (int i = 0; i < 4; i++)
            sout.append(this.rawStrings[i]).append("`");
        sout.append(this.counter.type).append("`");
        sout.append(this.counter.counter).append("`");

        if (this.warp.exists)
            sout.append(this.warp.key).append("`");
        else
            sout.append("NIL").append("`");

        if (this.waypoint.exists)
        {
            sout.append(this.waypoint.key).append("`");
            sout.append(this.waypoint.destLocation.x).append("`");
            sout.append(this.waypoint.destLocation.y).append("`");
            sout.append(this.waypoint.destLocation.z).append("`");
        }
        else
            sout.append("NIL").append("````");


        sout.append(this.owner).append("`");
        sout.append(this.datePlaced).append("`");
        return sout.toString();
    }




    /**************************************************************************
     **************************************************************************
     **                     Things for Warps and Counters                    **
     **************************************************************************
     **************************************************************************/

    /**
     * SignCounter, for the sign's counter.  Of course.
     */
    public class SignCounter
    {
        public int type = 0;
        public int counter = 0;

        /**
         * Initialization of a counter.
         * @param type Counter type.
         */
        public SignCounter(int type)
        {
            this.type = type;
        }

        /**
         * Preset initialization of a counter, normally from a file.
         * @param type Counter type.
         * @param counter Counter value.
         */
        public SignCounter(int type, int counter)
        {
            this.counter = counter;
            this.type = type;
        }

        /**
         * How the counter reacts to being left-clicked.
         */
        public void clear()
        {
            if (this.type == 1 || this.type == 4)
                this.counter = 0;
        }

        /**
         * How the counter reacts to being right-clicked.
         */
        public void add(int item)
        {
            if (this.type == 1 || this.type == 2)
                this.counter++;
            else if (this.type != 0)
            {
                switch(item)
                {
                    case 276: // diamond tools
                    case 277:
                    case 278:
                    case 279:
                    case 293:
                        this.counter += 100;
                        break;
                    case 283: // gold tools
                    case 284:
                    case 285:
                    case 286:
                    case 294:
                        this.counter += 50;
                        break;
                    case 256: // iron tools
                    case 257:
                    case 258:
                    case 267:
                    case 292:
                        this.counter += 25;
                        break;
                    case 272: // stone tools
                    case 273:
                    case 274:
                    case 275:
                    case 291:
                        this.counter += 10;
                        break;
                    case 268: // wood tools
                    case 269:
                    case 270:
                    case 271:
                    case 290:
                        this.counter += 5;
                        break;
                    default: // anything else
                        this.counter++;
                        break;
                }
            }
        }
    }
    
    /**
     * SignWarp, for the sign's warp capabilities.
     */
    public class SignWarp
    {
        public boolean exists = false;
        public int key = 0;
        
        /**
         * Initializer.
         */
        public SignWarp(int key)
        {
            this.exists = true;
            this.key = key;
        }
        
        /**
         * Null warp.  Does nothing.
         * Just here to make sure nothing breaks.
         */
        public SignWarp()
        {
        }
        
        /**
         * Handles a right click on a sign with warps.
         */
        public void handle(Player player)
        {
            Location ourWarp;
            
            if (!this.exists)
                return;
            
            if ((ourWarp = SignPrintfEtc.getWarp(this.key)) != null)
            {
                player.teleportTo(ourWarp.x, ourWarp.y, ourWarp.z, player.getRotation(), player.getPitch());
                player.sendMessage(Colors.LightGray + "The sign has teleported you to another location.");
            }
            else
            {
                player.sendMessage(Colors.LightGray + "This sign points to waypoint #" + this.key + ", which doesn't exist.");
            }
        }
    }

    /**
     * SignWaypoint, for the sign's waypoint holding abilities.
     */
    public class SignWaypoint
    {
        public boolean exists = false;
        public Location destLocation = new Location();
        public int key = 0;

        /**
         * Initialize everything from a string.
         */
        public SignWaypoint(int key, double x, double y, double z)
        {
            this.exists = true;
            this.key = key;
            this.destLocation = new Location(x, y, z);
        }

        /**
         * Waypoint type.  This is where you end up.
         * @param key
         * @param dest
         */
        public SignWaypoint(int key, Player player)
        {
            this.exists = true;
            this.key = key;
            this.destLocation = player.getLocation();
            player.sendMessage(Colors.LightGray + "Sign waypoint #" + key + " established at your location.");
        }

        /**
         * Dummy initializer.  Does nothing.
         */
        public SignWaypoint()
        {
        }
    }

    /**
     * Used to initialize the counter when necessary.
     * @return False if an error should be thrown.
     */
    private void initializeCounter() throws SignParserException
    {
        int newType = 0;

        for (int i = 0; i < 4; i++)
        {
            if (!this.check[i])
                continue;

            if (this.rawStrings[i].matches(".*%-?[0-9]{0,}c.*"))
                newType |= 1;
            if (this.rawStrings[i].matches(".*%-?[0-9]{0,}C.*"))
                newType |= 2;
            if (this.rawStrings[i].matches(".*%-?[0-9]{0,}x.*"))
                newType |= 4;
            if (this.rawStrings[i].matches(".*%-?[0-9]{0,}X.*"))
                newType |= 8;
        }

        if (Integer.highestOneBit(newType) != newType)
            throw new SignParserException("Mixed counters", "(multiple?)", -1);

        this.counter = new SignCounter(newType);
    }

    /**
     * Used to initialize the warp when necessary.
     * @return False if an error should be thrown.
     */
    private void initializeWarpsAndWaypoints(Player player) throws SignParserException
    {
        int start = 0;
        char c;

        StringBuilder number = new StringBuilder();

        boolean teleporter = false;
        boolean nwaypoint = false;
        int teleKey = 0;
        int pointKey = 0;

        // check our lines.
        for (int i = 0; i < 4; i++)
        {
            if (!this.check[i])
                continue;

            if (!this.rawStrings[i].matches("(?i).*%-?[0-9]{0,}w.*"))
                continue;

            for (int j = 0; j < this.rawStrings[i].length(); j++)
            {
                if (this.rawStrings[i].charAt(j) != '%')
                    continue;

                if (j >= this.rawStrings[i].length()-1)
                    throw new SignParserException("Out of Range", rawStrings[i], j+1);

                start = j++;
                number.setLength(0);

                for (;;j++)
                {

                    if (j >= this.rawStrings[i].length())
                        throw new SignParserException("Out of Range", rawStrings[i], j+1);

                    c = this.rawStrings[i].charAt(j);

                    if (c == '-')
                    {
                        if (j == start+1)
                            number.append(c);
                        else
                            this.error("Bad Syntax", rawStrings[i], j+1);
                    }
                    else if (Character.isDigit(c))
                        number.append(c);
                    else if (c == 'w' || c == 'W')
                    {
                        if (number.length() < 1)
                            throw new SignParserException("%"+c+" needs param.", rawStrings[i], j+1);
                        else if (number.length() == 1 && number.charAt(0) == '-')
                            throw new SignParserException("Bad Syntax", rawStrings[i], j+1);
                        else if (teleporter && c == 'w')
                            throw new SignParserException("Multiple warps", "(multiple?)", -1);
                        else if (nwaypoint && c == 'W')
                            throw new SignParserException("Multiple waypts", "(multiple?)", -1);
                        else if (!SignPrintfEtc.canMakeSignWarp(player))
                            throw new SignParserException("Not authorized!", rawStrings[i], j+1);

                        int parameter;
                        try
                        {
                            parameter = Integer.parseInt(number.toString());
                        }
                        catch (Exception e)
                        {
                            //e.printStackTrace();
                            throw new SignParserException("Overflow", rawStrings[i], j);
                        }

                        if (c == 'w')
                        {
                            teleporter = true;
                            teleKey = parameter;
                            break;
                        }

                        // At this point we haven't been added to the list of signs yet,
                        // so we won't run into ourselves while looking.
                        if (SignPrintfEtc.getWarp(parameter) != null)
                            throw new SignParserException("Waypoint exists", rawStrings[i], j);
                        else
                        {
                            nwaypoint = true;
                            pointKey = parameter;
                            break;
                        }
                    }
                    else
                        break;
                }
            }
        }

        // If we got down here and the variables aren't false, well.
        if (teleporter)
        {
            this.warp = new SignWarp(teleKey);
        }
        else
        {
            this.warp = new SignWarp(); // dummy
        }

        if (nwaypoint)
        {
            this.waypoint = new SignWaypoint(pointKey, player); // waypoint
        }
        else
        {
            this.waypoint = new SignWaypoint(); // waypoint
        }
    }




    /**************************************************************************
     **************************************************************************
     **                      Sign Parsing and Updating                       **
     **************************************************************************
     **************************************************************************/

    /**
     * Updates text as necessary.
     * @param needsUpdating Determines if an update is necessary. Is set to true elsewhere
     *                      if it really is necessary, but it can be set to true as a
     *                      parameter to force an update.
     * @return True if sign changed at all, false if not.
     */
    public boolean update(boolean needsUpdating)
    {
        // No sign?  Just stop.
        if (this.sign == null)
            return false;

        // reset time zone to GMT
        this.cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        this.cal.setTime(new Date());

        // check our lines.
        for (int i = 0; i < 4; i++)
        {
            if (!this.check[i])
                continue;

            String newLine;
            try
            {
                newLine = this.parse(rawStrings[i]);
                if (!(newLine.equals(this.sign.getText(i))))
                {
                    this.sign.setText(i, newLine);
                    needsUpdating = true;
                }
            }
            catch (SignParserException e)
            {
                this.error(e.error, e.line, e.point);
                //e.printStackTrace();
                return false;
            }
        }

        if (needsUpdating)
        {
            // Update the sign if forced, or something changed
            this.sign.update();
        }
        return needsUpdating;
    }

    /**
     * Parses a line for the updater.
     * @param line The line to parse.
     * @return The parsed and formatted line.
     * @throws AutoSignUpdater.SignParserException
     */
    private String parse(String line) throws SignParserException
    {
        int start = 0, j = 0;
        StringBuilder editedString = new StringBuilder();
        String replacement;

        StringBuilder number = new StringBuilder();
        char c;

        for (; j < line.length(); j++)
        {
            if (line.charAt(j) != '%')
            {
                editedString.append(line.charAt(j));
                continue;
            }

            if (j >= line.length()-1)
                throw new SignParserException("Out of Range", line, j+1);

            start = j++;
            number.setLength(0);

            if (line.charAt(j) == '%') // %% - escape
                replacement = "%";
            else
            {
                for (;;j++)
                {
                    if (j >= line.length())
                        throw new SignParserException("Out of Range", line, line.length());

                    c = line.charAt(j);

                    if (c == '-')
                    {
                        if (j == start+1)
                            number.append(c);
                        else
                            throw new SignParserException("Bad Syntax", line, j+1);
                    }
                    else if (Character.isDigit(c))
                        number.append(c);
                    else if (Character.isLetter(c))
                    {
                        if (number.length() < 1)
                            number.append(0);
                        else if (number.length() == 1 && number.charAt(0) == '-')
                            throw new SignParserException("Bad Syntax", line, j+1);

                        int parameter;

                        try
                        {
                            parameter = Integer.parseInt(number.toString());
                        }
                        catch (Exception e)
                        {
                            //e.printStackTrace();
                            throw new SignParserException("Overflow", line, j);
                        }

                        try
                        {
                            replacement = this.determineReplacement(parameter, c);
                            break;
                        }
                        catch (Exception e)
                        {
                            //e.printStackTrace();
                            throw new SignParserException(e.getMessage(), line, j+1);
                        }
                    }
                    else
                        throw new SignParserException("Bad Character", line, j+1);
                }
            }

            editedString.append(replacement);
        }
        return editedString.toString();
    }

    /**
     * Determines an appropriate replacement for a formatting code.
     * @param param  An optional number that precedes the code.
     * @param letter The letter code.
     * @return The string this code should be replaced with.
     * @throws Exception
     */
    private String determineReplacement(int param, char letter) throws Exception
    {
        StringBuilder retString = new StringBuilder();
        boolean minecrafting = false;

        switch(letter)
        {
            case 'z': // %#z set timezone
            {
                StringBuilder tz = new StringBuilder().append("GMT");
                tz.append(String.format("%+d",param % 24));
                this.cal.setTimeZone(TimeZone.getTimeZone(tz.toString()));
                return "";
            }
            case 't': // %t = shorthand for %h:%i
                retString.append(String.format("%02d:%02d", this.cal.get(Calendar.HOUR_OF_DAY), this.cal.get(Calendar.MINUTE)));
                break;
            case 'h': // %h = hour
                retString.append(String.format("%02d", this.cal.get(Calendar.HOUR_OF_DAY)));
                break;
            case 'i': // %i = minute
                retString.append(String.format("%02d", this.cal.get(Calendar.MINUTE)));
                break;

            case 'u': // %u = shorthand for %j:%i %p
            {
                String AMPM = (this.cal.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM";
                retString.append(String.format("%02d:%02d %s", ((this.cal.get(Calendar.HOUR) + 11) % 12) + 1, this.cal.get(Calendar.MINUTE), AMPM));
                break;
            }
            case 'j': // %j = 12 hour time
                retString.append(String.format("%02d", ((this.cal.get(Calendar.HOUR) + 11) % 12) + 1));
                break;
            case 'p': // %p = AM/PM notice
                String AMPM = (this.cal.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM";
                retString.append(String.format("%s", AMPM));
                break;

            case 'a': // %a = shorthand for %m/%d/%y
                retString.append(String.format("%02d/%02d/%04d", this.cal.get(Calendar.MONTH) + 1, this.cal.get(Calendar.DAY_OF_MONTH), this.cal.get(Calendar.YEAR)));
                break;
            case 'd': // %d = day
                retString.append(String.format("%02d", this.cal.get(Calendar.DAY_OF_MONTH)));
                break;
            case 'm': // %m = month
                retString.append(String.format("%02d", this.cal.get(Calendar.MONTH) + 1));
                break;
            case 'y': // %y = year
                retString.append(String.format("%04d", this.cal.get(Calendar.YEAR)));
                break;

            default:
                minecrafting = true;
        }

        if (!minecrafting)
            return retString.toString();

        // Minecraft stuff.
        long servertime = etc.getServer().getTime() + 6000;
        long hours, minutes, twelvehours;
        String timeofday;

        servertime %= 24000;
        if (servertime < 0) // Damned modulus with negative numbers!
            servertime += 24000;

        hours = servertime / 1000;
        minutes = (long)((servertime % 1000) / (50.0/3.0));
        twelvehours = ((hours + 23) % 12) + 1;
        timeofday = ((hours < 12) ? "AM" : "PM");

        switch (letter)
        {
            case 'T':
                retString.append(String.format("%02d:%02d", hours, minutes));
                break;
            case 'H':
                retString.append(String.format("%02d", hours));
                break;
            case 'I':
                retString.append(String.format("%02d", minutes));
                break;
            case 'U':
                retString.append(String.format("%02d:%02d %s", twelvehours, minutes, timeofday));
                break;
            case 'J':
                retString.append(String.format("%02d", twelvehours));
                break;
            case 'P':
                retString.append(String.format("%s", timeofday));
                break;
            case 'R':
                retString.append(etc.getServer().getTime());
                break;
            case 'c': // %c == counter, with reset
            case 'C': // %C == counter, without reset
            case 'x': // %x == counter, with tools speeding up increases
            case 'X': // %X == counter, with tools speeding up increases, without reset
                retString.append(this.counter.counter + param);
                break;
            case 'v': // %v == version string of current SignPrintf version
                retString.append(SignPrintf.version);
                break;
            case 'w':
            case 'W':
                break;
            default:
                throw new Exception("Bad character " + letter);
        }
        return retString.toString();
    }

    /**
     * Called if an error occurs while updating a sign.  Stops all further parsing
     * of this sign.
     * @param errorname Name of the error.
     * @param line The line it occurred on.
     * @param position Where in the line it occurred on.
     */
    private void error(String errorname, String line, int position)
    {
        this.sign.setText(0, "= SIGN ERROR =");
        this.sign.setText(1, errorname);
        this.sign.setText(2, "ch. " + position + " in line:");
        this.sign.setText(3, line);
        this.sign.update();

        // this will remove us from the list of signs.
        this.error = true;

        SignPrintf.log.warning("[SignPrintf] Error caught in sign creation.");
    }

    /**
     * SignParserException, for exceptions that occur in the sign
     * parsing phase.
     */
    private class SignParserException extends Exception
    {
        public String error;
        public String line;
        public int point;

        public SignParserException(String e, String l, int p)
        {
            super(e);
            error = e;
            line = l;
            point = p;
        }
    }
}