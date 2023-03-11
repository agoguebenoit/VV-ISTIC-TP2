package fr.istic.vv;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;
import com.github.javaparser.utils.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class JavaParserTCC extends VoidVisitorWithDefaults<Void> {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String CSV_SEPARATOR = ",";
    private static final String[] CSV_HEADER = {"Class", "TCC"};
    private final Map<String, List<MethodDeclaration>> fieldAccessMethodsListMap = new HashMap<>();
    private final StringBuilder fileContent = new StringBuilder();
    private MethodDeclaration currentMethod;
    private int numberOfMethods;
    private boolean appendToFile = false;

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        for (TypeDeclaration<?> type : unit.getTypes()) {
            type.accept(this, null);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterface, Void arg) {

        if (!appendToFile) {
            fileContent.append(CSV_HEADER[0]).append(CSV_SEPARATOR).append(CSV_HEADER[1]).append(LINE_SEPARATOR);
        }

        for (FieldDeclaration fieldDeclaration : classOrInterface.getFields()) {
            fieldDeclaration.accept(this, null);
        }

        for (MethodDeclaration methodDeclaration : classOrInterface.getMethods()) {
            methodDeclaration.accept(this, null);
        }

        String className = classOrInterface.getFullyQualifiedName().orElse("[Anonymous]");
        fileContent.append(className).append(CSV_SEPARATOR).append(calculateTCC()).append("\n");

        try (FileWriter fw = new FileWriter("tccResult.csv", appendToFile);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.write(fileContent.toString());
            appendToFile = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        fieldAccessMethodsListMap.clear();
        fileContent.setLength(0);
        numberOfMethods = 0;
    }

    @Override
    public void visit(FieldDeclaration field, Void arg) {
        for (VariableDeclarator variableDeclarator : field.getVariables()) {
            if (field.getAccessSpecifier().equals(AccessSpecifier.PRIVATE)) {
                fieldAccessMethodsListMap.putIfAbsent(variableDeclarator.getNameAsString(), new ArrayList<>());
            }
        }
    }

    @Override
    public void visit(MethodDeclaration method, Void arg) {
        numberOfMethods++;
        currentMethod = method;
        method.getBody().ifPresent(body -> body.accept(this, null));
    }

    @Override
    public void visit(FieldAccessExpr fieldAccess, Void arg) {
        String fieldAccessName = fieldAccess.getNameAsString();

        if (fieldAccessMethodsListMap.containsKey(fieldAccessName)) {
            List<MethodDeclaration> methods = fieldAccessMethodsListMap.get(fieldAccessName);
            methods.add(currentMethod);
        }
    }

    @Override
    public void defaultAction(Node n, Void arg) {
        n.getChildNodes().forEach(child -> child.accept(this, null));
    }

    private double calculateTCC() {

        // number of pairs in a set is n(n-1)/2
        int numberOfPairs = numberOfMethods * (numberOfMethods - 1) / 2;

        if (numberOfPairs == 0) {
            return 0;
        }

        Set<Pair<MethodDeclaration, MethodDeclaration>> uniquePairs = new HashSet<>();

        for (Map.Entry<String, List<MethodDeclaration>> entry : fieldAccessMethodsListMap.entrySet()) {
            // Compute all unique pairs in a list
            // https://stackoverflow.com/questions/11035653/get-unique-pairs-of-elements-from-an-arraylist-in-java
            List<MethodDeclaration> listWithoutDuplicates = new ArrayList<>(new HashSet<>(entry.getValue()));

            for (int i = 0; i < listWithoutDuplicates.size(); i++) {
                for (int j = i + 1; j < listWithoutDuplicates.size(); j++) {
                    uniquePairs.add(new Pair<>(listWithoutDuplicates.get(i), listWithoutDuplicates.get(j)));
                }
            }
        }

        // Two pairs are connected if they use the same attribute
        int connectedPairs = uniquePairs.size();

        return (double) connectedPairs / numberOfPairs;
    }
}
