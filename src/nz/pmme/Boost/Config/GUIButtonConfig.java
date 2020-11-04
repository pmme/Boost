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
    private FileConfiguration guiConfig;
    private String name;
    private boolean enabled;
    private String displayName;
    private Material material;
    private List<String> lore = new ArrayList<>();
    private int row;
    private int col;

    public GUIButtonConfig( Main plugin, String name, FileConfiguration guiConfig )
    {
        this.plugin = plugin;
        this.name = name;
        this.guiConfig = guiConfig;
        final String configPath = "gui.buttons." + this.name + ".";
        if( guiConfig != null )
        {
            this.enabled = guiConfig.getBoolean( configPath + "enabled", true );
            this.displayName = guiConfig.getString( configPath + "name", this.name );
            String item = guiConfig.getString( configPath + "item", "COBBLESTONE" ).toUpperCase();
            this.material = Material.getMaterial( item );
            if( this.material == null ) {
                this.material = Material.COBBLESTONE;
                plugin.getLogger().severe( "Material " + item + " for gui item " + this.name + " is invalid." );
            }
            this.lore.addAll( guiConfig.getStringList( configPath + "lore" ) );
            this.row = guiConfig.getInt( configPath + "row", 0 );
            this.col = guiConfig.getInt( configPath + "col", 0 );
        }
        else
        {
            this.enabled = false;
            this.displayName = name;
            this.material = Material.COBBLESTONE;
            this.lore.add( "Error - missing configuration " + configPath );
            this.row = 0;
            this.col = 0;
        }
        if( this.row == 0 ) {
            switch( this.name ) {
                case "join":
                case "leave":
                    this.row = 2;
                    break;
                case "stats":
                case "top_total":
                case "top_monthly":
                case "top_weekly":
                case "top_daily":
                    this.row = 4;
                    break;
            }
        }
        if( this.col == 0 ) {
            switch( this.name ) {
                case "join":
                case "leave":
                    this.col = 5;
                    break;
                case "stats":
                    this.col = 1;
                    break;
                case "top_total":
                    this.col = 9;
                    break;
                case "top_monthly":
                    this.col = 8;
                    break;
                case "top_weekly":
                    this.col = 7;
                    break;
                case "top_daily":
                    this.col = 6;
                    break;
            }
        }
    }

    public void setConfig()
    {
        if( this.guiConfig == null ) return;
        final String configPath = "gui.buttons." + this.name + ".";
        this.guiConfig.set( configPath + "enabled", this.enabled );
        this.guiConfig.set( configPath + "item", this.material.toString() );
        this.guiConfig.set( configPath + "name", this.displayName );
        this.guiConfig.set( configPath + "lore", this.lore );
        if( !this.name.equals( "main" ) && !this.name.equals( "fill" ) ) {
            this.guiConfig.set( configPath + "row", this.row );
            this.guiConfig.set( configPath + "col", this.col );
        }
    }

    public String getName() { return this.name; }

    public boolean isEnabled() { return this.enabled; }

    public ItemStack create()
    {
        return this.create( null, null, null );
    }

    public ItemStack create( String placeHolder, String gameDisplayName, Material guiItemOverride )
    {
        String displayName = placeHolder != null && gameDisplayName != null ? this.displayName.replaceAll( placeHolder, gameDisplayName ) : this.displayName;
        ItemStack item = new ItemStack( guiItemOverride != null ? guiItemOverride : this.material );
        ItemMeta itemMeta = item.getItemMeta();
        if( itemMeta != null ) {
            itemMeta.setDisplayName( !displayName.isEmpty() ? ChatColor.translateAlternateColorCodes( '&', displayName ) : " " );
            if( !this.lore.isEmpty() ) {
                List<String> translatedLore = new ArrayList<>();
                for( String string : this.lore ) {
                    translatedLore.add( ChatColor.translateAlternateColorCodes( '&', string ) );
                }
                itemMeta.setLore( translatedLore );
            }
            item.setItemMeta( itemMeta );
        }
        return item;
    }

    public void setMaterial( Material material ) {
        this.material = material != Material.AIR ? material : null;
        this.setConfig();
        this.plugin.getLoadedConfig().saveGuiConfig();
    }

    public Material getMaterial() { return this.material; }
    public int getRow() { return this.row; }
    public int getCol() { return this.col; }

    public boolean isGUIItem( ItemStack item )
    {
        if( item != null && item.getType() == this.material ) return true;
        return false;
    }
}
