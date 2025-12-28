package me.jasonhorkles.bsc;

import com.mattmalec.pterodactyl4j.ClientType;
import com.mattmalec.pterodactyl4j.PteroBuilder;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.PteroClient;
import me.jasonhorkles.bsc.controls.Restart;
import me.jasonhorkles.bsc.controls.Start;
import me.jasonhorkles.bsc.controls.Stop;
import me.jasonhorkles.bsc.controls.Update;
import me.jasonhorkles.bsc.utils.LogColor;
import me.jasonhorkles.bsc.utils.Servers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static me.jasonhorkles.bsc.utils.Servers.allServers;

public class Main {
    public static final PteroClient api = PteroBuilder.createClient(
        "https://panel.silverstonemc.net/",
        new Secrets().getPanelToken());

    static void main() {
        System.out.println(LogColor.YELLOW.get() + "Loading...");

        // Server names to ignore
        List<String> ignoredServers = List.of("Testing");

        // Cache all servers
        // Owner servers are implied to be bots
        for (ClientServer server : api.retrieveServers(ClientType.OWNER))
            if (!ignoredServers.contains(server.getName())) allServers.put(
                server,
                Servers.ServerType.DISCORD_BOT);

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
        System.out.println(LogColor.RESET.get() + """
            
            BULK SERVER CONTROL PANEL
            <action> <servers>
            
            Actions:
            's' - Start
            'r' - Restart
            'e' - Stop (exit)
            'u' - Update
            'sync' - (TODO) Sync the remote Minecraft plugins to the local copies
            'et' - Exit the program
            
            Servers:
            'a' - All servers
            'b' - All Discord bots
            'm' - All Minecraft servers
            
            ==================================
            """);

        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        while (true) {
            System.out.print(LogColor.RESET.get() + "> ");
            String text = in.nextLine().toLowerCase();

            if (text.equals("et")) {
                in.close();
                System.exit(0);
            } else if (text.equals("sync")) {
                System.out.println(LogColor.YELLOW.get() + "This feature is not yet implemented!");
                continue;
            } else if (text.isBlank()) continue;

            String serverTypeNamed;
            boolean includeBots = false;
            boolean includeMc = false;
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
                    System.out.println(LogColor.RED.get() + "Invalid server type!");
                    continue;
                }
            }

            switch (text.charAt(0)) {
                case 's' -> new Start().execute(
                    serverTypeNamed,
                    new Servers().getDesiredServers(includeBots, includeMc));

                case 'r' -> new Restart().execute(
                    serverTypeNamed,
                    new Servers().getDesiredServers(includeBots, includeMc));

                case 'e' -> new Stop().execute(
                    serverTypeNamed,
                    new Servers().getDesiredServers(includeBots, includeMc));

                case 'u' -> new Update().execute(serverTypeNamed, includeBots, includeMc);

                default -> System.out.println(LogColor.RED.get() + "Invalid action!");
            }
        }
    }
}
