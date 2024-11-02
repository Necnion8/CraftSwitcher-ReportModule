package com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.data;

import com.gmail.necnionch.myapp.craftswitcherreportmodule.socket.SerializableData;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatusData implements SerializableData {
    public Double tps;
    public Map<String, String> players;
    @SerializedName("max_players")
    public Integer maxPlayers;
    @SerializedName("cpu_usage")
    public Double cpuUsage;
    @SerializedName("total_memory")
    public Long totalMemory;
    @SerializedName("free_memory")
    public Long freeMemory;
    @SerializedName("max_memory")
    public Long maxMemory;

//    @Override
//    public String getDataKey() {
//        return "status";
//    }

    public StatusData(
            Double tps,
            Map<UUID, String> players,
            Integer maxPlayers,
            Double cpuUsage,
            Long totalMemory,
            Long freeMemory,
            Long maxMemory
    ) {
        this.tps = tps;
        this.maxPlayers = maxPlayers;
        if (players != null) {
            this.players = players.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
        } else {
            this.players = null;
        }

        this.cpuUsage = cpuUsage;
        this.totalMemory = totalMemory;
        this.freeMemory = freeMemory;
        this.maxMemory = maxMemory;
    }


}
