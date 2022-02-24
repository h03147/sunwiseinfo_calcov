package com.yejia.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
*
* @Description: 遍历项目resources目录下所有json文件名称
* @date: 2022/2/24
* @author: yj
 */
public class GetFoldFileNamesUtil {

    public static List<String> getFileName() {
        String ProjectPath = System.getProperty("user.dir");
        String path = ProjectPath + File.separator + "calcov project" + File.separator + "src" + File.separator + "resources" + File.separator + "jsonfiles"; // resources路径
        List<String> jsonFileList = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) {
            System.out.println(path + " not exists");
            return jsonFileList;
        }

        File[] fa = f.listFiles();
        if (null != fa) {
            for (int i = 0; i < fa.length; i++) {
                File fs = fa[i];
                if (fs.isDirectory()) {
                    System.out.println(fs.getName() + " [目录]");
                } else {
    //                System.out.println(fs.getName());
                    jsonFileList.add(fs.getName());
                }
            }
        }
        return jsonFileList;
    }
}
