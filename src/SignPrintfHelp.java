/**
 * Help for SignPrintf stored in a handy little class.
 * @author STJrInuyasha
 */
public final class SignPrintfHelp {
    private static final String help[] = {
// Page 1
/* 1 */ Colors.Yellow     + "Welcome to SignPrintf version " + SignPrintf.version + ".",
/* 2 */ Colors.White      + "SignPrintf contains a number of formatting codes for signs",
/* 3 */ Colors.White      + "that allow you to place important, constantly updating",
/* 4 */ Colors.White      + "information into your signs, as well as turning your signs",
/* 5 */ Colors.White      + "into simple teleporters and counters.",
/* 6 */ Colors.White      + "You use these formatting codes by placing a percent symbol",
/* 7 */ Colors.White      + "into your sign, followed by an optional numeric parameter",
/* 8 */ Colors.White      + "(if applicable) and a one letter code to determine the output.",

// Page 2
/* 1 */ Colors.White      + "The signs are automatically updated once every half a second",
/* 2 */ Colors.White      + "by default (your server owner may have changed the speed),",
/* 3 */ Colors.White      + "but only when necessary - Sign updates won't be sent to",
/* 4 */ Colors.White      + "players constantly when the sign is unchanged.",
/* 5 */ Colors.White      + "The following pages list all of the formatting codes available",
/* 6 */ Colors.White      + "in this version of SignPrintf.  Formatting codes that accept an",
/* 7 */ Colors.White      + "optional parameter are listed with a # after the % character.",
/* 8 */ Colors.White      + "Note that all codes are case sensitive - %w does not equal %W.",

// Page 3
/* 1 */ Colors.LightGreen + " - Displaying the server's current time (1/2) - ",
/* 2 */ Colors.Yellow     + "%#z " + Colors.White + "- Displays nothing; sets the timezone for later codes.",
/* 3 */ Colors.White      + "The parameter is the timezone offset from GMT time.",
/* 4 */ Colors.Yellow     + "%h  " + Colors.White + "- Displays the hour of the server time, in 24 hour format.",
/* 5 */ Colors.Yellow     + "%j  " + Colors.White + "- Displays the hour of the server time, in 12 hour format.",
/* 6 */ Colors.Yellow     + "%i  " + Colors.White + "- Displays the minute of the current server time.",
/* 7 */ Colors.Yellow     + "%p  " + Colors.White + "- Displays the AM/PM prefix of the current server time.",
/* 8 */ "",

// Page 4
/* 1 */ Colors.LightGreen + " - Displaying the server's current time (2/2) - ",
/* 2 */ Colors.Yellow     + "%t  " + Colors.White + "- Shorthand for \"%h:%i\" (default 24 hour time).",
/* 3 */ Colors.Yellow     + "%u  " + Colors.White + "- Shorthand for \"%j:%i %p\" (default 12 hour time).",
/* 4 */ "",
/* 5 */ "",
/* 6 */ "",
/* 7 */ "",
/* 8 */ "",

// Page 5
/* 1 */ Colors.LightGreen + " - Displaying the server's current date (1/1) - ",
/* 2 */ Colors.Yellow     + "%d  " + Colors.White + "- Displays the current day of the month.",
/* 3 */ Colors.Yellow     + "%m  " + Colors.White + "- Displays the current month.",
/* 4 */ Colors.Yellow     + "%y  " + Colors.White + "- Displays the current year, in 4 digit format.",
/* 5 */ Colors.Yellow     + "%a  " + Colors.White + "- Shorthand for \"%m/%d/%y\" (default date format).",
/* 6 */ "",
/* 7 */ "",
/* 8 */ "",

// Page 6
/* 1 */ Colors.LightGreen + " - Displaying the time of Minecraft (1/2) - ",
/* 2 */ Colors.Yellow     + "%H  " + Colors.White + "- Displays the hour of the Minecraft time, 24 hour format.",
/* 3 */ Colors.Yellow     + "%J  " + Colors.White + "- Displays the hour of the Minecraft time, 12 hour format.",
/* 4 */ Colors.Yellow     + "%I  " + Colors.White + "- Displays the minute of the current Minecraft time.",
/* 5 */ Colors.Yellow     + "%P  " + Colors.White + "- Displays the AM/PM prefix of the current Minecraft time.",
/* 6 */ Colors.Red        + " * NOTE: " + Colors.White + "Raw server time 0 equals 6:00 AM.",
/* 7 */ Colors.Yellow     + "%R  " + Colors.White + "- Displays the raw Minecraft time, with no formatting.",
/* 8 */ "",

// Page 7
/* 1 */ Colors.LightGreen + " - Displaying the time of Minecraft (2/2) - ",
/* 2 */ Colors.Yellow     + "%T  " + Colors.White + "- Shorthand for \"%H:%I\" (24 hour Minecraft time).",
/* 3 */ Colors.Yellow     + "%U  " + Colors.White + "- Shorthand for \"%J:%I %P\" (12 hour Minecraft time).",
/* 4 */ "",
/* 5 */ "",
/* 6 */ "",
/* 7 */ "",
/* 8 */ "",

// Page 8
/* 1 */ Colors.LightGreen + " - Counters (1/1) - ",
/* 2 */ Colors.Yellow     + "%#c " + Colors.White + "- A counter. Increased by 1 when the sign is right-clicked,",
/* 3 */ Colors.White      + "reset to 0 when left-clicked. Parameter = starting offset.",
/* 4 */ Colors.Yellow     + "%#C " + Colors.White + "- Same, except left-clicking doesn't reset the counter.",
/* 5 */ Colors.Yellow     + "%#x " + Colors.White + "- A counter designed for larger numbers. Right-clicking",
/* 6 */ Colors.White      + "with different level tools increases the counter faster.",
/* 7 */ Colors.Yellow     + "%#X " + Colors.White + "- Same, except left-clicking doesn't reset the counter.",
/* 8 */ Colors.Red        + " * NOTE: " + Colors.White + "You may only use one type of counter in a single sign.",

// Page 9
/* 1 */ Colors.LightGreen + " - Warps (1/1) - ",
/* 2 */ Colors.Yellow     + "%#w " + Colors.White + "- A warp! The parameter determines the waypoint number.",
/* 3 */ Colors.White      + "Right click the sign to teleport to the waypoint in a flash!",
/* 4 */ Colors.Red        + " * NOTE: " + Colors.White + "Multiple warps can point to the same waypoint.",
/* 5 */ Colors.Yellow     + "%#W " + Colors.White + "- A waypoint! The parameter designates the waypoint",
/* 6 */ Colors.White      + "number. Your location when you place this sign determines",
/* 7 */ Colors.White      + "where people that teleport to the waypoint will end up.",
/* 8 */ Colors.Red        + " * NOTE: " + Colors.White + "You can only have one waypoint sign for each number.",

// Page 10
/* 1 */ Colors.LightGreen + " - Miscellaneous (1/1) - ",
/* 2 */ Colors.Yellow     + "%v  " + Colors.White + "- Displays the current version of SignPrintf.",
/* 3 */ Colors.Yellow     + "%%  " + Colors.White + "- Escape sequence - will insert a raw % symbol in the sign.",
/* 4 */ "",
/* 5 */ "",
/* 6 */ "",
/* 7 */ "",
/* 8 */ "",
    };

    public static void sendHelp(Player player, int page)
    {
        int j;
        int numPages = help.length / 8;

        player.sendMessage(Colors.Green + "SignPrintf Quick Help - Page " + Colors.White + page + Colors.Green + " of " + Colors.White + numPages);
        for (int i = 0; i < 8; i++)
        {
            j = i + ((page-1) * 8);
            if (j >= help.length || j < 0)
                return;

            player.sendMessage(help[j]);
        }
    }
}
