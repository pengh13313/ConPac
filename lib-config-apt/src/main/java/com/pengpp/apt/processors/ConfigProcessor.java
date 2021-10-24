package com.pengpp.apt.processors;

import com.google.auto.service.AutoService;
import com.pengpp.annotations.PConfig;
import com.pengpp.apt.helpers.GenerateJavaHelper;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import static com.pengpp.apt.utils.Consts.ANNOTATION_TYPE_JSON;

@AutoService(Processor.class)
@SupportedAnnotationTypes({ANNOTATION_TYPE_JSON})
public class ConfigProcessor extends BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        PConfig config = null;
        String path = null;
        String format;
        if (set == null) {
            logger.info(">>> Cannot Found autowired field, failed <<<");
            return false;
        }
        logger.info(">>> Found autowired field, start... <<<");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(PConfig.class);
        if(elements == null) {
            logger.info(">>> Found elements PConfig null <<<");
            return false;
        }
        for(Element element : elements){
            PackageElement packageElement = elementUtils.getPackageOf(element);
            String packageName = packageElement.getQualifiedName().toString();
            logger.info(">>> Found element packageName: " + packageName + "<<<");
            logger.info(">>> Found element annotation: " + element.getAnnotation(PConfig.class) + "<<<");
            config = element.getAnnotation(PConfig.class);
            path = config.path();
            format = config.format();
            logger.info(">>> Found element config: " + path + "<<<");
            logger.info(">>> Found element format: " + format + "<<<");
            path = getConfigAbsolutePath(path);
            GenerateJavaHelper.generateJava(path,format,packageName,logger,mFiler);
        }
        return false;
    }


    public String getConfigAbsolutePath(String path){
        if(path == null || StringUtils.isEmpty(path)){
            //使用默认配置
            path = (new File("")).getAbsolutePath().replace("/app","") + "/config/config.json";
            logger.info(">>> use local path: " + path + " <<<");
        }else{
            path = (new File("")).getAbsolutePath().replace("/app","") + path;
            logger.info(">>> use set path: " + path + " <<<");
            if(!new File(path).exists()){
                logger.info(">>> path not exist <<<");
                return "";
            }
        }
        return path;
    }

}


