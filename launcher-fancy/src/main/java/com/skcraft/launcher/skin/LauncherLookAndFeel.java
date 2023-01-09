/*
 * SKCraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.skin;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.skcraft.launcher.dialog.LauncherFrame;

public class LauncherLookAndFeel extends FlatOneDarkIJTheme {

    public LauncherLookAndFeel() {
        FlatOneDarkIJTheme.registerCustomDefaultsSource("com.skcraft.launcher.skin");
        FlatOneDarkIJTheme.setup();
        
        UIManager.put( "Button.arc", 100 );
        UIManager.put( "Component.arc", 100 );
        UIManager.put( "ProgressBar.arc", 100 );
        UIManager.put( "TextComponent.arc", 100 );
        UIManager.put( "Button.margin", new Insets(10, 15, 10, 15) );
        UIManager.put( "Button.minimumWidth", 100);
        UIManager.put( "Component.arrowType", "chevron" );
        UIManager.put( "ScrollBar.thumbArc", 999 );
        UIManager.put( "ScrollBar.thumbInsets", new Insets( 2, 2, 2, 2 ) );
        UIManager.put( "ScrollBar.width", 15 );
        UIManager.put( "TitlePane.foreground", new Color( 225, 231, 240 ) );
        UIManager.put( "ScrollPane.smoothScrolling", true );
        UIManager.put( "Table.arc", 100 );
        UIManager.put( "Table.selectionBackground", new Color(0x444953) );
        UIManager.put( "Table.selectionInactiveBackground", new Color(0x444953) );
        UIManager.put( "Table.focusCellBackground", new Color(0x444953) );
        UIManager.put( "Table.background", new Color(0x0021242B, true) );
        UIManager.put( "Table.showHorizontalLines", true );
        UIManager.put( "ComboBox.popupInsets", new Insets( 2, 2, 2, 2 ) );
        UIManager.put( "ComboBox.selectionArc", 100 );
        UIManager.put( "List.selectionArc", 100 );
        UIManager.put( "MenuItem.selectionInsets", new Insets( 0, 2, 0, 2 ) );
        UIManager.put( "MenuItem.selectionArc", 100 );
    }
}