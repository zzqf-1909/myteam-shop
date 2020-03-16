package com.qf.controller;

import com.qf.mapper.TProductSearchDTOMapper;
import dto.ResultBean;
import dto.TProductSearchDTO;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/search")
public class SearchController {


    @Autowired
    private TProductSearchDTOMapper mapper;

    @Autowired
    private SolrClient solrClient;
    @RequestMapping("/searchByKeyword/{keyword}")
    public String searchByKeyword(@PathVariable String keyword){
        return "";
    }

    @RequestMapping("/addProductToSolr/{pid}")
    @ResponseBody
    public ResultBean addProductToSolr(@PathVariable String pid){
        //1.根据pid从数据库中获取该商品
        TProductSearchDTO product = mapper.selectById(pid);
        //2.封装成SolrInputDocument
        SolrInputDocument document = new SolrInputDocument();
        document.setField("id",product.getId());
        document.setField("t_product_name",product.getTProductName());
        document.setField("t_product_sale_price",product.getTProductSalePrice().floatValue());
        document.setField("t_product_pimage",product.getTProductPimage());
        document.setField("t_product_pdesc",product.getTProductPdesc());
        //插入到solr库中
        try {
            solrClient.add(document);
            solrClient.commit();
            return ResultBean.success("插入solr成功");
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResultBean.success("插入solr失败");
    }

}
