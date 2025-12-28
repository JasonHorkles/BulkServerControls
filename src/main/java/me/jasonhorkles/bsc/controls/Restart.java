package me.jasonhorkles.bsc.controls;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import me.jasonhorkles.bsc.ServerControls;
import me.jasonhorkles.bsc.utils.LogColor;

import java.util.List;

public class Restart implements ServerControls {
    public void execute(String serverType, List<ClientServer> servers) {
        System.out.println(LogColor.YELLOW.get() + "Restarting all " + serverType + "...");

        for (ClientServer server : servers) {
            UtilizationState state = server.retrieveUtilization().execute().getState();

            if (state == UtilizationState.STOPPING || state == UtilizationState.OFFLINE) {
                System.out.println(LogColor.YELLOW.get() + server.getName() + " was skipped due to it being stopped!");
                continue;
            }

            server.restart().executeAsync();
            System.out.println(LogColor.GREEN.get() + "Successfully restarted " + server.getName() + "!");
        }

        System.out.println(LogColor.GREEN.get() + "Done!");
    }
}
