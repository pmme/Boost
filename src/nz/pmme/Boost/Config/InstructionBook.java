package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class InstructionBook
{
    private Main plugin;
    private boolean enabled;
    private String instructionsTitle;
    private String instructionsAuthor;
    private List<String> instructionsPages = new ArrayList<>();

    InstructionBook( Main plugin, ConfigurationSection messagesConfig )
    {
        this.plugin = plugin;

        this.enabled = messagesConfig.getBoolean( "instructions.enabled", true );

        this.instructionsTitle = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "instructions.title", "Boost Instructions" ) );
        this.instructionsAuthor = ChatColor.translateAlternateColorCodes( '&', messagesConfig.getString( "instructions.author", "Boost" ) );

        for( int pageNum = 1; ; ++pageNum )
        {
            List<String> pageText = messagesConfig.getStringList( "instructions.page" + String.valueOf( pageNum ) );
            if( pageText.isEmpty() ) break;
            StringBuilder formattedPageText = new StringBuilder(64);
            for( String line : pageText ) {
                formattedPageText.append( ChatColor.translateAlternateColorCodes( '&', line ) ).append( '\n' );
            }
            instructionsPages.add( formattedPageText.toString() );
        }
    }

    public boolean isEnabled() { return this.enabled; }

    public ItemStack create()
    {
        ItemStack book = new ItemStack( Material.WRITTEN_BOOK, 1 );
        BookMeta bookMeta = (BookMeta)book.getItemMeta();
        bookMeta.setTitle( this.instructionsTitle );
        bookMeta.setAuthor( this.instructionsAuthor );
        bookMeta.setPages( this.instructionsPages );
        book.setItemMeta( bookMeta );
        return book;
    }

    public boolean isInstructionBook( ItemStack item )
    {
        if( item != null && item.getType() == Material.WRITTEN_BOOK ) {
            BookMeta bookMeta = (BookMeta)item.getItemMeta();
            if( bookMeta == null ) return false;
            String title = bookMeta.getTitle();
            if( title == null ) return false;
            return title.equals( this.instructionsTitle );
        }
        return false;
    }
}
