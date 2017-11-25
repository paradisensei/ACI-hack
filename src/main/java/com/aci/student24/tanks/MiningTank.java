package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.Size;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Aidar Shaifutdinov.
 */
public class MiningTank implements Algorithm {

    private int teamId;
    private int MAX_X;
    private int MAX_Y;

    private boolean firstRun = true;
    private boolean leftResp = false;
    private List<Position> positionsOfIndestructibles;
    private List<Tank> allTanks;
    private Tank defender;

    @Override
    public void setMyId(int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        // set size of current map
        Size mapSize = mapState.getSize();
        MAX_X = mapSize.getWidth();
        MAX_Y = mapSize.getHeight();

        // get all our tanks
        List<Tank> tanks = mapState.getTanks(teamId);
        allTanks = new ArrayList<>(tanks);

        // find out where resp is & choose DEFENDER tank
        if (firstRun) {
            leftResp = tanks.get(0).getX() < MAX_X / 2;
            firstRun = false;
            positionsOfIndestructibles = mapState.getIndestructibles()
                    .stream()
                    .map(Position::getPosition)
                    .collect(Collectors.toList());
            //TODO maybe change logic behind choosing DEFENDER tank
            if (leftResp) {
                defender = allTanks
                        .stream()
                        .min(Comparator.comparingInt(Position::getY)).get();
            } else {
                defender = allTanks
                        .stream()
                        .max(Comparator.comparingInt(Position::getY)).get();
            }
        }

        //TODO maybe change strategy for DEFENDER tank
        List<TankMove> resultingMoves = new ArrayList<>();
        allTanks.removeIf(t -> t.getId() == defender.getId());
        tanks.removeIf(t -> t.getId() == defender.getId());
        resultingMoves.add(moveDefender(mapState));

        Set<Integer> lastColumnTanksIds = fillUpLastColumnTanks();
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

    private TankMove moveDefender(MapState mapState) {
        //TODO defender strategy

        //getNearest enemy position and direction
        List<Tank> enemies = mapState.getTanks().stream().filter(tank -> tank.getTeamId() != teamId)
                .collect(Collectors.toList());

        int defenderX = defender.getX();
        int defenderY = defender.getY();

        int minDist = Integer.MAX_VALUE;
        int nearestEnemyId = -1;

        for (Tank enemy : enemies) {
            int enemyX = enemy.getX();
            int enemyY = enemy.getY();

            int dist = (int) Math.abs(Math.sqrt(enemyX - defenderX) + Math.sqrt(enemyY - defenderY));

            if (dist < minDist) {
                minDist = dist;
                nearestEnemyId = enemy.getId();
            }
        }

        int finalNearestEnemyId = nearestEnemyId;
        Tank nearestEnemy = enemies.stream().filter(tank -> tank.getId() == finalNearestEnemyId).collect(Collectors.toList()).get(0);

        int enemyX = nearestEnemy.getX();
        int enemyY = nearestEnemy.getY();
        byte enemyDir = nearestEnemy.getDir();

        // watch his direction and fire
        if (leftResp) {
            //if enemy higher
            if (enemyY < defenderY) {

                if (enemyX > defenderX) {

                    if (enemyDir == Direction.LEFT) {
                        if (defender.getDir() != Direction.UP) {
                            return new TankMove(defender.getId(), Direction.UP, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.DOWN) {
                        if (defender.getDir() != Direction.RIGHT) {
                            return new TankMove(defender.getId(), Direction.RIGHT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                } else {

                    if (enemyDir == Direction.RIGHT) {
                        if (defender.getDir() != Direction.UP) {
                            return new TankMove(defender.getId(), Direction.UP, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.DOWN) {
                        if (defender.getDir() != Direction.LEFT) {
                            return new TankMove(defender.getId(), Direction.LEFT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                }

                //if downer
            } else if (enemyY > enemyX) {

                if (enemyX > defenderX) {

                    if (enemyDir == Direction.LEFT) {
                        if (defender.getDir() != Direction.DOWN) {
                            return new TankMove(defender.getId(), Direction.DOWN, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.UP) {
                        if (defender.getDir() != Direction.RIGHT) {
                            return new TankMove(defender.getId(), Direction.RIGHT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                } else {

                    if (enemyDir == Direction.RIGHT) {
                        if (defender.getDir() != Direction.DOWN) {
                            return new TankMove(defender.getId(), Direction.DOWN, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.UP) {
                        if (defender.getDir() != Direction.LEFT) {
                            return new TankMove(defender.getId(), Direction.LEFT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                }
                //on the same line
            } else {
                return new TankMove(defender.getId(), Direction.UP, true);
            }
        } else {
            //if enemy higher
            if (enemyY < defenderY) {

                if (enemyX < defenderX) {

                    if (enemyDir == Direction.RIGHT) {
                        if (defender.getDir() != Direction.UP) {
                            return new TankMove(defender.getId(), Direction.UP, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.DOWN) {
                        if (defender.getDir() != Direction.LEFT) {
                            return new TankMove(defender.getId(), Direction.LEFT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                } else {

                    if (enemyDir == Direction.LEFT) {
                        if (defender.getDir() != Direction.UP) {
                            return new TankMove(defender.getId(), Direction.UP, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.DOWN) {
                        if (defender.getDir() != Direction.RIGHT) {
                            return new TankMove(defender.getId(), Direction.RIGHT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                }

                //if downer
            } else if (enemyY > defenderY) {

                if (enemyX < defenderX) {

                    if (enemyDir == Direction.RIGHT) {
                        if (defender.getDir() != Direction.DOWN) {
                            return new TankMove(defender.getId(), Direction.DOWN, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.UP) {
                        if (defender.getDir() != Direction.LEFT) {
                            return new TankMove(defender.getId(), Direction.LEFT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                } else {

                    if (enemyDir == Direction.LEFT) {
                        if (defender.getDir() != Direction.DOWN) {
                            return new TankMove(defender.getId(), Direction.DOWN, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                    if (enemyDir == Direction.UP) {
                        if (defender.getDir() != Direction.RIGHT) {
                            return new TankMove(defender.getId(), Direction.RIGHT, true);
                        } else {
                            return new TankMove(defender.getId(), Direction.NO, true);
                        }
                    }

                }
                //on the same line
            } else {
                return new TankMove(defender.getId(), Direction.UP, true);
            }
        }
        return new TankMove(defender.getId(), Direction.NO, true);
    }

    private List<TankMove> moveSpecial(Tank first, Tank second) {
        List<TankMove> tankMoves = new ArrayList<>();
        byte dir = getFirstSpecialDir(first);
        tankMoves.add(new TankMove(first.getId(), dir, shoot(first, dir)));
        dir = getSecondSpecialDir(second);
        tankMoves.add(new TankMove(second.getId(), dir, shoot(second, dir)));
        return tankMoves;
    }

    private byte getFirstSpecialDir(Tank tank) {
        final int y = tank.getY();
        if (leftResp) {
            int nextX = tank.getX() + 1;
            // if there is no obstacle in the straight direction (to the right)
            if (!positionsOfIndestructibles.contains(new Position(nextX, y))) {
                return Direction.RIGHT;
            } else if (!upperBound(nextX, y)) {
                return Direction.UP;
            } else if (!lowerBound(nextX, y)) {
                return Direction.DOWN;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = tank.getX() - 1;
            // if there is no obstacle in the straight direction (to the left)
            if (!positionsOfIndestructibles.contains(new Position(nextX, y))) {
                return Direction.LEFT;
            } else if (!lowerBound(nextX, y)) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, y)) {
                return Direction.UP;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    private byte getSecondSpecialDir(Tank tank) {
        final int y = tank.getY();
        if (leftResp) {
            int nextX = tank.getX() + 1;
            // if there is no obstacle in the straight direction (to the right)
            if (!positionsOfIndestructibles.contains(new Position(nextX, y))) {
                return Direction.RIGHT;
            } else if (!lowerBound(nextX, y)) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, y)) {
                return Direction.UP;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = tank.getX() - 1;
            // if there is no obstacle in the straight direction (to the left)
            if (!positionsOfIndestructibles.contains(new Position(nextX, y))) {
                return Direction.LEFT;
            } else if (!upperBound(nextX, y)) {
                return Direction.UP;
            } else if (!lowerBound(nextX, y)) {
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
        final int y = tank.getY();
        if (leftResp) {
            int nextX = tank.getX() + 1;
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(nextX, y))) {
                return Direction.RIGHT;
                //TODO ??? && !lowerCorner(tank) upperCorner()..
            } else if (!lowerBound(nextX, y)) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, y)) {
                return Direction.UP;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = tank.getX() - 1;
            // if there is no obstacle to the right
            if (!positionsOfIndestructibles.contains(new Position(nextX, y))) {
                return Direction.LEFT;
            } else if (!lowerBound(nextX, y)) {
                return Direction.DOWN;
            } else if (!upperBound(nextX, y)) {
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

    /**
     * Checks is some tanks have reached another side of map
     */
    private Set<Integer> fillUpLastColumnTanks() {
        Set<Integer> lastColumnTanksIds = new HashSet<>();
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
        return lastColumnTanksIds;
    }

    /**
     * @param tanks last column tanks
     * @return moves for last column tanks
     */
    private List<TankMove> moveLastColumnTanks(List<Tank> tanks) {
        if (leftResp) {
            return tanks.stream()
                    .map(t -> {
                        byte dir = getLastColumnDir(t);
                        return new TankMove(t.getId(), dir, shoot(t, dir));

                    })
                    .collect(Collectors.toList());
        } else {
            return tanks.stream()
                    .map(t -> {
                        byte dir = getLastColumnDir(t);
                        return new TankMove(t.getId(), dir, shoot(t, dir));
                    })
                    .collect(Collectors.toList());
        }
    }

    /**
     * @param tank one of last column tanks
     * @return moving directory for last column tank
     */
    private byte getLastColumnDir(Tank tank) {
        if (leftResp) {
            int nextY = tank.getY() + 1;
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), nextY))) {
                //TODO define strategy for destroying enemy's base
//                if (killChicken(tank)) {
//                    return Direction.RIGHT;
//                }
                return Direction.DOWN;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextY = tank.getY() - 1;
            if (!positionsOfIndestructibles.contains(new Position(tank.getX(), nextY))) {
//                if (killChicken(tank)) {
//                    return Direction.LEFT;
//                }
                return Direction.UP;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    private boolean killChicken(Tank tank) {
        //TODO hardcode!!!
        if (leftResp) {
            if (tank.getY() == 33 && tank.getX() >= 58) {
                return true;
            }
        } else {
            if (tank.getY() == 2 && tank.getX() <= 5) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param tank current tank
     * @param dir  moving directory
     * @return whether should make a shot or not
     */
    private boolean shoot(Tank tank, byte dir) {
        int x = tank.getX();
        int y = tank.getY();
        boolean found = false;
        switch (dir) {
            case Direction.LEFT:
                found = allTanks.stream()
                        .anyMatch(t -> t.getY() == y && t.getX() < x);
                break;
            case Direction.UP:
                found = allTanks.stream()
                        .anyMatch(t -> t.getX() == x && t.getY() < y);
                break;
            case Direction.RIGHT:
                found = allTanks.stream()
                        .anyMatch(t -> t.getY() == y && t.getX() > x);
                break;
            case Direction.DOWN:
                found = allTanks.stream()
                        .anyMatch(t -> t.getX() == x && t.getY() > y);
                break;
        }
        return !found;
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

    /**
     * @param tanks available tanks
     * @return the first special tank to go alongside enemy's base
     */
    private Tank getFirst(List<Tank> tanks) {
        Comparator<? super Tank> comparator = Comparator.comparingInt(Position::getY);
        Optional<Tank> tank;
        if (leftResp) {
            tank = tanks.stream().min(comparator);
        } else {
            tank = tanks.stream().max(comparator);
        }
        return tank.get();
    }

    private Tank getSecond(List<Tank> tanks) {
        Comparator<? super Tank> comparator = Comparator.comparingInt(Position::getY);
        Optional<Tank> tank;
        if (leftResp) {
            tank = tanks.stream().max(comparator);
        } else {
            tank = tanks.stream().min(comparator);
        }
        return tank.get();
    }

}
