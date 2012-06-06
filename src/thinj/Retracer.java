package thinj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Retracer {
	public static void main(String[] args) throws IOException {
		String traceFile = System.getProperty("trace.file");
		if (traceFile == null) {
			System.err.println("usage: java -Dtrace.file=<tracefile> " + Retracer.class.getName());
			System.exit(1);
		}

		// Load trace file:
		BufferedReader in = new BufferedReader(new FileReader(traceFile)); 
		LinkedList<TraceInfo> allInfo = new LinkedList<Retracer.TraceInfo>();
		String str = "";
		while (str != null) {
			str = in.readLine();
			if (str != null) {
				str = str.replaceAll("^[ \t]*::.*$", "");
				if (!str.isEmpty()) {
					allInfo.add(new TraceInfo(str));
				}
			}
		}
		in.close();
		
		// Read trace info from stdin:
		in = new BufferedReader(new InputStreamReader(System.in));
		str = "";
		while (str != null) {
			str = in.readLine();
			if (str != null) {
				str = str.replaceAll("^.*:", "");
				StringTokenizer st = new StringTokenizer(str, ", ");
				int count = st.countTokens();
				for (int i = 0; i < count; i++) {
					printTraceElement(allInfo, st.nextToken());					
				}
			}
		}
	}

	private static void printTraceElement(LinkedList<TraceInfo> allInfo, String str) {
		try {
			int pc = Integer.parseInt(str);
			TraceInfo prevTrace = null;
			for (TraceInfo trace : allInfo) {
				if (pc < trace.getPC()) {
					if (prevTrace != null) {
						System.out.println("*** " + prevTrace);
					}
					else {
						System.err.println("Failed decoding trace: " + pc);
						System.exit(1);
					}
					break;
				}
				prevTrace = trace;
			}
		} catch (NumberFormatException e) {
			System.err.println("Not a number: " + str);
			System.exit(1);
		}
	}

	private static class TraceInfo {
		private int aPC;
		private int aSourceLineNumber;
		private String aMethodName;

		public TraceInfo(String info) throws IOException {
			StringTokenizer st = new StringTokenizer(info);
			if (st.countTokens() != 3) {
				throw new IOException("Three fields expected; got: " + info);
			}
			try {
				aPC = Integer.parseInt(st.nextToken());
				aSourceLineNumber = Integer.parseInt(st.nextToken());
				aMethodName = st.nextToken();
			} catch (NumberFormatException e) {
				throw new IOException("Failed reading int: " + info, e);
			}
		}

		public int getPC() {
			return aPC;
		}

//		public int getSourceLineNumber() {
//			return aSourceLineNumber;
//		}
//
//		public String getMethodName() {
//			return aMethodName;
//		}

		@Override
		public String toString() {
			return String.format("%04x", aPC) + "->" + aMethodName + ":" + aSourceLineNumber;
		}
		
	}
}
