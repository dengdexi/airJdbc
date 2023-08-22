package sql.airJdbc.utils;

import java.io.*;
import java.net.URL;

public class FileUtil {
    //
    /**
     * 判断当前程序是否在jar中运行
     * @return
     */
    public static boolean isJARRunning(){
        return FileUtil.class.getResource("").getFile().toLowerCase().indexOf(".jar") > -1;
    }
    /**
     * 获取相对于默认用户目录的路径或文件名或URL，自动处理是否为jar中运行的情况
     * @param name 相对路径或文件名
     * @return
     */
    public static String getRelativeFilePath(String name){
        //jar相对路径
        if(isJARRunning()) return name;
        //非jar运行时获取配置文件路径
        Thread thread = Thread.currentThread();
        ClassLoader classLoader = thread.getContextClassLoader();
        URL url = classLoader.getResource(name);
        String path = url.getFile();

        return path;
    }
    public static String readFile(File file){
        final InputStreamReader reader;
        final StringBuffer buffer = new StringBuffer();

        try {
            reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
            int temp;

            while ((temp = reader.read()) != -1) {
                buffer.append((char)temp);
            }
            reader.close();
        } catch (IOException e) {
           System.out.println( e);
        }

        return buffer.toString();
    }
    //
    public static void writeFile(File file, String content){
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println( e);
        } catch (IOException e) {
            System.out.println( e);
        }
    }
}
