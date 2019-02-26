// MHNPattern.java
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

import java.util.*;

import jugglinglab.util.*;


public class MHNPattern {
    static final ResourceBundle guistrings = jugglinglab.JugglingLab.guistrings;
    static final ResourceBundle errorstrings = jugglinglab.JugglingLab.errorstrings;

    protected static double bps_default = -1.0; // calculate bps
    protected static double dwell_default = 1.3;
    protected static double gravity_default = 980.0;
    protected static double propdiam_default = 10.0;
    protected static double bouncefrac_default = 0.9;
    protected static String prop_default = "ball";

    // original config string:
    protected String config;

    // input parameters:
    protected String pattern;
    protected double bps = bps_default;
    protected double dwell = dwell_default;
    protected double gravity = gravity_default;
    protected double propdiam = propdiam_default;
    protected double bouncefrac = bouncefrac_default;
    protected String prop = prop_default;
    protected String[] color;

    // internal variables:
    protected int numjugglers;
    protected int numpaths;
    protected int period;
    protected int max_occupancy;
    protected MHNThrow[][][][] th;
    protected MHNHands hands;
    protected MHNBody bodies;
    protected int max_throw;
    protected int indexes;
    protected ArrayList<MHNSymmetry> symmetry;

    public static final int RIGHT_HAND = 0;
    public static final int LEFT_HAND = 1;

    protected int getNumberOfJugglers()         { return numjugglers; }
    protected int getNumberOfPaths()            { return numpaths; }
    protected int getPeriod()                   { return period; }
    protected int getIndexes()                  { return indexes; }
    protected int getMaxOccupancy()             { return max_occupancy; }
    protected int getMaxThrow()                 { return max_throw; }
    protected MHNThrow[][][][] getThrows()      { return th; }
    protected int getNumberOfSymmetries()       { return symmetry.size(); }
    protected String getPropName()              { return prop; }
    protected void addSymmetry(MHNSymmetry ss)  { symmetry.add(ss); }
    protected MHNSymmetry getSymmetry(int i)    { return symmetry.get(i); }


    public void parseInput(String config) throws JuggleExceptionUser, JuggleExceptionInternal {
        if (config.indexOf((int)'=') == -1) { // just the pattern
            config = "pattern=" + config;
        }

        ParameterList pl = new ParameterList(config);
        String temp = null;

        pattern = pl.getParameter("pattern");
        if (pattern == null)
            throw new JuggleExceptionUser(errorstrings.getString("Error_no_pattern"));

        if ((temp = pl.getParameter("bps")) != null) {
            try {
                bps = Double.valueOf(temp).doubleValue();
            } catch (NumberFormatException nfe) {
                throw new JuggleExceptionUser(errorstrings.getString("Error_bps_value"));
            }
        }

        if ((temp = pl.getParameter("dwell")) != null) {
            try {
                dwell = Double.valueOf(temp).doubleValue();
            } catch (NumberFormatException nfe) {
                throw new JuggleExceptionUser(errorstrings.getString("Error_dwell_value"));
            }
        }

        if ((temp = pl.getParameter("hands")) != null)
            hands = new MHNHands(temp);

        if ((temp = pl.getParameter("body")) != null)
            bodies = new MHNBody(temp);

        if ((temp = pl.getParameter("gravity")) != null) {
            try {
                gravity = Double.valueOf(temp).doubleValue();
            } catch (NumberFormatException e) {
            }
        }

        if ((temp = pl.getParameter("propdiam")) != null) {
            try {
                propdiam = Double.valueOf(temp).doubleValue();
            } catch (NumberFormatException e) {
            }
        }

        if ((temp = pl.getParameter("bouncefrac")) != null) {
            try {
                bouncefrac = Double.valueOf(temp).doubleValue();
            } catch (NumberFormatException e) {
            }
        }

        if ((temp = pl.getParameter("prop")) != null) {
            prop = temp;
        }

        if ((temp = pl.getParameter("colors")) != null) {
            if (temp.trim().equals("mixed"))
                temp = "{red}{green}{blue}{yellow}{cyan}{magenta}{orange}{pink}{gray}{black}";

            StringTokenizer st1 = new StringTokenizer(temp, "}", false);
            StringTokenizer st2 = null;
            String          str = null;

            int numcolors = st1.countTokens();
            color = new String[numcolors];

            // Parse the colors parameter
            for (int i = 0; i < numcolors; i++) {
                // Look for next {...} block
                str = st1.nextToken().replace('{', ' ').trim();

                // Parse commas
                st2 = new StringTokenizer(str, ",", false);

                switch (st2.countTokens()) {
                    case 1:
                        // Use the value as a color name
                        color[i] = st2.nextToken().trim().toLowerCase();
                        break;
                    case 3:
                        // Use the three values as RGB values
                        color[i] = "{" + str + "}";
                        break;
                    default:
                        throw new JuggleExceptionUser(errorstrings.getString("Error_color_format"));
                }

                // System.out.println("color "+i+" = "+color[i]);
            }
        }

        this.config = config;
    }

    // print out the configuration parameters in a standard order

    public String toString() {
        if (this.config == null)
            return null;

        ParameterList pl = new ParameterList(this.config);
        String result = "";

        // write the parameters out in a standard order
        List<String> keys = Arrays.asList("pattern", "bps", "dwell", "hands", "body",
                                "gravity", "propdiam", "bouncefrac", "prop", "colors");

        for (String key : keys) {
            String value = pl.getParameter(key);
            if (value != null)
                result += key + "=" + value + ";";
        }

        if (result.length() > 0)
            result = result.substring(0, result.length() - 1);

        return result;
    }

}
