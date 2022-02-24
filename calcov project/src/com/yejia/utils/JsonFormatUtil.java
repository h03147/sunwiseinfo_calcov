package com.yejia.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
*
* @Description: 读取指定目录下json文件转换成jsonObject的工具类
* @date: 2022/2/24
* @author: yj
 */
public class JsonFormatUtil {

    public JSONObject fileToJson(String fileName) {
        JSONObject json = null;
        try (
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        ) {
            json = JSONObject.parseObject(IOUtils.toString(is, "utf-8"));
        } catch (Exception e) {
            System.out.println(fileName + "文件读取异常" + e);
        }
        return json;
    }
}
