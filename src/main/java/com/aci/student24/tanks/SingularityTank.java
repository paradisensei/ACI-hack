package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Indestructible;
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
    private MapState mapState;

    @Override
    public void setMyId(int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        List<Tank> tanks = mapState.getTanks(teamId);

        this.mapState = mapState;

        if (firstRun) {
            leftResp = tanks.get(0).getX() < MAX_X / 2;
            firstRun = false;

            storePositions(tanks);
        }
        Tank first = getFirst(tanks);
        Tank second = getSecond(tanks);
        // remove special tanks
        tanks.removeIf(t -> t.getId() == first.getId());
        tanks.removeIf(t -> t.getId() == second.getId());
        // get moves
        List<TankMove> tankMovesSpecial = moveSpecial(first, second);
        List<TankMove> tankMovesCommon = moveCommon(tanks);
        tankMovesCommon.addAll(tankMovesSpecial);
        return tankMovesCommon;
    }

    private void staff() {
        //TODO ebash!!!
        List<TankMove> tankMoves = new ArrayList<>();
        List<Tank> didntDoAnything;
        else{
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

    private void storePositions(List<Tank> tanks) {
        tanks.forEach(t -> oldPos.put(t.getId(), t.getPosition()));
    }

    private List<TankMove> moveSpecial(Tank first, Tank second) {

    }

    private List<TankMove> moveCommon(List<Tank> tanks) {
        return tanks.stream().map(tank -> new TankMove(tank.getId(), getCommonDir(tank), true)).collect(Collectors.toList());
    }

    // 2, 3, 1
    private byte getCommonDir(Tank tank) {
        List<Position> positionsOfIndestructibles = mapState.getIndestructibles().stream()
                .map(Position::getPosition)
                .collect(Collectors.toList());

        if (leftResp) {
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), tank.getY() + 1))) {
                return 2;
                // if there is no obstacle to the bottom
            } else if (!positionsOfIndestructibles.contains(new Position(tank.getX() + 1, tank.getY()))) {
                return 3;
            } else {
                return 1;
            }
        } else {
            // if there is no obstacle to the left
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), tank.getY() - 1))) {
                return 4;
                // if there is no obstacle to the upper
            } else if (!positionsOfIndestructibles.contains(new Position(tank.getX() - 1, tank.getY()))) {
                return 1;
            } else {
                return 3;
            }
        }
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
