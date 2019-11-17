package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Config
{
    private Main plugin;

    private File messagesConfigFile = null;
    private FileConfiguration messagesConfig = null;
    private String messagePrefix;
    private EnumMap< Messages, String > messages = new EnumMap<>( Messages.class );
    private List<String> playerStatsTemplate = new ArrayList<>();
    private List<String> commandUsageUser = new ArrayList<>();
    private List<String> commandUsageAdmin = new ArrayList<>();

    private InstructionBook instructionBook;

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
    private String signStats;
    private String signTop;

    private SpawnLocation mainLobbySpawn;

    private boolean boostStickRandom;
    private String defaultBoostStick;
    private List<BoostStick> boostSticks = new ArrayList<>();
    private Map< String, BoostStick > boostSticksByName = new HashMap<>();
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
        this.playerStatsTemplate.clear();
        this.commandUsageUser.clear();
        this.commandUsageAdmin.clear();
        this.boostSticks.clear();
        this.boostSticksByName.clear();
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
        signStatus = ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( "signs.status", "Click for status" ) );
        signStats = ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( "signs.stats", "Your stats" ) );
        signTop = ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( "signs.top", "Leader board" ) );

        mainLobbySpawn = new SpawnLocation( plugin, "main_lobby" );

        boostStickRandom = plugin.getConfig().getBoolean( "boost_sticks.random", true );
        defaultBoostStick = plugin.getConfig().getString( "boost_sticks.default" );
        ConfigurationSection boostSticksSection = plugin.getConfig().getConfigurationSection( "boost_sticks.stick_types" );
        if( boostSticksSection != null ) {
            for( String boostStickName : boostSticksSection.getKeys( false ) ) {
                BoostStick boostStick = new BoostStick( plugin, boostStickName.toLowerCase() );
                boostSticks.add( boostStick );
                boostSticksByName.put( boostStick.getName(), boostStick );
            }
        }
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
        for( String string : messagesConfig.getStringList( "player_stats" ) ) {
            playerStatsTemplate.add( string );
        }
        for( String string : messagesConfig.getStringList( "command_usage_user" ) ) {
            commandUsageUser.add( ChatColor.translateAlternateColorCodes( '&', string ) );
        }
        for( String string : messagesConfig.getStringList( "command_usage_admin" ) ) {
            commandUsageAdmin.add( ChatColor.translateAlternateColorCodes( '&', string ) );
        }
        instructionBook = new InstructionBook( plugin, messagesConfig );
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
    public String getSignStats() { return signStats; }
    public String getSignTop() { return signTop; }

    public Location getMainLobbySpawn() { return mainLobbySpawn.getSpawn(); }

    public boolean isBoostStickRandom() { return boostStickRandom; }
    public String getDefaultBoostStick() { return defaultBoostStick; }
    public List<BoostStick> getBoostSticks() { return boostSticks; }
    public BoostStick getRandomBoostStick() {
        if( boostSticks.size() == 0 ) {
            plugin.getLogger().severe( "boost_sticks.stick_types configuration missing." );
            return null;
        }
        return boostSticks.get( (int)( Math.random() * ( 0.5D + boostSticks.size() ) ) );
    }
    public BoostStick getBoostStick( String name ) { return boostSticksByName.get( name ); }

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
    public List<String> getPlayerStatsTemplate() { return playerStatsTemplate; }
    public ItemStack createInstructionBook() { return instructionBook.create(); }

    public String[] getCommandUsage( boolean isAdmin )
    {
        List<String> commandUsage = new ArrayList<>( commandUsageUser );
        if( isAdmin ) commandUsage.addAll( commandUsageAdmin );
        return commandUsage.toArray(new String[0]);
    }
}
