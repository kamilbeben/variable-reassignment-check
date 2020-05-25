# Variable reassignment check

**tldr**; Sonar plugin providing check that reports when local variable / method parameter is being reassigned.

Reassigning variables (oh, the irony) adds unnecessary layer of complexity to the code and,
in most cases it means that the code could be better (for example, some parts could be extracted to another method). 

## Table of contents
 * [Introduction](#variable-reassignment-check)
 * [Examples](#examples)
 * [Supported versions](#supported-versions)
 * [Usage](#usage)

## Examples
### Noncompliant example
```java
void foo(Bar bar) {
  String barName = bar.getName();
  if (barName == null) {
    barName = "defaultName"; // Noncompliant
  }
}
```
### Compliant solution
```java
void foo(Bar bar) {
  String barName = Optional.ofNullable(bar.getName()).orElse("defaultName");
}
```
### Exceptions
 * Variables defined in loop's parenthesis, for example ` for (int i=0; i < size; i++) {}`
 * Class members

## Supported versions
Right now it was only tested on **sonarqube-8.3.1**, further info will be available soon.

## Usage
[Sonarqube docs](https://docs.sonarqube.org/latest/setup/install-plugin/) describes very well how to install plugins.

This plugin is not available on the marketplace (*it probably never will, one-rule plugin seems like an overkill to be honest, but maybe someday it will become part of something else*), so you will have to go with **Manual installation**. The JAR file is available in releases tab.
