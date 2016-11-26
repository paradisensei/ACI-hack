package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Aidar Shaifutdinov.
 */
public class SingularityTank implements Algorithm {

    private int teamId;
    private final int MAX_X = 64;
    private final int MAX_Y = 36;
    private boolean firstRun = true;
    private boolean leftResp = false;
    private MapState mapState;
    private List<Position> positionsOfIndestructibles;
    private List<Tank> allTanks;

    @Override
    public void setMyId(int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        this.mapState = mapState;
        List<Tank> tanks = mapState.getTanks(teamId);
        allTanks = new ArrayList<>(tanks);
        if (firstRun) {
            leftResp = tanks.get(0).getX() < MAX_X / 2;
            firstRun = false;
            positionsOfIndestructibles = mapState.getIndestructibles().stream()
                    .map(Position::getPosition)
                    .collect(Collectors.toList());
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

    private List<TankMove> moveSpecial(Tank first, Tank second) {
        List<TankMove> tankMoves = new ArrayList<>();
        tankMoves.add(new TankMove(first.getId(), getFirstSpecialDir(first), true));
        tankMoves.add(new TankMove(second.getId(), getSecondSpecialDir(second), true));
        return tankMoves;
    }

    private byte getFirstSpecialDir(Tank tank) {
        if (leftResp) {
            int nextX = tank.getX() + 1;
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(nextX, tank.getY()))) {
                return Direction.RIGHT;
            } else if (!upperBound(nextX, tank.getY())) {
                return Direction.UP;
            } else if (!lowerBound(nextX, tank.getY())) {
                return Direction.DOWN;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = tank.getX() - 1;
            // if there is no obstacle to the left
            if (!positionsOfIndestructibles.contains(new Position(nextX, tank.getY()))) {
                return Direction.LEFT;
            } else if (!lowerBound(nextX, tank.getY())) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, tank.getY())) {
                return Direction.UP;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    private byte getSecondSpecialDir(Tank tank) {
        if (leftResp) {
            int nextX = tank.getX() + 1;
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(nextX, tank.getY()))) {
                return Direction.RIGHT;
            } else if (!lowerBound(nextX, tank.getY())) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, tank.getY())) {
                return Direction.UP;
            }  else {
                return Direction.LEFT;
            }
        } else {
            int nextX = tank.getX() - 1;
            // if there is no obstacle to the left
            if (!positionsOfIndestructibles.contains(new Position(nextX, tank.getY()))) {
                return Direction.LEFT;
            } else if (!upperBound(nextX, tank.getY())) {
                return Direction.UP;
            } else if (!lowerBound(nextX, tank.getY())) {
                return Direction.DOWN;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    private List<TankMove> moveCommon(List<Tank> tanks) {
        return tanks.stream()
                .map(tank -> {
                    byte dir = getCommonDir(tank);
                    return new TankMove(tank.getId(), dir, shoot(tank, dir));
                })
                .collect(Collectors.toList());
    }

    private byte getCommonDir(Tank tank) {
        if (leftResp) {
            int nextX = tank.getX() + 1;
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(nextX, tank.getY()))) {
                return Direction.RIGHT;
            } else if (!lowerBound(nextX, tank.getY())) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, tank.getY())) {
                return Direction.UP;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = tank.getX() - 1;
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(nextX, tank.getY()))) {
                return Direction.LEFT;
            } else if (!lowerBound(nextX, tank.getY())) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, tank.getY())) {
                return Direction.UP;
            } else {
                return Direction.LEFT;
            }
        }
    }

    private boolean shoot(Tank tank, byte dir) {

    }
    
    // true, if upper bound exists
    private boolean upperBound(int x, int y) {
        for (int i = y - 1; i >= 0; i--) {
            if (!positionsOfIndestructibles.contains(new Position(x, i))) {
                return false;
            }
        }
        return true;
    }

    // true, if lower bound exists
    private boolean lowerBound(int x, int y) {
        for(int i = y + 1; i < MAX_Y; i++) {
            if(!positionsOfIndestructibles.contains(new Position(x, i))) {
                return false;
            }
        }
        return true;
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
