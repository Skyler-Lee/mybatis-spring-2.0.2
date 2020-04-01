package org.mybatis.study.app;

import org.mybatis.study.service.IUserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationMain {

  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
    IUserService userService = context.getBean(IUserService.class);
    userService.query();
  }
}
