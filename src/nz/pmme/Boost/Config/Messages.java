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
    START_SPAWN_SET("start_spawn_set"),
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

    END("end");

    private String path;

    Messages( String path )
    {
        this.path = path;
    }

    public String getPath() { return this.path; }
}

