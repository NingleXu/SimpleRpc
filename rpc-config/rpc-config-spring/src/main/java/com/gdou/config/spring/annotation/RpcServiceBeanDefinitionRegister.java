package com.gdou.config.spring.annotation;

import com.gdou.common.annotations.RpcService;
import com.gdou.config.api.ServiceConfig;
import com.gdou.common.config.metadata.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.util.*;

import static com.gdou.common.config.ConfigNameGenerator.generaServiceConfigName;
import static java.util.Arrays.asList;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.StringUtils.hasText;

/**
 * 负责扫描 RpcService 标注的类，将其加载为 BeanDefinition
 *
 * @author ningle
 * @version : RpcServiceBeanDefinitionRegister.java, v 0.1 2023/09/05 15:21 ningle
 **/
public class RpcServiceBeanDefinitionRegister implements BeanDefinitionRegistryPostProcessor, ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    private Logger logger = LoggerFactory.getLogger(RpcServiceBeanDefinitionRegister.class);

    private final static List<Class<? extends Annotation>> serviceAnnotationTypes = asList(
            RpcService.class
    );

    private final Set<String> packagesToScan;

    private ClassLoader classLoader;

    public RpcServiceBeanDefinitionRegister(String... packagesToScan) {
        this(asList(packagesToScan));
    }

    public RpcServiceBeanDefinitionRegister(Collection<String> packagesToScan) {
        this(new LinkedHashSet<>(packagesToScan));
    }

    public RpcServiceBeanDefinitionRegister(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        /**
         * 扫描所有的注册
         */
        registerServiceBeans(packagesToScan, registry);

    }

    private void registerServiceBeans(Set<String> packagesToScan, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner sc = new ClassPathBeanDefinitionScanner(registry, false);

        BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);

        serviceAnnotationTypes.forEach(serviceAnnotationType ->
                sc.addIncludeFilter(new AnnotationTypeFilter(serviceAnnotationType))
        );

        // 执行扫描
        packagesToScan.forEach(packageToScan -> {
            sc.scan(packageToScan);

            // 利用Spring去获取 @RpcService注解标注的 BeanDefinitionHolder
            Set<BeanDefinitionHolder> beanDefinitionHolders =
                    findServiceBeanDefinitionHolders(sc, packageToScan, registry, beanNameGenerator);

            if (!CollectionUtils.isEmpty(beanDefinitionHolders)) {
                for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                    // 利用 service impl 的bean 注册ServiceConfig
                    registerServiceConfig(beanDefinitionHolder, registry, sc);
                }
                if (logger.isInfoEnabled()) {
                    logger.info(beanDefinitionHolders.size() + " annotated Dubbo's @Service Components { " +
                            beanDefinitionHolders +
                            " } were scanned under package[" + packageToScan + "]");
                }
            } else {

                if (logger.isWarnEnabled()) {
                    logger.warn("No Spring Bean annotating Dubbo's @Service was found under package["
                            + packageToScan + "]");
                }

            }
        });


    }

    private void registerServiceConfig(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry, ClassPathBeanDefinitionScanner scanner) {
        // 获取当前bean的类 (impl 的类)
        Class<?> beanClass = resolveClass(beanDefinitionHolder);
        // 获取这个类 上面的注解信息
        Annotation service = findServiceAnnotation(beanClass);
        // 获取注解上的元信息
        AnnotationAttributes serviceAnnotationAttributes = getAnnotationAttributes(service, false, false);

        // 处理这个类实现的的接口
        Class<?> interfaceClass = resolveServiceInterfaceClass(serviceAnnotationAttributes, beanClass);

        // 获取 impl类的serviceBean name
        String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();

        // 构建serviceConfig
        AbstractBeanDefinition serviceConfigDefinition =
                buildServiceConfigDefinition(service, interfaceClass, annotatedServiceBeanName);
        // serviceConfig name name
        String beanName = generaServiceConfigName(interfaceClass,
                serviceAnnotationAttributes.getString("version"),
                serviceAnnotationAttributes.getString("group"));


        if (!registry.containsBeanDefinition(beanName)) {
            registry.registerBeanDefinition(beanName, serviceConfigDefinition);

            if (logger.isInfoEnabled()) {
                logger.info("The BeanDefinition[" + serviceConfigDefinition +
                        "] of ServiceBean has been registered with name : " + beanName);
            }
        }
    }

    private AbstractBeanDefinition buildServiceConfigDefinition(Annotation service, Class<?> interfaceClass,
                                                                String annotatedServiceBeanName) {

        BeanDefinitionBuilder builder = rootBeanDefinition(ServiceConfig.class);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        // meta data
        Map<String, Object> annotationAttributes = getAnnotationAttributes(service);
        ServiceMetadata serviceMetadata = new ServiceMetadata((String) annotationAttributes.get("version"),
                (String) annotationAttributes.get("group"),
                interfaceClass);
        propertyValues.addPropertyValue("metadata", serviceMetadata);

        // References "ref" property to annotated-@Service Bean
        builder.addPropertyReference("ref", annotatedServiceBeanName);

        return builder.getBeanDefinition();
    }


    private Annotation findServiceAnnotation(Class<?> beanClass) {
        return serviceAnnotationTypes
                .stream()
                .map(annotationType -> findMergedAnnotation(beanClass, annotationType))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Class<?> resolveClass(BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();
        return resolveClass(beanDefinition);

    }

    private Class<?> resolveClass(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        return resolveClassName(beanClassName, classLoader);
    }

    public static Class<?> resolveServiceInterfaceClass(AnnotationAttributes attributes, Class<?> defaultInterfaceClass)
            throws IllegalArgumentException {
        Object interfaceClassObject = attributes.get("interfaceClass");
        Class<?> interfaceClass = null;

        if (interfaceClassObject != null && !void.class.equals(interfaceClassObject)) { // default or set void.class for purpose.
            interfaceClass = (Class<?>) interfaceClassObject;
        }

        if (interfaceClass == null && defaultInterfaceClass != null) {
            // Find all interfaces from the annotated class
            Class<?>[] allInterfaces = getAllInterfacesForClass(defaultInterfaceClass);
            if (allInterfaces.length > 0) {
                interfaceClass = allInterfaces[0];
            }
        }
        Assert.notNull(interfaceClass,
                "@Service interfaceClass() or interfaceName() or interface class must be present!");
        Assert.isTrue(interfaceClass.isInterface(),
                "The annotated type must be an interface!");
        return interfaceClass;
    }

    private Set<BeanDefinitionHolder> findServiceBeanDefinitionHolders(
            ClassPathBeanDefinitionScanner scanner, String packageToScan, BeanDefinitionRegistry registry,
            BeanNameGenerator beanNameGenerator) {

        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);

        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>(beanDefinitions.size());

        for (BeanDefinition beanDefinition : beanDefinitions) {

            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);

        }

        return beanDefinitionHolders;

    }

    private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {

        BeanNameGenerator beanNameGenerator = null;

        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry singletonBeanRegistry = SingletonBeanRegistry.class.cast(registry);
            beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
        }

        if (beanNameGenerator == null) {

            if (logger.isInfoEnabled()) {

                logger.info("BeanNameGenerator bean can't be found in BeanFactory with name ["
                        + CONFIGURATION_BEAN_NAME_GENERATOR + "]");
                logger.info("BeanNameGenerator will be a instance of " +
                        AnnotationBeanNameGenerator.class.getName() +
                        " , it maybe a potential problem on bean name generation.");
            }

            beanNameGenerator = new AnnotationBeanNameGenerator();

        }

        return beanNameGenerator;

    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
