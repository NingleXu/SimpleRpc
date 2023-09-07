package com.gdou.config.spring.annotation;

import com.gdou.common.annotations.RpcReference;
import com.gdou.config.api.ReferenceConfig;
import com.gdou.config.api.ReferenceConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.gdou.common.config.ConfigNameGenerator.generaReferenceConfigName;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

/**
 * @author ningle
 * @version : RpcReferenceAnnotationBeanPostProcessor.java, v 0.1 2023/09/05 16:18 ningle
 **/
public class ReferenceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements BeanFactoryAware {
    public static final String BEAN_NAME = "referenceAnnotationBeanPostProcessor";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceAnnotationBeanPostProcessor.class);
    Set<Class<? extends Annotation>> referenceAnnotationTypes = new HashSet<>(4);

    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    private final ConcurrentMap<String, Object> injectedObjectsCache = new ConcurrentHashMap<String, Object>(16);

    private ConfigurableListableBeanFactory beanFactory;

    public ReferenceAnnotationBeanPostProcessor() {

        List<Class<? extends Annotation>> list = Arrays.asList(RpcReference.class);

        referenceAnnotationTypes = new HashSet<>(list);
    }

    /**
     * 在执行依赖注入时， 注入@RpcReference修饰的对象
     *
     * @param pvs      the property values that the factory is about to apply (never {@code null})
     * @param bean     the bean instance created, but whose properties have not yet been set
     * @param beanName the name of the bean
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        // 查找需要被注入的元数据
        InjectionMetadata metadata = findRpcReferenceMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return pvs;
    }

    private InjectionMetadata findRpcReferenceMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = buildRpcReferenceMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildRpcReferenceMetadata(Class<?> clazz) {
        if (!AnnotationUtils.isCandidateClass(clazz, this.referenceAnnotationTypes)) {
            return InjectionMetadata.EMPTY;
        }

        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                MergedAnnotation<?> ann = findReferenceAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Autowired annotation is not supported on static fields: " + field);
                        }
                        return;
                    }
                    // 获取注解上的属性
                    AnnotationAttributes annotationAttributes = getAnnotationAttributes(field, field.getAnnotation(RpcReference.class));
                    currElements.add(new AnnotatedFieldElement(field, annotationAttributes));
                }
            });

            ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                MergedAnnotation<?> ann = findReferenceAnnotation(bridgedMethod);
                if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Autowired annotation is not supported on static methods: " + method);
                        }
                        return;
                    }
                    if (method.getParameterCount() == 0) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Autowired annotation should only be used on methods with parameters: " +
                                    method);
                        }
                    }
                    // 获取注解上的属性
                    AnnotationAttributes annotationAttributes = getAnnotationAttributes(method, method.getAnnotation(RpcReference.class));
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, clazz);
                    currElements.add(new AnnotatedMethodElement(method, pd, annotationAttributes));
                }
            });

            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return InjectionMetadata.forElements(elements, clazz);
    }

    @Nullable
    private MergedAnnotation<?> findReferenceAnnotation(AccessibleObject ao) {
        MergedAnnotations annotations = MergedAnnotations.from(ao);
        for (Class<? extends Annotation> type : this.referenceAnnotationTypes) {
            MergedAnnotation<?> annotation = annotations.get(type);
            if (annotation.isPresent()) {
                return annotation;
            }
        }
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    /**
     * {@link Annotation Annotated} {@link Field} {@link InjectionMetadata.InjectedElement}
     */
    public class AnnotatedFieldElement extends InjectionMetadata.InjectedElement {

        private final Field field;
        private volatile Object bean;

        private final AnnotationAttributes attributes;

        protected AnnotatedFieldElement(Field field, AnnotationAttributes attributes) {
            super(field, null);
            this.field = field;
            this.attributes = attributes;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> injectedType = field.getType();

            Object injectedObject = getInjectedObject(injectedType, attributes);

            ReflectionUtils.makeAccessible(field);

            field.set(bean, injectedObject);

        }

    }

    /**
     * {@link Annotation Annotated} {@link Field} {@link InjectionMetadata.InjectedElement}
     */
    public class AnnotatedMethodElement extends InjectionMetadata.InjectedElement {

        private final Method method;
        private volatile Object bean;

        private final AnnotationAttributes attributes;

        protected AnnotatedMethodElement(Method method, PropertyDescriptor pd, AnnotationAttributes attributes) {
            super(method, pd);
            this.method = method;
            this.attributes = attributes;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {

            Class<?> injectedType = pd.getPropertyType();

            Object injectedObject = getInjectedObject(injectedType, attributes);

            ReflectionUtils.makeAccessible(method);

            method.invoke(bean, injectedObject);

        }

    }

    private Object getInjectedObject(Class<?> injectedType, AnnotationAttributes attributes) {

        String cachedKey = injectedType.getName() +
                "#attributest" + attributes;

        Object injectedObject = injectedObjectsCache.get(cachedKey);

        if (injectedObject == null) {
            injectedObject = doGetInjectedBean(injectedType, attributes);
            // Customized inject-object if necessary
            injectedObjectsCache.putIfAbsent(cachedKey, injectedObject);
        }

        return injectedObject;
    }

    private Object doGetInjectedBean(Class<?> injectedType, AnnotationAttributes attributes) {
        // spring中 bean引用bean的名称
        String referenceBeanName = generaReferenceConfigName(injectedType, attributes.getString("version"), attributes.getString("group"));

        // 构建 referenceConfig
        ReferenceConfig<?> referenceConfig =
                buildReferenceConfig(referenceBeanName, injectedType, attributes);

        registerReferenceConfig(referenceBeanName, referenceConfig);

        return referenceConfig.get();
    }

    private void registerReferenceConfig(String referenceBeanName, ReferenceConfig<?> referenceConfig) {
        if (!beanFactory.containsBean(referenceBeanName)) {
            beanFactory.registerSingleton(referenceBeanName, referenceConfig);
        }
    }

    ConcurrentHashMap<String, ReferenceConfig<?>> referenceConfigCache = new ConcurrentHashMap<>();

    private ReferenceConfig<?> buildReferenceConfig(String referenceBeanName, Class<?> injectedType, AnnotationAttributes attributes) {
        ReferenceConfig<?> referenceConfig = referenceConfigCache.get(referenceBeanName);

        if (referenceConfig == null) {
            referenceConfig = new ReferenceConfigBuilder<>()
                    .version(attributes.getString("version"))
                    .group(attributes.getString("group"))
                    .interfaceClass(injectedType)
                    .build();
            referenceConfigCache.put(referenceBeanName, referenceConfig);
        }

        return referenceConfig;
    }
}
