package com.candycrush.main;

import com.candycrush.main.handler.CandiesHandler;
import com.candycrush.main.handler.MouseHandler;
import com.candycrush.main.handler.ObjectHandler;
import com.candycrush.main.object.abstraction.Action;
import com.candycrush.main.object.concrete.ClickableGroup;
import com.candycrush.main.object.concrete.Level;
import com.candycrush.main.object.concrete.ObjectGroup;
import com.candycrush.main.object.uicomponent.Button;
import com.candycrush.main.object.uicomponent.Window;
import com.candycrush.main.object.uicomponent.*;
import com.candycrush.main.resourceloader.LevelLoader;
import com.candycrush.main.resourceloader.TextureLoader;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.Serial;
import java.util.ArrayList;

public class Game extends Canvas implements Runnable {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = WIDTH * 9 / 12;
    @Serial
    private static final long serialVersionUID = 8102020L;
    private static final ObjectHandler OBJECT_HANDLER = ObjectHandler.getInstance();
    private static final TextureLoader TEXTURE_LOADER = TextureLoader.getInstance();
    private static final MouseHandler MOUSE_HANDLER = MouseHandler.getInstance();
    private static final LevelLoader LEVEL_LOADER = LevelLoader.getInstance();
    private static final CandiesHandler CANDIES_HANDLER = CandiesHandler.getInstance();
    private static int fps = 0;
    private static Thread thread;
    private static boolean running = false;
    private static int pageNum = 0;
    private static Level currentLevel = null;
    private final Window window = new Window(WIDTH, HEIGHT, "Candy Crush - Student Game!", this);

    public Game() {
        this.addMouseListener(MOUSE_HANDLER);
        this.addMouseMotionListener(MOUSE_HANDLER);

        ObjectGroup mainMenuGroup = new ObjectGroup();
        ClickableGroup mainMenuClickable = new ClickableGroup();
        ObjectGroup levelSelectorGroup = new ObjectGroup();
        ClickableGroup levelSelectorClickable = new ClickableGroup();
        ObjectGroup gameSceneGroup = new ObjectGroup();
        ClickableGroup gameSceneClickable = new ClickableGroup();

        ArrayList<ClickableGroup> pages = new ArrayList<>();

        Background background = new Background();
        OBJECT_HANDLER.addObject(background);

        // Main menu
        PlainImage mainLogo = new PlainImage(TEXTURE_LOADER.getTexture("logo_main.png"), (WIDTH - 400) / 2, 50, 400, 400);
        Button exitButton = new Button("Exit", Color.WHITE, 45, (WIDTH - 250) / 2, 625, 250, 75, TEXTURE_LOADER.getTexture("button_pink_long.png"));
        Button playButton = new Button("Play", Color.WHITE, 50, (WIDTH - 250) / 2, 500, 250, 100, TEXTURE_LOADER.getTexture("button_yellow_long.png"));

        exitButton.addAction(new Action(exitButton) {
            @Override
            public void perform() {
                running = false;
            }
        });
        playButton.addAction(new Action(playButton) {
            @Override
            public void perform() {
                MOUSE_HANDLER.addObject(levelSelectorClickable);
                MOUSE_HANDLER.removeObject(mainMenuClickable);
                MOUSE_HANDLER.addObject(pages.get(pageNum));

                OBJECT_HANDLER.removeObject(mainMenuGroup);
                OBJECT_HANDLER.addObject(levelSelectorGroup);
                OBJECT_HANDLER.addObject(pages.get(pageNum));
            }
        });

        mainMenuGroup.addObject(mainLogo);
        mainMenuGroup.addObject(playButton);
        mainMenuGroup.addObject(exitButton);

        mainMenuClickable.addObject(playButton);
        mainMenuClickable.addObject(exitButton);

        OBJECT_HANDLER.addObject(mainMenuGroup);
        MOUSE_HANDLER.addObject(mainMenuClickable);

        // Levels selector
        Button backToMenu = new Button((WIDTH / 2) - 50, 800, 100, 100, TEXTURE_LOADER.getTexture("back_to_menu.png"));
        Button levelPageLeft = new Button((WIDTH / 2) - 170, 800, 100, 100, TEXTURE_LOADER.getTexture("arrow_pink_left.png"));
        Button levelPageRight = new Button((WIDTH / 2) + 70, 800, 100, 100, TEXTURE_LOADER.getTexture("arrow_pink_right.png"));
        PlainImage levelPanel = new PlainImage(TEXTURE_LOADER.getTexture("big_panel.png"), 0, 0, WIDTH, HEIGHT);

        Grid grid = new Grid(CANDIES_HANDLER);

        backToMenu.addAction(new Action(backToMenu) {
            @Override
            public void perform() {
                OBJECT_HANDLER.removeObject(levelSelectorGroup);
                OBJECT_HANDLER.removeObject(pages.get(pageNum));
                OBJECT_HANDLER.removeObject(gameSceneGroup);
                OBJECT_HANDLER.addObject(mainMenuGroup);

                MOUSE_HANDLER.addObject(mainMenuClickable);
                MOUSE_HANDLER.removeObject(pages.get(pageNum));
                MOUSE_HANDLER.removeObject(levelSelectorClickable);
                MOUSE_HANDLER.removeObject(gameSceneClickable);

                CANDIES_HANDLER.setLevel(null);
                backToMenu.setXY((WIDTH / 2) - 50, 800);
            }
        });

        levelSelectorGroup.addObject(levelPanel);
        levelSelectorGroup.addObject(backToMenu);
        levelSelectorGroup.addObject(levelPageLeft);
        levelSelectorGroup.addObject(levelPageRight);

        levelSelectorClickable.addObject(levelPageLeft);
        levelSelectorClickable.addObject(levelPageRight);
        levelSelectorClickable.addObject(backToMenu);

        int pageCount;
        if (LEVEL_LOADER.getNumberOfLevel() % 24 != 0)
            pageCount = (LEVEL_LOADER.getNumberOfLevel() / 24) + 1;
        else
            pageCount = LEVEL_LOADER.getNumberOfLevel() / 24;

        for (int i = 0; i < pageCount; i++) {
            pages.add(new ClickableGroup());
            ArrayList<Level> levels = LEVEL_LOADER.getLevels();
            int x, y = 1;
            for (int j = i * 24; j < (i + 1) * 24 && j < LEVEL_LOADER.getNumberOfLevel(); j++) {
                if ((j + 1) % 6 == 0) {
                    x = 6;
                } else {
                    x = (j + 1) % 6;
                    y = (j + 1 - 25 * i) / 6 + 1;
                }
                Button temp = new Button("" + levels.get(j).getNumber(), Color.WHITE, 30, x * (WIDTH / 7) - 50, y * (HEIGHT / 6) - 50, 100, 100, TEXTURE_LOADER.getTexture("square_button.png"));
                temp.addAction(new Action(temp) {
                    @Override
                    public void perform() {
                        OBJECT_HANDLER.removeObject(levelSelectorGroup);
                        OBJECT_HANDLER.addObject(gameSceneGroup);
                        OBJECT_HANDLER.removeObject(pages.get(pageNum));

                        MOUSE_HANDLER.removeObject(levelSelectorClickable);
                        MOUSE_HANDLER.removeObject(pages.get(pageNum));
                        MOUSE_HANDLER.addObject(gameSceneClickable);

                        backToMenu.setXY(30, 830);
                        currentLevel = LEVEL_LOADER.getLevels().get(Integer.parseInt(getParent().getString())-1);
                        grid.setUpGrid(currentLevel);
                        CANDIES_HANDLER.setLevel(currentLevel);
                    }
                });
                pages.get(i).addObject(temp);
            }
        }

        levelPageLeft.addAction(new Action(levelPageLeft) {
            @Override
            public void perform() {
                if (pageNum > 0) {
                    OBJECT_HANDLER.removeObject(pages.get(pageNum));
                    OBJECT_HANDLER.addObject(pages.get(pageNum - 1));
                    MOUSE_HANDLER.removeObject(pages.get(pageNum));
                    MOUSE_HANDLER.addObject(pages.get(pageNum - 1));
                    pageNum--;
                }
            }
        });

        levelPageRight.addAction(new Action(levelPageRight) {
            @Override
            public void perform() {
                if (pageNum < pageCount - 1) {
                    OBJECT_HANDLER.removeObject(pages.get(pageNum));
                    OBJECT_HANDLER.addObject(pages.get(pageNum + 1));
                    MOUSE_HANDLER.removeObject(pages.get(pageNum));
                    MOUSE_HANDLER.addObject(pages.get(pageNum + 1));
                    pageNum++;
                }
            }
        });


        // Game action!
        PlainImage gameHud = new PlainImage(TEXTURE_LOADER.getTexture("hud.png"), 0, 0, 300, 800);
        Text target = new Text("TimesRoman", Color.BLUE, 40, 50, 20, 200, 100) {
            @Override
            public void tick() {
                setText("Target: " + currentLevel.getTarget());
            }
        };
        Text move = new Text("TimesRoman", Color.BLUE, 50, 50, 180, 200, 100) {
            @Override
            public void tick() {
                setText("Move: " + currentLevel.getMove());
            }
        };
        Text scoreT = new Text("Score:", "TimesRoman", Color.BLUE, 50, 15, 350, 200, 100);
        Text scoreN = new Text("", "TimesRoman", Color.BLUE, 50, 15, 400, 200, 100) {
            @Override
            public void tick() {
                setText("" + currentLevel.getScore());
            }
        };

        gameSceneGroup.addObject(gameHud);
        gameSceneGroup.addObject(target);
        gameSceneGroup.addObject(move);
        gameSceneGroup.addObject(scoreT);
        gameSceneGroup.addObject(scoreN);
        gameSceneGroup.addObject(grid);
        gameSceneGroup.addObject(CANDIES_HANDLER.getCandies());
        gameSceneGroup.addObject(backToMenu);
        gameSceneClickable.addObject(backToMenu);
        gameSceneClickable.addObject(grid);
    }

    public static void main(String[] args) {
        System.setProperty("sun.java2d.opengl", "true");
        new Game();
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop() {
        try {
            thread.join(10);
            window.dispose();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60; // ticks per second
        double ns = 1000000000 / amountOfTicks; // nanoseconds per tick
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            // 1 delta means 1 tick has passed
            while (delta >= 1) {
                tick();
                delta--;
            }

            if (running)
                render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) { // 1000 milliseconds = 1 second
                timer += 1000;
                fps = frames;
                frames = 0;
            }
        }

        stop();
    }

    private void tick() {
        MOUSE_HANDLER.tick();
        OBJECT_HANDLER.tick();
        CANDIES_HANDLER.tick();
    }

    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.white);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        OBJECT_HANDLER.render(g);
        CANDIES_HANDLER.render(g);

        g.setFont(new Font("TimesRoman", Font.BOLD, 10));
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + fps, 10, 10);

        g.dispose();
        bs.show();
    }
}
