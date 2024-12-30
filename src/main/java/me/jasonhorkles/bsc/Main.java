package me.jasonhorkles.bsc;

import com.mattmalec.pterodactyl4j.ClientType;
import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import me.jasonhorkles.bsc.controls.Restart;
import me.jasonhorkles.bsc.controls.Start;
import me.jasonhorkles.bsc.controls.Stop;
import me.jasonhorkles.bsc.controls.Update;
import me.jasonhorkles.bsc.utils.Log;
import me.jasonhorkles.bsc.utils.Servers;

import java.util.List;
import java.util.Scanner;

import static me.jasonhorkles.bsc.utils.Servers.allServers;

public class Main {
    public static final PteroClient api = PteroBuilder.createClient(
        "https://panel.silverstonemc.net/",
        new Secrets().getPanelToken());
    public static final PteroClient proxyApi = PteroBuilder.createClient(
        "https://control.heavynode.com/",
        new Secrets().getProxyToken());

    public static void main(String[] args) {
        System.out.println(Log.Color.YELLOW.getColor() + "Loading...");

        // Server names to ignore
        final List<String> ignoredServers = List.of("Testing");

        // Cache all servers
        // Owner servers are implied to be bots
        for (ClientServer server : api.retrieveServers(ClientType.OWNER))
            if (!ignoredServers.contains(server.getName())) allServers.put(
                server,
                Servers.ServerType.DISCORD_BOT);

        for (ClientServer server : proxyApi.retrieveServers(ClientType.OWNER))
            if (!ignoredServers.contains(server.getName())) allServers.put(
                server,
                Servers.ServerType.MC_UPDATE_ONLY);

        // Admin servers are implied to be Minecraft
        for (ClientServer server : api.retrieveServers(ClientType.ADMIN))
            if (!ignoredServers.contains(server.getName()))
                // We only want to update the Events plugins without the other controls
                if (server.getName().contains("Events")) allServers.put(
                    server,
                    Servers.ServerType.MC_UPDATE_ONLY);
                else allServers.put(server, Servers.ServerType.MC);

        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println(Log.Color.RESET.getColor() + """
            
            BULK SERVER CONTROL PANEL
            <action> <servers>
            
            Actions:
            's' - Start
            'r' - Restart
            'e' - Stop (exit)
            'u' - Update
            'sync' - (TODO) Sync the local Minecraft plugins with the server plugins
            'et' - Exit the program
            
            Servers:
            'a' - All servers
            'b' - All Discord bots
            'm' - All Minecraft servers
            
            ==================================
            """);

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print(Log.Color.RESET.getColor() + "> ");
            String text = in.nextLine().toLowerCase();

            if (text.equals("et")) System.exit(0);
            else if (text.equals("sync")) {
                System.out.println(Log.Color.YELLOW.getColor() + "This feature is not yet implemented!");
                continue;
            } else if (text.isBlank()) continue;

            String serverTypeNamed;
            boolean includeBots = false;
            boolean includeMc = false;
            try {
                switch (text.charAt(1)) {
                    case 'a' -> {
                        includeBots = true;
                        includeMc = true;
                        serverTypeNamed = "servers";
                    }

                    case 'b' -> {
                        includeBots = true;
                        serverTypeNamed = "Discord bots";
                    }

                    case 'm' -> {
                        includeMc = true;
                        serverTypeNamed = "Minecraft servers";
                    }

                    default -> {
                        System.out.println(Log.Color.RED.getColor() + "Invalid server type!");
                        continue;
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {
                System.out.println(Log.Color.RED.getColor() + "Invalid server type!");
                continue;
            }

            if (text.startsWith("s")) new Start().execute(
                serverTypeNamed,
                new Servers().getDesiredServers(includeBots, includeMc));
            else if (text.startsWith("r")) new Restart().execute(
                serverTypeNamed,
                new Servers().getDesiredServers(includeBots, includeMc));
            else if (text.startsWith("e")) new Stop().execute(
                serverTypeNamed,
                new Servers().getDesiredServers(includeBots, includeMc));
            else if (text.startsWith("u")) new Update().execute(serverTypeNamed, includeBots, includeMc);
            else System.out.println(Log.Color.RED.getColor() + "Invalid action!");
        }
    }
}
