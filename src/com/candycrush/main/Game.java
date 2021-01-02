package com.candycrush.main;

import com.candycrush.main.handler.MouseHandler;
import com.candycrush.main.handler.ObjectGroup;
import com.candycrush.main.handler.ObjectHandler;
import com.candycrush.main.resourceloader.LevelLoader;
import com.candycrush.main.resourceloader.TextureLoader;
import com.candycrush.main.uicomponent.Background;
import com.candycrush.main.uicomponent.Button;
import com.candycrush.main.uicomponent.PlainImage;
import com.candycrush.main.uicomponent.Window;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.io.Serial;

public class Game extends Canvas implements Runnable {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = WIDTH * 9 / 12;
    private static int fps = 0;
    @Serial
    private static final long serialVersionUID = 8102020L;
    private static Thread thread;
    private static boolean running = false;
    private static final ObjectHandler OBJECT_HANDLER = ObjectHandler.getInstance();
    private static final TextureLoader TEXTURE_LOADER = TextureLoader.getInstance();
    private static final MouseHandler MOUSE_HANDLER = MouseHandler.getInstance();
    private static final LevelLoader LEVEL_LOADER = LevelLoader.getInstance();

    public Game() {
        com.candycrush.main.uicomponent.Window window = new Window(WIDTH, HEIGHT, "Candy Crush - Student Game!", this);
        this.addMouseListener(MOUSE_HANDLER);
        this.addMouseMotionListener(MOUSE_HANDLER);

        ObjectGroup mainMenu = new ObjectGroup();
        ObjectGroup levelSelector = new ObjectGroup();

        // Components declaration
        Background background = new Background();
        PlainImage mainLogo = new PlainImage(TEXTURE_LOADER.getTexture("logo_main.png"), (WIDTH-400)/2,50, 400, 400);
        PlainImage levelPanel = new PlainImage(TEXTURE_LOADER.getTexture("big_panel.png"), 0, 0, WIDTH, HEIGHT);

        Button exitButton = new Button("Exit",Color.WHITE,45, (WIDTH-250)/2, 625, 250, 75, TEXTURE_LOADER.getTexture("button_pink_long.png")) {
            @Override
            public void action() {
                window.dispose();
                running = false;
            }
        };
        Button playButton = new Button("Play", Color.WHITE, 50,(WIDTH-250)/2, 500, 250, 100, TEXTURE_LOADER.getTexture("button_yellow_long.png")) {
            @Override
            public void action() {
                mainMenu.setEnable(false);
                levelSelector.setEnable(true);
            }
        };
        Button backToMenu;

        OBJECT_HANDLER.addObject(background);
        OBJECT_HANDLER.addObject(mainMenu);
        OBJECT_HANDLER.addObject(levelSelector);

        MOUSE_HANDLER.addObject(playButton);
        MOUSE_HANDLER.addObject(exitButton);

        mainMenu.addComponent(mainLogo);
        mainMenu.addComponent(playButton);
        mainMenu.addComponent(exitButton);

        levelSelector.addComponent(levelPanel);
        levelSelector.setEnable(false);
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
            thread.join(1000);
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
        OBJECT_HANDLER.tick();
        MOUSE_HANDLER.tick();
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
        g.setFont(new Font("TimesRoman", Font.BOLD, 10));
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + fps, 10, 10);

        g.dispose();
        bs.show();
    }
}
