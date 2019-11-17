package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    private String configPath;

    public BoostStick( Main plugin, String name )
    {
        this.plugin = plugin;
        this.name = name;

        this.configPath = "boost_sticks.stick_types." + this.name + ".";

        this.displayName = ChatColor.translateAlternateColorCodes( '&', plugin.getConfig().getString( this.configPath + "name", this.name ) );
        String item = plugin.getConfig().getString( this.configPath + "item", "DIAMOND_HOE" ).toUpperCase();
        this.material = Material.getMaterial( item );
        if( this.material == null ) {
            this.material = Material.DIAMOND_HOE;
            plugin.getLogger().severe( "Material for boost stick " + this.name + " is invalid." );
        }
        for( String string : plugin.getConfig().getStringList( this.configPath + "lore" ) ) {
            lore.add( ChatColor.translateAlternateColorCodes( '&', string ) );
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
}
