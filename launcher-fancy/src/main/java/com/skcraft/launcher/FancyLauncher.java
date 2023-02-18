/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher;

import com.google.common.base.Supplier;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.extern.java.Log;
import com.skcraft.launcher.dialog.LauncherFrame;
import com.skcraft.launcher.themes.DarkTheme;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.themes.LightTheme;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import javax.swing.border.LineBorder;

@Log
public class FancyLauncher {

	public static void setUIFont (javax.swing.plaf.FontUIResource f){
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get (key);
      if (value instanceof javax.swing.plaf.FontUIResource)
        UIManager.put (key, f);
      }
    }

    public static void main(final String[] args) {
        Launcher.setupLogger();
        System.setProperty( "apple.awt.application.name", "Jadua Studios Client" );
        System.setProperty( "apple.awt.application.appearance", "system" );

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().setContextClassLoader(FancyLauncher.class.getClassLoader());
                    UIManager.getLookAndFeelDefaults().put("ClassLoader", FancyLauncher.class.getClassLoader());
                    UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
					setUIFont (new javax.swing.plaf.FontUIResource("Futura",Font.PLAIN,12));
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    System.setProperty( "flatlaf.animation", "true" );
                    System.setProperty("sun.awt.noerasebackground", "true");
                    System.setProperty("substancelaf.windowRoundedCorners", "true");
					System.setProperty("awt.useSystemAAFontSettings","on");
					System.setProperty("swing.aatext", "true");
                    
                        Launcher launcher = Launcher.createFromArguments(args);
                        Configuration config = launcher.getConfig();
                        if (config.isLightModeEnabled()) {
                            LightTheme.setup();
                            UIManager.put("TitlePane.foreground", new Color(0x36393D));
                            UIManager.put("RootPane.background", new Color(0xE4E7EE));
                            UIManager.put("TitlePane.menuBarEmbedded", false);
                            UIManager.put("Panel.background", new Color(0xE4E7EE));
                            UIManager.put("Toolbar.background", new Color(0xE4E7EE));
                            UIManager.put("TabbedPane.background", new Color(0xE4E7EE));
                            UIManager.put("TabbedPane.hoverColor", new Color(0x102979FF, true));
                            UIManager.put("TabbedPane.focusColor", new Color(0x302979FF, true));
                            UIManager.put("SplitPane.background", new Color(0xE4E7EE));
                            UIManager.put("Button.foreground", new Color (0xffffff));
                            UIManager.put("Button.background", new Color (0x2979FF));
                            UIManager.put("Button.default.background", new Color (0x94BD8D));
                            UIManager.put("Button.default.focusedBackground", new Color (0xA2E497));
                            UIManager.put("Button.default.hoverBackground", new Color (0xA2E497));
                            UIManager.put("Button.default.pressedBackground", new Color (0x94BD8D));
                            UIManager.put( "Table.background", new Color(0x002979FF, true));
                            UIManager.put( "Table.foreground", new Color(0x000));
                            UIManager.put( "Table.selectionBackground", new Color(0x302979FF, true));
                            UIManager.put( "Table.selectionInactiveBackground", new Color(0x102979FF, true));
                            UIManager.put( "Component.borderColor", new Color(0x502979FF, true));

                        }
                        else {
                            DarkTheme.setup();                  
                            UIManager.put( "TitlePane.foreground", new Color( 225, 231, 240 ) );
                            UIManager.put( "Table.selectionBackground", new Color(0x444953) );
                            UIManager.put( "Table.selectionInactiveBackground", new Color(0x444953) );
                            UIManager.put( "Table.focusCellBackground", new Color(0x444953) );
                            UIManager.put( "Table.background", new Color(0x0021242B, true) );
                        }
                        
                    UIManager.put( "ScrollPane.smoothScrolling", true );
                    UIManager.put( "Table.arc", 100 );
                    UIManager.put( "MenuItem.selectionArc", 100 );
                    UIManager.put( "MenuItem.selectionInsets", new Insets( 0, 2, 0, 2 ) );
                    UIManager.put( "List.selectionArc", 100 );
                    UIManager.put( "ComboBox.selectionArc", 100 );
                    UIManager.put( "Table.showHorizontalLines", true );
                    UIManager.put( "ComboBox.popupInsets", new Insets( 2, 2, 2, 2 ) );
                    UIManager.put( "ScrollBar.width", 15 );
                    UIManager.put( "Button.arc", 100 );
                    UIManager.put("Button.borderWidth", 0);
                    UIManager.put( "Component.arc", 100 );
                    UIManager.put( "ProgressBar.arc", 100 );
                    UIManager.put( "TextComponent.arc", 100 );
                    UIManager.put( "Button.margin", new Insets(10, 15, 10, 15) );
                    UIManager.put( "Button.minimumWidth", 100);
                    UIManager.put( "Component.arrowType", "chevron" );
                    UIManager.put( "ScrollBar.thumbArc", 999 );
                    UIManager.put( "ScrollBar.thumbInsets", new Insets( 2, 2, 2, 2 ) );
                    UIManager.put("Button.default.boldText", true);

                    //if (!SwingHelper.setLookAndFeel("com.skcraft.launcher.skin.LauncherLookAndFeel")) {
                    //    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    //}

                    launcher.setMainWindowSupplier(new CustomWindowSupplier(launcher));
                    launcher.showLauncherWindow();
                } catch (Throwable t) {
                    log.log(Level.WARNING, "Load failure", t);
                    SwingHelper.showErrorDialog(null, "Uh oh! The updater couldn't be opened because a " +
                            "problem was encountered.", "Launcher error", t);
                }
            }
        });
    }

    private static class CustomWindowSupplier implements Supplier<Window> {

        private final Launcher launcher;

        private CustomWindowSupplier(Launcher launcher) {
            this.launcher = launcher;
        }

        @Override
        public Window get() {
            return new FancyLauncherFrame(launcher);
        }
    }

	public class CustomTextArea extends JTextArea {

		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			super.paint(g);
		}

	}
}
