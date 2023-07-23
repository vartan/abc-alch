package com.vartan;

import com.vartan.abc.AbcAlchPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AbcAlchTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AbcAlchPlugin.class);
		RuneLite.main(args);
	}
}