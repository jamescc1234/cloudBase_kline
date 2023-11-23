package com.widget.stock.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

/**
 * 要存储、打开的对象或里面的对象不能为且必须实现Serializable接口，否则是不会序列化的（报异常）
 *
 * @author
 */
public class SaveObject {

    /**
     * 单例模式
     */
    public static SaveObject saveListObject;

    /**
     * 私有构造函数
     */
    private SaveObject() {
    }

    public synchronized static SaveObject getInstance() {
        if (saveListObject == null) {
            saveListObject = new SaveObject();
        }
        return saveListObject;
    }

    /**
     * 打开对象到内存
     *
     * @param file 文件路径，必须是文件
     * @return 返回null为走到异常
     */
    public Object openObject(File file) {
        if (file == null)
            return null;
        if (!file.exists()) {
            return null;
        }
        Object obj = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            obj = ois.readObject();
            fis.close();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return obj;
    }

    /**
     * 存储对象到指定路径
     *
     * @param file   存储文件路径
     * @param object 需要存储的对象
     * @return 返回false为走到异常，返回true为存储成功
     */
    public boolean saveObject(File file, Object object) {
        if (file == null) {
            return false;
        }
        if (object == null) {
            if (file.exists()) {
                file.delete();
            }
            return true;
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            fos.close();
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
