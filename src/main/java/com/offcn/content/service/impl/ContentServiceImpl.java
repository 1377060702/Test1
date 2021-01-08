package com.offcn.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.content.service.ContentService;
import com.offcn.mapper.TbContentCategoryMapper;
import com.offcn.mapper.TbContentMapper;
import com.offcn.pojo.TbContent;
import com.offcn.pojo.TbContentCategory;
import com.offcn.pojo.TbContentExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    TbContentMapper tbContentMapper;

    @Autowired
    TbContentCategoryMapper contentCategoryMapper;

    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public List<TbContent> findList(Long categoryId) {

        List<TbContent> list = (List<TbContent>) redisTemplate.boundHashOps("contentHash").get(categoryId);

        if (list == null) {
            TbContentExample example = new TbContentExample();
            TbContentExample.Criteria criteria = example.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId);

            list = tbContentMapper.selectByExample(example);//如果list为空,从数据库里加载该部分数据
            //并且将数据库中查询到的数据,存到缓存redis中
            redisTemplate.boundHashOps("contentHash").put(categoryId, list);
            System.out.println("------mysql-------");

        } else {
            System.out.println("------redis-------");

        }
        return list;
    }

    //广告查询
    @Override
    public List<TbContent> search() {

        List<TbContent> tbContents = tbContentMapper.selectByExample(null);
        return tbContents;
    }

    //广告添加
    @Override
    public void add(TbContent content) {

        tbContentMapper.insert(content);
    }

    //添加操作的广告分类查询
    @Override
    public List<TbContentCategory> searchCategory() {

        return contentCategoryMapper.selectByExample(null);
    }

    //广告的删除
    @Override
    public void deleteContent(Long[] ids) {

        for (Long id : ids) {

            Long categoryId = tbContentMapper.selectByPrimaryKey(id).getCategoryId();
            //删除redis里存储的广告信息
            redisTemplate.boundHashOps("contentHash").delete(categoryId);

            tbContentMapper.deleteByPrimaryKey(id);
        }
    }

    //广告修改操作的数据回显
    @Override
    public TbContent updateShow(Long id) {

        return tbContentMapper.selectByPrimaryKey(id);
    }

    //广告修改
    @Override
    public void updateContent(TbContent content) {

        tbContentMapper.updateByPrimaryKey(content);

    }
}