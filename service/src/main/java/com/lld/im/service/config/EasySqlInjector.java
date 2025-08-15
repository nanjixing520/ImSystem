package com.lld.im.service.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

import java.util.List;

public class EasySqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        // 1. 先获取父类（DefaultSqlInjector）默认提供的方法列表
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        // 2. 向方法列表中添加自定义的批量插入方法
        methodList.add(new InsertBatchSomeColumn());// InsertBatchSomeColumn 是 MyBatis-Plus 框架自带的扩展类（批量插入实现类），
                                                    // 位于 MyBatis-Plus 的核心依赖包中
        return methodList;
    }

}