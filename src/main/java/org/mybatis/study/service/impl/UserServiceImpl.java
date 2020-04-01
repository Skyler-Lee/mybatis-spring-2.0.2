package org.mybatis.study.service.impl;

import org.mybatis.study.mapper.IUserMapper;
import org.mybatis.study.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {

  @Autowired
  private IUserMapper userMapper;

  @Override
  public void query() {
    System.out.println(userMapper.query().toString());
  }
}
