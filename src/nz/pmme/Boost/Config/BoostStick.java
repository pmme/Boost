package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BoostStick
{
    private Main plugin;
    private String name;
    private String displayName;
    private Material material;
    private List<String> lore = new ArrayList<>();

    private BoostParticle boostParticle;
    private BoostParticle boostHitParticle;
    private BoostParticle boostedParticle;

    public BoostStick( Main plugin, String name, ConfigurationSection boostSticksConfig )
    {
        this.plugin = plugin;
        this.name = name;

        if( boostSticksConfig != null )
        {
            String configPath = "boost_sticks.stick_types." + this.name + ".";

            this.displayName = ChatColor.translateAlternateColorCodes( '&', boostSticksConfig.getString( configPath + "name", this.name ) );
            String item = boostSticksConfig.getString( configPath + "item", "DIAMOND_HOE" ).toUpperCase();
            this.material = Material.getMaterial( item );
            if( this.material == null ) {
                this.material = Material.DIAMOND_HOE;
                plugin.getLogger().warning( "Material " + item + " for boost stick " + this.name + " is invalid." );
            }
            for( String string : boostSticksConfig.getStringList( configPath + "lore" ) ) {
                lore.add( ChatColor.translateAlternateColorCodes( '&', string ) );
            }

            this.boostParticle = new BoostParticle( this.plugin, boostSticksConfig, configPath + "particles.boost" );
            this.boostHitParticle = new BoostParticle( this.plugin, boostSticksConfig, configPath + "particles.hit" );
            this.boostedParticle = new BoostParticle( this.plugin, boostSticksConfig, configPath + "particles.boosted" );
        }
        else
        {
            this.displayName = this.name;
            this.material = Material.DIAMOND_HOE;

            this.boostParticle = null;
            this.boostHitParticle = null;
            this.boostedParticle = null;
        }
    }

    public ItemStack create()
    {
        ItemStack boostStick = new ItemStack( this.material );
        ItemMeta boostStickItemMeta = boostStick.getItemMeta();
        if( boostStickItemMeta != null ) {
            boostStickItemMeta.setDisplayName( this.displayName );
            boostStickItemMeta.setLore( this.lore );
            boostStick.setItemMeta( boostStickItemMeta );
        }
        return boostStick;
    }

    public String getName() { return this.name; }

    public boolean isBoostStick( ItemStack item )
    {
        if( item != null && item.getType() == this.material ) {
            ItemMeta itemMeta = item.getItemMeta();
            if( itemMeta != null && itemMeta.getDisplayName().equals( this.displayName ) ) return true;
        }
        return false;
    }

    public BoostParticle getBoostParticle() { return boostParticle; }
    public BoostParticle getBoostHitParticle() { return boostHitParticle; }
    public BoostParticle getBoostedParticle() { return boostedParticle; }
}
