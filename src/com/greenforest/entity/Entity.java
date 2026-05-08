package com.greenforest.entity;

import java.awt.*;

public abstract class Entity {

    protected float x, y;
    protected int   hp, maxHp;
    protected int   size;
    protected boolean dead = false;

    public Entity(float x, float y, int hp, int size) {
        this.x     = x;
        this.y     = y;
        this.hp    = hp;
        this.maxHp = hp;
        this.size  = size;
    }

    public abstract void update(float dt, float playerX, float playerY);
    public abstract void draw(Graphics2D g2, int camX, int camY);

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            hp   = 0;
            dead = true;
        }
    }

    // Getters
    public float getX()    { return x; }
    public float getY()    { return y; }
    public int   getHp()   { return hp; }
    public int   getMaxHp(){ return maxHp; }
    public int   getSize() { return size; }
    public boolean isDead(){ return dead; }

    public Rectangle getBounds() {
        return new Rectangle((int)x - size/2, (int)y - size/2, size, size);
    }
}