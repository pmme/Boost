package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class Config
{
    private Main plugin;

    private File messagesConfigFile = null;
    private FileConfiguration messagesConfig = null;
    private String messagePrefix;
    private EnumMap< Messages, String > messages = new EnumMap<>( Messages.class );
    private List<String> commandUsage = new ArrayList<>();

    private int targetBoxH;
    private int targetBoxV;
    private double vertical_velocity;
    private double max_horizontal_velocity;
    private double min_horizontal_velocity;
    private double block_hit_horizontal_velocity;

    private String signTitle;
    private String signJoin;
    private String signLeave;
    private String signStatus;

    private List<GameConfig> gameConfigList = new ArrayList<>();

    public Config( Main plugin )
    {
        this.plugin = plugin;
    }

    public void init()
    {
        plugin.saveDefaultConfig();
        if( this.messagesConfigFile == null ) {
            // Same as JavaPlugin.init
            this.messagesConfigFile = new File( plugin.getDataFolder(), "messages.yml" );
        }
        // Same as JavaPlugin.saveDefaultConfig
        if( !this.messagesConfigFile.exists() ) {
            plugin.saveResource( "messages.yml", false );
        }
        this.createMessagesConfig();
        this.load();
    }

    public void reload()
    {
        plugin.reloadConfig();
        this.createMessagesConfig();
        this.messages.clear();
        this.commandUsage.clear();
        this.gameConfigList.clear();
        this.load();
    }

    private void createMessagesConfig()
    {
        // Same as JavaPlugin.reloadConfig
        this.messagesConfig = YamlConfiguration.loadConfiguration( this.messagesConfigFile );
        InputStream defConfigStream = plugin.getResource("messages.yml");
        if( defConfigStream != null ) {
            this.messagesConfig.setDefaults( YamlConfiguration.loadConfiguration( new InputStreamReader( defConfigStream, StandardCharsets.UTF_8 ) ) );
        }
    }

    private void load()
    {
        // targetDistance is the radius from the centre block, so a box of 3 blocks is (3-1)/2 = 1 block bigger than the main block. 1 block is (1-1)/2 = 0 blocks bigger, or just the main block.
        targetBoxH = ( plugin.getConfig().getInt( "physics.target_box_width", 3 ) - 1 ) / 2;
        targetBoxV = plugin.getConfig().getInt( "physics.target_box_height", 1 );
        vertical_velocity = plugin.getConfig().getDouble( "physics.vertical_velocity", 2.0 );
        max_horizontal_velocity = plugin.getConfig().getDouble( "physics.max_horizontal_velocity", 2.5 );
        min_horizontal_velocity = plugin.getConfig().getDouble( "physics.min_horizontal_velocity", 0.1 );
        block_hit_horizontal_velocity = plugin.getConfig().getDouble( "physics.block_hit_horizontal_velocity", 2.5 );

        signTitle = ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( "signs.title", "[Boost]" ) );
        signJoin = ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( "signs.join", "Click to join" ) );
        signLeave = ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( "signs.leave", "Click to leave" ) );
        signStatus = ChatColor.translateAlternateColorCodes(  '&', plugin.getConfig().getString( "signs.status", "Click for status" ) );

        ConfigurationSection gamesSection = plugin.getConfig().getConfigurationSection( "games" );
        if( gamesSection != null ) {
            for( String gameName : gamesSection.getKeys( false ) ) {
                GameConfig gameConfig = new GameConfig( plugin, gameName );
                gameConfigList.add( gameConfig );
            }
        }
        for( Messages message : Messages.values() ) {
            String messageText = messagesConfig.getString( message.getPath() );
            messages.put( message, messageText != null ? ChatColor.translateAlternateColorCodes( '&', messageText ) : "Missing message: " + message.getPath() );
        }
        messagePrefix = this.getMessage( Messages.TITLE ) + " " + ChatColor.RESET;
        for( String string : messagesConfig.getStringList( "command_usage" ) ) {
            commandUsage.add( ChatColor.translateAlternateColorCodes( '&', string ) );
        }
    }

    public int getTargetBoxH() { return targetBoxH; }
    public int getTargetBoxV() { return targetBoxV; }
    public double getVertical_velocity() { return vertical_velocity; }
    public double getMax_horizontal_velocity() { return max_horizontal_velocity; }
    public double getMin_horizontal_velocity() { return min_horizontal_velocity;}
    public double getBlock_hit_horizontal_velocity() { return block_hit_horizontal_velocity; }

    public String getSignTitle() { return signTitle; }
    public String getSignJoin() { return signJoin; }
    public String getSignLeave() { return signLeave; }
    public String getSignStatus() { return signStatus; }

    public List<GameConfig> getGameList() { return gameConfigList; }

    public GameConfig createNewGameConfig( String name )
    {
        GameConfig gameConfig = new GameConfig( plugin, name );
        gameConfig.setConfig();
        plugin.saveConfig();
        gameConfigList.add( gameConfig );
        return gameConfig;
    }

    public String getMessage( Messages message ) { return messages.get( message ); }
    public String getPrefix() { return messagePrefix; }
    public String[] getCommandUsage() { return commandUsage.toArray(new String[0]); }
}
