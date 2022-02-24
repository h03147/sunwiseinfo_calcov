package com.yejia.main;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yejia.utils.GetFoldFileNamesUtil;
import com.yejia.utils.JsonFormatUtil;

import java.io.File;
import java.util.*;

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

    // 打印初始的命令行操作菜单
    private List<String> printCmdMenuScreen() {
        List<String> list = GetFoldFileNamesUtil.getFileName();
        System.out.println("当前项目可查看覆盖率的json文件如下：");
        for(int i = 0; i < list.size(); ++i) {
            System.out.println(i + 1 + ": " + list.get(i));
        }
        System.out.println("请输入文件前面的编号查看其覆盖率，如需查看多个文件叠加结果请用空格隔开多个序号(回车执行):");
        Scanner scanner = new Scanner(System.in);
        String fileId = scanner.nextLine();
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
        Double totalLineCov = 0d;
        Double totalBranchCov = 0d;
        for(String fileName : seletedList) {
            String tempFileName = "resources" + File.separator + "jsonfiles" + File.separator + fileName;
            JSONObject jsonObject = jsonFormatUtil.fileToJson(tempFileName);
            List<Double> tempResultList = calculateALlCoverage(jsonObject);
            totalLineCov += tempResultList.get(0);
            totalBranchCov += tempResultList.get(1);
        }
        Double totalLineCovResult = totalLineCov / seletedList.size();
        Double totalBranchCovResult = totalBranchCov / seletedList.size();
        System.out.print("当前选择的" + seletedList.size() + "个文件总语句覆盖率为:" + String.format("%.2f", totalLineCovResult) + "%, ");
        System.out.print("总分支覆盖率为:" + String.format("%.2f", totalBranchCovResult) + "%, ");
    }

    // 按文件级json对象解析做语句、分支、mcdc覆盖率计算
    private List<Double> calculateALlCoverage(JSONObject jsonObject) {
        // 区分coxCovs字段和caseCovs字段
        JSONArray tempJsonArray = jsonObject.getJSONArray("coxCovs") == null ?  jsonObject.getJSONArray("caseCovs") :  jsonObject.getJSONArray("coxCovs");
        JSONArray tempFuncJsonArray = tempJsonArray.getJSONObject(0).getJSONArray("files")
                .getJSONObject(0).getJSONArray("funcs");
        int cntTrue = 0;
        int totalLineCovNumber = 0;
        // 当前文件的所有分支覆盖率字典
        Map<Integer, Double> branchCovDictionary = new HashMap<>();

        // 遍历所有json文件
        for(int i = 0; i < tempFuncJsonArray.size(); ++i) {
            JSONArray tempLineCovJsonArray = tempFuncJsonArray.getJSONObject(i).getJSONArray("lineCov");
            totalLineCovNumber += tempLineCovJsonArray.size();

            // 计算语句覆盖率
            for(int j = 0; j < tempLineCovJsonArray.size(); ++j) {
                String state = tempLineCovJsonArray.getJSONObject(j).getString("state");
                cntTrue = "T".equals(state) ? ++cntTrue : cntTrue;
            }

            // 计算分支覆盖率
            JSONArray tempBranchCov = tempFuncJsonArray.getJSONObject(i).getJSONArray("branchCov");
            for(int k = 0; k < tempBranchCov.size(); ++k) {
                Integer key = tempBranchCov.getJSONObject(k).getInteger("id");
                if(branchCovDictionary.containsKey(key)) {
                    // 如果当前分支覆盖率已经100%或者未执行，则当前分支覆盖率不变，直接跳过不做任何操作
                    if(branchCovDictionary.get(key) == 1.0 || "X".equals(tempBranchCov.getJSONObject(k).getString("state")) ||
                            "N".equals(tempBranchCov.getJSONObject(k).getString("state"))) {
                        continue;
                    } else if("P".equals(tempBranchCov.getJSONObject(k).getString("state"))){
                        // 为P不管之前覆盖率如何现在已经100%覆盖
                        branchCovDictionary.put(key, 1.0);
                    } else {
                        branchCovDictionary.put(key, branchCovDictionary.get(key) + 0.5);
                    }
                } else {
                    if("P".equals(tempBranchCov.getJSONObject(k).getString("state"))) {
                        branchCovDictionary.put(key, 1.0);
                    } else if("X".equals(tempBranchCov.getJSONObject(k).getString("state")) ||
                            "N".equals(tempBranchCov.getJSONObject(k).getString("state"))){
                        branchCovDictionary.put(key, 0.0);
                    } else {
                        branchCovDictionary.put(key, 0.5);
                    }
                }
            }
        }

        Iterator<Integer> iter = branchCovDictionary.keySet().iterator();
        double sumBranchCov = 0.0;
        while(iter.hasNext()) {
            Object key = iter.next();
            sumBranchCov = sumBranchCov + branchCovDictionary.get(key);
        }

        String codeName = tempJsonArray.getJSONObject(0).getJSONArray("files")
                .getJSONObject(0).getString("file");
        codeName = codeName.substring(codeName.lastIndexOf(File.separator) + 1);
        System.out.print("文件源码" +codeName + "的");

        double lineCov = (double) (cntTrue * 100) / totalLineCovNumber;
        System.out.print("语句覆盖率为: "+  String.format("%.2f", lineCov) + "%, ");

        double BranchCov = sumBranchCov * 100 / branchCovDictionary.size();
        System.out.println("分支覆盖率为" + String.format("%.2f", BranchCov) + "%, ");

        List<Double> resultList = new ArrayList<>();
        resultList.add(lineCov);
        resultList.add(BranchCov);
        return resultList;
    }
}
