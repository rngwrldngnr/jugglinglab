// MHNThrow.java
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

package jugglinglab.notation;

import jugglinglab.util.*;


public class MHNThrow {
    public int juggler;         ///< indexed from 0
    public int hand;            ///< MHNPattern.RIGHT_HAND or LEFT_HAND
    public int index;
    public int slot;
    public int targetjuggler;       ///< indexed from 0
    public int targethand;      ///< MHNPattern.RIGHT_HAND or LEFT_HAND
    public int targetindex;
    public int targetslot;
    public int handsindex;      ///< index of throw in hands sequence, if one exists
    public int pathnum = -1;
    public String mod;
    public MHNThrow master = null;
    public MHNThrow source = null;
    public MHNThrow target = null;
    public boolean catching = false;    ///< are we catching just before this throw?
    public int catchnum = -1;       ///< order (starting at 1) to make catches

    public MHNThrow() {}

    public MHNThrow(int j, int h, int i, int s, int tj, int th, int ti, int ts, String m) {
        this.juggler = j;
        this.hand = h;
        this.index = i;
        this.slot = s;
        this.targetjuggler = tj;
        this.targethand = th;   // 0 for right hand, 1 for left hand
        this.targetindex = ti;
        this.targetslot = ts;
        this.mod = m;
    }
}
