/**
 * Copyright ${license.git.copyrightYears} the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.spring.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * A {@link ImportBeanDefinitionRegistrar} to allow annotation configuration of MyBatis mapper scanning. Using
 * an @Enable annotation allows beans to be registered via @Component configuration, whereas implementing
 * {@code BeanDefinitionRegistryPostProcessor} will work for XML configuration.
 *
 * @author Michael Lanyon
 * @author Eduardo Macarron
 * @author Putthiphong Boonphong
 *
 * @see MapperFactoryBean
 * @see ClassPathMapperScanner
 * @since 1.2.0
 */
public class MapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

  /**
   * {@inheritDoc}
   *
   * @deprecated Since 2.0.2, this method not used never.
   */
  @Override
  @Deprecated
  public void setResourceLoader(ResourceLoader resourceLoader) {
    // NOP
  }

  /**
   * {@inheritDoc}
   *
   * spring在解析 @ComponentScan注解后，紧跟着解析 @Import 注解
   * 解析 @Import(ImportBeanDefinitionRegistrar.class)时进入此方法
   */
  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    /**
     * 通过 AnnotationAttributes.fromMap()方法获取 @MapperScan 注解元数据
     */
    AnnotationAttributes mapperScanAttrs = AnnotationAttributes
        .fromMap(importingClassMetadata.getAnnotationAttributes(MapperScan.class.getName()));
    if (mapperScanAttrs != null) {
      /**
       * 如果有 @MapperScan 注解，则进行注册
       * generateBaseBeanName()：org.mybatis.study.app.Appconfig#MapperScannerRegistrar#0，
       * 后面将这个beanName作为 key把对应的 BeanDefinition 注册到 beanDefinitionMap 中
       */
      registerBeanDefinitions(mapperScanAttrs, registry, generateBaseBeanName(importingClassMetadata, 0));
    }
  }

  void registerBeanDefinitions(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry, String beanName) {

    //封装一个 MapperScannerConfigurer 的 BeanDefinition，用来描述 @MapperScan 的配置信息
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
    //将 MapperScannerConfigurer 中的 processPropertyPlaceHolders 属性设置为true
    builder.addPropertyValue("processPropertyPlaceHolders", true);

    //接下来依次获取 @MapperScan 中的属性做类型比较，类型不同则更新 MapperScannerConfigurer 中的信息
    //其实就是将 @MapperScan 中的信息存储到 MapperScannerConfigurer
    Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
    if (!Annotation.class.equals(annotationClass)) {
      builder.addPropertyValue("annotationClass", annotationClass);
    }

    Class<?> markerInterface = annoAttrs.getClass("markerInterface");
    if (!Class.class.equals(markerInterface)) {
      builder.addPropertyValue("markerInterface", markerInterface);
    }

    Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
    if (!BeanNameGenerator.class.equals(generatorClass)) {
      builder.addPropertyValue("nameGenerator", BeanUtils.instantiateClass(generatorClass));
    }

    Class<? extends MapperFactoryBean> mapperFactoryBeanClass = annoAttrs.getClass("factoryBean");
    if (!MapperFactoryBean.class.equals(mapperFactoryBeanClass)) {
      builder.addPropertyValue("mapperFactoryBeanClass", mapperFactoryBeanClass);
    }

    String sqlSessionTemplateRef = annoAttrs.getString("sqlSessionTemplateRef");
    if (StringUtils.hasText(sqlSessionTemplateRef)) {
      builder.addPropertyValue("sqlSessionTemplateBeanName", annoAttrs.getString("sqlSessionTemplateRef"));
    }

    String sqlSessionFactoryRef = annoAttrs.getString("sqlSessionFactoryRef");
    if (StringUtils.hasText(sqlSessionFactoryRef)) {
      builder.addPropertyValue("sqlSessionFactoryBeanName", annoAttrs.getString("sqlSessionFactoryRef"));
    }

    //获取 @MapperScan 注解中传入的需要扫描的包，并将其存储在 basePackages 中
    List<String> basePackages = new ArrayList<>();
    basePackages.addAll(
        Arrays.stream(annoAttrs.getStringArray("value")).filter(StringUtils::hasText).collect(Collectors.toList()));

    basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText)
        .collect(Collectors.toList()));

    basePackages.addAll(Arrays.stream(annoAttrs.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName)
        .collect(Collectors.toList()));

    //是否需要延迟初始化标识
    String lazyInitialization = annoAttrs.getString("lazyInitialization");
    if (StringUtils.hasText(lazyInitialization)) {
      builder.addPropertyValue("lazyInitialization", lazyInitialization);
    }

    //将获取到的需要扫描的包的信息存储到 MapperScannerConfigurer 中
    builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

    //将描述 MapperScannerConfigurer 的 BeanDefinition注册到beanDefinitionMap中
    //beanName = 配置类的全限定名 + "#" + MapperScannerRegistrar + "#" + 0
    registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

  }

  private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata, int index) {
    return importingClassMetadata.getClassName() + "#" + MapperScannerRegistrar.class.getSimpleName() + "#" + index;
  }

  /**
   * A {@link MapperScannerRegistrar} for {@link MapperScans}.
   *
   * @since 2.0.0
   */
  static class RepeatingRegistrar extends MapperScannerRegistrar {
    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
      AnnotationAttributes mapperScansAttrs = AnnotationAttributes
          .fromMap(importingClassMetadata.getAnnotationAttributes(MapperScans.class.getName()));
      if (mapperScansAttrs != null) {
        AnnotationAttributes[] annotations = mapperScansAttrs.getAnnotationArray("value");
        for (int i = 0; i < annotations.length; i++) {
          registerBeanDefinitions(annotations[i], registry, generateBaseBeanName(importingClassMetadata, i));
        }
      }
    }
  }

}
