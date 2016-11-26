package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Aidar Shaifutdinov.
 */
public class SingularityTank implements Algorithm {

    private int teamId;
    private final int MAX_X = 64;
    private final int MAX_Y = 36;
    private Map<Integer, Position> oldPos = new HashMap<>();
    private boolean firstRun = true;
    private boolean leftResp = false;

    @Override
    public void setMyId(int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        List<Tank> tanks = mapState.getTanks(teamId);
        if (firstRun) {
            leftResp = tanks.get(0).getX() < MAX_X / 2;
            firstRun = false;
        }
        Tank first = getFirst(tanks);
        Tank second = getSecond(tanks);
        // remove special tanks
        tanks.removeIf(t -> t.getId() == first.getId());
        tanks.removeIf(t -> t.getId() == second.getId());
        //TODO ebash!!!
        List<TankMove> tankMoves = new ArrayList<>();
        List<Tank> didntDoAnything;
        if (firstRun) {
            tanks.forEach(t -> oldPos.put(t.getId(), t.getPosition()));
        } else {
            didntDoAnything = tanks.stream()
                    .filter(t -> t.getPosition().equals(oldPos.get(t.getId()).getPosition())
                            && t.getDir() == t.getOldDir()).collect(Collectors.toList());
        }
        didntDoAnything
        tanks.stream().map(t ->
                new TankMove(t.getId(), leftResp ? (byte) 2 : (byte) 4, true)
        ).collect(Collectors.toList());
        if (leftResp) {
            tankMoves.add(new TankMove(first.getId(), (byte) 1, true));
            tankMoves.add(new TankMove(second.getId(), (byte) 3, true));
        } else {
            tankMoves.add(new TankMove(first.getId(), (byte) 3, true));
            tankMoves.add(new TankMove(second.getId(), (byte) 1, true));
        }
        return tankMoves;
    }

    private List<TankMove> moveSpecial(Tank first, Tank second) {

    }

    private List<TankMove> moveCommon(List<Tank> tanks) {

    }

    private Tank getFirst(List<Tank> tanks) {
        Comparator<? super Tank> comparator = getComparator();
        Optional<Tank> tank;
        if (leftResp) {
            tank = tanks.stream().min(comparator);
        } else {
            tank = tanks.stream().max(comparator);
        }
        return tank.get();
    }

    private Tank getSecond(List<Tank> tanks) {
        Comparator<? super Tank> comparator = getComparator();
        Optional<Tank> tank;
        if (leftResp) {
            tank = tanks.stream().max(comparator);
        } else {
            tank = tanks.stream().min(comparator);
        }
        return tank.get();
    }

    private Comparator<? super Tank> getComparator() {
        return (Comparator<Tank>) (o1, o2) -> {
            if (o1.getY() < o2.getY()) {
                return -1;
            }
            if (o1.getY() > o2.getY()) {
                return 1;
            }
            return 0;
        };
    }

}
