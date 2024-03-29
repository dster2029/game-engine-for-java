package com.gej.object;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import com.gej.core.Global;
import com.gej.core.Updateable;
import com.gej.graphics.Animation;
import com.gej.input.GMouse;
import com.gej.util.GUtil;
import com.gej.util.ImageTool;

/**
 * This class represents objects in a game. Any object must extend this class.
 * The events are automatically managed if this object is dynamically loaded
 * through a map loader. Else you have to detect them on your own. This is an
 * example object.
 * 
 * <pre>
 * public class MyObject extends GObject {
 * 
 *     public MyObject(float x, float y) {
 *         super(Game.loadImage(&quot;&lt;image&gt;&quot;));
 *         setX(x);
 *         setY(y);
 *     }
 * 
 *     public void update(long elapsedTime){
 *         // Update this object
 *     }
 * 
 *     public void collision(GObject other){
 *         // Another object has been collided
 *         if (other instanceof MyObject2) {
 *             // Other is MyObject2
 *         }
 *     }
 * 
 * }
 * </pre>
 * 
 * @author Sri Harsha Chilakapati
 */
public class GObject implements Updateable {

    // The animation object
    private Animation anim;
    // The positions and velocities
    private float x;
    private float y;
    private float dx;
    private float dy;
    private float oldX;
    private float oldY;
    
    // The depth of this object
    protected int depth = 0;

    // Is this object visible??
    protected boolean visible = true;

    // If this object is solid and alive
    private boolean solid = false;
    private boolean alive = true;
    
    // Is this object a collision listener???
    private boolean collision_listener = true;
    
    // The bounds
    private Rectangle bounds;

    /**
     * Constructs an object which is invisible
     */
    public GObject() {
        this(ImageTool.getEmptyImage(1, 1));
    }

    /**
     * Constructs an invisible object at a position x,y
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public GObject(float x, float y) {
        this();
        setX(x);
        setY(y);
    }

    /**
     * Constructs an object with an image at a position x,y
     * 
     * @param img The image of the object
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public GObject(Image img, float x, float y) {
        this(img);
        setX(x);
        setY(y);
    }

    /**
     * Constructs an object with an animation at a position x,y
     * 
     * @param anim The animation object
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public GObject(Animation anim, float x, float y) {
        this(anim);
        setX(x);
        setY(y);
    }

    /**
     * Constructs an object with an animation object.
     * 
     * @param anim The animation used for this object
     */
    public GObject(Animation anim) {
        this.anim = anim;
    }

    /**
     * Constructs an object with an image object.
     * 
     * @param img The image used for this object
     */
    public GObject(Image img) {
        this.anim = new Animation(new Image[] { img }, 100);
    }

    /**
     * {@inheritDoc}
     */
    public GObject clone(){
        try {
            GObject obj = getClass().newInstance();
            obj.setAnimation(getAnimation());
            obj.setDepth(getDepth());
            obj.setSolid(isSolid());
            obj.setVelocityX(getVelocityX());
            obj.setVelocityY(getVelocityY());
            obj.setVisible(isVisible());
            obj.setX(getX());
            obj.setY(getY());
            return obj;
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    /**
     * Updates this object based on time
     */
    public void update(long elapsedTime){
    }

    /**
     * Updates the object's animations
     * 
     * @param elapsedTime
     */
    public final void superUpdate(long elapsedTime){
        update(elapsedTime);
        anim.update(elapsedTime);
    }

    /**
     * Moves this object based on time.
     */
    public void move(){
        moveHorizontally();
        moveVertically();
    }

    /**
     * Moves the object horizontally
     */
    public void moveHorizontally(){
        float nx = x + dx;
        if (check(nx, getY())) {
            setX(nx);
        }
    }

    /**
     * Moves the object vertically
     */
    public void moveVertically(){
        float ny = y + dy;
        if (check(getX(), ny)) {
            setY(ny);
        }
    }

    /**
     * Sets the depth of this object
     * 
     * @param depth The depth of this object
     */
    public void setDepth(int depth){
        this.depth = depth;
    }

    /**
     * Gets the depth of the object
     * 
     * @return The depth of the object
     */
    public int getDepth(){
        return depth;
    }

    /**
     * Could be used to give limits to the object.
     * 
     * @param nx The new x-position
     * @param ny The new y-position
     * @return True if can move, else false.
     */
    public boolean check(float nx, float ny){
        return true;
    }

    /**
     * Returns true if this object has been clicked by a mouse button
     */
    public boolean isClicked(){
        return (getBounds().contains(GMouse.MOUSE_X, GMouse.MOUSE_Y))
                && (GMouse.isClicked());
    }

    /**
     * Returns the boundaries of this object.
     * 
     * @return The boundaries of this object as a rectangle.
     */
    public Rectangle getBounds(){
        if (bounds==null)
            return bounds = new Rectangle(Math.round(getX()), Math.round(getY()), getWidth(), getHeight());
        bounds.x = (int)getX();
        bounds.y = (int)getY();
        bounds.width = getWidth();
        bounds.height = getHeight();
        return bounds;
    }

    /**
     * Checks if this object is colliding the other object. You can enable
     * pixel-perfect collision detection by using
     * 
     * <pre>
     * Global.USE_PIXELPERFECT_COLLISION = true;
     * </pre>
     * 
     * @param other The other object
     * @return True if a collision has been found.
     */
    public boolean isCollidingWith(GObject other){
        if (this == other) {
            return false;
        }
        if (!isAlive() || !other.isAlive()) {
            return false;
        }
        int x1 = (int) getX();
        int y1 = (int) getY();
        int x2 = (int) other.getX();
        int y2 = (int) other.getY();
        boolean bool = false;
        if (x1 < x2 + other.getWidth()) {
            if (x2 < x1 + getWidth()) {
                if (y1 < y2 + other.getHeight()) {
                    if (y2 < y1 + getHeight()) {
                        bool = true;
                    }
                }
            }
        }
        if (bool && Global.USE_PIXELPERFECT_COLLISION) {
            bool = GUtil.isPixelPerfectCollision(x, y, getAnimation().getBufferedImage(), other.getX(), other.getY(), other.getAnimation().getBufferedImage());
        }
        return bool;
    }
    
    /**
     * Automatically aligns this object with other
     * @param other The other object
     */
    public void alignWith(GObject other){
        // Get the intersection rectangle
        Rectangle i = getBounds().intersection(other.getBounds());
        if (i.width > i.height){
            // A vertical collision
            if (getY()<other.getY()){
                // We're on top of other
                setY(other.getY()-getHeight());
            } else if (getY()>other.getY()){
                // We're below other
                setY(other.getY()+other.getHeight());
            }
        } else {
            // A horizontal collision
            if (getX()<other.getX()){
                // We're left to other
                setX(other.getX()-getWidth());
            } else if (getX()>other.getX()){
                // We're right to other
                setX(other.getX()+other.getWidth());
            }
        }
    }

    /**
     * Sets the visibility of this object
     * 
     * @param val The visibility of this object
     */
    public void setVisible(boolean val){
        visible = val;
    }

    /**
     * Checks if this object is visible. Note that this value is independent of
     * the map view.
     * 
     * @return True if visible else false.
     */
    public boolean isVisible(){
        return visible;
    }

    /**
     * Returns the state of this object.
     * 
     * @return True if alive. else false.
     */
    public boolean isAlive(){
        return alive;
    }

    /**
     * Destroy's this object so that it won't receive any events and will not
     * render again.
     */
    public void destroy(){
        alive = false;
    }

    /**
     * Checks if an object has collided this on top. Like in this example
     * 
     * <pre>
     * ---------
     * |       |
     * | Other |
     * |       |
     * -----+--+------
     *      |        |
     *      |  this  |
     *      |        |
     *      ----------
     * </pre>
     * 
     * @param other The other object
     * @return True if collision on top
     */
    public boolean isTopCollision(GObject other){
        boolean bool = isCollidingWith(other);
        if (bool) {
            if (getY() >= other.getY()) {
                bool = true;
            } else {
                bool = false;
            }
        }
        return bool;
    }

    /**
     * Checks if an object has collided this on bottom like in this example
     * 
     * <pre>
     * ----------
     * |        |
     * |  This  |
     * |        |
     * -----+---------+
     *      |         |
     *      |  Other  |
     *      |         |
     *      -----------
     * </pre>
     * 
     * @param other The other object
     * @return True if bottom wise collision
     */
    public boolean isBottomCollision(GObject other){
        boolean bool = isCollidingWith(other);
        if (bool) {
            if (getY() <= other.getY()) {
                bool = true;
            } else {
                bool = false;
            }
        }
        return bool;
    }

    /**
     * Checks if an object has collided this from left like in this example.
     * 
     * <pre>
     * -----------
     * |         +-----------
     * |  Other  |          |
     * |         |   This   |
     * ----------|          |
     *           ------------
     * </pre>
     * 
     * @param other The other object
     * @return True if found a collision on the left
     */
    public boolean isLeftCollision(GObject other){
        boolean bool = isCollidingWith(other);
        if (bool) {
            if (getX() >= other.getX()) {
                bool = true;
            } else {
                bool = false;
            }
        }
        return bool;
    }

    /**
     * Checks if an object has collided this from the right like in this example
     * 
     * <pre>
     * ----------
     * |        |
     * |  this  +----------
     * |        |         |
     * ---------+  Other  |
     *          |         |
     *          -----------
     * </pre>
     * 
     * @param other The other object.
     * @return True if found a collision on the right.
     */
    public boolean isRightCollision(GObject other){
        boolean bool = isCollidingWith(other);
        if (bool) {
            if (getX() <= other.getX()) {
                bool = true;
            } else {
                bool = false;
            }
        }
        return bool;
    }

    /**
     * Called by the map automatically if a collision event occurs.
     * Don't call the super method.
     * 
     * @param other The object which has been collided.
     */
    public void collision(GObject other){
        collision_listener = false;
    }

    /**
     * Moves this object to a specified point with a specific speed. Note that
     * the velocity used is independent of vertical or horizontal velocities of
     * this object.
     * 
     * @param nx The new x-position
     * @param ny The new y-position
     * @param speed The speed with which to move
     * @return True if the new point has been reached
     */
    public boolean moveTo(float nx, float ny, float speed){
        boolean _x = false;
        boolean _y = false;
        int distance = (int) Math.sqrt((double) ((x - nx) * (x - nx) + (y - ny)
                * (y - ny)));
        float vel = Math.min(distance, speed);
        float newx = x;
        float newy = y;
        if (x > nx) {
            // We should move left
            newx -= vel;
        } else if (x < nx) {
            // We should move right
            newx += vel;
        } else {
            _x = true;
        }
        if (y > ny) {
            // We should move up
            newy -= vel;
        } else if (y < ny) {
            // We should move down
            newy += vel;
        } else {
            _y = true;
        }
        if (check(newx, newy)) {
            x = newx;
            y = newy;
        }
        return (_x && _y);
    }

    /**
     * Moves this object to a specified point with a specific speed. Note that
     * the velocity used is independent of vertical or horizontal velocities of
     * this object.
     * 
     * @param nx The new x-position
     * @param ny The new y-position
     * @param speed The speed with which to move
     * @return True if the new point has been reached
     */
    public boolean moveTo(int nx, int ny, float speed){
        return moveTo((float) nx, (float) ny, speed);
    }

    /**
     * This object bounces back from the other object in a natural way. Please
     * realize that the bounce is not completely accurate because this depends
     * on many properties. But in many situations the effect is good enough. Had
     * some bugs in pixel perfect detection mode if the image has a larger area
     * of complete alpha. If using PPCD, make the object fit the image size by
     * removing the alpha and resizing the image.
     */
    public void bounce(GObject other){
        int xd = (int) ((other.x + other.getWidth() / 2) - (x + getWidth() / 2));
        int yd = (int) ((other.y + other.getHeight() / 2) - (y + getHeight() / 2));
        if (xd < 0) {
            xd = -xd;
        }
        if (yd < 0) {
            yd = -yd;
        }
        if (xd > yd) {
            dx = -dx;
        } else {
            dy = -dy;
        }
    }
    
    /**
     * This object is aligned next to an object.
     */
    public void alignNextTo(GObject other){
        int xd = (int) ((other.x + other.getWidth() / 2) - (x + getWidth() / 2));
        int yd = (int) ((other.y + other.getHeight() / 2) - (y + getHeight() / 2));
        if (xd < 0) {
            xd = -xd;
        }
        if (yd < 0) {
            yd = -yd;
        }
        if (xd > yd) {
            alignHorizontallyTo(other);
        } else {
            alignVerticallyTo(other);
        }
    }
    
    /**
     * Align this object horizontally with other
     */
    public void alignHorizontallyTo(GObject other){
        if (getX()>other.getX()){
            setX(other.getX()+other.getWidth());
        } else if (getX()<other.getX()){
            setX(other.getX()-getWidth());
        }
    }
    
    /**
     * Align this object vertically with other
     */
    public void alignVerticallyTo(GObject other){
        if (getY()>other.getY()){
            setY(other.getY()+other.getHeight());
        } else if (getY()<other.getY()){
            setY(other.getY()-getHeight());
        }
    }

    /**
     * Predicts the next x-position based on the velocity
     * 
     * @return The next x-position
     */
    public float getNextX(){
        return x + dx;
    }

    /**
     * Predicts the next y-position based on the velocity
     * 
     * @return The next y-position
     */
    public float getNextY(){
        return y + dy;
    }

    /**
     * Moves this object to the next point which on next move would collide.
     * Works perfectly only in the pixel perfect collision detection mode. This
     * automatically resets the velocities.
     * 
     * @param other The other object
     */
    public void moveToContact(GObject other){
        moveToContact(other, true, true);
    }

    /**
     * Moves this object to the next point which on next move would collide.
     * Works perfectly only in the pixel perfect collision detection mode. This
     * automatically resets the velocities.
     * 
     * @param other The other object
     * @param horizontal Should move horizontally?
     * @param vertical Should move vertically?
     */
    public void moveToContact(GObject other, boolean horizontal, boolean vertical){
        if (horizontal) {
            if (isLeftCollision(other)) {
                setX(other.getX() + other.getWidth());
                while (!isCollidingWith(other)) {
                    setX(getX() - 1);
                }
            } else if (isRightCollision(other)) {
                setX(other.getX() - getWidth());
                while (!isCollidingWith(other)) {
                    setX(getX() + 1);
                }
            }
            setVelocityX(0);
        }
        if (vertical) {
            if (isTopCollision(other)) {
                setY(other.getY() + other.getHeight());
                while (!isCollidingWith(other)) {
                    setY(getY() - 1);
                }
            } else if (isBottomCollision(other)) {
                setY(other.getY() - getHeight());
                while (!isCollidingWith(other)) {
                    setY(getY() + 1);
                }
            }
            setVelocityY(0);
        }
    }

    /**
     * Set's the solid state of this object.
     * 
     * @param value The solid value.
     */
    public void setSolid(boolean value){
        solid = value;
    }

    /**
     * Checks the solid state of this object.
     * 
     * @return True if solid.
     */
    public boolean isSolid(){
        return solid;
    }

    /**
     * Returns the current x-position of this object
     * 
     * @return The current x-position of this object
     */
    public float getX(){
        return x;
    }

    /**
     * Returns the current y-position of this object
     * 
     * @return The current y-position of this object
     */
    public float getY(){
        return y;
    }

    /**
     * Set's the x-position of this object
     * 
     * @param x The new x-position
     */
    public void setX(float x){
        oldX = this.x;
        this.x = x;
    }

    /**
     * Set's the y-position of this object
     * 
     * @param y The new y-position
     */
    public void setY(float y){
        oldY = this.y;
        this.y = y;
    }
    
    /**
     * @return The last x-position of this object
     */
    public float getOldX(){
        return oldX;
    }
    
    /**
     * @return The last y-position of this object
     */
    public float getOldY(){
        return oldY;
    }

    /**
     * Gets you the width of this object
     * 
     * @return The width of this object in pixels
     */
    public int getWidth(){
        return anim.getImage().getWidth(null);
    }

    /**
     * Get's you the height of this object
     * 
     * @return The height of this object in pixels
     */
    public int getHeight(){
        return anim.getImage().getHeight(null);
    }

    /**
     * Gets you the horizontal velocity of this object
     * 
     * @return The horizontal velocity
     */
    public float getVelocityX(){
        return dx;
    }

    /**
     * Gets you the vertical velocity of this object
     * 
     * @return The vertical velocity
     */
    public float getVelocityY(){
        return dy;
    }

    /**
     * Sets the horizontal velocity of this object
     * 
     * @param dx The new horizontal velocity
     */
    public void setVelocityX(float dx){
        this.dx = dx;
    }

    /**
     * Sets the vertical velocity of this object
     * 
     * @param dy The new vertical velocity
     */
    public void setVelocityY(float dy){
        this.dy = dy;
    }

    /**
     * Returns the image of this object
     * 
     * @return The image which is used to represent this object
     */
    public Image getImage(){
        if (isAlive() && isVisible()) {
            return anim.getImage();
        } else {
            return null;
        }
    }

    /**
     * Returns the animation used by this object
     * 
     * @return The current animation
     */
    public Animation getAnimation(){
        return anim;
    }

    /**
     * Sets the animation of this object
     * 
     * @param anim The new animation
     */
    public void setAnimation(Animation anim){
        this.anim = anim;
    }

    /**
     * Sets the current image of this object
     * 
     * @param img The new image
     */
    public void setImage(Image img){
        this.anim = new Animation(new Image[] { img }, 150);
    }
    
    /**
     * @return Whether the collision method was implemented by the object.
     */
    public boolean isCollisionListener(){
        return collision_listener;
    }

    /**
     * Render's this object onto the graphics context.
     * 
     * @param g The graphics context.
     */
    public void render(Graphics2D g){
        g.drawImage(getImage(), Math.round(x), Math.round(y), null);
    }

}
