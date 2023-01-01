import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	/** buffer size */
	private static final int BUFFER = 1024;

	public static int unzip(String srcPath, String zipFileName) {
		String sep = File.separator;
		
		ZipInputStream zipInputStream = null;
		//ZIP文件入口
		ZipEntry zipEntry = null;
		//缓冲区
		byte[] buffer = new byte[BUFFER];
		// 本次读出来的长度
		int readLength = 0;
		//解压文件个数
		int count = 0;
		try {
			zipInputStream = 
			new ZipInputStream(new FileInputStream(srcPath + sep + zipFileName));
			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				// if is directory, mkdirs
				if (zipEntry.isDirectory()) {
					File dir = new File(srcPath + sep + zipEntry.getName());
					if (!dir.exists()) {
						dir.mkdirs();
						continue;
					}
				}
				// 若是文件，则需创建该文件
				File file = createFile(srcPath, zipEntry.getName());
				count ++;
				OutputStream outputStream = new FileOutputStream(file);
				while ((readLength = zipInputStream.read(buffer, 0, BUFFER)) != -1) {
					outputStream.write(buffer, 0, readLength);
				}
				outputStream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		} finally {
			// close the stream
			try {
				if (zipInputStream != null)
					zipInputStream.close();
			} catch (IOException e) {
				return -2;
			}
		}
		return count;
	}

	/**
	 * compression
	 * @param srcPath     待压缩文件目录
	 * @param zipFileName 压缩后文件名称
	 * @return 压缩文件个数
	 */
	public static int doZip(String srcPath, String zipFileName) {
		String sep = File.separator;

		File srcFile = new File(srcPath);
		//get all the files of certain path
		List <File> fileList = getAllFiles(srcFile);
		ArrayList<File> dotCorgitFileList = new ArrayList<File>();
		for (File file : fileList) {
			if (file.getAbsolutePath().contains(".corgit")) {
				dotCorgitFileList.add(file);
			}
		}
		for (File file : dotCorgitFileList) {
			fileList.remove(file);
		}
		// System.out.println(fileList.toString());
		//缓冲区
		byte[] buffer = new byte[BUFFER];
		//ZIP文件入口
		ZipEntry zipEntry = null;
		//本次读取长度
		int readLength = 0;
		ZipOutputStream zipOutputStream = null;
		//压缩后文件的名称
		String newZipFileName;
		//压缩文件总个数
		int count = 0;
		
		if (zipFileName == null || zipFileName.length() == 0) {
			newZipFileName = srcPath + "newZip.zip";
		} else {
			newZipFileName = srcPath + sep + zipFileName;
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(newZipFileName);			
			zipOutputStream = new ZipOutputStream(fos);
			for (File file : fileList) {
				// if is file, then compressed
				if (file.isFile()) {
					count ++;
					zipEntry = new ZipEntry(getRelativePath(srcPath, file));
					zipEntry.setSize(file.length());
					zipEntry.setTime(file.lastModified());
					zipOutputStream.putNextEntry(zipEntry);
					InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
					while ((readLength = inputStream.read(buffer, 0, BUFFER)) != -1) {
						zipOutputStream.write(buffer, 0, readLength);
					}
					inputStream.close();
				} else if (file.getName().equals(".corgit")) {
					// System.out.println("zip .corgit but pass");
					continue;
				} else {
					zipEntry = new ZipEntry(getRelativePath(srcPath, file) + sep);
					zipOutputStream.putNextEntry(zipEntry);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -2;
		} finally {
			try {
				if (zipOutputStream != null)
					zipOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return -2;
			}
		}
		return count;
	}
	
	/**
	 * get file list from srcFile
	 */
	private static List<File> getAllFiles(File srcFile) {
		List<File> fileList = new ArrayList<File>();
		File[] tmp = srcFile.listFiles();
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i].isFile()) {
				fileList.add(tmp[i]);
			}
			if (tmp[i].isDirectory()) {
				// 若不是空目录，则递归添加其下的目录和文件
				if (tmp[i].listFiles().length != 0) {
					fileList.addAll(getAllFiles(tmp[i]));
				}
				// 若是空目录，则添加这个目录到fileList
				else {
					fileList.add(tmp[i]);
				}
			}
		}
		return fileList;
	}

	private static String getRelativePath(String dirPath, File file) {
		String sep = File.separator;
		File dir = new File(dirPath);
		String relativePath = file.getName();
		while (true) {
			file = file.getParentFile();
			if (file == null) {
				break;
			}
			if (file.equals(dir)) {
				break;
			} else {
				relativePath = file.getName() + sep + relativePath;
			}
		}
		return relativePath;
	}

	private static File createFile(String srcPath, String fileName){
		String sep = File.separator;
		// split the fileName
		String[] dirs = fileName.split(sep);
		File file = new File(srcPath);
		// if file has parent
		if (dirs.length > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				file = new File(file, dirs[i]);
			}
			if (!file.exists()) {
				file.mkdirs();
			}
			// make file
			file = new File(file, dirs[dirs.length - 1]);
			return file;
		} else {
			if (!file.exists()) {
				file.mkdirs();
			}
			// make file
			file = new File(file, dirs[0]);
			return file;
		}
	}
}

