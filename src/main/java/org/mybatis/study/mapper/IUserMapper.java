package org.mybatis.study.mapper;

import org.apache.ibatis.annotations.Select;
import org.mybatis.study.entity.User;

import java.util.List;

public interface IUserMapper {

  @Select("select * from cdc_user")
  List<User> query();

  User selectOne(String id);
}
