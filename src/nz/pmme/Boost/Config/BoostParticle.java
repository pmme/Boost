package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class BoostParticle
{
    private Main plugin;
    private Particle particle = null;
    private Object blockData = null;
    private int number = 1;
    private double spacing = 1.0;

    public BoostParticle( Main plugin, ConfigurationSection config, String configPath, String def )
    {
        this.plugin = plugin;
        String particleName = config.getString( configPath + ".particle", def );
        try {
            this.particle = ( particleName == null || particleName.equals( "" ) ) ? null : Particle.valueOf( particleName.toUpperCase() );
        } catch( IllegalArgumentException e ) {
            plugin.getLogger().warning( "Particle " + config.getString( configPath + ".particle", def ) + " not recognised." );
        }
        if( this.particle != null ) {
            if( this.particle.getDataType().getName().equals( "BlockData" ) ) {
                try {
                    this.blockData = plugin.getServer().createBlockData( Material.valueOf( config.getString( configPath + ".type" ).toUpperCase() ) );
                } catch( IllegalArgumentException e ) {
                    plugin.getLogger().warning( "Particle material type " + config.getString( configPath + ".type" ) + " not recognised." );
                }
            } else if( this.particle.getDataType().getName().equals( "MaterialData" ) ) {
                try {
                    Material material = Material.valueOf( config.getString( configPath + ".type" ).toUpperCase() );
                    this.blockData = material.getData();
                } catch( IllegalArgumentException | NullPointerException e ) {
                    plugin.getLogger().warning( "Particle material type " + config.getString( configPath + ".type" ) + " not recognised." );
                }
            }
            this.number = config.getInt( configPath + ".number", 1 );
            this.spacing = config.getDouble( configPath + ".spacing", 1.0 );
        }
    }

    public boolean isEnabled() { return this.particle != null; }
    public double getSpacing() { return this.spacing; }

    public void spawn( World world, Location location )
    {
        if( !this.isEnabled() ) return;
        if( this.blockData == null ) {
            world.spawnParticle( this.particle, location, this.number );
        } else {
            world.spawnParticle( this.particle, location, this.number, this.blockData );
        }
    }

    public void spawn( World world, double x, double y, double z )
    {
        if( !this.isEnabled() ) return;
        if( this.blockData == null ) {
            world.spawnParticle( this.particle, x, y, z, this.number );
        } else {
            world.spawnParticle( this.particle, x, y, z, this.number, this.blockData );
        }
    }
}
