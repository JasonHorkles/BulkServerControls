package me.jasonhorkles.bsc.controls;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import me.jasonhorkles.bsc.ServerControls;
import me.jasonhorkles.bsc.utils.LogColor;

import java.util.List;

public class Stop implements ServerControls {
    public void execute(String serverType, List<ClientServer> servers) {
        System.out.println(LogColor.YELLOW.get() + "Stopping all " + serverType + "...");

        for (ClientServer server : servers) {
            UtilizationState state = server.retrieveUtilization().execute().getState();

            if (state == UtilizationState.STOPPING || state == UtilizationState.OFFLINE) {
                System.out.println(LogColor.YELLOW.get() + server.getName() + " was skipped due to it already being stopped!");
                continue;
            }

            server.stop().execute();
            System.out.println(LogColor.GREEN.get() + "Successfully stopped " + server.getName() + "!");
        }

        System.out.println(LogColor.GREEN.get() + "Done!");
    }
}
