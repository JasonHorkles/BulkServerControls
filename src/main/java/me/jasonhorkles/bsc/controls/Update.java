package me.jasonhorkles.bsc.controls;

import com.mattmalec.pterodactyl4j.UtilizationState;
import com.mattmalec.pterodactyl4j.client.entities.ClientServer;
import com.mattmalec.pterodactyl4j.client.entities.Directory;
import com.mattmalec.pterodactyl4j.client.entities.GenericFile;
import me.jasonhorkles.bsc.Main;
import me.jasonhorkles.bsc.utils.Log;
import me.jasonhorkles.bsc.utils.Servers;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Update {
    private final ArrayList<Thread> uploadThreads = new ArrayList<>();

    public void execute(String serverType, boolean includeBots, boolean includeMc) {
        System.out.println(Log.Color.YELLOW.getColor() + "Checking for all " + serverType.substring(
            0,
            serverType.length() - 1) + " updates...");

        if (includeBots) updateBots();

        // Add all the desired MC servers to a list
        List<ClientServer> mcServers = new ArrayList<>();
        if (includeMc)
            for (Map.Entry<ClientServer, Servers.ServerType> entry : Servers.allServers.entrySet()) {
                ClientServer server = entry.getKey();
                Servers.ServerType type = entry.getValue();

                if (type == Servers.ServerType.MC || type == Servers.ServerType.MC_UPDATE_ONLY) mcServers.add(
                    server);
            }

        for (ClientServer server : mcServers) updatePlugins(server);

        for (Thread thread : uploadThreads)
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        System.out.println(Log.Color.GREEN.getColor() + "Done!");
    }

    private void updateBots() {
        //noinspection DataFlowIssue
        for (File files : new File("C:/Intellij Outputs/Bots").listFiles()) {
            Thread thread = new Thread(
                () -> {
                    ClientServer server = Main.api.retrieveServersByName(
                        files.getName().replace(".jar", ""),
                        false).execute().getFirst();
                    UtilizationState state = server.retrieveUtilization().execute().getState();
                    Directory directory = server.retrieveDirectory().execute();

                    if (state != UtilizationState.STOPPING && state != UtilizationState.OFFLINE) {
                        System.out.println(Log.Color.YELLOW.getColor() + "Stopping " + server.getName() + "...");
                        server.stop().execute();
                    }

                    String fileName = files.getName();
                    System.out.println(Log.Color.YELLOW.getColor() + "Uploading file " + fileName + " to server " + server.getName() + "...");

                    try {
                        directory.upload().addFile(files).timeout(1, TimeUnit.MINUTES).execute();

                        Executors.newSingleThreadScheduledExecutor().schedule(
                            () -> {
                                //noinspection ResultOfMethodCallIgnored
                                files.delete();
                            }, 1, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        System.out.println(Log.Color.RED.getColor() + "Failed to upload file " + fileName + " to server " + server.getName() + "! " + e.getMessage());
                        return;
                    }

                    System.out.println(Log.Color.GREEN.getColor() + "Successfully uploaded file " + fileName + " to server " + server.getName() + "!");

                    System.out.println(Log.Color.GREEN.getColor() + "Starting " + server.getName() + "...");
                    server.start().execute();
                }, "Update Bot - " + files.getName());

            thread.start();
            uploadThreads.add(thread);
        }
    }

    private void updatePlugins(ClientServer server) {
        try {
            String serverName = server.getName().replace("[MC] ", "");
            boolean isProxy = serverName.equalsIgnoreCase("Proxy");

            Path path = Paths.get("C:/Users/jason/OneDrive/Documents/MC Server Plugins/Plugin Map.csv");
            List<List<String>> data = new ArrayList<>();

            // Read the file
            try (BufferedReader br = Files.newBufferedReader(path)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(",");
                    data.add(Arrays.asList(columns));
                }
            }

            int columnIndex = -1; // The index of the column that contains the server name
            int columnCount = data.getFirst().size(); // Assuming all rows have the same number of columns
            // Find the column index that contains the server name
            for (int i = 0; i < columnCount; i++)
                if (data.getFirst().get(i).equalsIgnoreCase(serverName)) {
                    columnIndex = i;
                    break;
                }

            if (columnIndex == -1) {
                System.out.println(Log.Color.RED.getColor() + "Server " + serverName + " not found in the CSV file!");
                return;
            }

            // The plugin list for the selected server
            List<String> plugins = new ArrayList<>();

            // Access the plugin names for the server
            for (int x = 1; x < data.size(); x++) {
                List<String> row = data.get(x);
                if (row.size() <= columnIndex) continue;

                plugins.add(row.get(columnIndex));
            }

            Directory pluginsDir = server.retrieveDirectory("/plugins").execute();
            Directory updateDir = isProxy ? pluginsDir : server.retrieveDirectory("/plugins/update")
                .execute();
            for (String pluginName : plugins) {
                if (pluginName.isEmpty()) continue;
                Thread thread = checkPlugins(pluginName, serverName, pluginsDir, updateDir);
                uploadThreads.add(thread);
            }

            // Delete plugins not in the CSV file
            for (GenericFile file : pluginsDir.getFiles()) {
                if (!file.isFile()) continue;
                if (plugins.contains(file.getName().replace(".jar", ""))) continue;

                file.delete().execute();
                System.out.println(Log.Color.RED.getColor() + "Deleted plugin " + file.getName() + " from server " + serverName + "!");
            }
        } catch (Exception e) {
            System.out.print(Log.Color.RED);
            e.printStackTrace();
        }
    }

    private Thread checkPlugins(String pluginName, String serverName, Directory pluginsDir, Directory updateDir) {
        Thread thread = new Thread(
            () -> {
                File plugin = new File("C:/Users/jason/OneDrive/Documents/MC Server Plugins/Plugins/" + pluginName + ".jar");
                if (!plugin.exists()) {
                    System.out.println(Log.Color.RED.getColor() + "Plugin " + pluginName + " not found! Ensure it's spelled correctly in the CSV file.");
                    return;
                }

                OffsetDateTime localLastModified = OffsetDateTime.ofInstant(
                    Instant.ofEpochMilli(plugin.lastModified()),
                    ZoneId.of("America/Denver"));

                // See if the plugin already exists in the plugins directory
                Optional<com.mattmalec.pterodactyl4j.client.entities.File> remotePlugin = pluginsDir.getFileByName(
                    pluginName + ".jar");

                // If it does, compare the last modified date and only upload an update if necessary
                if (remotePlugin.isPresent()) {
                    if (remotePlugin.get().getModifedDate().isBefore(localLastModified)) uploadPlugin(
                        plugin,
                        updateDir,
                        serverName,
                        "updated");

                    // If it doesn't, upload it directly to the plugins directory
                } else uploadPlugin(plugin, pluginsDir, serverName, "new");

            }, "Update Plugin - " + pluginName);

        thread.start();
        return thread;
    }

    private void uploadPlugin(File plugin, Directory location, String serverName, String fileType) {
        String fileName = plugin.getName();
        System.out.println(Log.Color.YELLOW.getColor() + "Uploading " + fileType + " plugin " + fileName + " to server " + serverName + "...");

        try {
            location.upload().addFile(plugin).timeout(1, TimeUnit.MINUTES).execute();
        } catch (Exception e) {
            System.out.println(Log.Color.RED.getColor() + "Failed to upload the " + fileType + " " + fileName + " plugin to the " + serverName + " server! " + e.getMessage());
            return;
        }

        System.out.println(Log.Color.GREEN.getColor() + "Successfully uploaded the " + fileType + " " + fileName + " plugin to the " + serverName + " server!");
    }
}
