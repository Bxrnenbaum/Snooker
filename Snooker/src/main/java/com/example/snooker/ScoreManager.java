package com.example.snooker;

import java.util.ArrayList;
import java.util.List;

public class ScoreManager {

    public static class TurnState {
        public Ball firstBallHit = null;
        public List<Ball> pottedBalls = new ArrayList<>();
        public boolean cueBallFouled = false;
    }

    private int score = 0;
    private boolean redRequired = true;
    private boolean phaseAtStartOfShot = true;
    private final Ball[] balls;
    private TurnState currentState = new TurnState();

    public ScoreManager(Ball[] balls) {
        this.balls = balls;
    }

    public void registerCollision(Ball b1, Ball b2) {
        if (b1.ballType == BallType.WHITE && currentState.firstBallHit == null) {
            currentState.firstBallHit = b2;
        } else if (b2.ballType == BallType.WHITE && currentState.firstBallHit == null) {
            currentState.firstBallHit = b1;
        }
    }

    public void lockPhaseForShot() {
        this.phaseAtStartOfShot = this.redRequired;
    }

    public void registerPocket(Ball ball) {
        if (ball.ballType == BallType.WHITE) {
            currentState.cueBallFouled = true;
        }
        if (!currentState.pottedBalls.contains(ball)) {
            currentState.pottedBalls.add(ball);
        }
    }

    public String processTurnEnd() {
        if (currentState.firstBallHit == null) {
            boolean wasCueBallFouled = currentState.cueBallFouled; // Save state flag
            resetAndRespotColors();

            if (!hasRedsRemaining()) {
                redRequired = false;
            }

            resetTurnState();

            if (wasCueBallFouled) {
                return applyFoul(4, "Foul: Cue ball potted");
            } else {
                return applyFoul(4, "Foul: Missed all balls");
            }
        }

        boolean hitWrongBallFirst = false;
        if (phaseAtStartOfShot && currentState.firstBallHit.ballType != BallType.RED) {
            hitWrongBallFirst = true;
        } else if (!phaseAtStartOfShot && currentState.firstBallHit.ballType == BallType.RED) {
            hitWrongBallFirst = true;
        }

        if (hitWrongBallFirst || currentState.cueBallFouled) {
            int penalty = calculatePenaltyValue(currentState.firstBallHit);
            resetAndRespotColors();
            resetTurnState();
            return applyFoul(penalty, "Foul: Illegally hit " + currentState.firstBallHit.ballType + " first");
        }

        //check for illegally potted balls
        for (Ball b : currentState.pottedBalls) {
            if (b == null) continue;
            boolean isIllegalPot = (phaseAtStartOfShot && b.ballType != BallType.RED) || (!phaseAtStartOfShot && b.ballType == BallType.RED);
            if (isIllegalPot) {
                int penalty = calculatePenaltyValue(b);
                resetAndRespotColors();
                resetTurnState();
                return applyFoul(penalty, "Foul: Illegally pocketed a " + b.ballType);
            }
        }

        if (!currentState.pottedBalls.isEmpty()) {
            int turnPoints = 0;

            for (Ball b : currentState.pottedBalls) {
                turnPoints += getBallValue(b.ballType);
            }

            score += turnPoints;

            if (redRequired) {
                redRequired = false;
            } else {
                respotColorsInList(currentState.pottedBalls);
                if (!hasRedsRemaining()) {
                    redRequired = false;
                } else {
                    redRequired = true;
                }
            }

            resetTurnState();
            return "Scored: +" + turnPoints;
        }

        if (!hasRedsRemaining()) {
            redRequired = false;
        }
        resetTurnState();
        return "safe";
    }

    private String applyFoul(int penalty, String message) {
        score -= penalty;
        return message + ". Penalty: -" + penalty;
    }

    private int calculatePenaltyValue(Ball triggerBall) {
        int maxVal = 4;
        if (triggerBall != null) {
            maxVal = Math.max(maxVal, getBallValue(triggerBall.ballType));
        }
        for (Ball b : currentState.pottedBalls) {
            maxVal = Math.max(maxVal, getBallValue(b.ballType));
        }
        return maxVal;
    }

    public int getBallValue(BallType type) {
        return switch (type) {
            case RED -> 1;
            case YELLOW -> 2;
            case GREEN -> 3;
            case BROWN -> 4;
            case BLUE -> 5;
            case PINK -> 6;
            case BLACK -> 7;
            default -> 0;
        };
    }

    private boolean hasRedsRemaining() {
        for (Ball b : balls) {
            if (b != null && b.isActive && b.ballType == BallType.RED) return true;
        }
        return false;
    }

    private void resetTurnState() {
        currentState.firstBallHit = null;
        currentState.pottedBalls.clear();
        currentState.cueBallFouled = false;
    }

    private void resetAndRespotColors() {
        respotColorsInList(currentState.pottedBalls);
        for (Ball b : currentState.pottedBalls) {
            if (b != null && b.ballType == BallType.WHITE) {
                b.setPosition(b.nominalPosition);
                b.velocity = new Vector2(0, 0);
                b.isActive = true;
                b.unfade();
            }
        }
    }

    private void respotColorsInList(List<Ball> potted) {
        for (Ball b : potted) {
            if (b != null && b.ballType != BallType.RED && b.ballType != BallType.WHITE) {
                b.isActive = true;
                b.unfade();
                b.velocity = new Vector2(0, 0);
                Vector2 targetSpot = b.nominalPosition;

                if (isSpotOccupied(targetSpot, b)) {
                    targetSpot = findAlternativeSpot(b);
                }
                b.setPosition(targetSpot);
            }
        }
    }

    private boolean isSpotOccupied(Vector2 spot, Ball currentBall) {
        double threshold = currentBall.radius * 2;
        for (Ball b : balls) {
            if (b == null || b == currentBall || !b.isActive) continue;
            if (b.position.distance(spot) < threshold) {
                return true;
            }
        }
        return false;
    }

    private Vector2 findAlternativeSpot(Ball currentBall) {
        BallType[] colorOrder = {BallType.BLACK, BallType.PINK, BallType.BLUE, BallType.BROWN, BallType.GREEN, BallType.YELLOW};

        for (BallType type : colorOrder) {
            Ball spotOwner = findBallByType(type);
            if (spotOwner != null && !isSpotOccupied(spotOwner.nominalPosition, currentBall)) {
                return spotOwner.nominalPosition;
            }
        }

        Vector2 fallback = currentBall.nominalPosition;
        while (isSpotOccupied(fallback, currentBall)) {
            fallback = fallback.sum(new Vector2(0, -((currentBall.radius * 2) + 1)));
        }
        return fallback;
    }

    private Ball findBallByType(BallType type) {
        for (Ball b : balls) {
            if (b != null && b.ballType == type) return b;
        }
        return null;
    }

    public int getScore() {
        return score;
    }

    public boolean isRedRequired() {
        return redRequired;
    }
}