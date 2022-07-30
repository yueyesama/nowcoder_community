package com.wby.community.dao;

import org.springframework.stereotype.Repository;

//括号后面加名字，getBean()的时候可以指定名字
@Repository("alphaHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao{
    @Override
    public String select() {
        return "Hibernate";
    }
}
