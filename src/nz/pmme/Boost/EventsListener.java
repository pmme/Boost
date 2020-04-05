package nz.pmme.Boost;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Utils.VectorToOtherPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;


/**
 * Created by paul on 19-Apr-16.
 */
public class EventsListener implements Listener
{
    private static final Vector VECTOR_UP = new Vector( 0, 1, 0 );
    private Main plugin;

    public EventsListener(Main plugin) {
        this.plugin = plugin;
    }

    private boolean inTargetBox( World targetWorld, Vector targetPosition, Location playerPosition )
    {
        if( !targetWorld.equals( playerPosition.getWorld() ) ) return false;
        int yDelta = playerPosition.getBlockY() - targetPosition.getBlockY();
        if(     Math.abs( playerPosition.getBlockX() - targetPosition.getBlockX() ) <= plugin.getLoadedConfig().getTargetBoxH()
            &&  yDelta >= 0 && yDelta <= plugin.getLoadedConfig().getTargetBoxV()
            &&  Math.abs( playerPosition.getBlockZ() - targetPosition.getBlockZ() ) <= plugin.getLoadedConfig().getTargetBoxH() )
        {
            return true;
        }
        return false;
    }

    private boolean hasBlockUnder( Location playerPosition )
    {
        for( int y = playerPosition.getBlockY(); y >= playerPosition.getBlockY() - plugin.getLoadedConfig().getTargetBoxV(); --y ) {
            for( int x = playerPosition.getBlockX() - 1; x <= playerPosition.getBlockX() + 1; ++x ) {
                for( int z = playerPosition.getBlockZ() - 1; z <= playerPosition.getBlockZ() + 1; ++z ) {
                    Block block = playerPosition.getWorld().getBlockAt( x, y, z );
                    Block blockUnder = playerPosition.getWorld().getBlockAt( x, y - 1, z );
                    if( block.isEmpty() && !blockUnder.isEmpty() ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract( PlayerInteractEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) )
        {
            final Player thisPlayer = event.getPlayer();

            if( event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK )
            {
                if( event.getClickedBlock().getState() instanceof Sign )
                {
                    Sign sign = (Sign)event.getClickedBlock().getState();
                    String[] lines = sign.getLines();
                    if( lines.length > 1 && ChatColor.stripColor( lines[0] ).equalsIgnoreCase( plugin.getLoadedConfig().getSignTitle() ) )
                    {
                        String strippedLine1 = ChatColor.stripColor( lines[1] );
                        if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignJoin() ) && lines.length >= 3 ) {
                            if( !plugin.hasPermission( thisPlayer, "boost.join", Messages.NO_PERMISSION_USE ) ) return;
                            plugin.getGameManager().joinGame( thisPlayer, ChatColor.stripColor( lines[2] ) );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignLeave() ) ) {
                            plugin.getGameManager().leaveGame( thisPlayer );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignStatus() ) ) {
                            if( !plugin.hasPermission( thisPlayer, "boost.status", Messages.NO_PERMISSION_USE ) ) return;
                            plugin.getGameManager().displayStatus( thisPlayer );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignStats() ) ) {
                            if( lines.length > 2 ) {

                            }
                            plugin.getGameManager().displayPlayerStats( thisPlayer, thisPlayer );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignTop() ) ) {
                            if( lines.length > 2 ) {
                                String strippedLine2 = ChatColor.stripColor( lines[2] );
                                if( strippedLine2.equalsIgnoreCase( plugin.getLoadedConfig().getSignDaily() ) ) {
                                    plugin.getGameManager().displayLeaderBoard( thisPlayer, StatsPeriod.DAILY );
                                } else if( strippedLine2.equalsIgnoreCase( plugin.getLoadedConfig().getSignWeekly() ) ) {
                                    plugin.getGameManager().displayLeaderBoard( thisPlayer, StatsPeriod.WEEKLY );
                                } else if( strippedLine2.equalsIgnoreCase( plugin.getLoadedConfig().getSignMonthly() ) ) {
                                    plugin.getGameManager().displayLeaderBoard( thisPlayer, StatsPeriod.MONTHLY );
                                } else {
                                    plugin.getGameManager().displayLeaderBoard( thisPlayer, StatsPeriod.TOTAL );
                                }
                            } else {
                                plugin.getGameManager().displayLeaderBoard( thisPlayer, StatsPeriod.TOTAL );
                            }
                            return;
                        }
                        plugin.messageSender( thisPlayer, Messages.ERROR_IN_SIGN );
                        return;
                    }
                }
            }

            final Game playersGame = plugin.getGameManager().getPlayersGame( thisPlayer );
            if( playersGame == null ) return;
            if( !playersGame.isActiveInGame( thisPlayer ) ) return;

            Block targetBlock = null;
            switch( event.getAction() ) {
                case LEFT_CLICK_BLOCK:
                case RIGHT_CLICK_BLOCK:
                    targetBlock = event.getClickedBlock();
                    break;
                case LEFT_CLICK_AIR:
                case RIGHT_CLICK_AIR:
                    targetBlock = event.getPlayer().getTargetBlock( null, playersGame.getGameConfig().getTargetDist() );
                    break;
                case PHYSICAL:
                    break;
            }
            if( targetBlock != null ) {
                thisPlayer.playSound( thisPlayer.getLocation(), plugin.getLoadedConfig().getBoostSound(), 1, 1 );

                // Check if there is a player standing on the target block.
                final Vector targetPotentialPlayerPosition = targetBlock.getLocation().toVector().add( VECTOR_UP );

                List< Player > otherPlayers = playersGame.getActivePlayerList();
                for( Player otherPlayer : otherPlayers ) {
                    if( this.inTargetBox( thisPlayer.getWorld(), targetPotentialPlayerPosition, otherPlayer.getLocation() ) && this.hasBlockUnder( otherPlayer.getLocation() ) )
                    {
                        VectorToOtherPlayer vectorToOtherPlayer = new VectorToOtherPlayer( otherPlayer, thisPlayer );

                        // Set a horizontal boost velocity of medium magnitude, a medium gain from hitting their block.
                        vectorToOtherPlayer.multiply( plugin.getLoadedConfig().getBlock_hit_horizontal_velocity() );

                        // Final boost vector is our calculated horizontal velocity and our constant vertical velocity.
                        Vector vectorBoost = new Vector( vectorToOtherPlayer.getX(), plugin.getLoadedConfig().getVertical_velocity(), vectorToOtherPlayer.getZ() );
                        otherPlayer.setVelocity( vectorBoost );

                        otherPlayer.getWorld().playSound( otherPlayer.getLocation(), plugin.getLoadedConfig().getBoostedSound(), 1, 1 );
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity( PlayerInteractEntityEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) {
            if( event.getRightClicked() instanceof Player )
            {
                final Player thisPlayer = event.getPlayer();
                final Player otherPlayer = (Player)event.getRightClicked();
                if( plugin.getGameManager().activeInSameGame( thisPlayer, otherPlayer ) )
                {
                    VectorToOtherPlayer vectorToOtherPlayer = new VectorToOtherPlayer( otherPlayer, thisPlayer );

                    // Calculate a horizontal boost velocity that is greater the closer you are to the target.
                    double horizontalVelocity = plugin.getLoadedConfig().getMax_horizontal_velocity() - vectorToOtherPlayer.getDistanceBetweenPlayers();
                    if( horizontalVelocity < plugin.getLoadedConfig().getMin_horizontal_velocity() )
                        horizontalVelocity = plugin.getLoadedConfig().getMin_horizontal_velocity();
                    vectorToOtherPlayer.multiply( horizontalVelocity );

                    // Final boost vector is our calculated horizontal velocity and our constant vertical velocity.
                    Vector vectorBoost = new Vector( vectorToOtherPlayer.getX(), plugin.getLoadedConfig().getVertical_velocity(), vectorToOtherPlayer.getZ() );
                    otherPlayer.setVelocity( vectorBoost );

                    otherPlayer.getWorld().playSound( otherPlayer.getLocation(), plugin.getLoadedConfig().getBoostedSound(), 1, 1 );
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak( BlockBreakEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) event.setCancelled( true );
    }

    @EventHandler
    public void onBlockPlace( BlockPlaceEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) event.setCancelled( true );
    }

    @EventHandler
    public void onEntityDamage( EntityDamageEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getEntity() ) ) {
            if( event.getEntity() instanceof Player ) {
                event.setCancelled( true );
            }
        }
    }

    @EventHandler
    public void onPlayerMove( PlayerMoveEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) {
            Game game = plugin.getGameManager().getPlayersGame( event.getPlayer() );
            if( game == null ) return;
            if( game.isActiveInGame( event.getPlayer() ) ) {
                if( event.getTo() != null && event.getTo().getBlockY() <= game.getGameConfig().getGroundLevel() ) {
                    game.setPlayerLost( event.getPlayer() );
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event )
    {
        if( plugin.getGameManager().isPlaying( event.getPlayer() ) ) {
            plugin.getGameManager().leaveGame( event.getPlayer() );
        }
        plugin.removeBuilder( event.getPlayer().getUniqueId() );
    }

    @EventHandler
    public void onPlayerJoin( PlayerJoinEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) ) {
            event.getPlayer().teleport( plugin.getLoadedConfig().getMainLobbySpawn() );
            ItemStack instructionBook = plugin.getLoadedConfig().createInstructionBook();
            event.getPlayer().getInventory().setItem( 0, instructionBook );
        }
    }

    @EventHandler
    public void onPlayerChangeWorld( PlayerChangedWorldEvent event )
    {
        if( plugin.isBoostEnabled() ) {
            if( !plugin.isInGameWorld( event.getPlayer() ) && plugin.isGameWorld( event.getFrom().getName() ) )
            {
                // Player left to some world that's not a boost world.
                if( plugin.getGameManager().isPlaying( event.getPlayer() ) ) {
                    plugin.getGameManager().leaveGame( event.getPlayer() );
                }
                plugin.removeBuilder( event.getPlayer().getUniqueId() );
            }
            else if( event.getPlayer().getWorld().equals( plugin.getLoadedConfig().getMainLobbySpawn().getWorld() ) && !plugin.isGameWorld( event.getFrom().getName() ) )
            {
                // Player arrived in the boost spawn world AND not from a boost arena world or other.
                event.getPlayer().teleport( plugin.getLoadedConfig().getMainLobbySpawn() );
                ItemStack instructionBook = plugin.getLoadedConfig().createInstructionBook();
                event.getPlayer().getInventory().setItem( 0, instructionBook );
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem( PlayerDropItemEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) ) {
            event.setCancelled( true );
        }
    }
}
