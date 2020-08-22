package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubCommand implements SubCommand
{
    protected final Main plugin;
    protected final String subCommand;
    protected final List<String> subCommandAsList = new ArrayList<>();
    protected static String[] adminPermission = { "boost.admin" };

    public AbstractSubCommand( final Main plugin, String subCommand ) {
        this.plugin = plugin;
        this.subCommand = subCommand.toLowerCase();
        this.subCommandAsList.add( this.subCommand );
    }

    @Override
    public String getSubCommandString() {
        return this.subCommand;
    }

    protected abstract String[] getRequiredPermissions();
    protected abstract boolean executeSubCommand( CommandSender sender, String[] args );
    protected abstract List<String> tabCompleteArgs( CommandSender sender, String[] args );

    @Override
    public boolean onSubCommand( CommandSender sender, String[] args ) {
        for( String permission : this.getRequiredPermissions() ) {
            if( !this.plugin.checkPermission( sender, permission, Messages.NO_PERMISSION_CMD ) ) return true;
        }
        return this.executeSubCommand( sender, args );
    }

    @Override
    public List<String> tabComplete( CommandSender sender, String[] args ) {
        for( String permission : this.getRequiredPermissions() ) {
            if( !sender.hasPermission( permission ) ) return null;
        }
        if( args.length == 1 ) return this.subCommandAsList;
        if( !this.subCommand.equals( args[0] ) ) return null;
        return this.tabCompleteArgs( sender, args );
    }
}
