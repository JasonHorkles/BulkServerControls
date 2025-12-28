package me.jasonhorkles.bsc.controls;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import me.jasonhorkles.bsc.ServerControls;
import me.jasonhorkles.bsc.utils.LogColor;

import java.util.List;

public class Start implements ServerControls {
    public void execute(String serverType, List<ClientServer> servers) {
        System.out.println(LogColor.YELLOW.get() + "Starting all " + serverType + "...");

        for (ClientServer server : servers) {
            UtilizationState state = server.retrieveUtilization().execute().getState();

            if (state == UtilizationState.RUNNING || state == UtilizationState.STARTING) {
                System.out.println(LogColor.YELLOW.get() + server.getName() + " was skipped due to it already running!");
                continue;
            }

            server.start().executeAsync();
            System.out.println(LogColor.GREEN.get() + "Successfully started " + server.getName() + "!");
        }

        System.out.println(LogColor.GREEN.get() + "Done!");
    }
}
