package com.gmail.necnionch.myapp.craftswitcherreportmodule.reporter;

import java.util.Map;
import java.util.UUID;

public interface IReporter {
    String getReporterName();

    Map<UUID, String> getOnlinePlayers();

    Integer getMaxPlayer();

    Double getTps();

    Boolean isStarted();

}
