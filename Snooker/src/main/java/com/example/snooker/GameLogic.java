package com.example.snooker;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class GameLogic {

    private int subSteps = 8;

    String filePath = new java.io.File("src/main/resources/config.properties").getAbsolutePath();
    private final ConfigReader config = new ConfigReader(filePath);

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

    private InputHandler inputHandler;
    private boolean mouseWasPressed;

    private Vector2 startingPoint;
    private Vector2 endPoint;

    private Line aimLine;

    private boolean showCushionLines = true;

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

        //initialize balls in BASE_WIDTH and BASE_HEIGHT space
        balls[0] = new Ball(new Image("/redBall.png"), 23, new Vector2(592, 708), 1);
        balls[1] = new Ball(new Image("/redBall.png"), 23, new Vector2(592, 754), 1);
        balls[2] = new Ball(new Image("/redBall.png"), 23, new Vector2(592, 800), 1);
        balls[3] = new Ball(new Image("/redBall.png"), 23, new Vector2(592, 846), 1);
        balls[4] = new Ball(new Image("/redBall.png"), 23, new Vector2(592, 892), 1);
        balls[5] = new Ball(new Image("/redBall.png"), 23, new Vector2(632, 869), 1);
        balls[6] = new Ball(new Image("/redBall.png"), 23, new Vector2(632, 823), 1);
        balls[7] = new Ball(new Image("/redBall.png"), 23, new Vector2(632, 777), 1);
        balls[8] = new Ball(new Image("/redBall.png"), 23, new Vector2(632, 731), 1);
        balls[9] = new Ball(new Image("/redBall.png"), 23, new Vector2(672, 846), 1);
        balls[10] = new Ball(new Image("/redBall.png"), 23, new Vector2(672, 800), 1);
        balls[11] = new Ball(new Image("/redBall.png"), 23, new Vector2(672, 754), 1);
        balls[12] = new Ball(new Image("/redBall.png"), 23, new Vector2(712, 823), 1);
        balls[13] = new Ball(new Image("/redBall.png"), 23, new Vector2(712, 777), 1);
        balls[14] = new Ball(new Image("/redBall.png"), 23, new Vector2(752, 800), 1);

        balls[15] = new Ball(new Image("/greenBall.png"), 23, new Vector2(2560, 934), 2);
        balls[16] = new Ball(new Image("/yellowBall.png"), 23, new Vector2(2560, 666), 3);
        balls[17] = new Ball(new Image("/brownBall.png"), 23, new Vector2(2560, 800), 4);
        balls[18] = new Ball(new Image("/blueBall.png"), 23, new Vector2(1600, 800), 5);
        balls[19] = new Ball(new Image("/pinkBall.png"), 23, new Vector2(800, 800), 6);

        if (Math.random() <= .01) {
            balls[20] = new Ball(new Image("/ball2.png"), 23, new Vector2(292, 800), 7);
        } else {
            balls[20] = new Ball(new Image("/blackBall.png"), 23, new Vector2(292, 800), 7);
        }

        balls[21] = new Ball(new Image("/cueBall.png"), 23, new Vector2(2700, 720), 0);

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

        if (showCushionLines) {
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
        }

        System.out.println("Substeps loaded: " + subSteps);
    }

    public void update(double deltaTime) {
        loadSubsteps();

        double friction = .45;
        double subDelta = deltaTime / subSteps;
        double frictionFactorPerSubStep = Math.pow(friction, subDelta);

        for (int step = 0; step < subSteps; step++) {
            for (Ball ball : balls) {
                if (ball == null) continue;
                ball.position = ball.position.sum(ball.velocity.scalar(subDelta));
                calculateWallCollisions(ball, cushions);
            }

            handleCollisions();

            for (Ball ball : balls) {
                if (ball == null) continue;
                ball.velocity = ball.velocity.scalar(frictionFactorPerSubStep);
            }
        }

        boolean areAllBallsStanding = true;
        for (Ball ball : balls) {
            if (ball == null) continue;
            if (ball.velocity.magnitude() >= 1) {
                areAllBallsStanding = false;
                break;
            }
        }

        if (areAllBallsStanding) {
            shootCueBall();
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
    }

    private void loadSubsteps() {
        config.load();
        subSteps = config.getInt("substeps", 8);
        if (subSteps < 1) subSteps = 1;
        if (subSteps > 10) subSteps = 10;
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

            if (startingPoint != null && endPoint != null) {
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

        double centreX = BASE_WIDTH / 2.0;
        double centreY = BASE_HEIGHT / 2.0;

        for (Cushion cushion : cushions) {
            for (LineSegment seg : cushion.segments) {
                Vector2 a = seg.a;
                Vector2 b = seg.b;

                double edgeX = b.x - a.x;
                double edgeY = b.y - a.y;
                double edgeLen = Math.sqrt(edgeX * edgeX + edgeY * edgeY);

                if (edgeLen < 1e-6) continue;

                double nx = edgeY / edgeLen;
                double ny = -edgeX / edgeLen;

                double toCentreX = centreX - a.x;
                double toCentreY = centreY - a.y;
                if (nx * toCentreX + ny * toCentreY < 0) {
                    nx = -nx;
                    ny = -ny;
                }

                double dx = ball.position.x - a.x;
                double dy = ball.position.y - a.y;
                double dist = dx * nx + dy * ny;

                if (dist < -ball.radius || dist >= ball.radius) continue;

                double edgeUX = edgeX / edgeLen;
                double edgeUY = edgeY / edgeLen;
                double t = dx * edgeUX + dy * edgeUY;

                if (t < -ball.radius || t > edgeLen + ball.radius) continue;

                double vDotN = ball.velocity.x * nx + ball.velocity.y * ny;
                if (vDotN >= 0) continue;

                ball.velocity.x -= (1 + returnEnergy) * vDotN * nx;
                ball.velocity.y -= (1 + returnEnergy) * vDotN * ny;

                double penetration = ball.radius - dist;
                if (penetration > 0) {
                    ball.position.x += penetration * nx;
                    ball.position.y += penetration * ny;
                }
            }
        }
    }

    public Cushion[] instantiateCushions(float bw, float bh) {
        float railX = bw * 0.04f;
        float railY = bh * 0.07f;

        float cornerJawX = bw * 0.015f;
        float cornerJawY = bh * 0.035f;
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
}