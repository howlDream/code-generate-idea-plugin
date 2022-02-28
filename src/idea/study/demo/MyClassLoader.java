package idea.study.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 自定义类加载器
 * Java 默认 ClassLoader，只加载指定目录下的 class，
 * 如果需要动态加载类到内存，例如要从远程网络下来类的二进制，然后调用这个类中的方法实现我的业务逻辑，如此，就需要自定义 ClassLoader。
 * @author zheng.li
 */
public class MyClassLoader extends ClassLoader {


    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        // 假设要加载的类在D盘
        String path = "D:\\" + name.replace('.', File.separatorChar) + ".class";
        byte[] classData = getClassData(path);
        if (classData == null) {
            throw new ClassNotFoundException();
        } else {
            return defineClass(name,classData,0,classData.length);
        }
    }

    /**
     * 读取类字节数据
     * @param path path
     * @return byte[]
     */
    private byte[] getClassData(String path){
        try (InputStream in = new FileInputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int byteNumRead;
            while ((byteNumRead = in.read(buffer)) != -1) {
                baos.write(buffer,0,byteNumRead);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 放开defineClass权限
     * @param name
     * @param b
     * @param off
     * @param len
     * @return
     */
    public Class<?> defineClass1(String name, byte[] b, int off, int len) {
        return defineClass(name,b,off,len);
    }

    /**
     * 从输入流获取类数据
     * @param in InputStream
     * @return byte[]
     */
    public static byte[] getClassData(InputStream in) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int bufferSize = 4096;
                byte[] buffer = new byte[bufferSize];
                int byteNumRead;
                while ((byteNumRead = in.read(buffer)) != -1) {
                    baos.write(buffer,0,byteNumRead);
                }
                return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

}
