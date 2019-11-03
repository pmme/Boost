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
    NO_JOIN_GAME_RUNNING("no_join_game_running"),
    NO_JOIN_GAME_STOPPED("no_join_game_stopped"),
    NO_JOIN_GAME_FULL("no_join_game_full"),
    LEAVE_GAME("leave_game"),
    LOST("lost"),
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

    END("end");

    private String path;

    Messages( String path )
    {
        this.path = path;
    }

    public String getPath() { return this.path; }
}

