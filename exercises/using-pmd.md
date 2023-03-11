# Using PMD

Pick a Java project from Github (see the [instructions](../sujet.md) for suggestions). Run PMD on its source code using any ruleset. Describe below an issue found by PMD that you think should be solved (true positive) and include below the changes you would add to the source code. Describe below an issue found by PMD that is not worth solving (false positive). Explain why you would not solve this issue.

## Answer

We use the apache commons collection to work on.

### True positive

There is not much true positive, most of them are readability issues.
There is one we found :
`./commons-collections/src/main/java/org/apache/commons/collections4/trie/analyzer/StringKeyAnalyzer.java`    76    Use one line for each declaration, it enhances code readability.

The line in question :
```
char k = 0, f = 0;
```

The correction would be :
```
char k = 0;
char f = 0;
```


### False negative

We found the issue : 
`./commons-collections/src/main/java/org/apache/commons/collections4/CollectionUtils.java`    line 627    Use equals() to compare object references.

The line in question :
```
if (helper.cardinalityA.size() != helper.cardinalityB.size()) {
```

As seen above, we see that it compare two sizes, wich are integers. So it is not needed to use the equals method.