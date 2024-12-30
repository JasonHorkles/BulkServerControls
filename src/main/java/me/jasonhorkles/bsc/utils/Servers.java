package me.jasonhorkles.bsc.utils;

import com.mattmalec.pterodactyl4j.client.entities.ClientServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Servers {
    // A list of all the servers, excluding any ignored ones
    public static final Map<ClientServer, Servers.ServerType> allServers = new HashMap<>();

    public List<ClientServer> getDesiredServers(boolean includeBots, boolean includeMc) {
        // Add all the desired servers to a list
        List<ClientServer> servers = new ArrayList<>();

        for (Map.Entry<ClientServer, ServerType> entry : allServers.entrySet()) {
            ClientServer server = entry.getKey();
            ServerType type = entry.getValue();

            if (includeBots) if (type == ServerType.DISCORD_BOT) servers.add(server);
            if (includeMc) if (type == ServerType.MC) servers.add(server);
        }

        return servers;
    }

    public enum ServerType {
        DISCORD_BOT, MC,
        /// A Minecraft server that only needs plugins updated (ignore other controls)
        MC_UPDATE_ONLY
    }
}
