/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.mop.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.SimpleDeclarationVisitor;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;


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
                processCommandMethod(methodDeclaration);
            }
        }
    }

    private void processCommandMethod(MethodDeclaration methodDeclaration) {
        TypeDeclaration type = methodDeclaration.getDeclaringType();


        String doc = methodDeclaration.getDocComment();

        String description = "";
        if (doc != null) {
            description = doc.trim();
        }

        StringBuilder builder = new StringBuilder();
        Collection<ParameterDeclaration> parameters = methodDeclaration.getParameters();
        for (ParameterDeclaration parameter : parameters) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append("<");
            builder.append(parameter.getSimpleName());
            builder.append(">");
        }
        String usage = builder.toString();

        String name = methodDeclaration.getSimpleName();

        List<String> list = entries.get(name);
        if (list == null) {
            list = new ArrayList<String>();
            entries.put(name, list);
        }
        list.add("description = " + description);
        list.add("usage = " + usage);

        System.out.println("@Command method: " + methodDeclaration + " on " + type);
        System.out.println("entries: " + list);

    }

    public void writeResources()  {
        String charSetName = null;
        String pkg = "";
        // TODO is there a better way to get the output dir of maven?
        //File file = new File("target/classes/META-INF/services/mop.properties");
        File file = new File("META-INF/services/mop.properties");
        try {
            PrintWriter out = env.getFiler().createTextFile(Filer.Location.SOURCE_TREE, pkg, file, charSetName);
            //PrintWriter out = env.getFiler().createSourceFile(file);
            //PrintWriter out = new PrintWriter(env.getFiler().createSourceFile(Filer.Location.CLASS_TREE, "", new File(file)));

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