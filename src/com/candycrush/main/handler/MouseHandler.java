package com.candycrush.main.handler;

import com.candycrush.main.object.abstraction.Clickable;
import com.candycrush.main.object.interface_.GroupInterface;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MouseHandler extends MouseAdapter {
    private static MouseHandler handler = null;
    private final ArrayList<Clickable> objects = new ArrayList<>();
    private final ArrayList<Clickable> objectsToRemove = new ArrayList<>();
    private final ArrayList<Clickable> objectsToAdd = new ArrayList<>();

    public static MouseHandler getInstance() {
        if (handler == null) {
            handler = new MouseHandler();
        }
        return handler;
    }

    public void addObject(Clickable object) {
        objectsToAdd.add(object);
    }

    public void removeObject(Clickable object) {
        objectsToRemove.add(object);
    }

    public void addObjects(GroupInterface<Clickable> group) {
        objectsToAdd.addAll(group.getObjects());
    }

    private boolean mouseOver(int mouseX, int mouseY, Clickable object) {
        int x = object.getX();
        int y = object.getY();
        int dx = x + object.getWidth();
        int dy = y + object.getHeight();
        return mouseX > x && mouseX < dx && mouseY > y && mouseY < dy;
    }

    public void tick() {
        objects.removeAll(objectsToRemove);
        objects.addAll(objectsToAdd);

        objectsToAdd.clear();
        objectsToRemove.clear();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        for (Clickable object : objects) {
            if (mouseOver(mouseX, mouseY, object) && object.isEnable())
                object.mousePressed();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        for (Clickable object : objects) {
            if (mouseOver(mouseX, mouseY, object) && object.isEnable())
                object.mouseReleased();
            else
                object.mouseReset();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        for (Clickable object : objects) {
            if (mouseOver(mouseX, mouseY, object) && object.isEnable())
                object.mouseHover();
            else
                object.mouseReset();
        }
    }
}