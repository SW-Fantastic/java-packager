package org.swdc.packager.core.builder;

import org.swdc.packager.core.entity.JavaEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JavaModuleExtractor extends ProcessExecutor {

    private JavaEnvironment environment;

    public JavaModuleExtractor(JavaEnvironment environment) {
        this.environment = environment;
    }

    public List<String> getModuleNames() {
        String javaPath = environment.getPath() + "/bin/java";
        try {
            String result = execute(Arrays.asList(
                    javaPath,
                    "--list-modules"
            ));
            List<String> resultList = new ArrayList<>();
            String[] modules = result.split("\n");
            for (String module : modules) {
                resultList.add(module.substring(0, module.indexOf("@")));
            }
            return resultList;
        } catch (Exception e){
            return Collections.emptyList();
        }
    }

}
