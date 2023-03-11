# Answer

The code is written in the file [JavaParserTCC.java](./JavaParserTCC.java)

It is a Java program that takes as input the path to the source code of the project and produces a report in a text file `tccResult.csv` that lists for each class: the package and class name and TCC value.
We only made the first part of the exercise, the histogram and the graph are not implemented.

There is a limitation for getting the attributes referenced in a method. Indeed if it is not reference as `this.attribute`, the visitor will no visit the field as FieldAccessExpr, so we cannot use it for TCC calculation.