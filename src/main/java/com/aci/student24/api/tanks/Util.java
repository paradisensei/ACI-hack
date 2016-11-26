package com.aci.student24.api.tanks;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.OutputMapState;
import com.aci.student24.api.tanks.state.TankMove;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static MapState deserializeInitialMapState(final String s) {
    if (s == null || "".equals(s)) {
      System.err.println("Received empty map state");
      return null;
    }
    if (s.startsWith("ERROR")) {
      throw new RuntimeException(String.format("Server has returned: %s\nExiting!%n", s));
    }
    try {
      return objectMapper.readValue(s, MapState.class);
    } catch (IOException e) {
      System.err.println("Cannot parse initial MapState: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public static OutputMapState deserializeMapState(final String s) {
    if (s == null || "".equals(s)) {
      System.err.println("Received empty map state");
      return null;
    }
    if (s.startsWith("ERROR")) {
      throw new RuntimeException(String.format("Server has returned: %s\nExiting!%n", s));
    }
    try {
      return objectMapper.readValue(s, OutputMapState.class);
    } catch (final Exception e) {
      System.err.println("Cannot parse OutputMapState: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public static String serializeMoves(final List<TankMove> moves) {
    try {
      return objectMapper.writeValueAsString(moves);
    } catch (final Exception e) {
      System.err.println("Cannot serialize tank moves: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public static String serializeUserMap(Map<Integer, String> users) {
    try {
      return objectMapper.writeValueAsString(users);
    } catch (final Exception e) {
      System.err.println("Cannot serialize user map: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }
}
