package com.unascribed.walnut.test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Scanner;

import com.unascribed.walnut.WalnutConfig;

public class LoaderHarness {
	public static void main(String[] args) throws IOException, ParseException {
		Scanner s = new Scanner(System.in);
		System.out.print("File to load: ");
		String path = s.nextLine();
		WalnutConfig conf = WalnutConfig.fromFile(new File(path));
		System.out.println("File loaded. Enter keys to get their values.");
		while (true) {
			try {
				System.out.print("> ");
				String key = s.nextLine();
				System.out.println(conf.get(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
