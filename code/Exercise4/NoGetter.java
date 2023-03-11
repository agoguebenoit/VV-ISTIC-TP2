package fr.istic.vv;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NoGetter extends VoidVisitorWithDefaults<Void> {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private final List<String> privateAttributesNames = new ArrayList<>();
    private final StringBuilder fileContent = new StringBuilder();
    private boolean appendToFile = false;

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        for (TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {

        for (FieldDeclaration fieldDeclaration : declaration.getFields()) {
            fieldDeclaration.accept(this, null);
        }

        for (MethodDeclaration method : declaration.getMethods()) {
            method.accept(this, null);
        }

        if (!privateAttributesNames.isEmpty()) {
            String className = declaration.getFullyQualifiedName().orElse("[Anonymous]");
            fileContent.append("No getter variables for class\u00A0").append(className).append(":").append(LINE_SEPARATOR);
            for (String attributeName : privateAttributesNames) {
                fileContent.append("- ").append(attributeName).append(LINE_SEPARATOR);
            }

            fileContent.append(LINE_SEPARATOR);
            privateAttributesNames.clear();
        }

        if (fileContent.length() == 0) {
            return;
        }

        try (FileWriter fw = new FileWriter("noGetterResult.txt", appendToFile); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
            out.write(fileContent.toString());
            appendToFile = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileContent.setLength(0);
    }


    @Override
    public void visit(MethodDeclaration declaration, Void arg) {
        if (declaration.getNameAsString().startsWith("get") && declaration.getAccessSpecifier() == AccessSpecifier.PUBLIC) {
            String fieldName = declaration.getNameAsString().substring(3);
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
            privateAttributesNames.remove(fieldName);
        }
    }

    @Override
    public void visit(FieldDeclaration field, Void arg) {
        for (VariableDeclarator variableDeclarator : field.getVariables()) {
            if (field.getAccessSpecifier().equals(AccessSpecifier.PRIVATE)) {
                variableDeclarator.accept(this, null);
            }
        }
    }

    @Override
    public void visit(VariableDeclarator declarator, Void arg) {
        privateAttributesNames.add(declarator.getNameAsString());
    }
}
