package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandStatus extends AbstractSubCommand
{
    private static final String[] permissions = { "boost.cmd", "boost.status" };

    public SubCommandStatus( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        plugin.getGameManager().displayStatus( sender );
        return true;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
