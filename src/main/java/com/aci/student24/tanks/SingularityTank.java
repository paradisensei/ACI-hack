package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
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
    private boolean firstRun = true;
    private boolean leftResp = false;
    private MapState mapState;
    private List<Position> positionsOfIndestructibles;
    private List<Tank> allTanks;
    private Set<Integer> lastColumnTanksIds;


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
            lastColumnTanksIds = new HashSet<>();
        }

        List<TankMove> resultingMoves = new ArrayList<>();

        fillUpLastColumnTanks();
        List<Tank> lastColumnTanks = allTanks.stream()
                .filter(t -> lastColumnTanksIds.contains(t.getId()))
                .collect(Collectors.toList());

        if (!lastColumnTanks.isEmpty()) {
            tanks.removeAll(lastColumnTanks);
            List<TankMove> tankMovesLastColumn = moveLastColumnTanks(lastColumnTanks);
            resultingMoves.addAll(tankMovesLastColumn);
        }

        if (!tanks.isEmpty()) {
            Tank first = getFirst(tanks);
            Tank second = getSecond(tanks);
            // remove special tanks
            tanks.removeIf(t -> t.getId() == first.getId());
            tanks.removeIf(t -> t.getId() == second.getId());

            // get moves
            List<TankMove> tankMovesSpecial = moveSpecial(first, second);
            List<TankMove> tankMovesCommon = moveCommon(tanks);

            resultingMoves.addAll(tankMovesSpecial);
            resultingMoves.addAll(tankMovesCommon);
        }
        return resultingMoves;
    }

    private List<TankMove> moveSpecial(Tank first, Tank second) {
        List<TankMove> tankMoves = new ArrayList<>();
        byte dir = getFirstSpecialDir(first);
        tankMoves.add(new TankMove(first.getId(), dir, shoot(first, dir)));
//        tankMoves.add(new TankMove(first.getId(), dir, true));
        dir = getSecondSpecialDir(second);
        tankMoves.add(new TankMove(second.getId(), dir, shoot(second, dir)));
//        tankMoves.add(new TankMove(second.getId(), dir, true));
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
            } else {
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
//                    return new TankMove(tank.getId(), dir, true);
                })
                .collect(Collectors.toList());
    }

    private byte getCommonDir(Tank tank) {
        if (leftResp) {
            int nextX = tank.getX() + 1;
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(nextX, tank.getY()))) {
                return Direction.RIGHT;
            } else if (!lowerBound(nextX, tank.getY()) && !lowerCorner(tank)) {
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

    private boolean lowerCorner(Tank tank) {
        Optional<Position> lowIndOpt = positionsOfIndestructibles.stream()
                .filter(ind -> tank.getX() == ind.getX()
                        && ind.getY() > tank.getY()).min((o1, o2) -> {
                    if (o1.getY() < o2.getY()) {
                        return -1;
                    }
                    if (o1.getY() > o2.getY()) {
                        return 1;
                    }
                    return 0;
                });
        if (!lowIndOpt.isPresent()) {
            return false;
        }
        Position lowInd = lowIndOpt.get();
        Optional<Position> lowRightIndOpt = positionsOfIndestructibles.stream()
                .filter(ind -> lowInd.getX() + 1 == ind.getX() && lowInd.getY() == ind.getY())
                .findAny();
        if (!lowRightIndOpt.isPresent()) {
            return false;
        }
        Position lowRightInd = lowRightIndOpt.get();
        Optional<Position> lowRightUpperIndOpt = positionsOfIndestructibles.stream()
                .filter(ind -> lowRightInd.getX() == ind.getX() && lowRightInd.getY() - 1 == ind.getY())
                .findAny();
        return lowRightUpperIndOpt.isPresent();
    }

    private boolean upperCorner(Tank tank) {
        Optional<Position> upIndOpt = positionsOfIndestructibles.stream()
                .filter(ind -> tank.getX() == ind.getX()
                        && ind.getY() < tank.getY()).max((o1, o2) -> {
                    if (o1.getY() < o2.getY()) {
                        return -1;
                    }
                    if (o1.getY() > o2.getY()) {
                        return 1;
                    }
                    return 0;
                });
        if (!upIndOpt.isPresent()) {
            return false;
        }
        Position upInd = upIndOpt.get();
        Optional<Position> upLeftIndOpt = positionsOfIndestructibles.stream()
                .filter(ind -> upInd.getX() - 1 == ind.getX() && upInd.getY() == ind.getY())
                .findAny();
        if (!upLeftIndOpt.isPresent()) {
            return false;
        }
        Position upLeftInd = upLeftIndOpt.get();
        Optional<Position> upLeftBottomIndOpt = positionsOfIndestructibles.stream()
                .filter(ind -> upLeftInd.getX() == ind.getX() && upLeftInd.getY() + 1 == ind.getY())
                .findAny();
        return upLeftBottomIndOpt.isPresent();
    }

    private void fillUpLastColumnTanks() {
        allTanks.forEach(tank -> {
            if (leftResp) {
                if (tank.getX() == (MAX_X - 1))
                    lastColumnTanksIds.add(tank.getId());
            } else {
                if (tank.getX() == 0) {
                    lastColumnTanksIds.add(tank.getId());
                }
            }
        });
    }

    private List<TankMove> moveLastColumnTanks(List<Tank> tanks) {
        if (leftResp) {
            return tanks.stream()
                    .map(t -> {
                        byte dir = getLastColumnDir(t);
                        return new TankMove(t.getId(), dir, shoot(t, dir));
//                        return new TankMove(t.getId(), dir, true);

                    })
                    .collect(Collectors.toList());
        } else {
            return tanks.stream()
                    .map(t -> {
                        byte dir = getLastColumnDir(t);
                        return new TankMove(t.getId(), dir, true);
                    })
                    .collect(Collectors.toList());
        }
    }

    private byte getLastColumnDir(Tank tank) {
        if (leftResp) {
            int nextY = tank.getY() + 1;
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), nextY))) {
                if (killChicken(tank)) {
                    return Direction.RIGHT;
                }
                return Direction.DOWN;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextY = tank.getY() - 1;
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), nextY))) {
                if (killChicken(tank)) {
                    return Direction.LEFT;
                }
                return Direction.UP;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    private boolean killChicken(Tank tank) {
        if (leftResp) {
            if (tank.getY() == 31 && tank.getX() >= 58) {
                return true;
            }
        } else {
            if (tank.getY() == 2 && tank.getX() <= 5) {
                return true;
            }
        }
        return false;
    }

    private boolean shoot(Tank tank, byte dir) {
        int x = tank.getX();
        int y = tank.getY();
        boolean found;
        switch (dir) {
            case Direction.LEFT:
                found = allTanks.stream()
                        .filter(t -> t.getY() == y && t.getX() < x)
                        .findAny().isPresent();
                if (found) {
                    return false;
                }
                break;
            case Direction.UP:
                found = allTanks.stream()
                        .filter(t -> t.getX() == x && t.getY() < y)
                        .findAny().isPresent();
                if (found) {
                    return false;
                }
                break;
            case Direction.RIGHT:
                found = allTanks.stream()
                        .filter(t -> t.getY() == y && t.getX() > x)
                        .findAny().isPresent();
                if (found) {
                    return false;
                }
                break;
            case Direction.DOWN:
                found = allTanks.stream()
                        .filter(t -> t.getX() == x && t.getY() > y)
                        .findAny().isPresent();
                if (found) {
                    return false;
                }
                break;
        }
        return true;
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
        for (int i = y + 1; i < MAX_Y; i++) {
            if (!positionsOfIndestructibles.contains(new Position(x, i))) {
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
