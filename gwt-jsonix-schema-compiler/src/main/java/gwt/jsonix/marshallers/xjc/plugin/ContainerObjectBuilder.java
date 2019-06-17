/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gwt.jsonix.marshallers.xjc.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.apache.commons.lang3.StringUtils;

import static gwt.jsonix.marshallers.xjc.plugin.BuilderUtils.log;

/**
 * Actual builder for the <b>JSInterop</b> <code>container</code> class that will be used by <b>marshaller</b> callback
 */
public class ContainerObjectBuilder {

    /**
     * Method to create the <b>JSInterop</b> <code>MainJs</code> class
     * @param mainObjectsList
     * @param jCodeModel
     * @return
     * @throws Exception
     */
    public static List<JDefinedClass> generateJSInteropContainerObjects(final Map<String, String> packageModuleMap, final List<JDefinedClass> mainObjectsList, JCodeModel jCodeModel) throws Exception {
        log(Level.FINE, "Generating  JSInterop containers objects ...", null);
        List<JDefinedClass> toReturn = new ArrayList<>();
        for (Map.Entry<String, String> entry : packageModuleMap.entrySet()) {
            addPackageContainerObject(entry.getKey(), entry.getValue(), jCodeModel, mainObjectsList, toReturn);
        }
        return toReturn;
    }

    protected static void addPackageContainerObject(String packageName, String containerObjectName, JCodeModel jCodeModel, final List<JDefinedClass> mainObjectsList, List<JDefinedClass> toPopulate) throws JClassAlreadyExistsException {
        log(Level.FINE, String.format("Looking for JSInterop container object %1$s for package %2$s ...", containerObjectName, packageName), null);
        Optional<JDefinedClass> containedClass = mainObjectsList.stream()
                .filter(definedClass -> Objects.equals(packageName, definedClass._package().name()))
                .findFirst();
        if (containedClass.isPresent()) {
            toPopulate.add(getContainerObject(packageName, containerObjectName, jCodeModel, containedClass.get()));
        }
    }

    protected static JDefinedClass getContainerObject(String packageName, String containerObjectName, JCodeModel jCodeModel, JDefinedClass containedClass) throws JClassAlreadyExistsException {
        log(Level.FINE, String.format("Creating  JSInterop container object %1$s for package %2$s ...", containerObjectName, packageName), null);
        final JDefinedClass toReturn = jCodeModel._class(packageName + "." + containerObjectName);
        toReturn.annotate(jCodeModel.ref(JsType.class)).param("isNative", true).param("namespace", jCodeModel.ref(JsPackage.class).staticRef("GLOBAL"));
        JDocComment comment = toReturn.javadoc();
        comment.append("JSInterop container for" + " " + "<code>" + containedClass.name() + "</code>");
        addNameProperty(jCodeModel, toReturn);
        addValueProperty(jCodeModel, toReturn, containedClass);
        return toReturn;
    }

    protected static void addNameProperty(JCodeModel jCodeModel, JDefinedClass toPopulate) {
        log(Level.FINE, String.format("Add getName property to object %1$s.%2$s ...", toPopulate._package().name(), toPopulate.name()), null);
        JClass parameterRef = jCodeModel.ref(String.class);
        addProperty(jCodeModel, toPopulate, parameterRef, "name");
    }

    protected static void addValueProperty(JCodeModel jCodeModel, JDefinedClass toPopulate, JDefinedClass containedClass) {
        log(Level.FINE, String.format("Add getValue property to object %1$s.%2$s ...", toPopulate._package().name(), toPopulate.name()), null);
        addProperty(jCodeModel, toPopulate, containedClass, "value");
    }

    protected static void addProperty(JCodeModel jCodeModel, JDefinedClass toPopulate, JClass parameterRef, String propertyName) {
        String methodName = "get" + StringUtils.capitalize(propertyName);
        log(Level.FINE, String.format("Add %1$s property to object %2$s.%3$s ...", methodName, toPopulate._package().name(), toPopulate.name()), null);
        int mod = JMod.PUBLIC + JMod.FINAL + JMod.NATIVE;
        JMethod method = toPopulate.method(mod, parameterRef, methodName);
        method.annotate(jCodeModel.ref(JsProperty.class));
    }
}