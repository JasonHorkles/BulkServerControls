package me.jasonhorkles.bsc;

import com.mattmalec.pterodactyl4j.client.entities.ClientServer;

import java.util.List;

public interface ServerControls {
    void execute(String serverType, List<ClientServer> servers);
}
