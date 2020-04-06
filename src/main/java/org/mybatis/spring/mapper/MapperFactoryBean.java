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
package org.mybatis.spring.mapper;

import static org.springframework.util.Assert.notNull;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.FactoryBean;

/**
 * BeanFactory that enables injection of MyBatis mapper interfaces. It can be set up with a SqlSessionFactory or a
 * pre-configured SqlSessionTemplate.
 * <p>
 * Sample configuration:
 *
 * <pre class="code">
 * {@code
 *   <bean id="baseMapper" class="org.mybatis.spring.mapper.MapperFactoryBean" abstract="true" lazy-init="true">
 *     <property name="sqlSessionFactory" ref="sqlSessionFactory" />
 *   </bean>
 *
 *   <bean id="oneMapper" parent="baseMapper">
 *     <property name="mapperInterface" value="my.package.MyMapperInterface" />
 *   </bean>
 *
 *   <bean id="anotherMapper" parent="baseMapper">
 *     <property name="mapperInterface" value="my.package.MyAnotherMapperInterface" />
 *   </bean>
 * }
 * </pre>
 * <p>
 * Note that this factory can only inject <em>interfaces</em>, not concrete classes.
 *
 * @author Eduardo Macarron
 *
 * @see SqlSessionTemplate
 */
public class MapperFactoryBean<T> extends SqlSessionDaoSupport implements FactoryBean<T> {

  private Class<T> mapperInterface;

  private boolean addToConfig = true;

  public MapperFactoryBean() {
    // intentionally empty
  }

  public MapperFactoryBean(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  /**
   * {@inheritDoc}
   *
   * MapperFactoryBean最终实现了 InitializingBean接口，在 mapper进行初始化的时候会调用
   * afterPropertiesSet()方法，这个方法是在父类DaoSupport中实现的，父类的实现方法中调用
   * 了checkDaoConfig()方法，这里重写了这个方法，所以会执行这里的逻辑
   */
  @Override
  protected void checkDaoConfig() {
    super.checkDaoConfig();

    //检查 mapperInterface 是否为空，这个 mapperInterface 就是 mapper 对应的接口，如XxxMapper
    notNull(this.mapperInterface, "Property 'mapperInterface' is required");

    Configuration configuration = getSqlSession().getConfiguration();
    /**
     * 如果 mapper 还没有被添加到 knownMappers 这个map中，说明 mapper 还没有被处理
     * 这个map非常重要，存放的是一个mapper代理工厂，每一个被处理的mapper都会在该map
     * 中对应一个mapper代理工厂，当获取mapper代理类的时候，会通过该工厂产生一个真正
     * 的mapper代理类（MapperProxy），调用mapper接口中的方法实际上执行的就是
     * MapperProxy 中的 invoke()方法
     */
    if (this.addToConfig && !configuration.hasMapper(this.mapperInterface)) {
      try {
        //将mapper添加到 knownMappers 这个map中来，然后对 mapper 接口进行解析
        configuration.addMapper(this.mapperInterface);
      } catch (Exception e) {
        logger.error("Error while adding the mapper '" + this.mapperInterface + "' to configuration.", e);
        throw new IllegalArgumentException(e);
      } finally {
        ErrorContext.instance().reset();
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * 获取mapper接口代理类的时候（被作为依赖进行注入时也会获取mapper的代理类），会执行该方法拿到一个对象
   * 这里返回的对象才是mapper接口真正的代理类对象，mybatis-spring中，getSqlSession()拿到的SqlSession对象
   * 是一个SqlSessionTemplate对象，getMapper()方法其实是从 knownMappers 这个map中获取到该mapper的一个代理
   * 工厂，然后通过mapper代理工厂生成一个mapper代理类（MapperProxy），生成代理类使用的是JDK动态代理
   */
  @Override
  public T getObject() throws Exception {
    return getSqlSession().getMapper(this.mapperInterface);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<T> getObjectType() {
    return this.mapperInterface;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSingleton() {
    return true;
  }

  // ------------- mutators --------------

  /**
   * Sets the mapper interface of the MyBatis mapper
   *
   * @param mapperInterface
   *          class of the interface
   */
  public void setMapperInterface(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  /**
   * Return the mapper interface of the MyBatis mapper
   *
   * @return class of the interface
   */
  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  /**
   * If addToConfig is false the mapper will not be added to MyBatis. This means it must have been included in
   * mybatis-config.xml.
   * <p>
   * If it is true, the mapper will be added to MyBatis in the case it is not already registered.
   * <p>
   * By default addToConfig is true.
   *
   * @param addToConfig
   *          a flag that whether add mapper to MyBatis or not
   */
  public void setAddToConfig(boolean addToConfig) {
    this.addToConfig = addToConfig;
  }

  /**
   * Return the flag for addition into MyBatis config.
   *
   * @return true if the mapper will be added to MyBatis in the case it is not already registered.
   */
  public boolean isAddToConfig() {
    return addToConfig;
  }
}
