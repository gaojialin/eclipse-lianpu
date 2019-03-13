package com.chinairi.build.popup.actions;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
 
public class ConsoleFactory implements IConsoleFactory {

	private static MessageConsole console = new MessageConsole("", null);
	static boolean exists = false;

	/**
	 * 描述:打开控制台
	 */
	public void openConsole() {
		showConsole();
	}

	/** */
	/**
	 * 描述:显示控制台
	 */
	private static void showConsole() {
		if (console != null) {
			// 得到默认控制台管理器
			IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();

			// 得到所有的控制台实例
			IConsole[] existing = manager.getConsoles();
			exists = false;
			// 新创建的MessageConsole实例不存在就加入到控制台管理器，并显示出来
			for (int i = 0; i < existing.length; i++) {
				if (console == existing[i])
					exists = true;
			}
			if (!exists) {
				manager.addConsoles(new IConsole[] { console });
			}

			// console.activate();

		}
	}

	/**
	 * 描述:关闭控制台
	 */
	public static void closeConsole() {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		if (console != null) {
			manager.removeConsoles(new IConsole[] { console });
		}
	}

	/**
	 * 获取控制台
	 * 
	 * @return
	 */
	public static MessageConsole getConsole() {

		showConsole();

		return console;
	}
}

