package com.example.snooker;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class GameLogic {

    private int subSteps = 8;
    private final ConfigReader config = new ConfigReader("config.properties");

    private final int BASE_WIDTH = 3200;
    private final int BASE_HEIGHT = 1600;

    private final double SCALING_FACTOR = .5;

    private Ball[] balls = new Ball[22];

    private Cushion[] cushions;

    private InputHandler inputHandler;
    private boolean mouseWasPressed;

    Vector2 startingPoint;
    Vector2 endPoint;

    private Line aimLine;

    private boolean showCushionLines = true;

    public void onStart(Scene scene, Pane pane) {
        config.load();
        subSteps = config.getInt("substeps", 8);

        balls[0] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 354), 1);
        balls[1] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 377), 1);
        balls[2] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 400), 1);
        balls[3] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 423), 1);
        balls[4] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(296, 446), 1);
        balls[5] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 434.5), 1);
        balls[6] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 411.5), 1);
        balls[7] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 388.5), 1);
        balls[8] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(316, 365.5), 1);
        balls[9] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(336, 423), 1);
        balls[10] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(336, 400), 1);
        balls[11] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(336, 377), 1);
        balls[12] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(356, 411.5), 1);
        balls[13] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(356, 388.5), 1);
        balls[14] = new Ball(new Image("/redBall.png"), 11.5, new Vector2(376, 400), 1);

        balls[15] = new Ball(new Image("/greenBall.png"), 11.5, new Vector2(1280, 467), 2);
        balls[16] = new Ball(new Image("/yellowBall.png"), 11.5, new Vector2(1280, 333), 3);
        balls[17] = new Ball(new Image("/brownBall.png"), 11.5, new Vector2(1280, 400), 4);
        balls[18] = new Ball(new Image("/blueBall.png"), 11.5, new Vector2(800, 400), 5);
        balls[19] = new Ball(new Image("/pinkBall.png"), 11.5, new Vector2(400, 400), 6);

        if (Math.random() <= .01) {
            balls[20] = new Ball(new Image("/ball2.png"), 11.5, new Vector2(146, 400), 7);
        } else {
            balls[20] = new Ball(new Image("/blackBall.png"), 11.5, new Vector2(146, 400), 7);
        }

        balls[21] = new Ball(new Image("/cueBall.png"), 11.5, new Vector2(1350, 360), 0);

        for (Ball ball : balls) {
            if (ball == null) {
                continue;
            }

            if (ball.image != null) {
                continue;
            }

            ball.setImage(new Image("/ball2.png"));
        }

        inputHandler = new InputHandler(scene);

        aimLine = new Line();
        aimLine.setStrokeWidth(3);

        pane.getChildren().add(aimLine);

        cushions = instantiateCushions(BASE_WIDTH, BASE_HEIGHT);

        if (showCushionLines) {
            for (Cushion cushion : cushions) {
                for (LineSegment seg : cushion.segments) {
                    Line line = new Line(seg.a.x, seg.a.y, seg.b.x, seg.b.y);
                    line.setStroke(Color.WHITE);
                    line.setStrokeWidth(1.5);
                    pane.getChildren().add(line);
                }
            }
        }

        System.out.println("Substeps loaded: " + subSteps);
    }

    public void update(double deltaTime, Scene scene, Pane pane) {
        config.load();
        subSteps = config.getInt("substeps", 8);

        if (subSteps < 1) {
            subSteps = 1;
        }

        if (subSteps > 10) {
            subSteps = 10;
        }

        double friction = .45;
        double subDelta = deltaTime / subSteps;
        double frictionFactorPerSubStep = Math.pow(friction, subDelta);

        for (int step = 0; step < subSteps; step++) {
            for (Ball ball : balls) {
                if (ball == null) {
                    continue;
                }

                ball.position = ball.position.sum(ball.velocity.scalar(subDelta));
                calculateWallCollisions(ball, scene, cushions);
            }

            handleCollisions();

            for (Ball ball : balls) {
                if (ball == null) {
                    continue;
                }

                ball.velocity = ball.velocity.scalar(frictionFactorPerSubStep);
            }
        }

        boolean areAllBallsStanding = true;

        for (Ball ball : balls) {
            if (ball == null) {
                continue;
            }

            if (ball.velocity.magnitude() >= 1) {
                areAllBallsStanding = false;
                break;
            }
        }

        if (areAllBallsStanding) {
            shootCueBall();
        }

        for (Ball ball : balls) {
            if (ball == null) {
                continue;
            }

            ball.imageView.setX(ball.position.x - ball.radius);
            ball.imageView.setY(ball.position.y - ball.radius);

            double speed = Math.sqrt(
                    ball.velocity.x * ball.velocity.x +
                            ball.velocity.y * ball.velocity.y
            );

            ball.imageView.setRotate(ball.imageView.getRotate() + (speed * deltaTime) % 360);
        }

        updateAimLine(areAllBallsStanding);
    }

    private void updateAimLine(boolean visible) {
        aimLine.setVisible(visible);

        if (!visible) {
            return;
        }

        boolean isCurrentlyPressed = inputHandler.isPressedMouse(MouseButton.PRIMARY);

        if (!isCurrentlyPressed || startingPoint == null) {
            aimLine.setVisible(false);
            return;
        }

        Vector2 currentMouse = inputHandler.getMousePosition();
        Vector2 cueBallPos = balls[21].position;

        Vector2 drag = currentMouse.difference(startingPoint);
        Vector2 aim = drag.scalar(-1);

        double power = drag.magnitude() * 2;

        Vector2 aimEnd = cueBallPos.sum(aim.normalize().scalar(power));

        aimLine.setStartX(cueBallPos.x);
        aimLine.setStartY(cueBallPos.y);

        aimLine.setEndX(aimEnd.x);
        aimLine.setEndY(aimEnd.y);
    }

    public void handleCollisions() {
        final double e = 0.95;
        final double minDist = 23.0;
        final double minDistSq = minDist * minDist;

        for (int i = 0; i < balls.length; i++) {
            if (balls[i] == null) {
                continue;
            }

            for (int j = i + 1; j < balls.length; j++) {
                if (balls[j] == null) {
                    continue;
                }

                double dx = balls[i].position.x - balls[j].position.x;
                double dy = balls[i].position.y - balls[j].position.y;
                double distSq = dx * dx + dy * dy;

                if (distSq >= minDistSq) {
                    continue;
                }

                double dist = Math.sqrt(distSq);

                if (dist == 0) {
                    dist = 0.01;
                }

                double nx = dx / dist;
                double ny = dy / dist;

                double dvx = balls[i].velocity.x - balls[j].velocity.x;
                double dvy = balls[i].velocity.y - balls[j].velocity.y;
                double speedAlongNormal = dvx * nx + dvy * ny;

                if (speedAlongNormal >= 0) {
                    continue;
                }

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
            startingPoint = inputHandler.getMousePosition();
        }

        if (!isCurrentlyPressed && mouseWasPressed) {
            endPoint = inputHandler.getMousePosition();
            balls[21].velocity = endPoint.difference(startingPoint).scalar(-7.5);
        }

        mouseWasPressed = isCurrentlyPressed;
    }

    public Ball[] getBalls() {
        return balls;
    }

    public void calculateWallCollisions(Ball ball, Scene scene, Cushion[] cushions) {
        final double returnEnergy = 0.87;

        for (Cushion cushion : cushions) {
            LineSegment[] pts = cushion.segments;
            int n = pts.length;

            for (int i = 0; i < n; i++) {
                Vector2 a = pts[i].a;
                Vector2 b = pts[i].b;

                double edgeX = b.x - a.x;
                double edgeY = b.y - a.y;
                double edgeLen = Math.sqrt(edgeX * edgeX + edgeY * edgeY);

                if (edgeLen < 1e-6) {
                    continue;
                }

                double nx = edgeY / edgeLen;
                double ny = -edgeX / edgeLen;

                double toCentreX = (scene.getWidth() / 2.0) - a.x;
                double toCentreY = (scene.getHeight() / 2.0) - a.y;

                if (nx * toCentreX + ny * toCentreY < 0) {
                    nx = -nx;
                    ny = -ny;
                }

                double dx = ball.position.x - a.x;
                double dy = ball.position.y - a.y;
                double dist = dx * nx + dy * ny;

                if (dist < -ball.radius || dist >= ball.radius) {
                    continue;
                }

                double edgeUX = edgeX / edgeLen;
                double edgeUY = edgeY / edgeLen;
                double t = dx * edgeUX + dy * edgeUY;

                if (t < -ball.radius || t > edgeLen + ball.radius) {
                    continue;
                }

                double vDotN = ball.velocity.x * nx + ball.velocity.y * ny;

                if (vDotN >= 0) {
                    continue;
                }

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

    public Cushion[] instantiateCushions(float BASE_WIDTH, float BASE_HEIGHT) {
        BASE_WIDTH *= SCALING_FACTOR;
        BASE_HEIGHT *= SCALING_FACTOR;

        float railX = BASE_WIDTH * 0.04f;
        float railY = BASE_HEIGHT * 0.07f;

        float cornerJawX = BASE_WIDTH * 0.015f;
        float cornerJawY = BASE_HEIGHT * 0.035f;
        float sideJawX = BASE_WIDTH * 0.025f;
        float diagOffset = cornerJawX * -1f;

        float faceTop = railY;
        float faceBottom = BASE_HEIGHT - railY;
        float faceLeft = railX;
        float faceRight = BASE_WIDTH - railX;
        float pocketCX = BASE_WIDTH / 2f;

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