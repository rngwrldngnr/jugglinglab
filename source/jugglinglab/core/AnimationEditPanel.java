// AnimationEditPanel.java
//
// Copyright 2018 by Jack Boyce (jboyce@gmail.com) and others

/*
    This file is part of Juggling Lab.

    Juggling Lab is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Juggling Lab is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Juggling Lab; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package jugglinglab.core;

import java.awt.*;
import java.awt.event.*;

import jugglinglab.util.*;
import jugglinglab.jml.JMLEvent;
import jugglinglab.jml.JMLPattern;


public class AnimationEditPanel extends AnimationPanel {
    protected LadderDiagram ladder;
    protected boolean event_active = false;
    protected JMLEvent event;
    protected int xlow1, xhigh1, ylow1, yhigh1;
    protected int xlow2, xhigh2, ylow2, yhigh2;
    protected boolean dragging = false;
    protected boolean dragging_left = false;
    protected int xstart, ystart, xdelta, ydelta;

    public AnimationEditPanel() {
        super();
    }

    @Override
    protected void initHandlers() {
        final JMLPattern fpat = anim.pat;

        this.addMouseListener(new MouseAdapter() {
            long lastpress = 0L;
            long lastenter = 1L;

            @Override
            public void mousePressed(MouseEvent me) {
                lastpress = me.getWhen();

                // The following (and the equivalent in mouseReleased()) is a hack to swallow
                // a mouseclick when the browser stops reporting enter/exit events because the
                // user has clicked on something else.  The system reports simultaneous enter/press
                // events when the user mouses down in the component; we want to swallow this as a
                // click, and just use it to get focus back.
                if (jc.mousePause && lastpress == lastenter)
                    return;

                if (!engineAnimating)
                    return;
                if (writingGIF)
                    return;

                if (event_active) {
                    int mx = me.getX();
                    int my = me.getY();
                    if (mx >= xlow1 && mx <= xhigh1 && my >= ylow1 && my <= yhigh1) {
                        dragging = true;
                        dragging_left = true;
                        xstart = mx;
                        ystart = my;
                        xdelta = ydelta = 0;
                        return;
                    }
                    int t = AnimationEditPanel.this.getSize().width / 2;
                    if (mx >= (xlow2+t) && mx <= (xhigh2+t) && my >= ylow2 && my <= yhigh2) {
                        dragging = true;
                        dragging_left = false;
                        xstart = mx;
                        ystart = my;
                        xdelta = ydelta = 0;
                        return;
                    }
                }

                // if we get here, start a reposition of the camera
                AnimationEditPanel.this.startx = me.getX();
                AnimationEditPanel.this.starty = me.getY();
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (jc.mousePause && lastpress == lastenter)
                    return;
                if (writingGIF)
                    return;
                if (!engineAnimating && engine != null && engine.isAlive()) {
                    setPaused(!enginePaused);
                    return;
                }

                if (event_active && dragging) {
                    JMLEvent master = (event.isMaster() ? event : event.getMaster());
                    boolean flipx = (event.getHand() != master.getHand());

                    Coordinate newgc = anim.ren1.getScreenTranslatedCoordinate(
                            event.getGlobalCoordinate(), xdelta, ydelta);
                    if (AnimationEditPanel.this.jc.stereo) {
                        Coordinate newgc2 = anim.ren2.getScreenTranslatedCoordinate(
                                event.getGlobalCoordinate(), xdelta, ydelta);
                        newgc = Coordinate.add(newgc, newgc2);
                        newgc.setCoordinate(0.5*newgc.x, 0.5*newgc.y, 0.5*newgc.z);
                    }

                    Coordinate newlc = anim.pat.convertGlobalToLocal(newgc,
                                            event.getJuggler(), event.getT());
                    Coordinate deltalc = Coordinate.sub(newlc,
                                            event.getLocalCoordinate());

                    if (flipx)
                        deltalc.x = -deltalc.x;
                    Coordinate orig = master.getLocalCoordinate();
                    master.setLocalCoordinate(Coordinate.add(orig, deltalc));
                    xdelta = ydelta = 0;

                    EditLadderDiagram eld = (EditLadderDiagram)ladder;
                    eld.activeEventMoved();
                }
                AnimationEditPanel.this.cameradrag = false;
                dragging = false;
                if (me.getX() == startx && me.getY() == starty &&
                                engine != null && engine.isAlive())
                    setPaused(!enginePaused);
                if (AnimationEditPanel.this.getPaused())
                    repaint();
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                lastenter = me.getWhen();
                if (jc.mousePause && !writingGIF)
                    setPaused(waspaused);
                outside = false;
                outside_valid = true;
            }

            @Override
            public void mouseExited(MouseEvent me) {
                if (jc.mousePause && !writingGIF) {
                    waspaused = getPaused();
                    // waspaused_valid = true;
                    setPaused(true);
                }
                outside = true;
                outside_valid = true;
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                if (!engineAnimating)
                    return;
                if (writingGIF)
                    return;
                if (dragging) {
                    int mx = me.getX();
                    int my = me.getY();
                    xdelta = mx - xstart;
                    ydelta = my - ystart;
                    repaint();
                } else if (!cameradrag) {
                    AnimationEditPanel.this.cameradrag = true;
                    AnimationEditPanel.this.lastx = AnimationEditPanel.this.startx;
                    AnimationEditPanel.this.lasty = AnimationEditPanel.this.starty;
                    AnimationEditPanel.this.dragcamangle = AnimationEditPanel.this.anim.getCameraAngle();
                }

                if (!cameradrag)
                    return;

                int xdelta = me.getX() - AnimationEditPanel.this.lastx;
                int ydelta = me.getY() - AnimationEditPanel.this.lasty;
                AnimationEditPanel.this.lastx = me.getX();
                AnimationEditPanel.this.lasty = me.getY();
                double[] ca = AnimationEditPanel.this.dragcamangle;
                ca[0] += (double)(xdelta) * 0.02;
                ca[1] -= (double)(ydelta) * 0.02;
                if (ca[1] < 0.0001)
                    ca[1] = 0.0001;
                if (ca[1] > JLMath.toRad(90.0))
                    ca[1] = JLMath.toRad(90.0);
                while (ca[0] < 0.0)
                    ca[0] += JLMath.toRad(360.0);
                while (ca[0] >= JLMath.toRad(360.0))
                    ca[0] -= JLMath.toRad(360.0);

                double[] snappedcamangle = snapCamera(ca);
                AnimationEditPanel.this.anim.setCameraAngle(snappedcamangle);

                if (event_active)
                    createEventView();
                if (AnimationEditPanel.this.getPaused())
                    repaint();
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (!engineAnimating)
                    return;
                if (writingGIF)
                    return;
                anim.setDimension(AnimationEditPanel.this.getSize());
                if (event_active)
                    createEventView();
                repaint();
            }
        });
    }

    @Override
    protected double[] snapCamera(double[] ca) {
        double[] result = new double[2];
        result[0] = ca[0];
        result[1] = ca[1];

        if (result[1] < snapangle)
            result[1] = 0.000001;
        if (result[1] > (JLMath.toRad(90.0) - snapangle))
            result[1] = JLMath.toRad(90.0);

        double a = 0.0;
        boolean snap_horizontal = true;

        if (event_active)
            a = JLMath.toRad(anim.pat.getJugglerAngle(event.getJuggler(), event.getT()));
        else if (anim.pat.getNumberOfJugglers() == 1)
            a = JLMath.toRad(anim.pat.getJugglerAngle(1, getTime()));
        else
            snap_horizontal = false;

        if (snap_horizontal) {
            if (anglediff(a - result[0]) < snapangle)
                result[0] = a;
            else if (anglediff(a + 90.0*0.0174532925194 - result[0]) < snapangle)
                result[0] = a + 90.0*0.0174532925194;
            else if (anglediff(a + 180.0*0.0174532925194 - result[0]) < snapangle)
                result[0] = a + 180.0*0.0174532925194;
            else if (anglediff(a + 270.0*0.0174532925194 - result[0]) < snapangle)
                result[0] = a + 270.0*0.0174532925194;
        }
        return result;
    }

    @Override
    public void restartJuggle(JMLPattern pat, AnimationPrefs newjc)
                    throws JuggleExceptionUser, JuggleExceptionInternal {
        super.restartJuggle(pat, newjc);
        if (event_active)
            createEventView();
    }

    public void setLadderDiagram(LadderDiagram ladder) {
        this.ladder = ladder;
    }

    // set position of tracker bar in ladder diagram as we animate
    @Override
    public void setTime(double time) {
        super.setTime(time);
        if (ladder != null)
            ladder.setTime(time);
    }

    public void activateEvent(JMLEvent ev) {
        if ((ladder != null) && !(ladder instanceof EditLadderDiagram))
            return;
        event = ev;
        event_active = true;
        createEventView();
    }

    public void deactivateEvent() {
        event_active = false;
    }

    protected void createEventView() {
        if (event_active) {
            // translate by one pixel and see how far it was in juggler space
        {
            Coordinate c = event.getGlobalCoordinate();
            Coordinate c2 = anim.ren1.getScreenTranslatedCoordinate(c, 1, 0);
            Coordinate dc = Coordinate.sub(c, c2);
            double dl = Math.sqrt(dc.x*dc.x + dc.y*dc.y + dc.z*dc.z);
            int boxhw = (int)(0.5 + 5.0 / dl);  // pixels corresponding to 5cm in juggler space

            int[] center = anim.ren1.getXY(c);
            xlow1 = center[0] - boxhw;
            ylow1 = center[1] - boxhw;
            xhigh1 = center[0] + boxhw;
            yhigh1 = center[1] + boxhw;
        }

            if (this.jc.stereo) {
                Coordinate c = event.getGlobalCoordinate();
                Coordinate c2 = anim.ren2.getScreenTranslatedCoordinate(c, 1, 0);
                Coordinate dc = Coordinate.sub(c, c2);
                double dl = Math.sqrt(dc.x*dc.x + dc.y*dc.y + dc.z*dc.z);
                int boxhw = (int)(0.5 + 5.0 / dl);  // pixels corresponding to 5cm in juggler space

                int[] center = anim.ren2.getXY(c);
                xlow2 = center[0] - boxhw;
                ylow2 = center[1] - boxhw;
                xhigh2 = center[0] + boxhw;
                yhigh2 = center[1] + boxhw;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (message != null)
            drawString(message, g);
        else if (engineRunning && !writingGIF) {
            try {
                anim.drawFrame(getTime(), g, this.cameradrag);
                drawEvent(g);
            } catch (JuggleExceptionInternal jei) {
                killAnimationThread();
                System.out.println(jei.getMessage());
                System.exit(0);
                // ErrorDialog.handleException(jei);
            }
        }
    }

    protected void drawEvent(Graphics g) throws JuggleExceptionInternal {
        if (!event_active)
            return;

        Dimension d = this.getSize();
        Graphics g2 = g;
        if (this.jc.stereo)
            g2 = g.create(0,0,d.width/2,d.height);

        if (g2 instanceof Graphics2D) {
            Graphics2D g22 = (Graphics2D)g2;
            g22.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g2.setColor(Color.green);
        g2.drawLine(xlow1+xdelta, ylow1+ydelta, xhigh1+xdelta, ylow1+ydelta);
        g2.drawLine(xhigh1+xdelta, ylow1+ydelta, xhigh1+xdelta, yhigh1+ydelta);
        g2.drawLine(xhigh1+xdelta, yhigh1+ydelta, xlow1+xdelta, yhigh1+ydelta);
        g2.drawLine(xlow1+xdelta, yhigh1+ydelta, xlow1+xdelta, ylow1+ydelta);

        if (this.jc.stereo) {
            g2 = g.create(d.width/2,0,d.width/2,d.height);
            if (g2 instanceof Graphics2D) {
                Graphics2D g22 = (Graphics2D)g2;
                g22.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g2.setColor(Color.green);
            g2.drawLine(xlow2+xdelta, ylow2+ydelta, xhigh2+xdelta, ylow2+ydelta);
            g2.drawLine(xhigh2+xdelta, ylow2+ydelta, xhigh2+xdelta, yhigh2+ydelta);
            g2.drawLine(xhigh2+xdelta, yhigh2+ydelta, xlow2+xdelta, yhigh2+ydelta);
            g2.drawLine(xlow2+xdelta, yhigh2+ydelta, xlow2+xdelta, ylow2+ydelta);
        }
    }
}
