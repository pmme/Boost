package nz.pmme.Boost;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Utils.VectorToOtherPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
            &&  yDelta >= -1 && yDelta <= plugin.getLoadedConfig().getTargetBoxV()
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
                    if( ( block.isEmpty() || !block.getType().isOccluding() ) && !blockUnder.isEmpty() ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void boostPlayer( Player otherPlayer, Player booster, boolean blockHit )
    {
        VectorToOtherPlayer vectorToOtherPlayer = new VectorToOtherPlayer( otherPlayer, booster );

        if( blockHit ) {
            // Set a horizontal boost velocity of medium magnitude, a medium gain from hitting their block.
            vectorToOtherPlayer.multiply( plugin.getLoadedConfig().getBlock_hit_horizontal_velocity() );
        } else {
            // Calculate a horizontal boost velocity that is greater the closer you are to the target.
            double horizontalVelocity = plugin.getLoadedConfig().getMax_horizontal_velocity() - vectorToOtherPlayer.getDistanceBetweenPlayers();
            if( horizontalVelocity < plugin.getLoadedConfig().getMin_horizontal_velocity() )
                horizontalVelocity = plugin.getLoadedConfig().getMin_horizontal_velocity();
            vectorToOtherPlayer.multiply( horizontalVelocity );
        }

        // Final boost vector is our calculated horizontal velocity and our constant vertical velocity.
        Vector vectorBoost = new Vector( vectorToOtherPlayer.getX(), plugin.getLoadedConfig().getVertical_velocity(), vectorToOtherPlayer.getZ() );
        otherPlayer.setVelocity( vectorBoost );

        otherPlayer.getWorld().playSound( otherPlayer.getLocation(), plugin.getLoadedConfig().getBoostedSound(), 1, 1 );
    }

    private boolean handleSignClickEvent( Block clickedBlock, Player player)
    {
        if( clickedBlock.getState() instanceof Sign )
        {
            Sign sign = (Sign)clickedBlock.getState();
            String[] lines = sign.getLines();
            if( lines.length > 1 && ChatColor.stripColor( lines[0] ).equalsIgnoreCase( plugin.getLoadedConfig().getSignTitleStripped() ) )
            {
                String strippedLine1 = ChatColor.stripColor( lines[1] );
                if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignJoinStripped() ) && lines.length >= 3 ) {
                    if( !plugin.hasPermission( player, "boost.join", Messages.NO_PERMISSION_USE ) ) return true;
                    plugin.getGameManager().joinGame( player, ChatColor.stripColor( lines[2] ) );
                    return true;
                } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignLeaveStripped() ) ) {
                    plugin.getGameManager().leaveGame( player );
                    return true;
                } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignStatusStripped() ) ) {
                    if( !plugin.hasPermission( player, "boost.status", Messages.NO_PERMISSION_USE ) ) return true;
                    plugin.getGameManager().displayStatus( player );
                    return true;
                } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignStatsStripped() ) ) {
                    if( lines.length > 2 ) {

                    }
                    plugin.getGameManager().displayPlayerStats( player, player );
                    return true;
                } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignTopStripped() ) ) {
                    if( lines.length > 2 ) {
                        String strippedLine2 = ChatColor.stripColor( lines[2] );
                        if( strippedLine2.equalsIgnoreCase( plugin.getLoadedConfig().getSignDailyStripped() ) ) {
                            plugin.getGameManager().displayLeaderBoard( player, StatsPeriod.DAILY );
                        } else if( strippedLine2.equalsIgnoreCase( plugin.getLoadedConfig().getSignWeeklyStripped() ) ) {
                            plugin.getGameManager().displayLeaderBoard( player, StatsPeriod.WEEKLY );
                        } else if( strippedLine2.equalsIgnoreCase( plugin.getLoadedConfig().getSignMonthlyStripped() ) ) {
                            plugin.getGameManager().displayLeaderBoard( player, StatsPeriod.MONTHLY );
                        } else {
                            plugin.getGameManager().displayLeaderBoard( player, StatsPeriod.TOTAL );
                        }
                    } else {
                        plugin.getGameManager().displayLeaderBoard( player, StatsPeriod.TOTAL );
                    }
                    return true;
                }
                plugin.messageSender( player, Messages.ERROR_IN_SIGN );
                return true;
            }
        }
        return false;   // Not a sign-click.
    }

    @EventHandler
    public void onPlayerInteract( PlayerInteractEvent event )
    {
        if( plugin.isBoostEnabled() && plugin.isInGameWorld( event.getPlayer() ) && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) )
        {
            final Player thisPlayer = event.getPlayer();
            final Game playersGame = plugin.getGameManager().getPlayersGame( thisPlayer );

            Block targetBlock = null;
            switch( event.getAction() ) {
                case LEFT_CLICK_BLOCK:
                case RIGHT_CLICK_BLOCK:
                    if( handleSignClickEvent( event.getClickedBlock(), event.getPlayer() ) ) return;
                    if( playersGame == null ) return;
                    if( !playersGame.isActiveInGame( thisPlayer ) && ( !playersGame.isQueuing() || !plugin.getLoadedConfig().canBoostWhileQueuing() ) ) return;
                    if( playersGame.isOnCoolDown( thisPlayer ) ) return;
                    targetBlock = event.getClickedBlock();
                    break;
                case LEFT_CLICK_AIR:
                case RIGHT_CLICK_AIR:
                    if( playersGame == null ) return;
                    if( !playersGame.isActiveInGame( thisPlayer ) && ( !playersGame.isQueuing() || !plugin.getLoadedConfig().canBoostWhileQueuing() ) ) return;
                    if( playersGame.isOnCoolDown( thisPlayer ) ) return;
                    targetBlock = event.getPlayer().getTargetBlock( null, playersGame.getGameConfig().getTargetDist() );
                    break;
                case PHYSICAL:
                    break;
            }
            if( targetBlock != null ) {
                thisPlayer.playSound( thisPlayer.getLocation(), plugin.getLoadedConfig().getBoostSound(), 1, 1 );

                // Check if there is a player standing on the target block.
                final Vector targetPotentialPlayerPosition = targetBlock.getLocation().toVector().add( VECTOR_UP );

                if( !playersGame.isQueuing() ) {
                    List< Player > otherPlayers = playersGame.getActivePlayerList();
                    for( Player otherPlayer : otherPlayers ) {
                        if( this.inTargetBox( thisPlayer.getWorld(), targetPotentialPlayerPosition, otherPlayer.getLocation() ) && this.hasBlockUnder( otherPlayer.getLocation() ) ) {
                            this.boostPlayer( otherPlayer, thisPlayer, true );
                        }
                    }
                }
                else if( plugin.getLoadedConfig().canBoostWhileQueuing() )
                {
                    if( this.inTargetBox( thisPlayer.getWorld(), targetPotentialPlayerPosition, thisPlayer.getLocation() ) && this.hasBlockUnder( thisPlayer.getLocation() ) ) {
                        this.boostPlayer( thisPlayer, thisPlayer, true );
                    }
                }
                playersGame.coolDown( thisPlayer );
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
                final Game playersGame = plugin.getGameManager().getPlayersGame( thisPlayer );
                if( playersGame.isOnCoolDown( thisPlayer ) ) return;
                final Player otherPlayer = (Player)event.getRightClicked();
                if( plugin.getGameManager().activeInSameGame( thisPlayer, otherPlayer ) )
                {
                    this.boostPlayer( otherPlayer, thisPlayer, false );
                }
                playersGame.coolDown( thisPlayer );
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
                if( event.getTo() != null ) {
                    if( event.getTo().getBlockY() <= game.getGameConfig().getGroundLevel() && game.getGameConfig().getGroundLevel() != -1 ) {
                        game.setPlayerLost( event.getPlayer() );
                    }
                    else if( event.getTo().getBlockY() >= game.getGameConfig().getCeilingLevel() && game.getGameConfig().getCeilingLevel() != -1 ) {
                        game.setPlayerWon( event.getPlayer() );
                    }
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
            event.getPlayer().getInventory().setItem( 8, instructionBook );
            event.getPlayer().getInventory().setHeldItemSlot( 8 );
        }
    }

    @EventHandler
    public void onPlayerChangeWorld( PlayerChangedWorldEvent event )
    {
        if( plugin.isBoostEnabled() ) {
            Location mainLobbySpawn = plugin.getLoadedConfig().getMainLobbySpawn();
            if( !plugin.isInGameWorld( event.getPlayer() ) && plugin.isGameWorld( event.getFrom().getName() ) )
            {
                // Player left to some world that's not a boost world.
                if( plugin.getGameManager().isPlaying( event.getPlayer() ) ) {
                    plugin.getGameManager().leaveGame( event.getPlayer() );
                }
                plugin.removeBuilder( event.getPlayer().getUniqueId() );
            }
            else if( mainLobbySpawn != null && event.getPlayer().getWorld().equals( mainLobbySpawn.getWorld() ) && !plugin.isGameWorld( event.getFrom().getName() ) )
            {
                // Player arrived in the boost spawn world AND not from a boost arena world or other.
                event.getPlayer().teleport( mainLobbySpawn );
                ItemStack instructionBook = plugin.getLoadedConfig().createInstructionBook();
                event.getPlayer().getInventory().setItem( 8, instructionBook );
                event.getPlayer().getInventory().setHeldItemSlot( 8 );
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
