/**
 *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.fusesource.mop.apt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.SimpleDeclarationVisitor;



/**
 * @version $Revision: 1.1 $
 */
public class CommandAnnotationProcessor extends SimpleDeclarationVisitor {
    private AnnotationProcessorEnvironment env;
    private Map<String, List<String>> entries = new TreeMap<String, List<String>>();

    public CommandAnnotationProcessor(AnnotationProcessorEnvironment env) {
        this.env = env;
    }

    @Override
    public void visitMethodDeclaration(MethodDeclaration methodDeclaration) {
        Collection<AnnotationMirror> annotations = methodDeclaration.getAnnotationMirrors();
        for (AnnotationMirror annotation : annotations) {
            String annotationName = annotation.getAnnotationType().toString();
            if (MopAnnotationProcessorFactory.COMMAND_ANNOTATION_CLASSNAME.equals(annotationName)) {
                processCommandMethod(methodDeclaration, annotation);
            }
        }
    }

    private void processCommandMethod(MethodDeclaration methodDeclaration, AnnotationMirror annotation) {
        TypeDeclaration type = methodDeclaration.getDeclaringType();


        String doc = methodDeclaration.getDocComment();

        String description = "";
        if (doc != null) {
            description = doc.trim();
        }

        StringBuilder builder = new StringBuilder();
        Collection<ParameterDeclaration> parameters = methodDeclaration.getParameters();
        for (ParameterDeclaration parameter : parameters) {
            if (ignoreParameterFromUsage(parameter)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append("<");
            builder.append(parameter.getSimpleName());
            builder.append(">");
        }
        String usage = builder.toString();

        String name = methodDeclaration.getSimpleName();

        for (Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : annotation.getElementValues().entrySet()) {
            if( entry.getKey().getSimpleName().equals("name") ) {
                String value = (String) entry.getValue().getValue();
                if( value!=null && value.length()!=0 ) {
                    name = value;
                }
            }
        }

        List<String> list = entries.get(name);
        if (list == null) {
            list = new ArrayList<String>();
            entries.put(name, list);
        }
        list.add("description = " + description);
        list.add("usage = " + usage);

        System.out.println("@Command name:"+name+", method: " + methodDeclaration + " on " + type);
        System.out.println("entries: " + list);

    }

    protected boolean ignoreParameterFromUsage(ParameterDeclaration parameter) {
        return parameter.getType().toString().equals("org.fusesource.mop.MOP");
    }

    public void writeResources()  {
        String charSetName = null;
        String pkg = "";
        // TODO is there a better way to get the output dir of maven?
        //File file = new File("target/classes/META-INF/services/mop.properties");
        File file = new File("META-INF/services/mop.properties");
        try {
            PrintWriter out = env.getFiler().createTextFile(Filer.Location.SOURCE_TREE, pkg,
                                                            file, charSetName);
            //PrintWriter out = new PrintWriter(env.getFiler()
            //    .createSourceFile(Filer.Location.CLASS_TREE, "", new File(file)));

            out.println("#");
            out.println("# AutoGenerated file by mop-apt on " + new Date());
            out.println("# Please DO NOT EDIT!");
            out.println("#");
            out.println();

            for (Map.Entry<String, List<String>> entry : entries.entrySet()) {
                String name = entry.getKey();
                List<String> lines = entry.getValue();
                for (String line : lines) {
                    out.println(name + "." + line);
                }
                out.println();
            }
        } catch (IOException e) {
            System.err.println("Failed to write file: " + file + ". Reason: " + e);
            e.printStackTrace();
        }
    }
}
