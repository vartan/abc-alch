package com.vartan.abc.model;

public class AmountDiffer {
    private int previousValue = 0;
    private int currentValue;
    private int emptyValues = 2;

    public AmountDiffer() {
    }

    public AmountDiffer(int currentValue) {
        this.put(currentValue);
    }

    public AmountDiffer put(int currentValue) {
        this.previousValue = this.currentValue;
        this.currentValue = currentValue;
        this.emptyValues = Math.max(emptyValues - 1, 0);
        return this;
    }

    public int getDiff() {
        if (emptyValues > 0) {
            return 0;
        }
        return this.currentValue - this.previousValue;
    }

    public void reset(int currentValue) {
        this.emptyValues = 1;
        this.currentValue = currentValue;
    }
}
