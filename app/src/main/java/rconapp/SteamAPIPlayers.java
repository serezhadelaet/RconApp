package rconapp;

import java.util.List;

public class SteamAPIPlayers {
    public Response response;

    public class Response {
        public List<GameData> players;

        public class GameData {
            public String steamid;

            public Integer communityvisibilitystate;

            public Integer profilestate;

            public String personaname;

            public Integer lastlogoff;

            public Integer commentpermission;

            public String profileurl;

            public String avatar;

            public String avatarmedium;

            public String avatarfull;

            public Integer personastate;

            public String realname;

            public String primaryclanid;

            public Integer timecreated;

            public Integer personastateflags;

            public String gameserverip;

            public String gameserversteamid;

            public String gameextrainfo;

            public String gameid;

            public String loccountrycode;
        }
    }
}
