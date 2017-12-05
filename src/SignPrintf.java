import java.util.logging.Logger;

/**
 * SignPrintf plugin.
 *
 * @author STJrInuyasha
 */
public class SignPrintf extends Plugin
{
    private static final SignPrintfListener pfListener = new SignPrintfListener();
    protected static final Logger log = Logger.getLogger("Minecraft");
    public static String version = "1.43";

    public void disable()
    {
        pfListener.disable();
        log.info("[SignPrintf] Plugin disabled.");
    }
    public void enable()
    {
        pfListener.enable();
    }
    public void initialize()
    {
        log.info("[SignPrintf] v" + version + " Initalized.");
        etc.getLoader().addListener(PluginLoader.Hook.SIGN_CHANGE,        pfListener, this, PluginListener.Priority.HIGH);
        etc.getLoader().addListener(PluginLoader.Hook.SIGN_SHOW,          pfListener, this, PluginListener.Priority.CRITICAL);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_DESTROYED,    pfListener, this, PluginListener.Priority.CRITICAL);
        etc.getLoader().addListener(PluginLoader.Hook.BLOCK_RIGHTCLICKED, pfListener, this, PluginListener.Priority.CRITICAL);
        etc.getLoader().addListener(PluginLoader.Hook.COMMAND,            pfListener, this, PluginListener.Priority.HIGH);
    }
}
