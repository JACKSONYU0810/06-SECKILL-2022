package com.jackson.seckill.controller;

import com.jackson.seckill.common.Constants;
import com.jackson.seckill.common.ReturnObject;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * ClassName: WebController
 * Package: com.jackson.seckill.controller
 * Description:
 *
 * @Date: 7/29/2022 3:12 PM
 * @Author: JacksonYu
 */
@Controller
public class GoodsController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${seckill.goodsService}")
    private String goodsService;


    @RequestMapping("/goodsList")
    public String goodsList(Model model){

        String url = goodsService + "goodsList";
        Map goodsList = restTemplate.getForObject(url, Map.class);
        model.addAttribute("goodsMapList", goodsList.get("result"));

        return "goodsList";
    }

    @RequestMapping("/showGoodsInfo/{id}")
    public String goodsList(Model model, @PathVariable Integer id) throws ParseException {

        String url = goodsService + "goodsInfo/" + id;
        Map goodsInfoMap = restTemplate.getForObject(url, Map.class);

        //时间转换
        String startStr = (String) ((Map<Object, Object>) goodsInfoMap.get("result")).get("startTime");
        String endStr = (String) ((Map<Object, Object>) goodsInfoMap.get("result")).get("endTime");

        startStr = startStr.substring(0, startStr.indexOf("T")) + startStr.substring(startStr.indexOf("T") + 1, startStr.indexOf("."));
        endStr = endStr.substring(0, endStr.indexOf("T")) + endStr.substring(endStr.indexOf("T") + 1, endStr.indexOf("."));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        Date startTime = sdf.parse(startStr);
        Date endTime = sdf.parse(endStr);

        ((Map) goodsInfoMap.get("result")).put("startTime", startTime.getTime() + 1000 * 60 * 60 * 8);
        ((Map) goodsInfoMap.get("result")).put("endTime", endTime.getTime() + 1000 * 60 * 60 * 8);


        model.addAttribute("goodsMap", goodsInfoMap.get("result"));

        return "goodsInfo";
    }

    @RequestMapping("/getSysTime")
    @ResponseBody
    public Object goodsList() {

        ReturnObject returnObject = new ReturnObject();
        returnObject.setCode(Constants.OK);
        returnObject.setMessage("操作成功");
        returnObject.setResult(System.currentTimeMillis());

        return returnObject;
    }


    @RequestMapping("/getRandomName/{id}")
    @ResponseBody
    public Object getRandomName(@PathVariable Integer id) throws ParseException {

        String url = goodsService + "goodsInfo/" + id;
        Map result = restTemplate.getForObject(url, Map.class);

        ReturnObject returnObject = new ReturnObject();
        //可能用户手动拼接的请求导致主键错误
        if (result == null || result.isEmpty()) {
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("商品信息异常请确认后再购买");
            returnObject.setResult("");
            return returnObject;
        }


        String startTimeStr = (String) ((Map) result.get("result")).get("startTime");
        String endTimeStr = (String) ((Map) result.get("result")).get("endTime");

        startTimeStr = startTimeStr.substring(0, startTimeStr.indexOf("T")) + startTimeStr.substring(startTimeStr.indexOf("T") + 1, startTimeStr.indexOf("."));
        endTimeStr = endTimeStr.substring(0, endTimeStr.indexOf("T")) + endTimeStr.substring(endTimeStr.indexOf("T") + 1, endTimeStr.indexOf("."));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        Date startTime = sdf.parse(startTimeStr);
        Date endTime = sdf.parse(endTimeStr);

        ((Map) result.get("result")).put("startTime", startTime.getTime() + 1000 * 60 * 60 * 8);
        ((Map) result.get("result")).put("endTime", endTime.getTime() + 1000 * 60 * 60 * 8);

        //获取当前系统时间
        Long currentTime = System.currentTimeMillis();
        if (currentTime < startTime.getTime()) {
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("该商品秒杀活动尚未开始!");
            returnObject.setResult("");
            return returnObject;
        }

        if (currentTime > endTime.getTime()) {
            returnObject.setCode(Constants.ERROR);
            returnObject.setMessage("该商品秒杀活动已经结束!");
            returnObject.setResult("");
            return returnObject;
        }

        returnObject.setCode(Constants.OK);
        returnObject.setMessage("获取随机名成功");
        returnObject.setResult(((Map)result.get("result")).get("randomName"));

        return returnObject;
    }

    @RequestMapping("/secKill/{goodsId}/{randomName}")
    @ResponseBody
    public Object secKill(@PathVariable Integer goodsId,@PathVariable String randomName){
        Integer uid = 1;

        String url = goodsService + "secKill/" + goodsId + "/" + randomName + "/" + uid;
        Map result = restTemplate.getForObject(url, Map.class);

        return result;
    }

}
