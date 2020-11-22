package nz.pmme.Boost.Config;

import nz.pmme.Boost.Data.PlayerStats;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Winner;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class Config
{
    private Main plugin;

    private String languagePrefix;
    private List<String> languagePrefixes;

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
    private long coolDown;
    private long gameStartBoostDelay;

    private String signTitle;
    private String signJoin;
    private String signLeave;
    private String signStatus;
    private String signStats;
    private String signTop;
    private String signDaily;
    private String signWeekly;
    private String signMonthly;
    private String signTotal;

    private SpawnLocation mainLobbySpawn;

    private int worldSoundRange;
    private Sound tickSound;
    private Sound joinSound;
    private Sound leaveSound;
    private Sound startSound;
    private Sound winSound;
    private Sound loseSound;
    private Sound boostSound;
    private Sound boostedSound;
    private Sound statsSound;
    private Sound leaderSound;

    private GameMode playingGameMode;
    private GameMode lostGameMode;
    private GameMode lobbyGameMode;
    private GameMode buildGameMode;

    private boolean boostWhileQueuing;
    private boolean commandsBlockedWhilePlaying;
    private Set<String> commandsAllowedWhilePlaying = new HashSet<>();

    private Map< StatsPeriod, EnumMap< Winner, List<String> > > winCommands = new EnumMap<>(StatsPeriod.class);

    private BarColor gameStartBoostDelayBarColor;
    private BarStyle gameStartBoostDelayBarStyle;

    private File sticksConfigFile = null;
    private FileConfiguration sticksConfig = null;
    private boolean boostStickRandom;
    private String defaultBoostStick;
    private boolean giveOnlyBestBoostStick;
    private List<BoostStick> boostSticks = new ArrayList<>();
    private Map< String, BoostStick > boostSticksByName = new HashMap<>();

    private File guiConfigFile = null;
    private FileConfiguration guiConfig = null;
    private String guiName;
    private int guiRows;
    private Map< String, GUIButtonConfig > guiButtons = new HashMap<>();

    private List<GameConfig> gameConfigList = new ArrayList<>();
    private Map< String, Boolean > gameWorlds = new HashMap<>();

    private File statsResetConfigFile = null;
    private FileConfiguration statsResetConfig = null;
    private int trackedDay = 0;
    private int trackedWeek = 0;
    private int trackedMonth = 0;

    public Config( Main plugin )
    {
        this.plugin = plugin;
    }

    public void init()
    {
        plugin.saveDefaultConfig();
        this.load();
    }

    public void reload()
    {
        plugin.reloadConfig();
        this.messages.clear();
        this.playerStatsTemplate.clear();
        this.commandUsageUser.clear();
        this.commandUsageAdmin.clear();
        this.boostSticks.clear();
        this.boostSticksByName.clear();
        this.guiButtons.clear();
        this.gameConfigList.clear();
        this.gameWorlds.clear();
        this.load();
    }

    public boolean updateConfigFiles()
    {
        boolean somethingWasAdded = false;
        if( this.updateConfig( plugin.getConfig(), "config.yml" ) ) {
            plugin.saveConfig();
            somethingWasAdded = true;
        }
        if( this.updateConfig( this.messagesConfig, this.languagePrefix + "messages.yml" ) ) {
            try {
                this.messagesConfig.save( this.messagesConfigFile );
            } catch( IOException e ) {
                plugin.getLogger().severe( "Could not save to file '" + this.messagesConfigFile.getPath() + "'" );
            }
            somethingWasAdded = true;
        }
        if( this.updateConfig( this.sticksConfig, this.languagePrefix + "boostSticks.yml" ) ) {
            try {
                this.sticksConfig.save( this.sticksConfigFile );
            } catch( IOException e ) {
                plugin.getLogger().severe( "Could not save to file '" + this.sticksConfigFile.getPath() + "'" );
            }
            somethingWasAdded = true;
        }
        if( this.updateConfig( this.guiConfig, this.languagePrefix + "gui.yml" ) ) {
            try {
                this.guiConfig.save( this.guiConfigFile );
            } catch( IOException e ) {
                plugin.getLogger().severe( "Could not save to file '" + this.guiConfigFile.getPath() + "'" );
            }
            somethingWasAdded = true;
        }
        plugin.getLogger().info( somethingWasAdded ? "Configuration files updated to contain latest content." : "Configuration files already up to date." );
        return somethingWasAdded;
    }

    private void loadMessagesConfig()
    {
        final String messagesConfigFileName = this.languagePrefix + "messages.yml";
        this.messagesConfigFile = new File( plugin.getDataFolder(), messagesConfigFileName );
        if( !this.messagesConfigFile.exists() ) {
            try {
                plugin.saveResource( messagesConfigFileName, false );
            } catch( IllegalArgumentException e ) {
                plugin.getLogger().severe( "There is no resource named " + messagesConfigFileName + " to create in the plugins folder. " + ( !this.languagePrefix.isEmpty() ? "Check the 'language' setting in the config." : "" ) );
            }
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration( this.messagesConfigFile );
        InputStream defConfigStream = plugin.getResource( messagesConfigFileName );
        if( defConfigStream != null ) {
            this.messagesConfig.setDefaults( YamlConfiguration.loadConfiguration( new InputStreamReader( defConfigStream, StandardCharsets.UTF_8 ) ) );
        }

        // Check for previous signs in config.yml. These will now live in messages.yml
        Object oldSignsConfigSection = plugin.getConfig().get( "signs", null );
        if( oldSignsConfigSection != null ) {
            this.messagesConfig.set( "signs", oldSignsConfigSection );
            this.saveMessagesConfig();
            plugin.getConfig().set( "signs", null );
            plugin.saveConfig();
        }

        // Check for previous structure with messages at root level. These are now under "messages".
        if( this.messagesConfig.contains( "title" ) ) {
            ConfigurationSection oldMessagesConfigSection = new MemoryConfiguration();
            for( String messageKey : this.messagesConfig.getKeys( false ) ) {
                if( !messageKey.equals( "player_stats" ) && !messageKey.equals( "command_usage_user" ) && !messageKey.equals( "command_usage_admin" ) && !messageKey.equals( "instructions" ) && !messageKey.equals( "signs" ) ) {
                    oldMessagesConfigSection.set( messageKey, this.messagesConfig.get( messageKey ) );
                    this.messagesConfig.set( messageKey, null );
                }
                this.messagesConfig.set( "messages", oldMessagesConfigSection );
            }
            this.saveMessagesConfig();
        }
    }

    private void saveMessagesConfig()
    {
        try {
            this.messagesConfig.save( this.messagesConfigFile );
        } catch (IOException var2) {
            plugin.getLogger().severe( "Could not save to file '" + this.messagesConfigFile.getPath() + "'" );
        }
    }

    private void loadSticksConfig()
    {
        this.sticksConfig = null;
        final String sticksConfigFileName = this.languagePrefix + "boostSticks.yml";
        this.sticksConfigFile = new File( plugin.getDataFolder(), sticksConfigFileName );
        if( !this.sticksConfigFile.exists() ) {
            // Check for previous boost_sticks in config.yml
            Object oldBoostSticksConfigSection = plugin.getConfig().get( "boost_sticks", null );
            if( oldBoostSticksConfigSection != null ) {
                this.sticksConfig = new YamlConfiguration();
                this.sticksConfig.set( "boost_sticks", oldBoostSticksConfigSection );
                this.saveSticksConfig();
                plugin.getConfig().set( "boost_sticks", null );
                plugin.saveConfig();
            } else {
                try {
                    plugin.saveResource( sticksConfigFileName, false );
                } catch( IllegalArgumentException e ) {
                    plugin.getLogger().severe( "There is no resource named " + sticksConfigFileName + " to create in the plugins folder. " + ( !this.languagePrefix.isEmpty() ? "Check the 'language' setting in the config." : "" ) );
                }
            }
        }
        if( this.sticksConfig == null ) this.sticksConfig = YamlConfiguration.loadConfiguration( this.sticksConfigFile );
        InputStream defConfigStream = plugin.getResource( sticksConfigFileName );
        if( defConfigStream != null ) {
            this.sticksConfig.setDefaults( YamlConfiguration.loadConfiguration( new InputStreamReader( defConfigStream, StandardCharsets.UTF_8 ) ) );
        }
    }

    private void saveSticksConfig()
    {
        try {
            this.sticksConfig.save( this.sticksConfigFile );
        } catch (IOException var2) {
            plugin.getLogger().severe( "Could not save to file '" + this.sticksConfigFile.getPath() + "'" );
        }
    }

    private void loadGuiConfig()
    {
        final String guiConfigFileName = this.languagePrefix + "gui.yml";
        this.guiConfigFile = new File( plugin.getDataFolder(), guiConfigFileName );
        if( !this.guiConfigFile.exists() ) {
            try {
                plugin.saveResource( guiConfigFileName, false );
            } catch( IllegalArgumentException e ) {
                plugin.getLogger().severe( "There is no resource named " + guiConfigFileName + " to create in the plugins folder. " + ( !this.languagePrefix.isEmpty() ? "Check the 'language' setting in the config." : "" ) );
            }
        }
        this.guiConfig = YamlConfiguration.loadConfiguration( this.guiConfigFile );
        InputStream defConfigStream = plugin.getResource( guiConfigFileName );
        if( defConfigStream != null ) {
            this.guiConfig.setDefaults( YamlConfiguration.loadConfiguration( new InputStreamReader( defConfigStream, StandardCharsets.UTF_8 ) ) );
        }
    }

    public void saveGuiConfig()
    {
        try {
            this.guiConfig.save( this.guiConfigFile );
        } catch (IOException var2) {
            plugin.getLogger().severe( "Could not save to file '" + this.guiConfigFile.getPath() + "'" );
        }
    }

    private void loadStatsResetConfig()
    {
        this.statsResetConfigFile = new File( plugin.getDataFolder(), "statsReset.yml" );
        if( !this.statsResetConfigFile.exists() ) {
            try {
                this.statsResetConfigFile.createNewFile();
            } catch( IOException e ) {
                plugin.getLogger().log( Level.SEVERE, "Failed to create file \'" + this.statsResetConfigFile.getPath() + "\'", e );
            }
        }
        this.statsResetConfig = YamlConfiguration.loadConfiguration( this.statsResetConfigFile );
    }

    private boolean updateConfig( ConfigurationSection config, String configFileName )
    {
        boolean somethingWasAdded = false;
        InputStream defConfigStream = plugin.getResource( configFileName );
        if( defConfigStream != null ) {
            ConfigurationSection defConfig = YamlConfiguration.loadConfiguration( new InputStreamReader( defConfigStream, StandardCharsets.UTF_8 ) );
            for( String key : defConfig.getKeys( true ) ) {
                if( !config.contains( key, true ) ) {
                    config.set( key, defConfig.get( key ) );
                    somethingWasAdded = true;
                }
            }
        }
        return somethingWasAdded;
    }

    private <T extends Enum<T>> T tryGetEnumValueFromConfig( Class<T> enumType, String path, String def )
    {
        try {
            String value = plugin.getConfig().getString( path, def );
            if( value == null || value.equals( "" ) ) return null;
            return T.valueOf( enumType, value.toUpperCase() );
        } catch( IllegalArgumentException e ) {
            plugin.getLogger().warning( "Enum " + enumType.getName() + " " + plugin.getConfig().getString( path, def ) + " not recognised." );
            return null;
        }
    }

    private void load()
    {
        // See https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes for preferred codes.
        String languagePrefixSetting = plugin.getConfig().getString( "language" );
        languagePrefix = ( languagePrefixSetting == null || languagePrefixSetting.equalsIgnoreCase( "en" ) ) ? "" : ( languagePrefixSetting + "_" );
        languagePrefixes = plugin.getConfig().getStringList( "languages" );

        this.loadMessagesConfig();
        this.loadSticksConfig();
        this.loadGuiConfig();
        this.loadStatsResetConfig();

        // targetDistance is the radius from the centre block, so a box of 3 blocks is (3-1)/2 = 1 block bigger than the main block. 1 block is (1-1)/2 = 0 blocks bigger, or just the main block.
        targetBoxH = ( plugin.getConfig().getInt( "physics.target_box_width", 3 ) - 1 ) / 2;
        targetBoxV = plugin.getConfig().getInt( "physics.target_box_height", 1 );
        vertical_velocity = plugin.getConfig().getDouble( "physics.vertical_velocity", 2.0 );
        max_horizontal_velocity = plugin.getConfig().getDouble( "physics.max_horizontal_velocity", 2.5 );
        min_horizontal_velocity = plugin.getConfig().getDouble( "physics.min_horizontal_velocity", 0.1 );
        block_hit_horizontal_velocity = plugin.getConfig().getDouble( "physics.block_hit_horizontal_velocity", 2.5 );
        coolDown = plugin.getConfig().getLong( "physics.cooldown", 500L );
        gameStartBoostDelay = plugin.getConfig().getLong( "physics.game_start_boost_delay", 5L );

        signTitle = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.title", "[Boost]" ) );
        signJoin = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.join", "Click to join" ) );
        signLeave = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.leave", "Click to leave" ) );
        signStatus = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.status", "Click for status" ) );
        signStats = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.stats", "Your stats" ) );
        signTop = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.top", "Leader board" ) );
        signDaily = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.daily", "Daily" ) );
        signWeekly = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.weekly", "Weekly" ) );
        signMonthly = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.monthly", "Monthly" ) );
        signTotal = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "signs.total", "All time" ) );

        mainLobbySpawn = new SpawnLocation( plugin, "main_lobby" );

        worldSoundRange = plugin.getConfig().getInt( "sounds.world_sound_range", 80 );
        tickSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.tick", "ITEM_FLINTANDSTEEL_USE" );
        joinSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.join", "UI_LOOM_SELECT_PATTERN" );
        leaveSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.leave", "UI_LOOM_TAKE_RESULT" );
        startSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.start", "ENTITY_EVOKER_PREPARE_SUMMON" );
        winSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.win", "UI_TOAST_CHALLENGE_COMPLETE" );
        loseSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.lose", "ENTITY_GHAST_HURT" );
        boostSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.boost", "ITEM_TRIDENT_THROW" );
        boostedSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.boosted", "ITEM_TRIDENT_RIPTIDE_3" );
        statsSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.stats", "ENTITY_ENDER_EYE_DEATH" );
        leaderSound = this.tryGetEnumValueFromConfig( Sound.class, "sounds.leader", "ENTITY_ENDER_EYE_DEATH" );

        playingGameMode = GameMode.valueOf( plugin.getConfig().getString( "gamemode.playing", "ADVENTURE" ).toUpperCase() );
        lostGameMode = GameMode.valueOf( plugin.getConfig().getString( "gamemode.lost", "SPECTATOR" ).toUpperCase() );
        lobbyGameMode = GameMode.valueOf( plugin.getConfig().getString( "gamemode.lobby", "ADVENTURE" ).toUpperCase() );
        buildGameMode = GameMode.valueOf( plugin.getConfig().getString( "gamemode.build", "CREATIVE" ).toUpperCase() );

        boostWhileQueuing = plugin.getConfig().getBoolean( "boost_while_queuing", false );
        commandsBlockedWhilePlaying = plugin.getConfig().getBoolean( "other_commands.block_while_playing", true );
        for( String command : plugin.getConfig().getStringList( "other_commands.allowed_commands" ) ) {
            commandsAllowedWhilePlaying.add( command.toLowerCase() );
        }

        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            if( statsPeriod == StatsPeriod.TOTAL ) break;
            EnumMap< Winner, List<String> > periodicWinCommands = new EnumMap<>(Winner.class);
            for( Winner winner : Winner.values() ) {
                periodicWinCommands.put( winner, plugin.getConfig().getStringList( "win_commands." + statsPeriod.toString().toLowerCase() + "." + winner.toString().toLowerCase() ) );
            }
            winCommands.put( statsPeriod, periodicWinCommands );
        }

        gameStartBoostDelayBarColor = this.tryGetEnumValueFromConfig( BarColor.class, "bar.game_start_boost_delay.color", "BLUE" );
        int segments = plugin.getConfig().getInt( "bar.game_start_boost_delay.segments", 0 );
        switch( segments ) {
            case 0: gameStartBoostDelayBarStyle = BarStyle.SOLID; break;
            case 6: gameStartBoostDelayBarStyle = BarStyle.SEGMENTED_6; break;
            case 10: gameStartBoostDelayBarStyle = BarStyle.SEGMENTED_10; break;
            case 12: gameStartBoostDelayBarStyle = BarStyle.SEGMENTED_12; break;
            case 20: gameStartBoostDelayBarStyle = BarStyle.SEGMENTED_20; break;
            default:
                plugin.getLogger().severe( "bar.game_start_boost_delay.segments value may only be one of: 0, 6, 10, 12 or 20. Value " + segments + " not recognised." );
                gameStartBoostDelayBarStyle = BarStyle.SOLID;
                break;
        }

        boostStickRandom = sticksConfig.getBoolean( "boost_sticks.random", true );
        defaultBoostStick = sticksConfig.getString( "boost_sticks.default" );
        giveOnlyBestBoostStick = sticksConfig.getBoolean( "boost_sticks.give_only_best", false );
        ConfigurationSection boostSticksSection = sticksConfig.getConfigurationSection( "boost_sticks.stick_types" );
        if( boostSticksSection != null ) {
            for( String boostStickName : boostSticksSection.getKeys( false ) ) {
                BoostStick boostStick = new BoostStick( plugin, boostStickName.toLowerCase(), sticksConfig );
                boostSticks.add( boostStick );
                boostSticksByName.put( boostStick.getName().toLowerCase(), boostStick );
            }
        }

        guiName = guiConfig.getString( "gui.name", "Boost menu" );
        guiRows = guiConfig.getInt( "gui.rows", 4 );
        ConfigurationSection guiButtonsSection = guiConfig.getConfigurationSection( "gui.buttons" );
        if( guiButtonsSection != null ) {
            for( String guiButtonName : guiButtonsSection.getKeys( false ) ) {
                GUIButtonConfig guiButtonConfig = new GUIButtonConfig( plugin, guiButtonName.toLowerCase(), guiConfig );
                guiButtons.put( guiButtonConfig.getName(), guiButtonConfig );
            }
        }

        ConfigurationSection gamesSection = plugin.getConfig().getConfigurationSection( "games" );
        if( gamesSection != null ) {
            for( String gameName : gamesSection.getKeys( false ) ) {
                GameConfig gameConfig = new GameConfig( plugin, gameName );
                gameConfigList.add( gameConfig );
            }
        }
        for( String world : plugin.getConfig().getStringList( "boost_worlds" ) ) {
            gameWorlds.put( world.toLowerCase(), true );
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

        trackedDay = statsResetConfig.getInt( "day", 0 );
        trackedWeek = statsResetConfig.getInt( "week", 0 );
        trackedMonth = statsResetConfig.getInt( "month", 0 );
    }

    public List<String> getLanguagePrefixes() { return languagePrefixes; }

    public int getTargetBoxH() { return targetBoxH; }
    public int getTargetBoxV() { return targetBoxV; }
    public double getVertical_velocity() { return vertical_velocity; }
    public double getMax_horizontal_velocity() { return max_horizontal_velocity; }
    public double getMin_horizontal_velocity() { return min_horizontal_velocity;}
    public double getBlock_hit_horizontal_velocity() { return block_hit_horizontal_velocity; }

    public long getCoolDown() { return coolDown; }
    public void setCoolDown( long coolDown ) {
        this.coolDown = coolDown;
        plugin.getConfig().set( "physics.cooldown", this.coolDown );
        plugin.saveConfig();
    }

    public long getGameStartBoostDelay() { return gameStartBoostDelay; }
    public void setGameStartBoostDelay( long gameStartBoostDelay ) {
        this.gameStartBoostDelay = gameStartBoostDelay;
        plugin.getConfig().set( "physics.game_start_boost_delay", this.gameStartBoostDelay );
        plugin.saveConfig();
    }

    public String getSignTitle() { return signTitle; }
    public String getSignTitleStripped() { return ChatColor.stripColor( signTitle ); }
    public String getSignJoin() { return signJoin; }
    public String getSignJoinStripped() { return ChatColor.stripColor( signJoin ); }
    public String getSignLeave() { return signLeave; }
    public String getSignLeaveStripped() { return ChatColor.stripColor( signLeave ); }
    public String getSignStatus() { return signStatus; }
    public String getSignStatusStripped() { return ChatColor.stripColor( signStatus ); }
    public String getSignStats() { return signStats; }
    public String getSignStatsStripped() { return ChatColor.stripColor( signStats ); }
    public String getSignTop() { return signTop; }
    public String getSignTopStripped() { return ChatColor.stripColor( signTop ); }
    public String getSignDaily() { return signDaily; }
    public String getSignDailyStripped() { return ChatColor.stripColor( signDaily ); }
    public String getSignWeekly() { return signWeekly; }
    public String getSignWeeklyStripped() { return ChatColor.stripColor( signWeekly ); }
    public String getSignMonthly() { return signMonthly; }
    public String getSignMonthlyStripped() { return ChatColor.stripColor( signMonthly ); }
    public String getSignTotal() { return signTotal; }
    public String getSignTotalStripped() { return ChatColor.stripColor( signTotal ); }

    public Location getMainLobbySpawn() { return mainLobbySpawn.getSpawn(); }
    public void setMainLobbySpawn( Location spawn ) {
        mainLobbySpawn.setSpawn( spawn );
        plugin.saveConfig();
    }

    public int getWorldSoundRange() { return worldSoundRange; }
    public Sound getTickSound() { return tickSound; }
    public Sound getJoinSound() { return joinSound; }
    public Sound getLeaveSound() { return leaveSound; }
    public Sound getStartSound() { return startSound; }
    public Sound getWinSound() { return winSound; }
    public Sound getLoseSound() { return loseSound; }
    public Sound getBoostSound() { return boostSound; }
    public Sound getBoostedSound() { return boostedSound; }
    public Sound getStatsSound() { return statsSound; }
    public Sound getLeaderSound() { return leaderSound; }

    public GameMode getPlayingGameMode() { return playingGameMode; }
    public GameMode getLostGameMode() { return lostGameMode; }
    public GameMode getLobbyGameMode() { return lobbyGameMode; }
    public GameMode getBuildGameMode() { return buildGameMode; }

    public boolean canBoostWhileQueuing() { return boostWhileQueuing; }
    public void setBoostWhileQueuing( boolean boostWhileQueuing ) {
        this.boostWhileQueuing = boostWhileQueuing;
        plugin.getConfig().set( "boost_while_queuing", this.boostWhileQueuing );
        plugin.saveConfig();
    }

    public boolean areCommandsBlockedWhilePlaying() { return commandsBlockedWhilePlaying; }

    public boolean isCommandAllowed( String command ) {
        if( !this.areCommandsBlockedWhilePlaying() ) return true;
        String cmdLower = command.toLowerCase();
        return this.commandsAllowedWhilePlaying.contains( cmdLower ) || cmdLower.equals( "boost" ) || cmdLower.equals( "bst" );
    }

    public void allowCommandWhilePlaying( String command ) {
        String cmdLower = command.toLowerCase();
        if( !this.commandsAllowedWhilePlaying.contains( cmdLower ) ) {
            this.commandsAllowedWhilePlaying.add( cmdLower );
            plugin.getConfig().set( "other_commands.allowed_commands", new ArrayList<>( this.commandsAllowedWhilePlaying ) );
            plugin.saveConfig();
        }
    }

    public void blockCommandWhilePlaying( String command ) {
        String cmdLower = command.toLowerCase();
        if( this.commandsAllowedWhilePlaying.contains( cmdLower ) ) {
            this.commandsAllowedWhilePlaying.remove( cmdLower );
            plugin.getConfig().set( "other_commands.allowed_commands", new ArrayList<>( this.commandsAllowedWhilePlaying ) );
            plugin.saveConfig();
        }
    }

    public boolean isBoostStickRandom() { return boostStickRandom; }
    public boolean isGiveOnlyBestBoostStick() { return giveOnlyBestBoostStick; }

    public List<BoostStick> getBoostSticks() { return boostSticks; }

    public BoostStick getRandomBoostStick() {
        if( boostSticks.size() == 0 ) {
            plugin.getLogger().severe( "boost_sticks.stick_types configuration missing." );
            return null;
        }
        return boostSticks.get( (int)( Math.random() * ( boostSticks.size() ) ) );
    }

    public List<BoostStick> getBoostSticksByPerms( Player player ) {
        List<BoostStick> sticks = new ArrayList<>();
        if( boostSticks.size() == 0 ) {
            plugin.getLogger().severe( "boost_sticks.stick_types configuration missing." );
        } else {
            for( int stick = boostSticks.size() - 1; stick >= 0; --stick ) {
                if( player.hasPermission( "boost.stick." + boostSticks.get( stick ).getName() ) ) {
                    sticks.add( boostSticks.get( stick ) );
                    if( plugin.getLoadedConfig().isGiveOnlyBestBoostStick() ) break;
                }
            }
            if( sticks.isEmpty() ) {
                if( this.getBoostStick( defaultBoostStick ) == null ) {
                    plugin.getLogger().severe( "boost_sticks.default of '" + defaultBoostStick + "' does not match boost_sticks.stick_types.");
                } else {
                    sticks.add( this.getBoostStick( defaultBoostStick ) );
                }
            }
        }
        return sticks;
    }

    public BoostStick getBoostStick( String name ) { return boostSticksByName.get( name.toLowerCase() ); }

    public List<BoostStick> getBoostSticksAllowedForPlayer( Player player ) {
        if( this.isBoostStickRandom() ) {
            List<BoostStick> sticks = new ArrayList<>();
            BoostStick stick = this.getRandomBoostStick();
            if( stick != null ) {
                sticks.add( stick );
            } else {
                sticks.add( new BoostStick( plugin, "Boost stick", null ) );
            }
            return sticks;
        } else {
            List<BoostStick> sticks = this.getBoostSticksByPerms( player );
            if( sticks.isEmpty() ) {
                sticks.add( new BoostStick( plugin, "Boost stick", null ) );
            }
            return sticks;
        }
    }

    public String getGuiName() { return guiName; }
    public int getGuiRows() { return guiRows; }

    public GUIButtonConfig getGuiButtonConfig( String name ) {
        GUIButtonConfig guiButtonConfig = guiButtons.get( name.toLowerCase() );
        if( guiButtonConfig == null ) {
            guiButtonConfig = new GUIButtonConfig( plugin, name.toLowerCase(), null );
            guiButtons.put( name, guiButtonConfig );
        }
        return guiButtonConfig;
    }

    public boolean isGuiButtonEnabled( String name ) {
        return this.getGuiButtonConfig( name ).isEnabled();
    }

    public List<GameConfig> getGameList() { return gameConfigList; }

    public GameConfig createNewGameConfig( String name )
    {
        GameConfig gameConfig = new GameConfig( plugin, name );
        gameConfig.setConfig();
        plugin.saveConfig();
        gameConfigList.add( gameConfig );
        return gameConfig;
    }

    public void deleteGameConfig( GameConfig gameConfig )
    {
        gameConfigList.remove( gameConfig );
        plugin.getConfig().set( "games." + gameConfig.getName(), null );
        plugin.saveConfig();
    }

    public boolean isGameWorld( String world ) {
        return gameWorlds.isEmpty() || gameWorlds.containsKey( world.toLowerCase() );
    }

    public void addGameWorld( String world ) {
        gameWorlds.put( world.toLowerCase(), true );
        plugin.getConfig().set( "boost_worlds", this.getGameWorlds() );
        plugin.saveConfig();
    }

    public void delGameWorld( String world ) {
        gameWorlds.remove( world.toLowerCase() );
        plugin.getConfig().set( "boost_worlds", this.getGameWorlds() );
        plugin.saveConfig();
    }

    public String[] getGameWorlds() {
        return gameWorlds.keySet().toArray(new String[0]);
    }

    public String getMessage( Messages message ) { return messages.get( message ); }
    public String getPrefix() { return messagePrefix; }
    public List<String> getPlayerStatsTemplate() { return playerStatsTemplate; }
    public ItemStack createInstructionBook() { return instructionBook.create(); }
    public InstructionBook getInstructionBookConfig() { return instructionBook; }

    public String[] getCommandUsage( boolean isAdmin )
    {
        List<String> commandUsage = new ArrayList<>( commandUsageUser );
        if( isAdmin ) commandUsage.addAll( commandUsageAdmin );
        return commandUsage.toArray(new String[0]);
    }

    public String[] getCommandUsage( boolean isAdmin, String boostCommand )
    {
        List<String> commandUsage = new ArrayList<>();
        commandUsage.add( commandUsageUser.get(0) );
        for( String command : commandUsageUser ) {
            try {
                if( command.split( " ", 3 )[1].equalsIgnoreCase( boostCommand ) ) {
                    commandUsage.add( command );
                }
            } catch( IndexOutOfBoundsException ignored ) {}
        }
        if( isAdmin ) {
            for( String command : commandUsageAdmin ) {
                try {
                    if( command.split( " ", 3 )[1].equalsIgnoreCase( boostCommand ) ) {
                        commandUsage.add( command );
                    }
                } catch( IndexOutOfBoundsException ignored ) {}
            }
        }
        return commandUsage.size() > 1 ? commandUsage.toArray(new String[0]) : getCommandUsage( isAdmin );
    }

    public int getPeriodTracker( StatsPeriod statsPeriod )
    {
        switch( statsPeriod ) {
            case DAILY: return trackedDay;
            case WEEKLY: return trackedWeek;
            case MONTHLY: return trackedMonth;
        }
        return 0;
    }

    public void setPeriodTracker( StatsPeriod statsPeriod, int periodCounter )
    {
        switch( statsPeriod ) {
            case DAILY:  trackedDay = periodCounter; break;
            case WEEKLY: trackedWeek = periodCounter; break;
            case MONTHLY: trackedMonth = periodCounter; break;
        }
        statsResetConfig.set( statsPeriod.getConfigNode(), periodCounter );
        try {
            statsResetConfig.save( statsResetConfigFile );
        } catch( IOException e ) {
            plugin.getLogger().log( Level.SEVERE, "Could not save config to \'" + statsResetConfigFile.getPath() + "\'", e );
        }
    }

    public List<String> getWinCommands( StatsPeriod statsPeriod, Winner winner ) {
        return winCommands.get( statsPeriod ).get( winner );
    }

    public void runWinCommands( UUID playerUuid, String gameName, List<String> winCommands )
    {
        if( winCommands == null || winCommands.isEmpty() ) return;
        String playerName = plugin.getServer().getOfflinePlayer( playerUuid ).getName();
        for( String commandTemplate : winCommands ) {
            if( playerName == null && commandTemplate.contains( "%player%" ) ) {
                plugin.getLogger().warning( "Offline player '" + playerUuid.toString() + "' missing name for command '" + commandTemplate + "'." );
            } else {
                String command = commandTemplate
                        .replace( "%player%", playerName != null ? playerName : "" )
                        .replace( "%uuid%", playerUuid.toString() )
                        .replace( "%game%", gameName != null ? gameName : "" );
                try {
                    plugin.getServer().dispatchCommand( plugin.getServer().getConsoleSender(), command );
                } catch( CommandException e ) {
                    plugin.getLogger().severe( "Error in win command: '" + command + "'." );
                    break;
                }
            }
        }
    }

    public void runPeriodicWinCommandsForTop3( StatsPeriod statsPeriod, List<PlayerStats> top3 )
    {
        for( Winner winner : Winner.values() ) {
            try {
                UUID uuid = top3.get( winner.getTop3Listing() ).getUuid();
                if( uuid == null ) break;
                this.runWinCommands( uuid, null, this.getWinCommands( statsPeriod, winner ) );
            } catch( IndexOutOfBoundsException e ) {
                break;
            }
        }
    }

    public void addWinCommand( StatsPeriod statsPeriod, Winner winner, String winCommand )
    {
        EnumMap< Winner, List<String> > periodicWinCommands = winCommands.get( statsPeriod );
        List<String> periodicWinCommandForWinner = periodicWinCommands.get( winner );
        periodicWinCommandForWinner.add( winCommand );
        periodicWinCommands.put( winner, periodicWinCommandForWinner );
        winCommands.put( statsPeriod, periodicWinCommands );
        plugin.getConfig().set( "win_commands." + statsPeriod.toString().toLowerCase() + "." + winner.toString().toLowerCase(), periodicWinCommandForWinner );
        plugin.saveConfig();
    }

    public void removeWinCommand( StatsPeriod statsPeriod, Winner winner, int index ) throws IndexOutOfBoundsException
    {
        EnumMap< Winner, List<String> > periodicWinCommands = winCommands.get( statsPeriod );
        List<String> periodicWinCommandForWinner = periodicWinCommands.get( winner );
        periodicWinCommandForWinner.remove( index );
        periodicWinCommands.put( winner, periodicWinCommandForWinner );
        winCommands.put( statsPeriod, periodicWinCommands );
        plugin.getConfig().set( "win_commands." + statsPeriod.toString().toLowerCase() + "." + winner.toString().toLowerCase(), periodicWinCommandForWinner );
        plugin.saveConfig();
    }

    public BarColor getGameStartBoostDelayBarColor() { return this.gameStartBoostDelayBarColor; }
    public BarStyle getGameStartBoostDelayBarStyle() { return this.gameStartBoostDelayBarStyle; }
}
