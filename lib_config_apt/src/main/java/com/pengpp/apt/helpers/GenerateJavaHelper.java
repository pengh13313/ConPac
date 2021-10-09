package com.pengpp.apt.helpers;

import com.pengpp.apt.utils.Logger;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang3.StringUtils;
import org.gsonformat.intellij.entity.ClassEntity;
import org.gsonformat.intellij.entity.FieldEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;

import static com.pengpp.apt.utils.Consts.WARNING_TIPS;
import static javax.lang.model.element.Modifier.PUBLIC;


public class GenerateJavaHelper {
    private static Logger logger;
    private static Filer filer;
    private static String packageName;
    private ClassEntity generateClassEntity = new ClassEntity();

    private HashMap<String, FieldEntity> declareFields;
    public static void generateJava(String path, String format,String pName, Logger runtimelogger, Filer mFiler){
        logger = runtimelogger;
        filer = mFiler;
        packageName = pName;
        logger.info(">>> generateJava start <<<");
        if(format != null && StringUtils.equals(format,".json")){
            JsonToJava(path);
        }
    }

    private static void JsonToJava(String path) {
        logger.info(">>> JsonToJava start <<<");
        if(StringUtils.isEmpty(path)){
            logger.info(">>> path is null <<<");
            return;
        }
        try {
            File file = new File(path);
            if(!file.exists()){
                logger.info(">>> file is not exist <<<");
            }
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(path), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            logger.info(">>> json string is "+ stringBuilder.toString() +"<<<");
            parseJson(stringBuilder.toString());
        } catch (UnsupportedEncodingException e) {
            logger.info(">>> JsonToJava exception "+ e.toString() +"<<<");
            e.printStackTrace();
        } catch (IOException e) {
            logger.info(">>> JsonToJava exception "+ e.toString() +"<<<");
            e.printStackTrace();
        }
    }

    private static void parseJson(String jsonStr) {
        JSONObject json = null;
        try {

            json = parseJSONObject(jsonStr);
        } catch (Exception e) {
            String jsonTS = removeComment(jsonStr);
            jsonTS = jsonTS.replaceAll("^.*?\\{", "{");
            try {
                json = parseJSONObject(jsonTS);
            } catch (Exception e2) {
            }
        }
        List<String> generateFiled = collectGenerateFiled(json);
        for (int i = 0; i < generateFiled.size(); i++) {
            logger.info(">>> generateFiled: " + generateFiled.get(i) + " <<<");
        }
        try {
            writeJava(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 过滤掉// 和/** 注释
     *
     * @param str
     * @return
     */
    public static String removeComment(String str) {
        String temp = str.replaceAll("/\\*" +
                "[\\S\\s]*?" +
                "\\*/", "");
        return temp.replaceAll("//[\\S\\s]*?\n", "");
    }

    private static JSONObject parseJSONObject(String jsonStr) {
        if (jsonStr.startsWith("{")) {
            return new JSONObject(jsonStr);
        } else if (jsonStr.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(jsonStr);

            if (jsonArray.length() > 0 && jsonArray.get(0) instanceof JSONObject) {
                return getJsonObject(jsonArray);
            }
        }
        return null;

    }

    private static JSONObject getJsonObject(JSONArray jsonArray) {
        JSONObject resultJSON = jsonArray.getJSONObject(0);

        for (int i = 1; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (!(value instanceof JSONObject)) {
                break;
            }
            JSONObject json = (JSONObject) value;
            for (String key : json.keySet()) {
                if (!resultJSON.keySet().contains(key)) {
                    resultJSON.put(key, json.get(key));
                }
            }
        }
        return resultJSON;
    }

    private static List<String> collectGenerateFiled(JSONObject json) {
        Set<String> keySet = json.keySet();
        List<String> fieldList = new ArrayList();
        Iterator iterator = keySet.iterator();
        while(iterator.hasNext()) {
            String key = (String)iterator.next();
            fieldList.add(key);
        }

        return fieldList;
    }

    private boolean existDeclareField(String key, JSONObject json) {
        FieldEntity fieldEntity = (FieldEntity)declareFields.get(key);
        return fieldEntity == null ? false : fieldEntity.isSameType(json.get(key));
    }
    

    public static void writeJava(JSONObject json) throws IOException {
        List<String> generateFiled = collectGenerateFiled(json);
        List<FieldSpec> fieldSpecList = new ArrayList<>();
        for (int i = 0; i < generateFiled.size(); i++) {
            fieldSpecList.add(FieldSpec.builder(json.get(generateFiled.get(i)).getClass(),generateFiled.get(i), PUBLIC)
                    .initializer(getPlaceholder(json.get(generateFiled.get(i)).getClass()),json.get(generateFiled.get(i))).build());
        }
        // Write to disk(Write file even interceptors is empty.)
        if (StringUtils.isEmpty(packageName)) {
            logger.info(">>> packageName is null. <<<");
        }
        JavaFile.builder(packageName,
                TypeSpec.classBuilder("PConifg")
                        .addModifiers(PUBLIC)
                        .addJavadoc(WARNING_TIPS)
                        .addFields(fieldSpecList)
                        .build()
        ).build().writeTo(filer);
        logger.info(">>> Interceptor group write over. <<<");
    }

    private static String getPlaceholder(Class<?> aClass) {
        if(aClass == String.class){
            return "$S";
        }else{
            return "$L";
        }
    }


}
