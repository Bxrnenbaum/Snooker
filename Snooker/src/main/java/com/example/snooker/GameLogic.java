package com.example.snooker;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class GameLogic {

    private int subSteps = 8;

    String filePath = new java.io.File("config.properties").getAbsolutePath();
    private final ConfigReader config = new ConfigReader();

    private final int width; //Display width
    private final int height; //Display height
    private final Scene scene;
    private final Pane pane;

    //Internal coordinate space, used for physics calculations
    private final int BASE_WIDTH = 3200;
    private final int BASE_HEIGHT = 1600;

    private double scaleX;
    private double scaleY;

    private Ball[] balls = new Ball[22];
    private Cushion[] cushions;
    private Vector2[] holes;
    private float holeRadius = 45;

    private InputHandler inputHandler;
    private boolean mouseWasPressed;

    private Vector2 startingPoint;
    private Vector2 endPoint;

    private Line aimLine;

    private boolean showDebugLines = true;

    private Circle cueRing;
    private boolean ringVisible;

    Vector2[] positions = {
            new Vector2(592, 708),
            new Vector2(592, 754),
            new Vector2(592, 800),
            new Vector2(592, 846),
            new Vector2(592, 892),
            new Vector2(632, 869),
            new Vector2(632, 823),
            new Vector2(632, 777),
            new Vector2(632, 731),
            new Vector2(672, 846),
            new Vector2(672, 800),
            new Vector2(672, 754),
            new Vector2(712, 823),
            new Vector2(712, 777),
            new Vector2(752, 800),
            new Vector2(2560, 934),
            new Vector2(2560, 666),
            new Vector2(2560, 800),
            new Vector2(1600, 800),
            new Vector2(800, 800),
            new Vector2(292, 800),
            new Vector2(2700, 720)
    };

    public GameLogic(int width, int height, Scene scene, Pane pane) {
        this.width = width;
        this.height = height;
        this.scene = scene;
        this.pane = pane;

        this.scaleX = (double) width / BASE_WIDTH;
        this.scaleY = (double) height / BASE_HEIGHT;
    }


    private double toDisplayX(double baseX) {
        return baseX * scaleX;
    }

    private double toDisplayY(double baseY) {
        return baseY * scaleY;
    }

    private double toBaseX(double displayX) {
        return displayX / scaleX;
    }

    private double toBaseY(double displayY) {
        return displayY / scaleY;
    }

    private Vector2 toBase(Vector2 display) {
        return new Vector2(toBaseX(display.x), toBaseY(display.y));
    }


    public void onStart() {
        loadSubsteps();
        // Initialize the ballers
        balls[0] = new Ball(new Image("/redBall.png"), 23, positions[0], BallType.RED);
        balls[1] = new Ball(new Image("/redBall.png"), 23, positions[1], BallType.RED);
        balls[2] = new Ball(new Image("/redBall.png"), 23, positions[2], BallType.RED);
        balls[3] = new Ball(new Image("/redBall.png"), 23, positions[3], BallType.RED);
        balls[4] = new Ball(new Image("/redBall.png"), 23, positions[4], BallType.RED);
        balls[5] = new Ball(new Image("/redBall.png"), 23, positions[5], BallType.RED);
        balls[6] = new Ball(new Image("/redBall.png"), 23, positions[6], BallType.RED);
        balls[7] = new Ball(new Image("/redBall.png"), 23, positions[7], BallType.RED);
        balls[8] = new Ball(new Image("/redBall.png"), 23, positions[8], BallType.RED);
        balls[9] = new Ball(new Image("/redBall.png"), 23, positions[9], BallType.RED);
        balls[10] = new Ball(new Image("/redBall.png"), 23, positions[10], BallType.RED);
        balls[11] = new Ball(new Image("/redBall.png"), 23, positions[11], BallType.RED);
        balls[12] = new Ball(new Image("/redBall.png"), 23, positions[12], BallType.RED);
        balls[13] = new Ball(new Image("/redBall.png"), 23, positions[13], BallType.RED);
        balls[14] = new Ball(new Image("/redBall.png"), 23, positions[14], BallType.RED);

        balls[15] = new Ball(new Image("/greenBall.png"), 23, positions[15], BallType.YELLOW);
        balls[16] = new Ball(new Image("/yellowBall.png"), 23, positions[16], BallType.GREEN);
        balls[17] = new Ball(new Image("/brownBall.png"), 23, positions[17], BallType.BROWN);
        balls[18] = new Ball(new Image("/blueBall.png"), 23, positions[18], BallType.BLUE);
        balls[19] = new Ball(new Image("/pinkBall.png"), 23, positions[19], BallType.PINK);

        if (Math.random() <= .01) {
            balls[20] = new Ball(new Image("/ball2.png"), 23, positions[20], BallType.BLACK);
        } else {
            balls[20] = new Ball(new Image("/blackBall.png"), 23, positions[20], BallType.BLACK);
        }

        balls[21] = new Ball(new Image("/cueBall.png"), 23, positions[21], BallType.WHITE);
        for (Ball ball : balls) {
            if (ball == null) continue;
            if (ball.image != null) continue;
            ball.setImage(new Image("/ball2.png"));
        }

        inputHandler = new InputHandler(scene);

        aimLine = new Line();
        aimLine.setStrokeWidth(toDisplayX(5));
        pane.getChildren().add(aimLine);

        cushions = instantiateCushions(BASE_WIDTH, BASE_HEIGHT);
        holes = instantiateHoles(BASE_WIDTH);

        if (showDebugLines) {
            for (Cushion cushion : cushions) {
                for (LineSegment seg : cushion.segments) {
                    // Convert to display space for rendering
                    Line line = new Line(
                            toDisplayX(seg.a.x), toDisplayY(seg.a.y),
                            toDisplayX(seg.b.x), toDisplayY(seg.b.y)
                    );
                    line.setStroke(Color.WHITE);
                    line.setStrokeWidth(1.5);
                    pane.getChildren().add(line);
                }
            }
            for (Vector2 hole : holes) {
                double displayX = toDisplayX(hole.x);
                double displayY = toDisplayY(hole.y);

                double displayRadius = toDisplayX(holeRadius) - toDisplayX(0);

                Circle outline = new Circle(displayX, displayY, displayRadius);
                outline.setFill(Color.TRANSPARENT);
                outline.setStroke(Color.WHITE);
                outline.setStrokeWidth(1.5);

                Circle centerDot = new Circle(displayX, displayY, 2.0);
                centerDot.setFill(Color.WHITE);

                pane.getChildren().addAll(outline, centerDot);
            }
        }

        System.out.println("Substeps loaded: " + subSteps);
    }

    public void update(double deltaTime) {


        double friction = .45;
        double subDelta = deltaTime / subSteps;
        double frictionFactorPerSubStep = Math.pow(friction, subDelta);

        for (int step = 0; step < subSteps; step++) {
            for (Ball ball : balls) {
                if (ball == null) continue;
                ball.position = ball.position.sum(ball.velocity.scalar(subDelta));
                calculateWallCollisions(ball, cushions);
                potBall(ball, holes);
            }

            handleCollisions();

            for (Ball ball : balls) {
                if (ball == null) continue;
                ball.velocity = ball.velocity.scalar(frictionFactorPerSubStep);
            }
        }

        boolean areAllBallsStanding = true;
        for (Ball ball : balls) {
            if (ball == null || !ball.isActive) continue;
            if (ball.velocity.magnitude() >= 5 || balls[21].velocity.magnitude() >= 1) {
                areAllBallsStanding = false;
                break;
            }
        }

        if (areAllBallsStanding) {
            shootCueBall();
            showCueRing();
        } else {
            hideCueRing();
        }

        for (Ball ball : balls) {
            if (ball == null) continue;

            double displayX = toDisplayX(ball.position.x);
            double displayY = toDisplayY(ball.position.y);
            double displayRadius = ball.radius * scaleX;

            ball.imageView.setX(displayX - displayRadius);
            ball.imageView.setY(displayY - displayRadius);

            ball.imageView.setFitWidth(displayRadius * 2);
            ball.imageView.setFitHeight(displayRadius * 2);

            double speed = ball.velocity.magnitude();
            ball.imageView.setRotate(ball.imageView.getRotate() + (speed * deltaTime) % 360);
        }

        updateAimLine(areAllBallsStanding);

        for(Ball ball : balls){
            if(!ball.isActive && areAllBallsStanding){
                replaceBall(ball);
            }
        }
    }

    private void loadSubsteps() {
        config.load();
        subSteps = config.getInt("substeps", 8);
        if (subSteps < 1) subSteps = 1;
    }

    private void updateAimLine(boolean visible) {
        aimLine.setVisible(visible);
        if (!visible) return;

        boolean isCurrentlyPressed = inputHandler.isPressedMouse(MouseButton.PRIMARY);

        if (!isCurrentlyPressed || startingPoint == null) {
            aimLine.setVisible(false);
            return;
        }

        Vector2 currentMouseBase = toBase(inputHandler.getMousePosition());
        Vector2 startingPointBase = startingPoint;
        Vector2 cueBallPos = balls[21].position;

        Vector2 drag = currentMouseBase.difference(startingPointBase);
        Vector2 aim = drag.scalar(-1);
        double power = drag.magnitude() * 2;

        Vector2 aimEndBase = cueBallPos.sum(aim.normalize().scalar(power));

        aimLine.setStartX(toDisplayX(cueBallPos.x));
        aimLine.setStartY(toDisplayY(cueBallPos.y));
        aimLine.setEndX(toDisplayX(aimEndBase.x));
        aimLine.setEndY(toDisplayY(aimEndBase.y));
    }

    public void handleCollisions() {
        final double e = 0.95;
        final double minDist = 2 * balls[21].radius;
        final double minDistSq = minDist * minDist;

        for (int i = 0; i < balls.length; i++) {
            if (balls[i] == null) continue;

            for (int j = i + 1; j < balls.length; j++) {
                if (balls[j] == null) continue;

                double dx = balls[i].position.x - balls[j].position.x;
                double dy = balls[i].position.y - balls[j].position.y;
                double distSq = dx * dx + dy * dy;

                if (distSq >= minDistSq) continue;

                double dist = Math.sqrt(distSq);
                if (dist == 0) dist = 0.01;

                double nx = dx / dist;
                double ny = dy / dist;

                double dvx = balls[i].velocity.x - balls[j].velocity.x;
                double dvy = balls[i].velocity.y - balls[j].velocity.y;
                double speedAlongNormal = dvx * nx + dvy * ny;

                if (speedAlongNormal >= 0) continue;

                double jImpulse = -(1 + e) * speedAlongNormal / 2.0;

                balls[i].velocity.x += jImpulse * nx;
                balls[i].velocity.y += jImpulse * ny;
                balls[j].velocity.x -= jImpulse * nx;
                balls[j].velocity.y -= jImpulse * ny;

                double overlap = (minDist - dist) / 2.0;
                balls[i].position.x += overlap * nx;
                balls[i].position.y += overlap * ny;
                balls[j].position.x -= overlap * nx;
                balls[j].position.y -= overlap * ny;
            }
        }
    }

    public void shootCueBall() {
        boolean isCurrentlyPressed = inputHandler.isPressedMouse(MouseButton.PRIMARY);

        if (isCurrentlyPressed && !mouseWasPressed) {
            startingPoint = toBase(inputHandler.getMousePosition());
        }

        if (!isCurrentlyPressed && mouseWasPressed) {
            endPoint = toBase(inputHandler.getMousePosition());

            if (startingPoint != null) {
                balls[21].velocity = endPoint.difference(startingPoint).scalar(-7.5);
            }
        }

        mouseWasPressed = isCurrentlyPressed;
    }

    public Ball[] getBalls() {
        return balls;
    }

    public void calculateWallCollisions(Ball ball, Cushion[] cushions) {
        final double returnEnergy = 0.87;

        for (Cushion cushion : cushions) {
            for (LineSegment seg : cushion.segments) {
                Vector2 a = seg.a;
                Vector2 b = seg.b;

                double edgeX = b.x - a.x;
                double edgeY = b.y - a.y;
                double edgeLen = Math.sqrt(edgeX * edgeX + edgeY * edgeY);
                if (edgeLen < 1e-6) continue;

                double dx = ball.position.x - a.x;
                double dy = ball.position.y - a.y;

                double edgeUX = edgeX / edgeLen;
                double edgeUY = edgeY / edgeLen;
                double t = dx * edgeUX + dy * edgeUY;

                double clampedT = Math.max(0, Math.min(edgeLen, t));

                //find closest point on this segment to the ball
                double closestX = a.x + clampedT * edgeUX;
                double closestY = a.y + clampedT * edgeUY;

                //calculate distance to closest point
                double distVecX = ball.position.x - closestX;
                double distVecY = ball.position.y - closestY;
                double dist = Math.sqrt(distVecX * distVecX + distVecY * distVecY);
                if (dist == 0) dist = 0.001; // Avoid division by 0

                if (dist >= ball.radius) continue;

                //calc normals of cushion faces
                double nx = distVecX / dist;
                double ny = distVecY / dist;

                // prevent sticking to cushion
                double vDotN = ball.velocity.x * nx + ball.velocity.y * ny;
                if (vDotN >= 0) continue;

                // bounce physics
                ball.velocity.x -= (1 + returnEnergy) * vDotN * nx;
                ball.velocity.y -= (1 + returnEnergy) * vDotN * ny;

                // penetration corrections
                double penetration = ball.radius - dist;
                ball.position.x += penetration * nx;
                ball.position.y += penetration * ny;
            }
        }
    }

    public void potBall(Ball ball, Vector2[] holes) {
        for (Vector2 hole : holes) {
            double dist = ball.position.distance(hole);

            if (dist <= holeRadius) {
                ball.isPotting = true;

                Vector2 direction = hole.difference(ball.position).normalize();
                double pullStrength = 1.0 - (dist / holeRadius);
                double suckSpeed = 150.0;

                ball.velocity = direction.scalar(suckSpeed * pullStrength);
                ball.fade(2);

                if (dist <= holeRadius * 0.1) {
                    ball.velocity = Vector2.zero;
                    ball.isActive = false;
                    ball.isPotting = false;
                }

                return;
            }
        }

        ball.isPotting = false;
        ball.isActive = true;
    }

    public Cushion[] instantiateCushions(float bw, float bh) {
        float railX = bw * 0.04f;
        float railY = bh * 0.07f;

        float cornerJawX = bw * 0.02f;
        float cornerJawY = bh * 0.04f;
        float sideJawX = bw * 0.025f;
        float diagOffset = cornerJawX * -1f;

        float faceTop = railY;
        float faceBottom = bh - railY;
        float faceLeft = railX;
        float faceRight = bw - railX;
        float pocketCX = bw / 2f;

        Cushion topLeft = new Cushion(new LineSegment[]{
                new LineSegment(
                        new Vector2(faceLeft - diagOffset * 0.25f, faceTop + diagOffset * 0.7),
                        new Vector2(faceLeft + cornerJawX, faceTop)
                ),
                new LineSegment(
                        new Vector2(faceLeft + cornerJawX, faceTop),
                        new Vector2(pocketCX - sideJawX, faceTop)
                ),
                new LineSegment(
                        new Vector2(pocketCX - sideJawX, faceTop),
                        new Vector2(pocketCX - sideJawX * .55f, faceTop + diagOffset * 0.7f)
                )
        });

        Cushion topRight = new Cushion(new LineSegment[]{
                new LineSegment(
                        new Vector2(pocketCX + sideJawX * 0.55f, faceTop + diagOffset * 0.7f),
                        new Vector2(pocketCX + sideJawX, faceTop)
                ),
                new LineSegment(
                        new Vector2(pocketCX + sideJawX, faceTop),
                        new Vector2(faceRight - cornerJawX, faceTop)
                ),
                new LineSegment(
                        new Vector2(faceRight - cornerJawX, faceTop),
                        new Vector2(faceRight + diagOffset * 0.25f, faceTop + diagOffset * 0.7f)
                )
        });

        Cushion bottomLeft = new Cushion(new LineSegment[]{
                new LineSegment(
                        new Vector2(faceLeft - diagOffset * 0.25f, faceBottom - diagOffset * 0.7f),
                        new Vector2(faceLeft + cornerJawX, faceBottom)
                ),
                new LineSegment(
                        new Vector2(faceLeft + cornerJawX, faceBottom),
                        new Vector2(pocketCX - sideJawX, faceBottom)
                ),
                new LineSegment(
                        new Vector2(pocketCX - sideJawX, faceBottom),
                        new Vector2(pocketCX - sideJawX * 0.55f, faceBottom - diagOffset * 0.7f)
                )
        });

        Cushion bottomRight = new Cushion(new LineSegment[]{
                new LineSegment(
                        new Vector2(pocketCX + sideJawX * 0.55f, faceBottom - diagOffset * 0.7f),
                        new Vector2(pocketCX + sideJawX, faceBottom)
                ),
                new LineSegment(
                        new Vector2(pocketCX + sideJawX, faceBottom),
                        new Vector2(faceRight - cornerJawX, faceBottom)
                ),
                new LineSegment(
                        new Vector2(faceRight - cornerJawX, faceBottom),
                        new Vector2(faceRight + diagOffset * 0.25f, faceBottom - diagOffset * 0.7f)
                )
        });

        Cushion left = new Cushion(new LineSegment[]{
                new LineSegment(
                        new Vector2(faceLeft + diagOffset, faceTop + cornerJawY * 0.3f),
                        new Vector2(faceLeft, faceTop + cornerJawY)
                ),
                new LineSegment(
                        new Vector2(faceLeft, faceTop + cornerJawY),
                        new Vector2(faceLeft, faceBottom - cornerJawY)
                ),
                new LineSegment(
                        new Vector2(faceLeft, faceBottom - cornerJawY),
                        new Vector2(faceLeft + diagOffset, faceBottom - cornerJawY * 0.3f)
                )
        });

        Cushion right = new Cushion(new LineSegment[]{
                new LineSegment(
                        new Vector2(faceRight - diagOffset, faceTop + cornerJawY * 0.3f),
                        new Vector2(faceRight, faceTop + cornerJawY)
                ),
                new LineSegment(
                        new Vector2(faceRight, faceTop + cornerJawY),
                        new Vector2(faceRight, faceBottom - cornerJawY)
                ),
                new LineSegment(
                        new Vector2(faceRight, faceBottom - cornerJawY),
                        new Vector2(faceRight - diagOffset, faceBottom - cornerJawY * 0.3f)
                )
        });

        return new Cushion[]{topLeft, topRight, bottomLeft, bottomRight, left, right};
    }

    public Vector2[] instantiateHoles(float bw) {
        return new Vector2[]{
                new Vector2(106, 98),
                new Vector2(bw / 2, 80),
                new Vector2(3093, 98),
                new Vector2(106, 1501),
                new Vector2(bw / 2, 1519),
                new Vector2(3093, 1501)
        };
    }


    private void showCueRing() {
        if (ringVisible) return;

        double displayX = toDisplayX(balls[21].position.x);
        double displayY = toDisplayY(balls[21].position.y);
        double displayRadius = balls[21].radius * scaleX * 1.5;

        cueRing = new Circle(displayX, displayY, displayRadius);
        cueRing.setFill(Color.TRANSPARENT);
        cueRing.setStroke(Color.WHITE);
        cueRing.setStrokeWidth(5 * scaleX);
        cueRing.setOpacity(1);

        pane.getChildren().add(cueRing);
        ringVisible = true;
    }

    private void hideCueRing() {
        if (!ringVisible || cueRing == null) return;
        pane.getChildren().remove(cueRing);
        ringVisible = false;
    }

    public void replaceBall(Ball ball){
        if(ball.ballType == BallType.RED) return;
        ball.position = findColoredBallRespawnPosition(ball, ball.ballType, ball.radius);
        ball.unfade();
    }

    public Vector2 findColoredBallRespawnPosition(Ball ball, BallType type, double ballRadius) {
        //try normal spot
        if (isPositionFree(ball.nominalPosition, ballRadius)) {
            return ball.nominalPosition;
        }

        //fallback spots: black, pink, blue, brown, green, yellow
        BallType[] fallbackOrder = {BallType.BLACK, BallType.PINK, BallType.BLUE, BallType.BROWN, BallType.GREEN, BallType.YELLOW};
        for (BallType spot : fallbackOrder) {
            Vector2 candidate = getColorSpot(spot);
            if (isPositionFree(candidate, ballRadius)) {
                return candidate;
            }
        }

        // when all fallbacks are occupied the ball has to go to the first
        // empty position left of the nominal spot
        // black and pink balls move to wards the center instead of the left, as they are closest to the left

        // snooker rules are weird dont ask me
        double railX = BASE_WIDTH * 0.04f;
        double topCushionX = railX + ballRadius;
        double baulkCushionX = BASE_WIDTH - railX - ballRadius;

        // Step along the center line toward the top cushion
        double step = ballRadius * 0.1f;
        double x = ball.nominalPosition.x - step;

        while (x >= topCushionX) {
            Vector2 candidate = new Vector2(x, ball.nominalPosition.y);
            if (isPositionFree(candidate, ballRadius)) {
                return candidate;
            }
            x -= step;
        }

        //go to the right if theres no space on the left
        x = ball.nominalPosition.x + step;
        while (x <= baulkCushionX) {
            Vector2 candidate = new Vector2(x, ball.nominalPosition.y);
            if (isPositionFree(candidate, ballRadius)) {
                return candidate;
            }
            x += step;
        }

        //should never be reached, absolute fallback if all positions are occupied
        return ball.nominalPosition;
    }

    private boolean isPositionFree(Vector2 pos, double ballRadius) {
        double minDist = ballRadius * 2;
        for (Ball ball : balls) {
            if (ball == null || !ball.isActive) continue;
            double dx = ball.position.x - pos.x;
            double dy = ball.position.y - pos.y;
            if (dx * dx + dy * dy < minDist * minDist) {
                return false;
            }
        }
        return true;
    }

    private Vector2 getColorSpot(BallType type) {
        return switch (type) {
            case BLACK  -> new Vector2(292,  800);
            case PINK   -> new Vector2(800,  800);
            case BLUE   -> new Vector2(1600, 800);
            case BROWN  -> new Vector2(2560, 800);
            case GREEN  -> new Vector2(2560, 666);
            case YELLOW -> new Vector2(2560, 934);
            default -> throw new IllegalArgumentException("Not a colour ball: " + type);
        };
    }
}