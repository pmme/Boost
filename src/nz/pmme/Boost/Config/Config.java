package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;

public class Config
{
    private Main plugin;

    private int targetDistanceH;
    private int targetDistanceV;
    private double vertical_velocity;
    private double max_horizontal_velocity;
    private double min_horizontal_velocity;
    private double block_hit_horizontal_velocity;

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
        this.load();
    }

    private void load()
    {
        // targetDistance is the radius from the centre block, so a box of 3 blocks is (3-1)/2 = 1 block bigger than the main block. 1 block is (1-1)/2 = 0 blocks bigger, or just the main block.
        targetDistanceH = ( plugin.getConfig().getInt( "physics.target_box_width" ) - 1 ) / 2;
        targetDistanceV = plugin.getConfig().getInt( "physics.target_box_height" );
        vertical_velocity = plugin.getConfig().getDouble( "physics.vertical_velocity" );
        max_horizontal_velocity = plugin.getConfig().getDouble( "physics.max_horizontal_velocity" );
        min_horizontal_velocity = plugin.getConfig().getDouble( "physics.min_horizontal_velocity" );
        block_hit_horizontal_velocity = plugin.getConfig().getDouble( "physics.block_hit_horizontal_velocity" );
    }

    public int getTargetDistanceH() { return targetDistanceH; }
    public int getTargetDistanceV() { return targetDistanceV; }
    public double getVertical_velocity() { return vertical_velocity; }
    public double getMax_horizontal_velocity() { return max_horizontal_velocity; }
    public double getMin_horizontal_velocity() { return min_horizontal_velocity;}
    public double getBlock_hit_horizontal_velocity() { return block_hit_horizontal_velocity; }
}
