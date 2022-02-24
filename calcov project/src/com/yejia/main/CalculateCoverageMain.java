package com.yejia.main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yejia.utils.GetFoldFileNamesUtil;
import com.yejia.utils.JsonFormatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
*
* @Description: 程序主入口Main
* @date: 2022/2/24
* @author: yj
 */
public class CalculateCoverageMain {

    public static void main(String[] args) {
        CalculateCoverageMain calculateCoverageMain = new CalculateCoverageMain();
        calculateCoverageMain.genarateCoverage(calculateCoverageMain.printCmdMenuScreen());
    }

    // 打印操作命令行菜单的方法
    private List<String> printCmdMenuScreen() {
        List<String> list = GetFoldFileNamesUtil.getFileName();
        System.out.println("当前项目可查看覆盖率的文件如下：");
        for(int i = 0; i < list.size(); ++i) {
            System.out.println(i + 1 + ": " + list.get(i));
        }
        System.out.println("请输入文件前面的编号查看其覆盖率，如需查看多个文件叠加结果请用空格隔开:");
        Scanner scanner = new Scanner(System.in);
        String fileId = scanner.nextLine();
//        System.out.println(fileId);
        String[] fileIdArr = fileId.split(" ");
        List<String> seletedtList = new ArrayList<>();
        for(String seletedId : fileIdArr) {
            seletedtList.add(list.get(Integer.parseInt(seletedId) - 1));
        }
        return seletedtList;
    }

    // 根据已选择文件解析json文件输出结果
    private void genarateCoverage(List<String> seletedList) {
        JsonFormatUtil jsonFormatUtil = new JsonFormatUtil();
        for(String fileName : seletedList) {
            String tempFileName = "resources" + File.separator + "jsonfiles" + File.separator + fileName;
            JSONObject jsonObject = jsonFormatUtil.fileToJson(tempFileName);
//            System.out.println(json);
            calculateALlCoverage(jsonObject);
        }
    }

//        System.out.println(json.getJSONArray("coxCovs").getJSONObject(0).getJSONArray("files")
//                .getJSONObject(0).getJSONArray("funcs").getJSONObject(0).getJSONArray("lineCov")
//                .getJSONObject(0).getInteger("line"));
    // 按文件级json对象解析做语句、分支、mcdc覆盖率计算
    private List<Double> calculateALlCoverage(JSONObject jsonObject) {
        // 区分coxCovs字段和caseCovs字段
        JSONArray tempJsonArray = jsonObject.getJSONArray("coxCovs") == null ?  jsonObject.getJSONArray("caseCovs") :  jsonObject.getJSONArray("coxCovs");
//        System.out.println(tempJsonArray);
        JSONArray tempFuncJsonArray = tempJsonArray.getJSONObject(0).getJSONArray("files")
                .getJSONObject(0).getJSONArray("funcs");
        int cntTrue = 0;
        int totalLineCovNumber = 0;
        for(int i = 0; i < tempFuncJsonArray.size(); ++i) {
            JSONArray tempLineCovJsonArray = tempFuncJsonArray.getJSONObject(i).getJSONArray("lineCov");
            totalLineCovNumber += tempLineCovJsonArray.size();
            for(int j = 0; j < tempLineCovJsonArray.size(); ++j) {
                String state = tempLineCovJsonArray.getJSONObject(j).getString("state");
                cntTrue = "T".equals(state) ? ++cntTrue : cntTrue;
            }
        }
        System.out.println(cntTrue);
        System.out.println(totalLineCovNumber);
        double lineCov = (double) (cntTrue * 100) / totalLineCovNumber;
        System.out.println("语句覆盖率为: "+  String.format("%.2f", lineCov) + "%");
        return new ArrayList<Double>() {};
    }
}
