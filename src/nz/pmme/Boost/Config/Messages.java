package nz.pmme.Boost.Config;

public enum Messages
{
    TITLE("title"),
    BOOST_ENABLED("enabled"),
    BOOST_DISABLED("disabled"),
    CONFIG_RELOADED("reloaded"),
    NO_CONSOLE("no_console"),
    GAME_ALREADY_EXISTS("game_already_exists"),
    GAME_DOES_NOT_EXIST("game_does_not_exist"),
    GAMES_CLEARED("games_cleared"),
    ERROR_IN_SIGN("error_in_sign"),
    JOIN_GAME("join_game"),
    PLAYER_JOINED_GAME("player_joined_game"),
    NO_JOIN_GAME_RUNNING("no_join_game_running"),
    NO_JOIN_GAME_STOPPED("no_join_game_stopped"),
    NO_JOIN_GAME_FULL("no_join_game_full"),
    LEAVE_GAME("leave_game"),
    LOST("lost"),
    PLAYER_LOST("player_lost"),
    GAME_STARTED("game_started"),
    GAME_ENDED("game_ended"),
    GAME_COUNTDOWN("game_countdown"),
    WINNER("winner"),
    ALREADY_IN_GAME("already_in_game"),
    NOT_IN_GAME("not_in_game"),
    GAME_ALREADY_QUEUING("game_already_queuing"),
    GAME_ALREADY_RUNNING("game_already_running"),
    GAME_NOT_RUNNING("game_not_running"),
    FAILED_TO_FIND_WORLD("failed_to_find_world"),
    NO_PERMISSION_CMD("no_permission_cmd"),
    NO_PERMISSION_USE("no_permission_use"),
    PLAYER_NOT_FOUND("player_not_found"),
    PLAYER_NO_STATS("player_no_stats"),
    DELETED_STATS("deleted_stats"),
    LEADER_BOARD_TITLE("leader_board_title"),
    LEADER_BOARD_ENTRY("leader_board_entry"),
    BUILD_ENABLED("build_enabled"),
    BUILD_DISABLED("build_disabled"),
    GAME_NOT_CONFIGURED("game_not_configured"),
    PLAYERS_REQUIRED("players_required"),
    GAME_CREATED("game_created"),
    GROUND_SET("ground_set"),
    CEILING_SET("ceiling_set"),
    START_SPAWN_SET("start_spawn_set"),
    START_SPAWN_DELETED("start_spawn_deleted"),
    START_SPAWN_NOT_FOUND("start_spawn_not_found"),
    LOBBY_SPAWN_SET("lobby_spawn_set"),
    LOSS_SPAWN_SET("loss_spawn_set"),
    SPREAD_SET("spread_set"),
    MAIN_SPAWN_SET("main_spawn_set"),
    STARTED_QUEUING("started_queuing"),
    NOT_IN_GAME_WORLD("not_in_game_world"),
    ADDED_GAME_WORLD("added_game_world"),
    REMOVED_GAME_WORLD("removed_game_world"),
    NOT_A_GAME_WORLD("not_a_game_world"),
    MUST_TYPE_CONFIRM("must_type_confirm"),
    GAME_DELETED("game_deleted"),
    MIN_PLAYERS_SET("min_players_set"),
    MAX_PLAYERS_SET("max_players_set"),
    AUTO_QUEUE_ENABLED("auto_queue_enabled"),
    AUTO_QUEUE_DISABLED("auto_queue_disabled"),
    COUNTDOWN_SET("countdown_set"),
    COUNTDOWN_ANNOUNCEMENT_SET("countdown_announcement_set"),
    DISPLAY_NAME_SET("display_name_set"),
    DISPLAY_NAME_MISMATCH("display_name_mismatch"),
    GROUND_HIGHER("ground_higher"),
    CEILING_LOWER("ceiling_lower"),
    NOT_FACING_A_SIGN("not_facing_a_sign"),
    SIGN_SET("sign_set"),
    SIGN_COMMAND_NOT_RECOGNISED("sign_command_not_recognised"),
    BOOST_WHILE_QUEUING("boost_while_queuing"),
    NO_BOOST_WHILE_QUEUING("no_boost_while_queuing"),
    GAME_COMMANDS_UPDATED("game_commands_updated"),
    PERIODIC_COMMANDS_UPDATED("periodic_commands_updated"),
    GAME_COMMANDS_TESTED("game_commands_tested"),
    PERIODIC_COMMANDS_TESTED("periodic_commands_tested"),
    GAME_PERM_ENABLED("game_perm_enabled"),
    GAME_PERM_DISABLED("game_perm_disabled"),
    GAME_NOT_AVAILABLE("game_not_available"),
    COOL_DOWN_SET("cool_down_set"),
    START_DELAY_SET("start_delay_set"),
    WIN_BLOCK_SET("win_block_set"),
    WIN_BLOCK_DISABLED("win_block_disabled"),
    BOOST_BLOCK_SET("boost_block_set"),
    BOOST_BLOCK_DISABLED("boost_block_disabled"),
    RETURN_TO_START_ENABLED( "return_to_start_enabled" ),
    RETURN_TO_START_DISABLED( "return_to_start_disabled" ),
    GUI_ITEM_SET( "gui_item_set" ),
    GUI_ITEM_DISABLED( "gui_item_disabled" ),
    CONFIG_UP_TO_DATE( "config_up_to_date" ),
    CONFIG_UPDATED( "config_updated" ),
    COMMAND_BLOCKED( "command_blocked" ),
    COMMAND_NOW_ALLOWED( "command_now_allowed" ),
    COMMAND_NOW_BLOCKED( "command_now_blocked" ),
    START_DELAY_TITLE( "start_delay_title" ),
    GLOW_AFTER_BOOST_ENABLED( "glow_after_boost_enabled" ),
    GLOW_AFTER_BOOST_DISABLED( "glow_after_boost_disabled" ),
    SPAWN_NOT_SET( "spawn_not_set" ),
    GAME_TYPE_SET( "game_type_set" ),
    TARGET_PLAYERS_ENABLED( "target_players_enabled" ),
    TARGET_PLAYERS_DISABLED( "target_players_disabled" ),

    END("end");

    private String path;

    Messages( String path )
    {
        this.path = "messages." + path;
    }

    public String getPath() { return this.path; }
}

