package com.aci.student24.tanks;

import com.aci.student24.api.tanks.Algorithm;
import com.aci.student24.api.tanks.objects.Position;
import com.aci.student24.api.tanks.objects.Tank;
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

    @Override
    public void setMyId(int id) {
        teamId = id;
    }

    @Override
    public List<TankMove> nextMoves(MapState mapState) {
        List<Tank> tanks = mapState.getTanks(teamId);
        Tank first, second;
        Comparator<? super Tank> tankComparator = (Comparator<Tank>) (o1, o2) -> {
            if (o1.getY() < o2.getY()) {
                return -1;
            }
            if (o1.getY() > o2.getY()) {
                return 1;
            }
            return 0;
        };
        boolean leftResp = tanks.get(0).getX() < MAX_X / 2;
        if (leftResp) {
            Optional<Tank> bottomTank = tanks.stream().max(tankComparator);
            Optional<Tank> upperTank = tanks.stream().min(tankComparator);
            first = upperTank.get();
            second = bottomTank.get();
        } else {
            Optional<Tank> bottomTank = tanks.stream().max(tankComparator);
            Optional<Tank> upperTank = tanks.stream().min(tankComparator);
            first = bottomTank.get();
            second = upperTank.get();
        }
        tanks.remove(first);
        tanks.remove(second);
        List<TankMove> tankMoves = new ArrayList<>();
        tanks.stream().map(t ->
            new TankMove(t.getId(), leftResp ? (byte) 2 : (byte) 4, true)
        ).collect(Collectors.toList());

    }
}
