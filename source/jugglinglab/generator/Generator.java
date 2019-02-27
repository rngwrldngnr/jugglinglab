// Generator.java
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

package jugglinglab.generator;

import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.JPanel;

import jugglinglab.util.*;


    // This class defines a general object that is capable of generating tricks
    // and converting them into commands that the animator understands.
public abstract class Generator {
    static final ResourceBundle guistrings = jugglinglab.JugglingLab.guistrings;
    static final ResourceBundle errorstrings = jugglinglab.JugglingLab.errorstrings;

    // The built-in generators
    public static final String[] builtinGenerators = { "Siteswap" };

    // This is a factory to create Generators from names.  Note the
    // naming convention.
    public static Generator getGenerator(String name) {
        // make sure the notation name is capitalized
        name = name.substring(0,1).toUpperCase() + name.substring(1);

        try {
            Object obj = Class.forName("jugglinglab.generator."+
                                       name+"Generator").newInstance();
            if (!(obj instanceof Generator)) {
                return null;
                // throw new JuggleExceptionUser("Generator type '"+name+"' doesn't work");
            }
            return (Generator)obj;
        }
        catch (ClassNotFoundException cnfe) {
            return null;
            // throw new JuggleExceptionUser("Generator type '"+name+"' not found");
        }
        catch (IllegalAccessException iae) {
            return null;
            // throw new JuggleExceptionUser("Cannot access '"+name+"' generator file (security)");
        }
        catch (InstantiationException ie) {
            return null;
            // throw new JuggleExceptionUser("Couldn't create '"+name+"' generator");
        }
    }

    public abstract String getStartupMessage();
    public abstract JPanel getGeneratorControls();
    public abstract void resetGeneratorControls();

    // use parameters from controller frame
    public abstract void initGenerator() throws JuggleExceptionUser;

    // use command line args
    public abstract void initGenerator(String[] args) throws JuggleExceptionUser;

    public void initGenerator(String arg) throws JuggleExceptionUser {
        int i, numargs;
        StringTokenizer st = new StringTokenizer(arg, " \n");

        numargs = st.countTokens();
        String args[] = new String[numargs];

        for (i = 0; i < numargs; i++)
            args[i] = st.nextToken();

        this.initGenerator(args);
    }

    public abstract int runGenerator(GeneratorTarget t);
    // The following is identical to the above, but imposes
    // bounds in space and time
    public abstract int runGenerator(GeneratorTarget t, int max_num,
                                     double secs) throws JuggleExceptionUser;
}
