/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.skcraft.launcher.themes;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme;

public class LightTheme
	extends FlatAtomOneLightIJTheme
{
	public static final String NAME = "LightTheme";

	public static boolean setup() {
		return setup(new LightTheme() );
	}

	public static void installLafInfo() {
		installLafInfo(NAME, LightTheme.class );
	}

	@Override
	public String getName() {
		return NAME;
	}
}
