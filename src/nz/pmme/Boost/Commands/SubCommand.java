package nz.pmme.Boost.Commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand
{
    String getSubCommandString();
    boolean onSubCommand( CommandSender sender, String[] args );
    List<String> tabComplete( CommandSender sender, String[] args );
}
