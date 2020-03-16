package com.qf.controller;

import dto.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/search")
@ResponseBody
public class SearchController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/searchByKeyword")
    public ResultBean searchByKeyword(String keyword){
        String url = String.format("http://search-service/search/searchByKeyword/%s",keyword);
        ResultBean resultBean = restTemplate.getForObject(url, ResultBean.class);
        return resultBean;
    }

    @RequestMapping("/addProductToSolr")
    public ResultBean addProductToSolr(String pid){
        String url = String.format("http://search-service/search/addProductToSolr/%s",pid);
        ResultBean resultBean = restTemplate.getForObject(url, ResultBean.class);
        return resultBean;
    }


}
