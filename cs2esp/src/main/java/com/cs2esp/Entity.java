package com.cs2esp;

public class Entity {
    
    public String name;
    public int health;
    public int armor;
    public int teamNum;
    public long pawn;
    public Vector3 position;
    
    public Entity(String name, int health, int armor, int teamNum, long pawn, Vector3 position) {
        this.name = name;
        this.health = health;
        this.armor = armor;
        this.teamNum = teamNum;
        this.pawn = pawn;
        this.position = position;
    }

    public Entity() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getArmor() {
        return armor;
    }

    public void setArmor(int armor) {
        this.armor = armor;
    }

    public int getTeamNum() {
        return teamNum;
    }

    public void setTeamNum(int teamNum) {
        this.teamNum = teamNum;
    }

    public long getPawn() {
        return pawn;
    }

    public void setPawn(long pawn) {
        this.pawn = pawn;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }
}
