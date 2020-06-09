/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;

/**
 * @author G. Annunziata
 * 
 */
public class FileUtil {

	public static String readFile(InputStream in) {

		StringBuilder buf = new StringBuilder();
		try {
			BufferedReader rin = new BufferedReader(new InputStreamReader(in));
			String l;
			while ((l = rin.readLine()) != null) {
				buf.append(l);
				buf.append("\r\n");
			}
		} catch (IOException e) {
			// ignore
		}
		return buf.toString();
	}

	public static byte[] readBinaryFile(String filePath) {
		try {
			InputStream in = getFileOrResourceStream(filePath);

			ArrayList<Byte> buf = new ArrayList<Byte>(10000);
			int c;
			while ((c = in.read()) >= 0) {
				buf.add((byte) c);
			}
			byte[] file = new byte[buf.size()];
			Byte[] f = (Byte[]) buf.toArray();
			for (int i = 0; i < f.length; i++) {
				file[i] = f[i].byteValue();
			}
			return file;
		} catch (Exception e) {
			return null;
		}
	}

	public static List<String> readDir(String dir) throws IOException {
		return readDir(dir, null);
	}

	public static List<String> readDir(String dir, String fileExt) throws IOException {

		File dirFile = new File(dir);
		if (dirFile == null || !dirFile.isDirectory() || !dirFile.canRead())
			throw new IOException("Read error!");

		String as[] = dirFile.list();

		if (fileExt != null) {
			if (fileExt.charAt(0) != '.')
				fileExt = "." + fileExt;
		} else {
			fileExt = "";
		}

		File f = null;
		List<String> logsPath = new ArrayList<String>();
		for (int i = 0; i < as.length; i++) {
			f = new File(dirFile + "/" + as[i]);
			if (f.isFile() && as[i].endsWith(fileExt))
				logsPath.add(f.getAbsolutePath());
		}

		return logsPath;
	}

	private static String sizePrec(float size, int precision) {
		if (precision <= 0)
			return String.valueOf(size);
		else {

			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(precision);
			String res = df.format(size);
			return res;
		}

	}

	public static String toFileSize(long size, int precision) {

		float fsize = size;

		try {
			if (fsize < 999) {
				return String.valueOf(size) + " byte";

			}
			fsize = ((float) fsize) / 1024.0f;
			if (fsize < 999) {
				return sizePrec(fsize, precision) + " Kbyte";

			}
			fsize = ((float) fsize) / 1024.0f;
			if (fsize < 999) {
				return sizePrec(fsize, precision) + " MByte";

			}
			fsize = ((float) fsize) / 1024.0f;
			if (fsize < 999) {
				return sizePrec(fsize, precision) + " GByte";

			}
			fsize = ((float) fsize) / 1024.0f;
			if (fsize < 999) {
				return sizePrec(fsize, precision) + " TByte";

			}

			return String.valueOf(fsize) + " byte";
		} catch (Exception e) {
			return String.valueOf(fsize);
		}

	}

	public static String toElapsedDateString(Date elapsedDate) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd days hh:mm:ss");

		return simpleDateFormat.format(elapsedDate);
	}

	/**
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static InputStream getFileOrResourceStream(String filePath) throws Exception {
		InputStream res = null;
		Exception resEx = null;

		try {
			File f = new File(filePath);
			res = new FileInputStream(f);
		} catch (Exception e) {
			resEx = e;
			res = null;
		}

		if (res == null) {
			try {
				res = FileUtil.class.getResourceAsStream(filePath);
			} catch (Exception eee) {
				resEx = eee;
				res = null;
			}
		}

		// ci riprovo con uno / avanti se e' il caso
		if (!filePath.startsWith("/"))
			filePath = "/" + filePath;

		if (res == null) {

			try {

				res = FileUtil.class.getResourceAsStream(filePath);

			} catch (Exception ee) {
				resEx = ee;
				res = null;
			}
		}

		if (res == null) {
			if (resEx != null)
				throw resEx;
			else
				throw new IOException("IO error");
		}
		return res;
	}

	/**
	 * @param file
	 * @param content
	 * @throws IOException
	 */
	public static void appendContent(File file, String content) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
		writer.write(content);
		writer.close();
	}

	/**
	 * @param file
	 * @return content of file
	 */
	public static String readFile(String file) {
		try {
			return readFile(new FileInputStream(new File(file)));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * @param file
	 * @return content of file
	 */
	public static String readFile(File file) {
		try {
			return readFile(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * @param content
	 */
	public static boolean writeFile(File file, String content) {
		try {
			FileUtils.writeStringToFile(file, content, "UTF-8");
			return true;
		} catch (IOException e) {
			// ignore
			return false;
		}
	}

	/**
	 * @param file
	 * @param rawFile
	 */
	public static boolean writeRaw(File file, byte[] rawFile) {
		try {
			FileUtils.writeByteArrayToFile(file, rawFile);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @param file
	 * @return
	 */
	public static String extension(File file) {
		if (file == null)
			return "";
		String fn = file.getName();
		int pos = fn.lastIndexOf(".");
		if (pos > 0) {
			return fn.substring(pos + 1);
		}

		return "";
	}

	/**
	 * @param file
	 * @return
	 */
	public static String extension(String file) {
		if (file == null)
			return "";
		String fn = file;
		int pos = fn.lastIndexOf(".");
		if (pos > 0) {
			return fn.substring(pos + 1);
		}

		return "";
	}

	/**
	 * @param file
	 * @return
	 */
	public static String onlyname(String file) {
		if (file == null)
			return "";
		String fn = file;
		int pos = fn.lastIndexOf(".");
		if (pos > 0) {
			return fn.substring(0, pos);
		}
		// clean / ?
		//
		return fn;
	}

	/**
	 * @param remotePath
	 * @return
	 */
	public static boolean isValidPath(String remotePath) {
		if (remotePath == null)
			return false;
		if (remotePath.isEmpty())
			return true;

		return remotePath.matches("/?([a-zA-Z0-9\\$_ .,-]+/?)+");
	}

	/**
	 * Calcola la dimensione in bytes di un certo path
	 * 
	 * @param textPath
	 * @return
	 */
	public static long size(String textPath) {
		final AtomicLong size = new AtomicLong(0);
		try {
			// TODO: il controllo del path per windows non funziona
			// if (!isValidPath(textPath))
			// return size.get();
			// controllo a mano che il file esista
			try {
				File f = new File(textPath);
				if (f == null || !f.exists())
					return size.get();
			} catch (Exception ex) {
				return size.get();
			}
			Path path = Paths.get(textPath);
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					size.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					System.out.println("skipped: " + file + " (" + exc + ")");
					// Skip folders that can't be traversed
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

					if (exc != null)
						System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
					// Ignore errors traversing a folder
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
		}
		return size.get();
	}

}
