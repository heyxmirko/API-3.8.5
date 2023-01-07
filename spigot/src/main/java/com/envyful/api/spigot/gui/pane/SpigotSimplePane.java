package com.envyful.api.spigot.gui.pane;

import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.gui.pane.TickHandler;
import com.envyful.api.type.Pair;

import javax.annotation.Nullable;

/**
 *
 * Simple implementation of the {@link Pane} interface where the height and width of the pane are unchanging.
 *
 */
public class SpigotSimplePane implements Pane {

    private final int topLeftX;
    private final int topLeftY;
    private final int width;
    private final int height;
    private final Displayable[][] items;

    private boolean full = false;
    private Pair<Integer, Integer> lastPos = Pair.of(0, 0);

    private SpigotSimplePane(int topLeftX, int topLeftY, int height, int width) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
        this.items = new Displayable[height][width];

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.items[y][x] = null;
            }
        }
    }

    public Displayable[][] getItems() {
        return this.items;
    }

    public int updateIndex(int index) {
        return index + (9 * this.topLeftY) + this.topLeftX;
    }

    @Override
    public void add(Displayable displayable) {
        if (this.full) {
            return;
        }

        this.items[this.lastPos.getY()][this.lastPos.getX()] = displayable;

        if (this.width == (this.lastPos.getX() + 1)) {
            if (this.height == (this.lastPos.getY() + 1)) {
                this.full = true;
                return;
            }

            this.lastPos = Pair.of(0, this.lastPos.getY() + 1);
        } else {
            this.lastPos = Pair.of(this.lastPos.getX() + 1, this.lastPos.getY());
        }
    }

    @Override
    public void set(int posX, int posY, Displayable displayable) {
        if (posX >= (this.topLeftX + this.width)) {
            throw new RuntimeException("Cannot set an X position greater than the width");
        }

        if (posY >= (this.topLeftY + this.height)) {
            throw new RuntimeException("Cannot set an Y position greater than the height");
        }

        this.items[posY][posX] = displayable;
    }

    @Override
    public void set(int pos, Displayable displayable) {
        this.set(pos % (this.width), pos / (this.height), displayable);
    }

    @javax.annotation.Nullable
    @Override
    public Displayable get(int pos) {
        return this.get(pos % this.width, pos / this.height);
    }

    @Nullable
    @Override
    public Displayable get(int posX, int posY) {
        if (posX >= (this.topLeftX + this.width)) {
            throw new RuntimeException("Cannot get an X position greater than the width");
        }

        if (posY >= (this.topLeftY + this.height)) {
            throw new RuntimeException("Cannot get a Y position greater than the height");
        }

        return this.items[posY][posX];
    }

    @Override
    public void fill(Displayable displayable) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.set(x, y, displayable);
            }
        }
    }

    @Override
    public void clear() {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.set(x, y, null);
            }
        }
    }

    public static final class Builder implements Pane.Builder {

        private int topLeftX = 0;
        private int topLeftY = 0;
        private int width = 9;
        private int height = 5;

        public Builder() {}

        @Override
        public Pane.Builder topLeftX(int topLeftX) {
            this.topLeftX = topLeftX;
            return this;
        }

        @Override
        public Pane.Builder topLeftY(int topLeftY) {
            this.topLeftY = topLeftY;
            return this;
        }

        @Override
        public Pane.Builder tickHandler(TickHandler tickHandler) {
            return this;
        }

        @Override
        public Pane.Builder width(int width) {
            this.width = width;
            return this;
        }

        @Override
        public Pane.Builder height(int height) {
            this.height = height;
            return this;
        }

        @Override
        public Pane build() {
            return new SpigotSimplePane(this.topLeftX, this.topLeftY, this.height, this.width);
        }
    }
}
