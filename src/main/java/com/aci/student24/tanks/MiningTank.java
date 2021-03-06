package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Brick;
import com.aci.student24.api.tanks.objects.Indestructible;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
import com.aci.student24.api.tanks.state.Direction;
import com.aci.student24.api.tanks.state.MapState;
import com.aci.student24.api.tanks.state.Size;
import com.aci.student24.api.tanks.state.TankMove;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

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
    private List<Position> positionsOfBricks;
    private List<Tank> allTanks;

    private Tank defender;
    private Position enemy;
    private Position base;

    // keep track of number of iterations of the game
    private int iter = 0;
    private final int FIRST_ITER = 7;

    @Override
    public void setMyId(int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        iter++;

        // get all our tanks
        List<Tank> tanks = mapState.getTanks(teamId);
        allTanks = new ArrayList<>(tanks);

        // find out where resp is & choose DEFENDER tank
        if (firstRun) {
            // set size of current map
            Size mapSize = mapState.getSize();
            MAX_X = mapSize.getWidth();
            MAX_Y = mapSize.getHeight();

            // set position of enemy's base (chicken to kill)
            enemy = mapState.getBases()
                    .stream()
                    .filter(b -> b.getTeamId() != teamId)
                    .findFirst().get().getPosition();

            try {
                // set position of our base
                base = mapState.getBases()
                        .stream()
                        .filter(b -> b.getTeamId() == teamId)
                        .findFirst().get().getPosition();
            } catch (Exception ignored) {
            }

            leftResp = tanks.get(0).getX() < MAX_X / 2;
            firstRun = false;

            // get indestructibles & check for null
            List<Indestructible> indestructibles = mapState.getIndestructibles();
            if (indestructibles == null) {
                positionsOfIndestructibles = Collections.emptyList();
            } else {
                positionsOfIndestructibles = mapState.getIndestructibles()
                        .stream()
                        .map(Position::getPosition)
                        .collect(Collectors.toList());
            }

            // get bricks & check for null
            List<Brick> bricks = mapState.getBricks();
            if (bricks == null) {
                positionsOfBricks = Collections.emptyList();
            } else {
                positionsOfBricks = mapState.getBricks()
                        .stream()
                        .map(Position::getPosition)
                        .collect(Collectors.toList());
            }

            // choose DEFENDER tank to be the farthest from enemy's base
            int enemyX = enemy.getX();
            int enemyY = enemy.getY();

            defender = tanks.stream().max((t1, t2) -> {
                double dist1 = Math.pow(t1.getX() - enemyX, 2) + Math.pow(t1.getY() - enemyY, 2);
                double dist2 = Math.pow(t2.getX() - enemyX, 2) + Math.pow(t2.getY() - enemyY, 2);
                return Double.compare(dist1, dist2);
            }).get();
        }

        List<TankMove> resultingMoves = new ArrayList<>();
        allTanks.removeIf(t -> t.getId() == defender.getId());
        tanks.removeIf(t -> t.getId() == defender.getId());
        resultingMoves.add(moveDefender(mapState));

        List<Tank> lastColumnTanks = getLastColumnTanks();

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
        try {

            List<Tank> enemies = mapState.getTanks()
                    .stream()
                    .filter(tank -> tank.getTeamId() != teamId)
                    .collect(Collectors.toList());
            System.out.println("SIZE: " + enemies.size());

            int defenderX = defender.getX();
            int defenderY = defender.getY();

            /*
            // enemy can kill
            boolean isDangerous = false;
            for (Tank enemy : enemies) {
                if (enemy.getX() == defenderX) {
                    isDangerous = true;
                }
            }

            if (isDangerous) {
                if (leftResp) {
                    return new TankMove(defender.getId(), Direction.RIGHT, true);
                } else {
                    return new TankMove(defender.getId(), Direction.LEFT, true);
                }
            }
            */

            //no enemy who can kill then
            //getNearest enemy position and direction
            int minDist = Integer.MAX_VALUE;
            int nearestEnemyId = -1;

            for (Tank enemy : enemies) {
                int enemyX = enemy.getX();
                int enemyY = enemy.getY();

                int dist = (int) (Math.pow(enemyX - defenderX, 2) + Math.pow(enemyY - defenderY, 2));

                if (dist < minDist) {
                    minDist = dist;
                    nearestEnemyId = enemy.getId();
                }
            }

            if (minDist < 18) {

                int finalNearestEnemyId = nearestEnemyId;
                Tank nearestEnemy = enemies
                        .stream()
                        .filter(tank -> tank.getId() == finalNearestEnemyId)
                        .findAny().get();

                int enemyX = nearestEnemy.getX();
                int enemyY = nearestEnemy.getY();
                byte enemyDir = nearestEnemy.getDir();

                // watch his direction and fire
                if (leftResp) {
                    //if enemy higher
                    if (enemyY < defenderY) {

                        //enemy is on the right
                        if (enemyX > defenderX) {

                            if (enemyDir == Direction.LEFT) {
                                if (defender.getDir() != Direction.UP) {
                                    return new TankMove(defender.getId(), Direction.UP, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                            if (enemyDir == Direction.DOWN) {
                                if (defender.getDir() != Direction.RIGHT) {
                                    return new TankMove(defender.getId(), Direction.RIGHT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                            //enemy is on the left
                        } else {

                            if (enemyDir == Direction.RIGHT) {
                                if (defender.getDir() != Direction.UP) {
                                    return new TankMove(defender.getId(), Direction.UP, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                            if (enemyDir == Direction.DOWN) {
                                if (defender.getDir() != Direction.LEFT) {
                                    return new TankMove(defender.getId(), Direction.LEFT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                        }

                        //if downer
                    } else if (enemyY > defenderY) {

                        if (enemyX > defenderX) {

                            if (enemyDir == Direction.LEFT) {
                                if (defender.getDir() != Direction.DOWN) {
                                    return new TankMove(defender.getId(), Direction.DOWN, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                            if (enemyDir == Direction.UP) {
                                if (defender.getDir() != Direction.RIGHT) {
                                    return new TankMove(defender.getId(), Direction.RIGHT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                        } else {

                            if (enemyDir == Direction.RIGHT) {
                                if (defender.getDir() != Direction.DOWN) {
                                    return new TankMove(defender.getId(), Direction.DOWN, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                            if (enemyDir == Direction.UP) {
                                if (defender.getDir() != Direction.LEFT) {
                                    return new TankMove(defender.getId(), Direction.LEFT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                        }
                        //on the same line
                    } else {
                        return new TankMove(defender.getId(), Direction.DOWN, shoot(defender, defender.getDir()));
                    }
                } else {
                    //if enemy higher
                    if (enemyY < defenderY) {

                        if (enemyX < defenderX) {

                            if (enemyDir == Direction.RIGHT) {
                                if (defender.getDir() != Direction.UP) {
                                    return new TankMove(defender.getId(), Direction.UP, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                            if (enemyDir == Direction.DOWN) {
                                if (defender.getDir() != Direction.LEFT) {
                                    return new TankMove(defender.getId(), Direction.LEFT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                        } else {

                            if (enemyDir == Direction.LEFT) {
                                if (defender.getDir() != Direction.UP) {
                                    return new TankMove(defender.getId(), Direction.UP, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }

                            }

                            if (enemyDir == Direction.DOWN) {
                                if (defender.getDir() != Direction.RIGHT) {
                                    return new TankMove(defender.getId(), Direction.RIGHT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                        }

                        //if downer
                    } else if (enemyY > defenderY) {

                        if (enemyX < defenderX) {

                            if (enemyDir == Direction.RIGHT) {
                                if (defender.getDir() != Direction.DOWN) {
                                    return new TankMove(defender.getId(), Direction.DOWN, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));

                                }
                            }

                            if (enemyDir == Direction.UP) {
                                if (defender.getDir() != Direction.LEFT) {
                                    return new TankMove(defender.getId(), Direction.LEFT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                        } else {

                            if (enemyDir == Direction.LEFT) {
                                if (defender.getDir() != Direction.DOWN) {
                                    return new TankMove(defender.getId(), Direction.DOWN, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }

                            }

                            if (enemyDir == Direction.UP) {
                                if (defender.getDir() != Direction.RIGHT) {
                                    return new TankMove(defender.getId(), Direction.RIGHT, shoot(defender, defender.getDir()));
                                } else {
                                    return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
                                }
                            }

                        }
                        //on the same line
                    } else {
                        return new TankMove(defender.getId(), Direction.DOWN, shoot(defender, defender.getDir()));
                    }
                }
            } else {

                if (defender.getY() > MAX_Y / 2) {
                    return new TankMove(defender.getId(), Direction.UP, false);
                }

                if (defender.getY() < MAX_Y / 2) {
                    return new TankMove(defender.getId(), Direction.DOWN, false);
                }

            }
        } catch (Exception ignored) {
        }
        return new TankMove(defender.getId(), Direction.NO, shoot(defender, defender.getDir()));
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
        final int x = tank.getX();
        if (leftResp) {
            int nextX = x + 1;
            // if there is no obstacle in the straight direction (to the right)
            if (!straightObstacle(new Position(nextX, y))) {
                return Direction.RIGHT;
            } else if (!upperBound(x, nextX, y)) {
                return Direction.UP;
            } else if (!lowerBound(x, nextX, y)) {
                return Direction.DOWN;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = x - 1;
            // if there is no obstacle in the straight direction (to the left)
            if (!straightObstacle(new Position(nextX, y))) {
                return Direction.LEFT;
            } else if (!lowerBound(x, nextX, y)) {
                return Direction.DOWN;
            } else if (!upperBound(x, nextX, y)) {
                return Direction.UP;
            } else {
                return Direction.RIGHT;
            }
        }
    }

    private byte getSecondSpecialDir(Tank tank) {
        final int y = tank.getY();
        final int x = tank.getX();
        if (leftResp) {
            int nextX = x + 1;
            // if there is no obstacle in the straight direction (to the right)
            if (!straightObstacle(new Position(nextX, y))) {
                return Direction.RIGHT;
            } else if (!lowerBound(x, nextX, y)) {
                return Direction.DOWN;
            } else if (!upperBound(x, nextX, y)) {
                return Direction.UP;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = x - 1;
            // if there is no obstacle in the straight direction (to the left)
            if (!straightObstacle(new Position(nextX, y))) {
                return Direction.LEFT;
            } else if (!upperBound(x, nextX, y)) {
                return Direction.UP;
            } else if (!lowerBound(x, nextX, y)) {
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
        final int x = tank.getX();
        if (leftResp) {
            int nextX = x + 1;
            // if there is no obstacle to the right
            if (!straightObstacle(new Position(nextX, y))) {
                return Direction.RIGHT;
            } else if (!lowerBound(x, nextX, y)) {
                return Direction.DOWN;
            } else if (!upperBound(x, nextX, y)) {
                return Direction.UP;
            } else {
                return Direction.LEFT;
            }
        } else {
            int nextX = x - 1;
            // if there is no obstacle to the right
            if (!straightObstacle(new Position(nextX, y))) {
                return Direction.LEFT;
            } else if (!upperBound(x, nextX, y)) {
                return Direction.UP;
            } else if (!lowerBound(x, nextX, y)) {
                return Direction.DOWN;
            } else {
                return Direction.LEFT;
            }
        }
    }

    private boolean straightObstacle(Position position) {
        boolean flag = positionsOfIndestructibles.contains(position);
        if (base != null) {
            flag = flag || position.equals(base);
        }
        if (iter < FIRST_ITER) {
            flag = flag || positionsOfBricks.contains(position);
        }
        return flag;
    }

    /**
     * Checks is some tanks have reached another side of map
     */
    private List<Tank> getLastColumnTanks() {
        return allTanks.stream()
                .filter(t -> t.getX() == enemy.getX()).
                        collect(Collectors.toList());
    }

    /**
     * @param tanks 2 last column tanks
     * @return moves for 2 last column tanks
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
        int x = tank.getX();
        int y = tank.getY();
        int enemyY = enemy.getY();

        if (leftResp) {
            // find out right moving direction along Y axis
            int nextY;
            byte dir;
            if (y < enemyY) {
                nextY = y + 1;
                dir = Direction.DOWN;
            } else {
                nextY = y - 1;
                dir = Direction.UP;
            }
            if (!straightObstacle(new Position(x, nextY))) {
                return dir;
            } else {
                //TODO add obstacle avoidance for 2 last column tanks
                return Direction.LEFT;
            }
        } else {
            // find out right moving direction along Y axis
            int nextY;
            byte dir;
            if (y < enemyY) {
                nextY = y + 1;
                dir = Direction.DOWN;
            } else {
                nextY = y - 1;
                dir = Direction.UP;
            }
            if (!straightObstacle(new Position(x, nextY))) {
                return dir;
            } else {
                //TODO add obstacle avoidance for 2 last column tanks
                return Direction.RIGHT;
            }
        }
    }

    /**
     * @param tank current tank
     * @param dir  moving directory
     * @return whether should make a shot or not
     */
    private boolean shoot(Tank tank, byte dir) {
        // don`t shoot in the start of the game
        if (iter < FIRST_ITER) {
            return false;
        }
        if (base != null && abs(base.getPosition().getX() - tank.getX()) <= 1) {
            return false;
        }
        int x = tank.getX();
        int y = tank.getY();
        List<Position> allies;
        switch (dir) {
            case Direction.LEFT:
                allies = allTanks.stream()
                        .filter(t -> t.getY() == y && t.getX() < x)
                        .collect(Collectors.toList());
                break;
            case Direction.UP:
                allies = allTanks.stream()
                        .filter(t -> t.getX() == x && t.getY() < y)
                        .collect(Collectors.toList());
                break;
            case Direction.RIGHT:
                allies = allTanks.stream()
                        .filter(t -> t.getY() == y && t.getX() > x)
                        .collect(Collectors.toList());
                break;
            case Direction.DOWN:
                allies = allTanks.stream()
                        .filter(t -> t.getX() == x && t.getY() > y)
                        .collect(Collectors.toList());
                break;
            default:
                allies = Collections.emptyList();
        }
        if (allies.isEmpty()) {
            return true;
        }
        return allies.stream().anyMatch(p -> {
            int enemyY = enemy.getY();
            int pY = p.getY();
            return enemyY >= pY && enemyY <= y || enemyY >= y && enemyY <= pY;
        });
    }

    // true, if upper bound exists
    private boolean upperBound(int x, int nextX, int y) {
        if (positionsOfIndestructibles.contains(new Position(x, y - 1))) {
            return true;
        }
        for (int i = y - 1; i >= 0; i--) {
            if (!positionsOfIndestructibles.contains(new Position(nextX, i))) {
                return false;
            }
        }
        return true;
    }

    // true, if lower bound exists
    private boolean lowerBound(int x, int nextX, int y) {
        if (positionsOfIndestructibles.contains(new Position(x, y + 1))) {
            return true;
        }
        for (int i = y + 1; i < MAX_Y; i++) {
            if (!positionsOfIndestructibles.contains(new Position(nextX, i))) {
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
