package nz.pmme.Boost;

import nz.pmme.Boost.Config.BoostParticle;
import nz.pmme.Boost.Config.GUIButtonConfig;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Gui.GUI;
import nz.pmme.Utils.RayIterator;
import nz.pmme.Utils.VectorToOtherPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.List;


/**
 * Created by paul on 19-Apr-16.
 */
public class EventsListener implements Listener
{
    private static final Vector VECTOR_UP = new Vector( 0, 1, 0 );
    private static final Vector VECTOR_0 = new Vector();
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
        // First check for a block directly under the player.
        for( int y = playerPosition.getBlockY(); y >= playerPosition.getBlockY() - plugin.getLoadedConfig().getTargetBoxV(); --y ) {
            Block blockUnder = playerPosition.getWorld().getBlockAt( playerPosition.getBlockX(), y - 1, playerPosition.getBlockZ() );
            if( !blockUnder.isEmpty() ) {
                return true;
            }
        }
        // Check for a block under the position next to the player in case they're standing on an edge but prevent wall climbing by requiring empty space over a block.
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

    private void boostPlayer( Player player, Vector vector )
    {
        // Final boost vector is our calculated horizontal velocity and our constant vertical velocity.
        Vector vectorBoost = new Vector( vector.getX(), plugin.getLoadedConfig().getVertical_velocity(), vector.getZ() );
        player.setVelocity( vectorBoost );

        player.getWorld().playSound( player.getLocation(), plugin.getLoadedConfig().getBoostedSound(), 1, 1 );
    }

    private void boostPlayer( Player player, Location from, Location to )
    {
        VectorToOtherPlayer vector = new VectorToOtherPlayer( from, to );
        vector.multiply( plugin.getLoadedConfig().getBlock_hit_horizontal_velocity() );
        this.boostPlayer( player, vector );
    }

    private void boostPlayer( Player otherPlayer, Player booster )
    {
        VectorToOtherPlayer vectorToOtherPlayer = new VectorToOtherPlayer( booster.getLocation(), otherPlayer.getLocation() );
        // Calculate a horizontal boost velocity that is greater the closer you are to the target.
        double horizontalVelocity = plugin.getLoadedConfig().getMax_horizontal_velocity() - vectorToOtherPlayer.getDistanceBetweenPlayers();
        if( horizontalVelocity < plugin.getLoadedConfig().getMin_horizontal_velocity() )
            horizontalVelocity = plugin.getLoadedConfig().getMin_horizontal_velocity();
        vectorToOtherPlayer.multiply( horizontalVelocity );
        this.boostPlayer( otherPlayer, vectorToOtherPlayer );
    }

    private boolean handleMainGuiItemClickEvent( Player player )
    {
        GUIButtonConfig guiButtonConfig = plugin.getLoadedConfig().getGuiButtonConfig( "main" );
        if( guiButtonConfig.isEnabled() && guiButtonConfig.isGUIItem( player.getInventory().getItemInMainHand() ) )
        {
            GUI gui = new GUI( this.plugin, player );
            gui.openInventory();
            return true;
        }
        return false;
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
                    if( !plugin.checkPermission( player, "boost.join", Messages.NO_PERMISSION_USE ) ) return true;
                    plugin.getGameManager().joinGame( player, ChatColor.stripColor( lines[2] ) );
                    return true;
                } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignLeaveStripped() ) ) {
                    plugin.getGameManager().leaveGame( player );
                    return true;
                } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignStatusStripped() ) ) {
                    if( !plugin.checkPermission( player, "boost.status", Messages.NO_PERMISSION_USE ) ) return true;
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
                    if( handleMainGuiItemClickEvent( event.getPlayer() ) ) return;
                    if( handleSignClickEvent( event.getClickedBlock(), event.getPlayer() ) ) return;
                    if( playersGame == null ) return;
                    if( !playersGame.isActiveInGame( thisPlayer ) && ( !playersGame.isQueuing() || !plugin.getLoadedConfig().canBoostWhileQueuing() ) ) return;
                    if( playersGame.isOnCoolDown( thisPlayer ) ) return;
                    targetBlock = event.getClickedBlock();
                    break;
                case LEFT_CLICK_AIR:
                case RIGHT_CLICK_AIR:
                    if( handleMainGuiItemClickEvent( event.getPlayer() ) ) return;
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
                        if( this.inTargetBox( thisPlayer.getWorld(), targetPotentialPlayerPosition, otherPlayer.getLocation() ) ) {
                            if( thisPlayer != otherPlayer || hasBlockUnder( thisPlayer.getLocation() ) ) {
                                plugin.getLoadedConfig().getBoostedParticleForPlayer( thisPlayer ).spawn( otherPlayer.getWorld(), otherPlayer.getLocation() );
                                this.boostPlayer( otherPlayer, thisPlayer.getLocation(), otherPlayer.getLocation() );
                            }
                        }
                    }
                }
                else if( plugin.getLoadedConfig().canBoostWhileQueuing() )
                {
                    if( this.inTargetBox( thisPlayer.getWorld(), targetPotentialPlayerPosition, thisPlayer.getLocation() ) && this.hasBlockUnder( thisPlayer.getLocation() ) ) {
                        this.boostPlayer( thisPlayer, thisPlayer.getLocation(), thisPlayer.getLocation() );
                    }
                }
                BoostParticle boostParticle = plugin.getLoadedConfig().getBoostParticleForPlayer( thisPlayer );
                if( boostParticle.isEnabled() ) {
                    for( RayIterator rayIterator = new RayIterator( thisPlayer, targetBlock.getLocation(), boostParticle.getSpacing() ); !rayIterator.end(); rayIterator.step() ) {
                        boostParticle.spawn( thisPlayer.getWorld(), rayIterator.toLocation( thisPlayer.getWorld() ) );
                    }
                }
                plugin.getLoadedConfig().getBoostHitParticleForPlayer( thisPlayer ).spawn( thisPlayer.getWorld(), targetPotentialPlayerPosition.getBlockX() + 0.5, targetPotentialPlayerPosition.getBlockY() + 0.5, targetPotentialPlayerPosition.getBlockZ() + 0.5 );
                playersGame.coolDown( thisPlayer );
            } else {
                BoostParticle boostParticle = plugin.getLoadedConfig().getBoostParticleForPlayer( thisPlayer );
                if( boostParticle.isEnabled() ) {
                    for( RayIterator rayIterator = new RayIterator( thisPlayer, playersGame.getGameConfig().getTargetDist(), boostParticle.getSpacing() ); !rayIterator.end(); rayIterator.step() ) {
                        boostParticle.spawn( thisPlayer.getWorld(), rayIterator.toLocation( thisPlayer.getWorld() ) );
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
                final Game playersGame = plugin.getGameManager().getPlayersGame( thisPlayer );
                if( playersGame == null ) return;
                if( playersGame.isOnCoolDown( thisPlayer ) ) return;
                final Player otherPlayer = (Player)event.getRightClicked();
                if( plugin.getGameManager().activeInSameGame( thisPlayer, otherPlayer ) )
                {
                    plugin.getLoadedConfig().getBoostedParticleForPlayer( thisPlayer ).spawn( otherPlayer.getWorld(), otherPlayer.getLocation() );
                    this.boostPlayer( otherPlayer, thisPlayer );
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
                        if( game.getGameConfig().isReturnToStartAtGround() ) {
                            event.getPlayer().setVelocity( EventsListener.VECTOR_0 );
                            event.getPlayer().setFlying( false );
                            event.getPlayer().teleport( game.getGameConfig().getStartSpawn() );
                        } else {
                            game.setPlayerLost( event.getPlayer() );
                        }
                    }
                    else if( event.getTo().getBlockY() >= game.getGameConfig().getCeilingLevel() && game.getGameConfig().getCeilingLevel() != -1 ) {
                        game.setPlayerWon( event.getPlayer() );
                    }
                    else if( game.getGameConfig().getBoostBlock() != null ) {
                        Block block = event.getPlayer().getWorld().getBlockAt( event.getTo().getBlockX(), event.getTo().getBlockY()-1, event.getTo().getBlockZ() );
                        if( block.getType() == game.getGameConfig().getBoostBlock() ) {
                            this.boostPlayer( event.getPlayer(), event.getTo(), event.getTo() ); // Uses same location, which will end up taking players current facing direction.
                        }
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
            plugin.getInventoryManager().removeBoostItems( event.getPlayer() );
            plugin.getInventoryManager().giveInstructionBook( event.getPlayer() );
            plugin.getInventoryManager().giveMainGuiItem( event.getPlayer(), true );
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
                plugin.getInventoryManager().removeBoostItems( event.getPlayer() );
                plugin.getInventoryManager().giveInstructionBook( event.getPlayer() );
                plugin.getInventoryManager().giveMainGuiItem( event.getPlayer(), true );
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

    @EventHandler
    public void onCommand( PlayerCommandPreprocessEvent event )
    {
        Player player = event.getPlayer();
        if( plugin.getLoadedConfig().areCommandsBlockedWhilePlaying() && plugin.getGameManager().isPlaying( player ) && !player.hasPermission( "boost.bypassblocking" ) && !player.isOp() )
        {
            int commandSeparator = event.getMessage().indexOf(' ');
            int end = ( commandSeparator >= 0 ) ? commandSeparator : event.getMessage().length();
            int start = ( event.getMessage().charAt(0) == '/' ) ? 1 : 0;
            String command = event.getMessage().substring( start, end );
            if( !plugin.getLoadedConfig().isCommandAllowed( command ) ) {
                plugin.messageSender( player, Messages.COMMAND_BLOCKED );
                event.setCancelled( true );
            }
        }
    }
}
