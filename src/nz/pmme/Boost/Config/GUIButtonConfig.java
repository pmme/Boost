package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIButtonConfig
{
    private Main plugin;
    private String name;
    private boolean enabled;
    private String displayName;
    private Material material;
    private List<String> lore = new ArrayList<>();

    public GUIButtonConfig( Main plugin, String name, FileConfiguration guiConfig )
    {
        this.name = name;
        final String configPath = "gui.buttons." + this.name + ".";
        if( guiConfig != null )
        {
            this.enabled = guiConfig.getBoolean( configPath + "enabled", true );
            this.displayName = ChatColor.translateAlternateColorCodes( '&', guiConfig.getString( configPath + "name", this.name ) );
            String item = guiConfig.getString( configPath + "item", "COBBLESTONE" ).toUpperCase();
            this.material = Material.getMaterial( item );
            if( this.material == null ) {
                this.material = Material.COBBLESTONE;
                plugin.getLogger().severe( "Material " + item + " for gui item " + this.name + " is invalid." );
            }
            for( String string : guiConfig.getStringList( configPath + "lore" ) ) {
                this.lore.add( ChatColor.translateAlternateColorCodes( '&', string ) );
            }
        }
        else
        {
            this.enabled = false;
            this.displayName = name;
            this.material = Material.COBBLESTONE;
            this.lore.add( "Error - missing configuration " + configPath );
        }
    }

    public String getName() { return this.name; }

    public boolean isEnabled() { return this.enabled; }

    public ItemStack create()
    {
        ItemStack item = new ItemStack( this.material );
        ItemMeta itemMeta = item.getItemMeta();
        if( itemMeta != null ) {
            itemMeta.setDisplayName( this.displayName );
            itemMeta.setLore( this.lore );
            item.setItemMeta( itemMeta );
        }
        return item;
    }

    public ItemStack create( String placeHolder, String gameDisplayName, Material guiItemOverride )
    {
        String displayName = this.displayName.replaceAll( placeHolder, gameDisplayName );
        ItemStack item = new ItemStack( guiItemOverride != null ? guiItemOverride : this.material );
        ItemMeta itemMeta = item.getItemMeta();
        if( itemMeta != null ) {
            itemMeta.setDisplayName( displayName );
            itemMeta.setLore( this.lore );
            item.setItemMeta( itemMeta );
        }
        return item;
    }

    public Material getMaterial() { return this.material; }
}
