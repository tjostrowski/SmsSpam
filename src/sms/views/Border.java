/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sms.views;

import android.graphics.Color;

/**
 *
 * @author tomek
 */
public class Border {
    private int orientation;
    private int width;
    private int color = Color.BLACK;
    private int style;
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public int getStyle() {
        return style;
    }
    public void setStyle(int style) {
        this.style = style;
    }
    public int getOrientation() {
        return orientation;
    }
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    public Border(int style, int orientation, int width, int color) {
        this.width = width;
        this.color = color;        
        this.style = style;
        this.orientation = orientation;
    }
}
