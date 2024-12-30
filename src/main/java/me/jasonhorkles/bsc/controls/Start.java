package me.jasonhorkles.bsc.controls;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import me.jasonhorkles.bsc.ServerControls;
import me.jasonhorkles.bsc.utils.Log;

import java.util.List;

public class Start implements ServerControls {
    public void execute(String serverType, List<ClientServer> servers) {
        System.out.println(Log.Color.YELLOW.getColor() + "Starting all " + serverType + "...");

        for (ClientServer server : servers) {
            UtilizationState state = server.retrieveUtilization().execute().getState();

            if (state == UtilizationState.RUNNING || state == UtilizationState.STARTING) {
                System.out.println(Log.Color.YELLOW.getColor() + server.getName() + " was skipped due to it already running!");
                continue;
            }

            server.start().executeAsync();
            System.out.println(Log.Color.GREEN.getColor() + "Successfully started " + server.getName() + "!");
        }

        System.out.println(Log.Color.GREEN.getColor() + "Done!");
    }
}
