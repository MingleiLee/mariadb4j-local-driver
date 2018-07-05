package com.jeedsoft.marialocal.util;

public class SystemUtil
{
	private static boolean isWindows;
	private static boolean isMacOS;
	
	static
	{
		String os = System.getProperties().get("os.name").toString().toLowerCase();
		isWindows = os.contains("windows");
		isMacOS = os.contains("mac");
	}
	
	public static boolean isWindows()
	{
		return isWindows;
	}
	
	public static boolean isMacos()
	{
		return isMacOS;
	}
	
	public static boolean isLinux()
	{
		return !isWindows && !isMacOS;
	}
}
