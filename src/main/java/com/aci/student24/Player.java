package com.aci.student24;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.Util;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.OutputMapState;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.List;

/**
 * Sample class, to show how your Algorithm will be executed.
 */
public class Player {
  private final int id;
  private final Algorithm algorithm;

  public Player(final int id) {
    this.id = id;
    algorithm = new Algorithm() { //There are a couple of examples in the project
      public void setMyId(int id) {
        //TODO
      }

      public List<TankMove> nextMoves(MapState mapState) {
        //TODO
        return null;
      }
    };
  }

  public void doPlay() {
    try {
      MapState map = Util.deserializeInitialMapState(readMsg());
      while (true) {
        final List<TankMove> moves = algorithm.nextMoves(map);
        sendMsg(Util.serializeMoves(moves));
        final OutputMapState mapState = Util.deserializeMapState(readMsg());
        if (mapState == null) {
          break;
        }
        map = mapState.getMap();
      }
      System.out.println("Algorithm has finished (move is empty)");
    } catch (final Exception e) {
      System.err.println("Algorithm has failed: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void sendMsg(final String s) {
    //do nothing, as there is no Server locally
  }

  private String readMsg() {
    //TODO: i.e., read line from log (JSON)
    return null;
  }

  public static void main(String[] args) {
    new Player(1).doPlay();
  }
}
