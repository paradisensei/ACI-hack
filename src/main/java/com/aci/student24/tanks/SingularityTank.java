package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Indestructible;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/**
 * @author Aidar Shaifutdinov.
 */
public class SingularityTank implements Algorithm {

    private int teamId;
    private final int MAX_X = 64;
    private final int MAX_Y = 36;
    private Map<Integer, Position> oldPos = new HashMap<>();
    private Map<Integer, Boolean> changedDir = new HashMap<>();
    private boolean firstRun = true;
    private boolean leftResp = false;
    private MapState mapState;

    @Override
    public void setMyId(int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        this.mapState = mapState;
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
        List<TankMove> tankMoves = new ArrayList<>();
        //TODO use it!
        List<Indestructible> indestructibles = mapState.getIndestructibles();
        if (leftResp) {
            if (firstRun) {
                tankMoves.add(new TankMove(first.getId(), Direction.RIGHT, true));
                tankMoves.add(new TankMove(second.getId(), Direction.RIGHT, true));
                changedDir.put(first.getId(), false);
                changedDir.put(second.getId(), false);
            } else {
                int oldDir = first.getOldDir();
                Position pos = oldPos.get(first.getId());
                if (pos.getY() == first.getY() && pos.getX() == first.getX()) {
                    if (!changedDir.get(first.getId())) {
                        changedDir.put(first.getId(), true);
                        if (oldDir == Direction.RIGHT) {
                            tankMoves.add(new TankMove(first.getId(), Direction.UP, true));
                        } else if (oldDir == Direction.UP) {
                            tankMoves.add(new TankMove(first.getId(), Direction.RIGHT, true));
                        }
                    } else {
                        changedDir.put(first.getId(), false);
                        tankMoves.add(new TankMove(first.getId(), (byte) oldDir, true));
                    }
                } else {
                    tankMoves.add(new TankMove(first.getId(), (byte) oldDir, true));
                }
                oldDir = second.getOldDir();
                pos = oldPos.get(second.getId());
                if (pos.getY() == second.getY() && pos.getX() == second.getX()) {
                    if (!changedDir.get(second.getId())) {
                        changedDir.put(second.getId(), true);
                        if (oldDir == Direction.RIGHT) {
                            tankMoves.add(new TankMove(second.getId(), Direction.DOWN, true));
                        } else if (oldDir == Direction.DOWN) {
                            tankMoves.add(new TankMove(second.getId(), Direction.RIGHT, true));
                        }
                    } else {
                        changedDir.put(second.getId(), false);
                        tankMoves.add(new TankMove(second.getId(), (byte) oldDir, true));
                    }
                } else {
                    tankMoves.add(new TankMove(second.getId(), (byte) oldDir, true));
                }
            }
        } else {
            if (firstRun) {
                tankMoves.add(new TankMove(first.getId(), Direction.LEFT, true));
                tankMoves.add(new TankMove(second.getId(), Direction.LEFT, true));
                changedDir.put(first.getId(), false);
                changedDir.put(second.getId(), false);
            } else {
                int oldDir = first.getOldDir();
                Position pos = oldPos.get(first.getId());
                if (pos.getY() == first.getY() && pos.getX() == first.getX()) {
                    if (!changedDir.get(first.getId())) {
                        changedDir.put(first.getId(), true);
                        if (oldDir == Direction.LEFT) {
                            tankMoves.add(new TankMove(first.getId(), Direction.DOWN, true));
                        } else if (oldDir == Direction.DOWN) {
                            tankMoves.add(new TankMove(first.getId(), Direction.LEFT, true));
                        }
                    } else {
                        changedDir.put(first.getId(), false);
                        tankMoves.add(new TankMove(first.getId(), (byte) oldDir, true));
                    }
                } else {
                    tankMoves.add(new TankMove(first.getId(), (byte) oldDir, true));
                }
                oldDir = second.getOldDir();
                pos = oldPos.get(second.getId());
                if (pos.getY() == second.getY() && pos.getX() == second.getX()) {
                    if (!changedDir.get(second.getId())) {
                        changedDir.put(second.getId(), true);
                        if (oldDir == Direction.LEFT) {
                            tankMoves.add(new TankMove(second.getId(), Direction.UP, true));
                        } else if (oldDir == Direction.UP) {
                            tankMoves.add(new TankMove(second.getId(), Direction.LEFT, true));
                        }
                    } else {
                        changedDir.put(second.getId(), false);
                        tankMoves.add(new TankMove(second.getId(), (byte) oldDir, true));
                    }
                } else {
                    tankMoves.add(new TankMove(second.getId(), (byte) oldDir, true));
                }
            }
        }
    }

    private List<TankMove> moveCommon(List<Tank> tanks) {
        return tanks.stream().map(tank -> new TankMove(tank.getId(), getCommonDir(tank), true)).collect(Collectors.toList());
    }

    private byte getCommonDir(Tank tank) {
        List<Position> positionsOfIndestructibles = mapState.getIndestructibles().stream()
                .map(Position::getPosition)
                .collect(Collectors.toList());

        if (leftResp) {
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), tank.getY() + 1))) {
                return Direction.RIGHT;
                // if there is no obstacle to the bottom
            } else if (!positionsOfIndestructibles.contains(new Position(tank.getX() + 1, tank.getY()))) {
                return Direction.DOWN;
            } else {
                return Direction.UP;
            }
        } else {
            // if there is no obstacle to the left
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), tank.getY() - 1))) {
                return Direction.LEFT;
                // if there is no obstacle to the upper
            } else if (!positionsOfIndestructibles.contains(new Position(tank.getX() - 1, tank.getY()))) {
                return Direction.UP;
            } else {
                return Direction.DOWN;
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
