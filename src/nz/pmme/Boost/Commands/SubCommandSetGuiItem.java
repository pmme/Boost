package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandSetGuiItem extends AbstractSubCommand
{
    public SubCommandSetGuiItem( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game == null ) {
                plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                return true;
            }
            if( sender instanceof Player ) {
                Material material = ((Player)sender).getInventory().getItemInMainHand().getType();
                game.getGameConfig().setGuiItem( material );
                if( game.getGameConfig().getGuiItem() != null ) {
                    plugin.messageSender( sender, Messages.GUI_ITEM_SET, game.getGameConfig().getDisplayName(), "%item%", game.getGameConfig().getGuiItem().toString() );
                } else {
                    plugin.messageSender( sender, Messages.GUI_ITEM_DISABLED, game.getGameConfig().getDisplayName() );
                }
            } else {
                plugin.messageSender( sender, Messages.NO_CONSOLE );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getGameManager().getGameNames();
        return null;
    }
}
