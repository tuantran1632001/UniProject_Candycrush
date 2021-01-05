package com.candycrush.main.handler;

import com.candycrush.main.CandiesID;
import com.candycrush.main.object.concrete.Candy;
import com.candycrush.main.object.concrete.Level;
import com.candycrush.main.object.concrete.ObjectGroup;
import com.candycrush.main.resourceloader.TextureLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CandiesHandler {
    private static CandiesHandler candiesHandler = null;
    private final ObjectGroup candiesGroup = new ObjectGroup();
    private final ArrayList<Candy> candies = new ArrayList<>();
    private final ArrayList<Candy> moving = new ArrayList<>();
    private Level level = null;
    private int[][] grid = new int[10][9];
    private Random random;

    private int selX = 0;
    private int selY = 0;
    private int oldSelX = 0;
    private int oldSelY = 0;

    public static CandiesHandler getInstance() {
        if (candiesHandler == null) {
            candiesHandler = new CandiesHandler();
        }
        return candiesHandler;
    }

    public void setLevel(Level level) {
        this.level = level;

        if (level == null)
            return;

        for (int x = 0; x < 9; x++) {
            grid[0][x] = 3;
        }

        grid = new int[10][9];
        candies.clear();
        candiesGroup.clear();
        random = new Random();

        for (int y = 1; y < 10; y++) {
            for (int x = 0; x < 9; x++) {
                if (level.getEmpty()[y - 1][x])
                    grid[y][x] = 2;
                else
                    grid[y][x] = 0;
            }
        }
    }

    public void selected(int x, int y) {
        if (grid[(y+70)/100][(x-350)/100] != 2) {
            selX = ((x-350) / 100) * 100 + 350;
            selY = ((y-30) / 100) * 100 +30;
        }
    }

    public int[][] getGrid() {
        return grid;
    }

    public ObjectGroup getCandies() {
        return candiesGroup;
    }

    private void updateCandiesGroup() {
        candiesGroup.clear();
        for (Candy candy : candies) {
            candiesGroup.addObject(candy);
        }
    }

    private void spawnCandies() {
        CandiesID[] candiesID = CandiesID.values();
        for (int x = 0; x < 9; x++) {
            if (grid[0][x] == 0) {
                Candy temp = new Candy(x * 100 + 350, -70, candiesID[random.nextInt(candiesID.length)], this);
                candies.add(temp);
                grid[0][x] = 1;
            }
        }
    }

    private void updateGrid() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 9; x++) {
                if (grid[y][x] == 1)
                    grid[y][x] = 0;
            }
        }

        for (Candy candy : candies) {
            if (grid[(candy.getY() + 70) / 100][(candy.getX() - 350) / 100] == 0)
                grid[(candy.getY() + 70) / 100][(candy.getX() - 350) / 100] = 1;
        }
    }

    private Candy findCandy(int x, int y) {
        for (Candy candy : candies) {
            if (candy.getX() == x && candy.getY() == y)
                return candy;
        }
        return null;
    }

    private ArrayList<Candy> checkVerticalUp(Candy candy) {
        ArrayList<Candy> matches = new ArrayList<>();
        Candy c1 = findCandy(candy.getX(), candy.getY()-100);
        if (c1 != null && c1.getId() == candy.getId()) {
            matches.addAll(checkVerticalUp(c1));
            matches.add(c1);
        }

        return matches;
    }

    private ArrayList<Candy> checkVerticalDown(Candy candy) {
        ArrayList<Candy> matches = new ArrayList<>();
        Candy c1 = findCandy(candy.getX(), candy.getY()+100);
        if (c1 != null && c1.getId() == candy.getId()) {
            matches.addAll(checkVerticalDown(c1));
            matches.add(c1);
        }

        return matches;
    }

    private ArrayList<Candy> checkVertical(Candy candy) {
        ArrayList<Candy> matches = new ArrayList<>();
        matches.addAll(checkVerticalUp(candy));
        matches.addAll(checkVerticalDown(candy));
        matches.add(candy);
        return matches;
    }

    private ArrayList<Candy> checkHorizontalRight(Candy candy) {
        ArrayList<Candy> matches = new ArrayList<>();
        Candy c1 = findCandy(candy.getX()+100, candy.getY());
        if (c1 != null && c1.getId() == candy.getId()) {
            matches.addAll(checkHorizontalRight(c1));
            matches.add(c1);
        }

        return matches;
    }

    private ArrayList<Candy> checkHorizontalLeft(Candy candy) {
        ArrayList<Candy> matches = new ArrayList<>();
        Candy c1 = findCandy(candy.getX()-100, candy.getY());
        if (c1 != null && c1.getId() == candy.getId()) {
            matches.addAll(checkHorizontalLeft(c1));
            matches.add(c1);
        }

        return matches;
    }

    private ArrayList<Candy> checkHorizontal(Candy candy) {
        ArrayList<Candy> matches = new ArrayList<>();
        matches.addAll(checkHorizontalLeft(candy));
        matches.addAll(checkHorizontalRight(candy));
        matches.add(candy);
        return matches;
    }

    private Set<Candy> checkMatches(Candy candy) {
        ArrayList<Candy> matches1 = checkHorizontal(candy);
        ArrayList<Candy> matches2 = checkVertical(candy);
        Set<Candy> matches = new HashSet<>();

        if (matches1.size() >= 3 || matches2.size() >= 3) {
            matches.addAll(matches1);
            matches.addAll(matches2);
        }
        return matches;
    }

    public boolean isMoving() {
        for (Candy candy : candies) {
            if (candy.isMoving())
                return true;
        }
        return false;
    }

    public void tick() {
        // Don't do anything if level is not assigned.
        if (level == null)
            return;

        spawnCandies();

        // Candies falling
        for (Candy candy : candies) {
            int y = (candy.getY() + 70) / 100;
            int x = (candy.getX() - 350) / 100;
            while (y <= 8) {
                if (grid[y + 1][x] == 0) {
                    y++;
                    continue;
                }
                if (grid[y + 1][x] == 1) {
                    break;
                }
                if (grid[y + 1][x] == 2) {
                    int temp = y;
                    while (temp <= 8 && grid[temp + 1][x] == 2)
                        temp++;

                    if (grid[temp + 1][x] == 0) {
                        y = temp;
                    } else
                        break;
                }
            }

            candy.gotoXY(x * 100 + 350, y * 100 - 70);

            updateGrid();
        }

        // Check after moving two candies.
        if (oldSelX != selX || oldSelY != selY)  {
            if ((Math.abs(oldSelX - selX) == 100 && Math.abs(oldSelY - selY) == 0) || (Math.abs(oldSelX - selX) == 0 && Math.abs(oldSelY - selY) == 100)) {
                Candy c1 = findCandy(oldSelX,oldSelY);
                Candy c2 = findCandy(selX,selY);

                if (c1 != null && c2 != null) {
                    Candy.swap(c1, c2);
                    moving.clear();
                    moving.add(c1);
                    moving.add(c2);
                }
            }

            oldSelX = selX;
            oldSelY = selY;
        }

        // Check after every candies was moved
        if (!isMoving()) {
            Set<Candy> matches = new HashSet<>();
            for (Candy candy : candies) {
                matches.addAll(checkMatches(candy));
            }
            candies.removeAll(matches);

            // If the moved candies have not been deleted (matched), move them back.
            if (moving.size() == 2) {
                Candy.swap(moving.get(0), moving.get(1));

                moving.clear();
            }
        }

        updateCandiesGroup();
    }

    public void render(Graphics2D graphic) {
        if (level == null || selY == 0 || selX == 0)
            return ;

        BufferedImage selector = SpriteHandler.cutSprite(TextureLoader.getInstance().getTexture("candies.png"), 274, 1569, 160, 160);
        graphic.drawImage(selector, selX, selY, 100, 100, null);
    }
}