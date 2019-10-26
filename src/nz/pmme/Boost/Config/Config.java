package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class Config
{
    private Main plugin;

    private int targetBoxH;
    private int targetBoxV;
    private double vertical_velocity;
    private double max_horizontal_velocity;
    private double min_horizontal_velocity;
    private double block_hit_horizontal_velocity;

    private String signTitle;
    private String signJoin;
    private String signLeave;

    private List<GameConfig> gameConfigList = new ArrayList<>();

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
        this.gameConfigList.clear();
        this.load();
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

        ConfigurationSection gamesSection = plugin.getConfig().getConfigurationSection( "games" );
        if( gamesSection != null ) {
            for( String gameName : gamesSection.getKeys( false ) )
            {
                GameConfig gameConfig = new GameConfig( plugin, gameName );
                gameConfigList.add( gameConfig );
            }
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

    public List<GameConfig> getGameList() { return gameConfigList; }

    public GameConfig createNewGameConfig( String name )
    {
        GameConfig gameConfig = new GameConfig( plugin, name );
        gameConfig.setConfig();
        plugin.saveConfig();
        gameConfigList.add( gameConfig );
        return gameConfig;
    }
}
