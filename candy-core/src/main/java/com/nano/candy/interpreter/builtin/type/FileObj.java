package com.nano.candy.interpreter.builtin.type;

import com.nano.candy.interpreter.builtin.CandyClass;
import com.nano.candy.interpreter.builtin.CandyObject;
import com.nano.candy.interpreter.builtin.type.ArrayObj;
import com.nano.candy.interpreter.builtin.type.BoolObj;
import com.nano.candy.interpreter.builtin.type.FileObj;
import com.nano.candy.interpreter.builtin.type.IntegerObj;
import com.nano.candy.interpreter.builtin.type.StringObj;
import com.nano.candy.interpreter.builtin.type.error.ArgumentError;
import com.nano.candy.interpreter.builtin.type.error.IOError;
import com.nano.candy.interpreter.builtin.type.error.TypeError;
import com.nano.candy.interpreter.builtin.utils.OptionalArg;
import com.nano.candy.interpreter.cni.CNIEnv;
import com.nano.candy.interpreter.cni.NativeClass;
import com.nano.candy.interpreter.cni.NativeClassRegister;
import com.nano.candy.interpreter.cni.NativeFunc;
import com.nano.candy.interpreter.cni.NativeFuncRegister;
import com.nano.candy.interpreter.cni.NativeMethod;
import com.nano.candy.std.Names;
import com.nano.common.io.FilePathUtils;
import com.nano.common.io.FileUtils;
import com.nano.common.io.IOUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

import static com.nano.candy.interpreter.builtin.type.error.IOError.*;

/**
 * This provides APIs for File operations.
 */
@NativeClass(name="File", isInheritable=true)
public class FileObj extends CandyObject {

	public static final CandyClass FILE_CLASS = 
		NativeClassRegister.generateNativeClass(FileObj.class);
	static {
		NativeFuncRegister.register(FILE_CLASS, FileObj.class);
			FILE_CLASS.setBuiltinMetaData("separator", StringObj.valueOf(File.separator));
	}

	/**
	 * Convert an exception into an {@link IOError IOError} and throw it.
	 */
	private static void throwIOError(Exception e) {
		new IOError(e).throwSelfNative();
	}
	
	/**
	 * Convert an {@link ArrayObj} into a String array.
	 * This expects all elements in the specified array are a String object.
	 */
	private static String[] toStringArray(ArrayObj arr) {
		final int len = arr.length();
		String[] strArr = new String[len];
		for (int i = 0; i < len; i ++) {
			strArr[i] = TypeError.requiresStringObj(arr.get(i)).value();
		}
		return strArr;
	}

	private static String getOptions(OptionalArg options) {
		return TypeError.requiresStringObj(options.getValue("")).value();
	}
	
	@NativeFunc(name="createFile", varArgsIndex=1)
	public static CandyObject createFile(CNIEnv env, StringObj path, ArrayObj more) {
		try {
			Path p = Paths.get(path.value(), toStringArray(more));
			if (!p.toFile().createNewFile()) return null;
			return new FileObj(p);
		} catch (Exception e) {
			throwIOError(e);
		}
		return null;
	}
	
	@NativeFunc(name="mkdir", varArgsIndex = 1)
	public static CandyObject mkdir(CNIEnv env, StringObj pathStr, ArrayObj more) {
		Path path = Paths.get(pathStr.value(), toStringArray(more));
		if (path.toFile().mkdir()) {
			return new FileObj(path);	
		}
		return null;
	}
	
	@NativeFunc(name="mkdirs", varArgsIndex = 1)
	public static CandyObject mkdirs(CNIEnv env, StringObj pathStr, ArrayObj more) {
		Path path = Paths.get(pathStr.value(), toStringArray(more));
		if (path.toFile().mkdirs()) {
			return new FileObj(path);	
		}
		return null;
	}

	@NativeFunc(name="link")
	public static CandyObject link(CNIEnv env, StringObj path, StringObj existing) {
		try {
			Path p = Files.createLink(Paths.get(path.value()), Paths.get(existing.value()));
			return new FileObj(p.toFile());
		} catch (Exception e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeFunc(name="chmod")
	public static CandyObject chmod(CNIEnv env, StringObj path, CandyObject perms) {
		String permsStr = getPerms(perms);
		try {
			Files.setPosixFilePermissions(
				Paths.get(path.value()),
				PosixFilePermissions.fromString(permsStr)
			);
		} catch (Exception e) {
			throwIOError(e);
		}
		return null;
	}

	/**
	 * Converts a Candy object into a permissions string like "rwxrwxrwx".
	 *
	 * <p> If the object is an Integer, it will be treated as
	 * a number which the lowest three digits will be parsed as the
	 * permissions string.
	 *
	 * <P> For example:
	 *
	 * <pre>{@code
	 * The number 12755 will be parsed as the binary number:
	 *       111, 101, 101 (Lowest three digits 755)
	 * and this mefhod will convert them into the permissions string:
	 *       rwx  r-x  r-x
	 * }</pre>
	 *
	 * <p> If the object is a string, returns it's value.
	 */
	private static String getPerms(CandyObject perms) {
		if (perms instanceof StringObj) {
			return ((StringObj) perms).value();
		}
		if (perms instanceof IntegerObj) {
			long permNumbers = ((IntegerObj) perms).intValue();
			StringBuilder permsStr = new StringBuilder();
			int base = 100;
			for (int i = 0; i < 3; i ++) {
				int n = (int) (permNumbers/base) % 10;
				base /= 10;
				// 111 -> rwx
				permsStr.append(((n>>2)&1) == 1 ? "r" : "-");
				permsStr.append(((n>>1)&1) == 1 ? "w" : "-");
				permsStr.append(( n    &1) == 1 ? "x" : "-");
			}
			return permsStr.toString();
		}
		new ArgumentError("Illegal argument type. Expected an integer or " +
		                  "a string as the permissions of a file.").throwSelfNative();
		return null;
	}
	
	/**
	 * Removes a file or a directory.
	 *
	 * @param isRecursion This is an optional argument. If true,
	 *                    it will recursively delete the file or directory.
	 */
	@NativeFunc(name="rm")
	public static CandyObject rm(CNIEnv env, StringObj path, OptionalArg isRecursion) {
		boolean recursion = isRecursion.getValue(false).boolValue(env).value();
		File f = new File(path.value());
		if (recursion) {
			return BoolObj.valueOf(deleteDir(f));
		} else {
			return BoolObj.valueOf(f.delete());
		}
	}

	private static boolean deleteDir(File f) {
		boolean success = true;
		if (f.isDirectory()) {
			for (File subfile : f.listFiles()) {
				success &= deleteDir(subfile);
			}
		}
		success &= f.delete();
		return success;
	}
	
	/**
	 * Copy a file/directory to another file/directory.
	 *
	 * The {@code options} which is a string may include any character of the following:
	 *
	 * <pre>
	 * 1. 'r': It's 'recursion'. If it's present, you can copy a directory to
	 *         another directory.
	 * 2, 'f': It's 'force'. If it's present, you can replace old files with new
	 *         files.
	 * </pre>
	 */
	@NativeFunc(name="cp")
	public static CandyObject cp(CNIEnv env, StringObj srcPath, StringObj destPath, 
	                             OptionalArg options) {
		boolean recursion = false, replace = false;
		String ops = options.getValue("").callStr(env).value();
		int len = ops.length();
		for (int i = 0; i < len; i ++) {
			char ch = ops.charAt(i);
			if (ch == 'r') recursion = true;
			else if (ch == 'f') replace = true;
		}
		File src = new File(srcPath.value());
		File dest = new File(destPath.value());
		if (!recursion) {
			if (!src.isFile()) {
				new IOError(
					"You cannot copy a directory into another directory. If " +
					"want, add the 'r' to the options argument: copy '%s' -> '%s'.",
					src.getAbsolutePath(), dest.getAbsolutePath()
				).throwSelfNative();
			} else if (dest.isDirectory()) {
				File destFile = new File(dest, src.getName());
				fastCopyFile(src, destFile, replace);
			} else {
				fastCopyFile(src, dest, replace);
			}
		} else {
			try {
				deepCopy(src, dest, replace);
			} catch (IOException e) {
				throwIOError(e);
			}
		}
		return null;
	}

	public static void deepCopy(File src, File dest, boolean replace) throws IOException {
		checkArgumentsForCopy(src, dest, replace);
		String srcRootPath = src.getParentFile().getAbsolutePath();
		String destRootPath = dest.getAbsolutePath();
		
		List<File> files = FilePathUtils.getFilesByBfsOrder(src);
		for (File file : files) {
			String relativePath = FilePathUtils.getRelativePathOf(
				file.getAbsolutePath(), 
				srcRootPath
			);
			File newFile = new File(destRootPath, relativePath);
			if (file.isDirectory()) {
				newFile.mkdirs();
			} else {
				fastCopyFile(file, newFile, replace);
			}
		}
	}

	private static void checkArgumentsForCopy(File src, File dest, boolean replace) 
		throws IOException {
		if (!src.exists()) {
			throw new IOException("The source file doesn't exist: " + src.getAbsolutePath());
		}

		if (dest.isFile()) {
			if (src.isDirectory()) {
				throw new IOException("Can't copy a directory to a file.");
			}
			fastCopyFile(src, dest, replace);
			return;
		}
	}

	private static void fastCopyFile(File srcFile, File destFile, boolean replace) {
		try {
			if (!replace) {
				Files.copy(srcFile.toPath(), destFile.toPath());
			} else {
				Files.copy(
					srcFile.toPath(), destFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING
				);
			}
		} catch (FileAlreadyExistsException e) {
			new IOError(
				"The '%s' already exists. If you want to replace it with the " +
				"source file, then you neet to add the 'f' to " +
				"the options argument.", e.getMessage()
			).throwSelfNative();
		} catch (IOException e) {
			throwIOError(e);
		}
	}

	@NativeFunc(name="mv")
	public static CandyObject mv(CNIEnv env, StringObj src, StringObj dest, OptionalArg options) {
		String optionsStr = getOptions(options);
		boolean replace = optionsStr.indexOf('r')!=-1;
		Path srcPath = Paths.get(src.value());
		Path destPath = Paths.get(dest.value());
		try {
			if (!Files.isDirectory(destPath)) {
				Path parent = destPath.getParent();
				if (parent == null || !Files.exists(parent)) {
					new IOError(
						"Cannot to move '%s' to '%s': no such file or directory.",
						srcPath, destPath).throwSelfNative();
				}
			} else {
				destPath = destPath.resolve(srcPath.getFileName());
			}
			if (replace) {
				return new FileObj(Files.move(
					srcPath, destPath,
					StandardCopyOption.REPLACE_EXISTING
				));
			} else {
				return new FileObj(Files.move(srcPath, destPath));
			}
		} catch (FileAlreadyExistsException e) {
			new IOError(
				"Cannot to move '%s' to '%s': the target file already exists. " +
				"you need to add 'r' to the options argument.",
				srcPath, destPath).throwSelfNative();
		} catch (DirectoryNotEmptyException e) {
			new IOError(
				"Cannot to move '%s' to '%s': the replaced file is a non-empty directory. " +
				srcPath, destPath).throwSelfNative();
		} catch (IOException e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeFunc(name="du")
	public static IntegerObj du(CNIEnv env, StringObj filePath) {
		return IntegerObj.valueOf(diskUsage(new File(filePath.value())));
	}

	private static long diskUsage(File f) {
		long total = f.length();
		if (f.isDirectory()) {
			for (File subfile : f.listFiles()) {
				total += diskUsage(subfile);
			}
		}
		return total;
	}

	@NativeFunc(name="join", varArgsIndex = 0)
	public static CandyObject join(CNIEnv env, ArrayObj paths) {
		final int len = paths.length();
		if (len == 0) {
			return StringObj.EMPTY_STR;
		}
		String last = paths.get(0).callStr(env).value();
		StringBuilder path = new StringBuilder(last);
		for (int i = 1; i < len; i ++) {	
			String p = TypeError.requiresStringObj(paths.get(i)).value();
			boolean lastEndsWith = last.endsWith(File.separator);
			boolean curStartsWith = p.startsWith(File.separator);
			if (!lastEndsWith && !curStartsWith) {
				path.append(File.separator);
				path.append(p);
			} else if (lastEndsWith && curStartsWith) {
				path.append(p.substring(1));
			} else {
				path.append(p);
			}
			last = p.isEmpty() ? "/" : p;
		}
		return StringObj.valueOf(path.toString());
	}

	@NativeFunc(name="formatSize")
	public static CandyObject formatSize(CNIEnv env, long size) {
		return StringObj.valueOf(FileUtils.sizeFormat(size));
	}


	private Path path;

	protected FileObj() {
		super(FILE_CLASS);
	}

	public FileObj(File f) {
		super(FILE_CLASS);
		resetFile(f.toPath());
	}

	public FileObj(Path path) {
		super(FILE_CLASS);
		resetFile(path);
	}

	protected final void resetFile(Path path) {
		this.path = path;
		resetBuiltinAttrs();
	}

	private void resetBuiltinAttrs() {
		setBuiltinMetaData("path", StringObj.valueOf(path.toString()));
		setBuiltinMetaData("parent", StringObj.valueOf(getParent()));
		setBuiltinMetaData("name", StringObj.valueOf(getName()));
		setBuiltinMetaData("baseName", StringObj.valueOf(getBaseName()));
		setBuiltinMetaData("suffix", StringObj.valueOf(getSuffix()));
	}

	public String getParent() {
		Path parent = path.getParent();
		if (parent == null) {
			return "";
		}
		return parent.toString();
	}

	public String getName() {
		Path name = path.getFileName();
		return name == null ? "" : name.toString();
	}

	public String getBaseName() {
		String name = getName();
		int i = name.lastIndexOf('.');
		return i <= 0 ? name : name.substring(0, i);
	}

	public String getSuffix() {
		String name = getName();
		int i = name.lastIndexOf('.');
		return i <= 0 ? "" : name.substring(i + 1);
	}

	@Override
	public String toString() {
		return path.toString();
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	protected BoolObj equals(CNIEnv env, CandyObject operand) {
		if (operand instanceof FileObj) {
			try {
				return 
					BoolObj.valueOf(Files.isSameFile(path, ((FileObj) operand).path));
			} catch (Exception e) {
				return BoolObj.FALSE;
			}
		}
		return super.equals(env, operand);
	}

	@NativeMethod(name=Names.METHOD_INITALIZER, varArgsIndex = 1)
	protected final CandyObject init(CNIEnv env, StringObj path, ArrayObj more) {
		if (more.length() == 0) {
			resetFile(Paths.get(path.value()));
		} else {
			String[] morePaths = toStringArray(more);
			resetFile(Paths.get(path.value(), morePaths));
		}
		return this;
	}

	@NativeMethod(name = "isFile")
	public BoolObj isFile(CNIEnv env) {
		return BoolObj.valueOf(path.toFile().isFile());
	}

	@NativeMethod(name = "isDirectory")
	public BoolObj isDirectory(CNIEnv env) {
		return BoolObj.valueOf(Files.isDirectory(path));
	}

	@NativeMethod(name = "exists")
	public BoolObj exists(CNIEnv env) {
		return BoolObj.valueOf(Files.exists(path));
	}

	@NativeMethod(name = "isHidden")
	public BoolObj isHidden(CNIEnv env) {
		try {
			return BoolObj.valueOf(Files.isHidden(path));
		} catch (IOException e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "isReadable")
	public BoolObj canRead(CNIEnv env) {
		return BoolObj.valueOf(Files.isReadable(path));
	}

	@NativeMethod(name = "isWritable")
	public BoolObj canWrite(CNIEnv env) {
		return BoolObj.valueOf(Files.isWritable(path));
	}

	@NativeMethod(name = "isExecutable")
	public BoolObj isExecutable(CNIEnv env) {
		return BoolObj.valueOf(Files.isExecutable(path));
	}

	@NativeMethod(name = "size")
	public IntegerObj length(CNIEnv env) {
		try {
			return IntegerObj.valueOf(Files.size(path));
		} catch (IOException e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "lastModified")
	public IntegerObj lastModified(CNIEnv env) {
		try {
			return IntegerObj.valueOf(Files.getLastModifiedTime(path).toMillis());
		} catch (IOException e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "list")
	public ArrayObj javam(CNIEnv env) {
		try {
			CandyObject[] list = Files.list(path)	
				.map(FileObj::new)
				.toArray(CandyObject[]::new);
			return new ArrayObj(list);
		} catch (Exception e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "createFile")
	public BoolObj createFile(CNIEnv env) {
		try {
			return BoolObj.valueOf(path.toFile().createNewFile());
		} catch (IOException e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "mkdir")
	public BoolObj mkdir(CNIEnv env) {
		return BoolObj.valueOf(path.toFile().mkdir());
	}

	@NativeMethod(name = "mkdirs")
	public BoolObj mkdirs(CNIEnv env) {
		return BoolObj.valueOf(path.toFile().mkdirs());
	}

	@NativeMethod(name = "delete")
	public BoolObj delete(CNIEnv env) {
		try {
			return BoolObj.valueOf(Files.deleteIfExists(path));
		} catch (Exception e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "deleteOnExit")
	public CandyObject deleteOnExit(CNIEnv env) {
		path.toFile().deleteOnExit();
		return this;
	}

	@NativeMethod(name = "getPermsStr")
	public CandyObject getPerms(CNIEnv env) {
		try {
			Set<PosixFilePermission> fPermssions = Files.getPosixFilePermissions(path);
			StringBuilder permsStr = new StringBuilder();
			int i = 0;
			for (PosixFilePermission p : PosixFilePermission.values()) {
				if (fPermssions.contains(p)) {
					permsStr.append("rwx".charAt(i));
				} else {
					permsStr.append("-");
				}
				i = (i+1) % 3;
			}
			return StringObj.valueOf(permsStr.toString());
		} catch (IOException e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "rename")
	public CandyObject rename(CNIEnv env, StringObj newName) {
		try {
			Path newPath = Files.move(path, path.resolveSibling(newName.value()));
			resetFile(newPath);
		} catch (Exception e) {
			throwIOError(e);
		}
		return this;
	}

	@NativeMethod(name = "read")
	public StringObj read(CNIEnv env) {
		try {
			return StringObj.valueOf(FileUtils.readText(path.toFile()));
		} catch (Exception e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "readLines")
	public ArrayObj readlines(CNIEnv env) {
		try {
			List<String> lines = Files.readAllLines(path);
			CandyObject[] ls = new CandyObject[lines.size()];
			int i = 0;
			for (String line : lines) {
				ls[i ++] = StringObj.valueOf(line);
			}
			return new ArrayObj(ls);
		} catch (Exception e) {
			throwIOError(e);
		}
		return null;
	}

	@NativeMethod(name = "write")
	public CandyObject write(CNIEnv env, String data) {
		BufferedWriter bw = null;
		try {
			bw = Files.newBufferedWriter(path);
			bw.write(data);
			bw.flush();
		} catch (Exception e) {
			throwIOError(e);
		} finally {
			IOUtils.closesQuietly(bw);
		}
		return this;
	}

	@NativeMethod(name = "append")
	public CandyObject append(CNIEnv env, String data) {
		BufferedWriter bw = null;
		try {
			bw = Files.newBufferedWriter(
				path, 
				StandardOpenOption.APPEND,
				StandardOpenOption.CREATE
			);
			bw.write(data);
			bw.flush();
		} catch (Exception e) {
			throwIOError(e);
		} finally {
			IOUtils.closesQuietly(bw);
		}
		return this;
	}
}
